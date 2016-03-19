/*
 *  Copyright (c) 2015 Thomas Dunnick (https://mywebspace.wisc.edu/tdunnick/web)
 *  
 *  This file is part of jPhineas
 *
 *  jPhineas is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  jPhineas is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with jPhineas.  If not, see <http://www.gnu.org/licenses/>.
 */

package tdunnick.jphineas.queue;

import java.util.*;
import java.io.*;

import tdunnick.jphineas.config.XmlConfig;
import tdunnick.jphineas.logging.*;
import tdunnick.jphineas.util.DateFmt;

/**
 * An in memory implementation of a queue with file as backing (persistence)
 * store.  Note that each queue gets its own file, but they all share a common
 * folder.
 * 
 * @author tld
 *
 */
public class PhineasMemQ extends PhineasQConnection
{
	/** our lock */
	private final Object lock = new Object();
	/** field separator */
	private static final String fs = "\1";
	/** in memory set of tables */
	private HashMap <String, ArrayList <String>> tables = 
		new HashMap <String, ArrayList <String>> ();
	/** in memory set of table files */
	private HashMap <String, File> files =
		new HashMap <String, File> ();
	

	/**
	 * Convert a row to a string
	 * @param row to convert
	 * @return the string
	 */
	private String row2string (PhineasQRow row)
	{
		StringBuffer buf = new StringBuffer (row.getValue(0));
		int n = row.numFields ();
		for (int i = 1; i < n; i++)
			buf.append (fs + row.getValue(i));
		return buf.toString();
	}
	
	/**
	 * Convert a string to a row
	 * @param r the string to convert
	 * @return the row
	 */
	private PhineasQRow string2row (PhineasQ queue, String r)
	{
		PhineasQRow row = new PhineasQRow (queue);
		String[] f = r.split(fs);
		for (int i = 0; i < f.length; i++)
		{
			String s = f[i];
			if (s.equals("null"))
				row.setValue (i, null);
			else
			  row.setValue (i, s);
		}
		return row;		
	}
	
	/**
	 * Get the index of the row for a rowId
	 * @param rowId
	 * @return rows index
	 */
	private int getRowIndex (ArrayList <String> rows, int rowId)
	{
		String m = "" + rowId + fs;
		synchronized (lock)
		{
			for (int i = 0; i < rows.size(); i++)
			{
				if (rows.get(i).startsWith(m))
				  return i;
			}
		}
		return -1;
	}
	
	/**
	 * Get the next available row index
	 * @return next row index
	 */
	private int nextRow (ArrayList <String> rows)
	{
		int r = rows.size();
		if (r < 1)
			return 1;
		String s = rows.get(r - 1);
		r = s.indexOf(fs);
		return Integer.parseInt(s.substring(0, r)) + 1;
	}
	
	/**
	 * open a queue - this gets deferred until actual use
	 * @param queue to open
	 * @return true if successful
	 * @see tdunnick.jphineas.queue.PhineasQConnection#open(tdunnick.jphineas.queue.PhineasQ)
	 */
	protected boolean open (PhineasQ queue)
	{
		return true;
	}
	
	/**
	 * REALLY open a queue
	 * @param queue to open
	 * @return true if successful
	 * @see tdunnick.jphineas.queue.PhineasQConnection#open(tdunnick.jphineas.queue.PhineasQ)
	 */
	private boolean openQ (PhineasQ queue)
	{
		String name = queue.getName();
		// the connection UNC designates the folder used for queues
		File dir = config.getFolder("Unc");
		if (!(dir.isDirectory() || dir.mkdirs()))
		{
			Log.error("Can't create memory folder " + dir.getAbsolutePath());
			return false;
		}
		// make an entry in tables
		ArrayList <String> rows = new ArrayList <String> ();
		tables.put(name, rows);
		// and files
		File f = new File (dir.getAbsoluteFile() + "/" + queue.getTable());
		files.put(name, f);
		// if queue previously built, then read it
		if (!f.canRead())
			return true;
		try
		{
			FileInputStream is = new FileInputStream(f);
			StringBuffer buf = new StringBuffer ();
			int c;
			while ((c = is.read()) >= 0)
			{
				if ((c == '\r') || (c == '\n'))
				{
					if (buf.length() > 0)
					  rows.add(buf.toString());
					buf.setLength(0);
				}
				else
					buf.append((char) c);
			}
			if (buf.length() > 0)
				rows.add(buf.toString());
			is.close ();
			return true;
		}
		catch (IOException e)
		{
			Log.error ("Can't open " + f.getPath(), e);
		}
		tables.remove(name);
		files.remove(name);
		return false;
	}
	
	/**
	 * flush all tables to disk and remove them
	 * @see tdunnick.jphineas.queue.PhineasQConnection#close()
	 */
	protected boolean close()
	{
		boolean ok = flush ();
		tables.clear();
		return ok;
	}
	
	/**
	 * get all the rows for the named queue
	 * @param queue
	 * @return
	 */
	private ArrayList <String> getRows (PhineasQ queue)
	{
		String name = queue.getName();
		if (!tables.containsKey(name))
			openQ (queue);
		return tables.get(name);
	}
	
  /**
   * Retrieve rows that fall in this date range.  The field to match is based on\
   * the queue type with MESSAGERECEIVEDTIME used for sender queues and RECEIVEDTIME
   * used for receiver queues.  The rows are returned ordered by ascending date.
   * 
   * @param queue affected
   * @param start date to search from
   * @param end date to search to
   * @return matched rows
	 * @see tdunnick.jphineas.queue.PhineasQConnection#findByDate(tdunnick.jphineas.queue.PhineasQ, long, long)
	 */
	public ArrayList<PhineasQRow> findByDate(PhineasQ queue, long start, long end)
	{
		if (queue == null)
			return null;
		PhineasQType t = queue.getType();
		int di;
		if (t.getType().equals("EbXmlSndQ"))
			di = t.getFieldIndex("MESSAGERECEIVEDTIME");
		else
			di = t.getFieldIndex("RECEIVEDTIME");
		if (di < 0)
			return null;
		ArrayList <String> rows = getRows (queue);
		if (rows == null)
			return null;
		ArrayList <PhineasQRow> results = new ArrayList <PhineasQRow>();
		for (int i = 0; i < rows.size(); i++)
		{
			String[] r = rows.get(i).split(fs);
			long d = DateFmt.getTime(r[di]);
			if ((d >= start) && (d <= end))
				results.add (string2row (queue, rows.get(i)));
		}
		return results;
	}
	
	/**
	 * Look up data by recordId.  Search from recordId on down and return list in
	 * descending order.  Both constraint and recordId are optional.  The matching
	 * constraint field is determined by the queue type where the route is matched
	 * for sender queues and the party id for receiver queues.
	 * 
	 * @param constraint on the search
	 * @param recordId to search from
	 * @return rows found
	 * @see tdunnick.jphineas.queue.PhineasQConnection#findByRecordId(tdunnick.jphineas.queue.PhineasQ, java.lang.String, int)
	 */
	public ArrayList<PhineasQRow> findByRecordId(PhineasQ queue,
			String constraint, int recordId)
	{
		if (queue == null)
			return null;
		PhineasQType t = queue.getType();
		int ci = -1;
		if (constraint != null)
		{
			String f;
			if (t.getType().equals("EbXmlSndQ"))
				ci = t.getFieldIndex("ROUTEINFO");
			else
				ci = t.getFieldIndex("FROMPARTYID");
		}
		ArrayList <String> rows = getRows (queue);
		if (rows == null)
			return null;
		ArrayList <PhineasQRow> results = new ArrayList <PhineasQRow>();
		for (int i = rows.size() - 1; i >= 0; i--)
		{
			String[] r = rows.get(i).split(fs);
			if (Integer.parseInt(r[0]) > recordId)
				continue;
			if ((ci < 0) || r[ci].equals (constraint))
				results.add (string2row (queue, rows.get(i)));
		}
		return results;
	}
	
	/**
	 * Get a distinct list for one or more fields
	 * @param field list separated by comma's
	 * @return the list of comma delimited fields
	 * @see tdunnick.jphineas.queue.PhineasQConnection#findDistinct(tdunnick.jphineas.queue.PhineasQ, java.lang.String)
	 */
	public ArrayList <String> findDistinct(PhineasQ queue, String field)
	{
		if ((queue == null) || (field == null))
			return null;
		ArrayList <String> rows = getRows (queue);
		if (rows == null)
			return null;
		PhineasQType type = queue.getType();
		String[] fields = field.split(",");
		if (fields.length < 1)
			return null;
		int[] index = new int[fields.length];
		for (int i = 0; i < fields.length; i++)
		{
			if ((index[i] = type.getFieldIndex(fields[i])) < 0)
				return null;
		}
		String s = null;
		ArrayList <String> results = new ArrayList <String>();
		for (int i = 0; i < rows.size(); i++)
		{
			String[] r = rows.get(i).split(fs);
			StringBuffer buf = new StringBuffer ();
			for (int f = 0; f < index.length; f++)
			{
				s = r[index[f]];
				if (s != null)
					buf.append("," + s);
			}
			s = buf.substring(1);
			if (results.contains(s))
				continue;
			results.add (s);
		}
		return results;
	}

	/**
	 * returns row with matching PROCESSINGSTATUS status
	 * @param queue to search
	 * @param status to match
	 * @return rows matching or null if error
	 * @see tdunnick.jphineas.queue.PhineasQConnection#findProcessingStatus(tdunnick.jphineas.queue.PhineasQ, java.lang.String)
	 */
	public ArrayList <PhineasQRow> findProcessingStatus (PhineasQ queue, String status)
	{
		if ((status == null) || (status.length() == 0))
			return null;
		int index = queue.getType().getFieldIndex ("PROCESSINGSTATUS");
		if (index < 0)
		{
			Log.error ("PROCESSINGSTATUS not found in " + queue.getName());
			return null;
		}
		ArrayList <String> rows = getRows (queue);
		if (rows == null)
			return null;
		ArrayList <PhineasQRow> results = new ArrayList <PhineasQRow>();
		for (int i = 0; i < rows.size(); i++)
		{
			String s = rows.get(i).split(fs)[index];
			if (s.equals (status))
				results.add (string2row (queue, rows.get(i)));
		}
		return results;
	}

	/**
	 * copy queues out to disk
	 * @return true if successfull
	 * @see tdunnick.jphineas.queue.PhineasQConnection#flush()
	 */
	public boolean flush ()
	{
		boolean ok = true;
		synchronized (lock)
		{
			Iterator <String> it = tables.keySet().iterator();
			while (it.hasNext())
			{
				if (!flush (it.next()))
					ok = false;
			}
		}
		return ok;
	}
	
	/**
	 * move one queue to disk storage
	 * @param f name of disk file to use
	 * @param rows to store
	 * @return true if successful
	 */
	private boolean flush (String name)
	{
		File f = files.get(name);
		if (f == null)
			return false;
		ArrayList <String> rows = tables.get (name);
		// Log.debug("Flushing " + rows.size() + " rows for "+ config.getValue("Name"));
		try
		{
			FileOutputStream os = new FileOutputStream(f);
			for (int i = 0; i < rows.size(); i++)
				os.write((rows.get(i) + "\n").getBytes());
			os.close ();
			return true;
		}
		catch (IOException e)
		{
			Log.error ("Can't flush " + f.getPath(), e);
		}
		return false;
	}
	
	/**
	 * get the number of rows in this queue
	 * @param queue to check
	 * @return number of rows or -1 if queue not found
	 * @see tdunnick.jphineas.queue.PhineasQConnection#numRows(tdunnick.jphineas.queue.PhineasQ)
	 */
	public int numRows(PhineasQ queue)
	{
		ArrayList <String> rows = getRows (queue);
		if (rows == null)
			return -1;
		return rows.size();
	}
	
	/**
	 * get the row ID for the last entry in this queue
	 * @param queue to search
	 * @return the last row id
	 */
	public int lastRow(PhineasQ queue)
	{
		ArrayList <String> rows = getRows (queue);
		if (rows == null)
			return -1;
		int n = rows.size ();
		if (n-- == 0)
			return (0);
		String r = rows.get (n);
		return Integer.parseInt (r.substring (0, r.indexOf (fs)));
	}
	
	/**
	 * remove a row from this queue\
	 * @param queue with the row to remove
	 * @param rowid of row to remove
	 * @return true if successful
	 * @see tdunnick.jphineas.queue.PhineasQConnection#remove(tdunnick.jphineas.queue.PhineasQ, int)
	 */
	public boolean remove(PhineasQ queue, int rowid)
	{
		synchronized (lock)
		{
			ArrayList <String> rows = getRows (queue);
			if (rows == null)
				return false;
			rowid = getRowIndex (rows, rowid);
			if (rowid < 0)
			  return false;
			rows.remove(rowid);
			return true;
		}
	}

	/**
	 * return a row from a queue
	 * @param rowid of row to retrieve
	 * @return the row or null if it fails
	 * @see tdunnick.jphineas.queue.PhineasQConnection#retrieve(tdunnick.jphineas.queue.PhineasQ, int)
	 */
	public PhineasQRow retrieve(PhineasQ queue, int rowid)
	{
		synchronized (lock)
		{
			ArrayList <String> rows = getRows (queue);
			if (rows == null)
				return null;
			rowid = getRowIndex (rows, rowid);
			if (rowid < 0)
			  return null;
			PhineasQRow row = string2row (queue, rows.get(rowid));
			return row;
		}
	}

	/**
	 * append this row and return it's row ID (primary key)\
	 * @param row to append
	 * @see tdunnick.jphineas.queue.PhineasQConnection#append(tdunnick.jphineas.queue.PhineasQRow)
	 */
	public int append(PhineasQRow row)
	{
		synchronized (lock)
		{
			ArrayList <String> rows = getRows (row.getQueue());
			if (rows == null)
				return -1;
			int r = nextRow (rows);
			row.setRowId (r);
			rows.add(row2string (row));
			return r;
		}
	}
	

	/**
	 * update a row
	 * @param row to update
	 * @see tdunnick.jphineas.queue.PhineasQConnection#update(tdunnick.jphineas.queue.PhineasQRow)
	 */
	public int update(PhineasQRow row)
	{
		synchronized (lock)
		{
			ArrayList <String> rows = getRows (row.getQueue());
			if (rows == null)
				return -1;
			int r = getRowIndex (rows, row.getRowId());
			if (r < 0)
				return -1;
			rows.set(r, row2string(row));
			return r;
		}
	}
}
