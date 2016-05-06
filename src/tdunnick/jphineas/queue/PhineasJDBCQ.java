/*
 *  Copyright (c) 2015-2016 Thomas Dunnick (https://mywebspace.wisc.edu/tdunnick/web)
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
import java.sql.*;

import tdunnick.jphineas.config.XmlConfig;
import tdunnick.jphineas.logging.*;
import tdunnick.jphineas.util.DateFmt;

/**
 * A JDBC connection implementation for a jPhineas queue.
 * 
 * @author Thomas Dunnick
 *
 */
public class PhineasJDBCQ extends PhineasQConnection
{	
	/** the jdbc connection for this queue */
	private Connection conn = null;
	/** a lock for inserting new rows */
	private Object lock = new Object();
	/** SQL to flush pending operations */
	private String flush = null;
	/**
	 * add any needed escapes for SQL functions.
	 * 
	 * @param s data to escape
	 * @return escaped data
	 */
	private String sqlEscape(String s)
	{
		if (s == null)
			return null;
		StringBuffer b = new StringBuffer(s);
		for (int i = 0; i < b.length(); i++)
		{
			if (b.charAt(i) == '\'')
				b.deleteCharAt(i--);
		}
		return b.toString();
	}
	
	/**
	 * quote and escape an SQL argument
	 * @param s argument to quote
	 * @return quoted string
	 */
	private String quote (String s)
	{
		return "'" + sqlEscape (s) + "'";
	}
	
	/**
	 * query this queue - be sure to close the returned statement
	 * @param query to run
	 * @return result rows or null if fails
	 */
	private Statement runQuery (String query, int limit)
	{
		Statement stmt = null;
		try
		{
			stmt = conn.createStatement();
			if (limit > 0)
			  stmt.setMaxRows(limit);
			//(ResultSet.TYPE_SCROLL_SENSITIVE,  ResultSet.CONCUR_READ_ONLY);
			if (stmt.execute (query))
				return stmt;
			stmt.close();
		}
		catch (SQLException e)
		{
			Log.debug ("Query \"" + query + "\" failed " +  e.getLocalizedMessage());
		}
		return null;
	}
	
	/**
	 * change this queue
	 * @param query with update, delete, or insert
	 * @return number rows updated, or -1 if failed
	 */
	private int runUpdate (String query)
	{
		Statement stmt = null;
		int n = -1;
		try
		{
			// Log.debug("Trying \"" + query + "\"");			
			stmt = conn.createStatement();
			//(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);
			n = stmt.executeUpdate(query);
		}
		catch (SQLException e)
		{
			Log.error ("Update \"" + query + "\" failed", e);
		}
		finally
		{
			try
			{
				if (stmt != null)
					stmt.close ();
			}
			catch (SQLException e)
			{
				Log.error ("failed closing statement", e);			
			}
		}
		return n;	
	}
	
	/**
	 * closes this connection
	 * @return true if successful
	 * @see tdunnick.jphineas.queue.PhineasQConnection#close()
	 */
	protected boolean close()
	{
		if (conn == null)
			return true;
		// Log.debug("Closing connection for " + getQName());
		flush ();
		try
		{
			if (!conn.isClosed())
			{
				conn.rollback();
				conn.close();
			}
			/*
			Driver driver = DriverManager.getDriver(unc);
			if (driver != null)
			  DriverManager.deregisterDriver(driver);
			 */
		}
		catch (SQLException e)
		{
			return false;
		}
		conn = null;
		return true;
	}
	
	/**
	 * flushes tables to persistence if needed
	 * @return true if successful
	 * @see tdunnick.jphineas.queue.PhineasQConnection#flush()
	 */
	public boolean flush()
	{
		if (flush != null)
			return (runUpdate (flush) == 0);
		return true;
	}
	
	/**
	 * gets the number of rows in this queue
	 * @param queue to use
	 * @return number of rows
	 * @see tdunnick.jphineas.queue.PhineasQConnection#numRows(tdunnick.jphineas.queue.PhineasQ)
	 */
	public int numRows(PhineasQ queue)
	{
		Statement stmt =  runQuery ("select count(*) from " + queue.getTable(), 0);
		if (stmt == null)
			return -1;
		try
		{
			ResultSet rs = stmt.getResultSet();
			int rows = 0;
			if (rs.next())
			{
			  rows = rs.getInt(1);
			}
		  stmt.close();
		  return rows;
		}
		catch (SQLException e)
		{
			Log.error ("Can't get " + queue.getKeyName() + " from " + queue.getTable(), e);						
		}
		return -1;
	}

	/**
	 * get the row ID for the last entry in this queue
	 * @param queue to search
	 * @return the last row id
	 */
	public int lastRow(PhineasQ queue)
	{
		Statement stmt = 
			runQuery ("select max(" + queue.getKeyName() + ") from " + queue.getTable (), 0);
		if (stmt == null)
			return -1;
		try
		{
			ResultSet rs = stmt.getResultSet();
			int row = 0;
			if (rs.next())
			{
			  row = rs.getInt(1);
			  // Log.debug("got max row " + row);
			}
		  stmt.close();
		  return row;
		}
		catch (SQLException e)
		{
			Log.error ("Can't get " + queue.getKeyName() + " from " + queue.getTable (), e);									
		}
		return -1;
	}

	/**
	 * opens a queue and its connection, creating the table if needed
	 * @param queue to open
	 * @return true if successful
	 * @see tdunnick.jphineas.queue.PhineasQConnection#open(tdunnick.jphineas.queue.PhineasQ)
	 */
	protected boolean open(PhineasQ queue)
	{
		// first open a connection if needed
		if (conn == null)
		{
			// load the driver
			String s = config.getValue("Driver");
			try
			{
				Class.forName(s);
			}
			catch (ClassNotFoundException e)
			{
				Log.error("Can't load driver " + s, e);
				return false; 
			}
			// if this is an hsql file, change relative path to absolute
			s = config.getValue("Unc");
			if (s.startsWith("jdbc:hsqldb:file:"))
			{
				File f = new File (s.substring(17));
				if (!f.isAbsolute())
					s = "jdbc:hsqldb:file:" + config.findValue(config.DEFAULTDIR) 
					  + "/" + f.getPath();
			}
		  // make the connection - (change to pooled driver manager?)
			try 
			{
				Log.debug ("opening " + s);
				conn = DriverManager.getConnection (s, config.getValue("Id"), 
						config.getValue("Password"));
				conn.setAutoCommit(true);
			}
			catch (SQLException e)
			{
				Log.error("Can't connect to " + s, e);
				return false;
			}
		}
		// if no table, create one!
		if (numRows (queue) < 0)
		{
			// table doesn't exist??? create the table
			String table = queue.getTable();
			PhineasQType t = queue.getType();
			StringBuffer buf = new StringBuffer ("create table " + table
			+ " (" + queue.getKeyName() + " INTEGER NOT NULL PRIMARY KEY");
			for (int i = 1; i < t.numFields(); i++)
				buf.append ("," + t.getName(i) + " VARCHAR");
			buf.append(")");
			int n = runUpdate (buf.toString());
			if (n != 0)
			{
				close ();
				Log.error("Couldn't create table " + table);
				return false;
			}
			Log.info("Created table " + table + " for queue " + queue.getName());
		}
		return true;
	}
	
	/**
	 * Searchs a queue for matching records and returns them
	 * @param queue to search
	 * @param whereclause to use in the search
	 * @return rows found or null if it fails
	 */
	private ArrayList <PhineasQRow> find (PhineasQ queue, String whereclause, int limit)
	{
		String query = "select * from " + queue.getTable();
		if ((whereclause != null) && (whereclause.length() > 0))
		{
			if (whereclause.startsWith(" "))
				query += whereclause;
			else
			  query += " where " + whereclause;
		}
		// Log.debug(query);
		Statement stmt = runQuery (query, limit);
		if (stmt == null)
			return null;
		try
		{
			ResultSet rs = stmt.getResultSet();
			ArrayList<PhineasQRow> rows = new ArrayList<PhineasQRow>();
			PhineasQType t = queue.getType();
			while (rs.next())
			{
				PhineasQRow row = new PhineasQRow (queue);
				for (int i = 0; i < t.numFields(); i++)
					row.setValue(i, rs.getString(t.getName(i)));
				rows.add(row);
			}
			stmt.close();
			return rows;
		}
		catch (SQLException e)
		{
			Log.error("Failed accessing rows of find " + whereclause + " in "
					+ queue.getTable(), e);
		}
		return null;
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
		PhineasQType t = queue.getType();
		StringBuffer where = new StringBuffer ();
		String f;
		if (t.getType().equals("EbXmlSndQ"))
			f = t.getName("MESSAGERECEIVEDTIME");
		else
			f = t.getName("RECEIVEDTIME");
		if (start > 0L)
		{
			where.append (f + ">=" + quote (DateFmt.getTimeStamp(start)));
		}
		if (end > 0L)
		{
			if (where.length() > 0)
				where.append (" and ");
			where.append (f + "<=" + quote (DateFmt.getTimeStamp(end)));
		}
		where.append (" order by " + f + " asc");
		return find (queue, where.toString(), 0);
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
		PhineasQType t = queue.getType();
		StringBuffer where = new StringBuffer ();
		if (constraint != null)
		{
			String f;
			if (t.getType().equals("EbXmlSndQ"))
				f = t.getName("ROUTEINFO");
			else
				f = t.getName("FROMPARTYID");
			where.append (f + "=" + quote (constraint));
		}
		if (recordId > 0)
		{
			if (where.length() > 0)
				where.append (" and ");
			where.append(t.getName(0) + "<=" + recordId);
		}
		where.append (" order by " + t.getName(0) + " desc");
		return find (queue, where.toString(), rowLimit);
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
		PhineasQType t = queue.getType();
		String[] fields = field.split(",");
		if (fields.length < 1)
			return null;
		StringBuffer buf = new StringBuffer ();
		for (int i = 0; i < fields.length; i++)
		{
			String f = t.getName(fields[i]);
			if (f == null)
				return null;
			buf.append("," +  f);
		}
		Statement stmt = runQuery("select distinct " + buf.substring(1) 
				+ " from " + queue.getTable() 
				+ " where " + buf.substring(1) + " is not null", 0);
		if (stmt == null)
		  return null;
		try
		{
			ResultSet rs = stmt.getResultSet();
			ArrayList<String> values = new ArrayList <String> ();
			while (rs.next())
			{
				buf.setLength(0);
				for (int i = 1; i <= 1; i++)
					buf.append ("," + rs.getString (i));
				values.add (buf.substring (1));
			}
			stmt.close();
			return values;
		}
		catch (SQLException e)
		{
			Log.error("Failed distinct query for " + field + " in "
					+ queue.getTable(), e);
		}
		return null;
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
		String name = queue.getType().getName ("PROCESSINGSTATUS");
		if (name == null)
		{
			Log.error ("PROCESSINGSTATUS not found in " + queue.getName());
			return null;
		}
		return find (queue, name + "='" + sqlEscape (status) + "'", 0);
	}

	/**
	 * removes a row from the queue
	 * @param queue to use
	 * @param rowid to remove
	 * @return true if successful
	 * @see tdunnick.jphineas.queue.PhineasQConnection#remove(tdunnick.jphineas.queue.PhineasQ, int)
	 */
	public boolean remove(PhineasQ queue, int rowid)
	{
		int n = runUpdate ("delete from " + queue.getTable() + " where " 
				+ queue.getKeyName() + "=" + rowid);
		return n == 1;
	}
	
	/**
	 * returns a row from this queue
	 * @param queue to use
	 * @param rowid to return
	 * @return the row or null if row is not found
	 * @see tdunnick.jphineas.queue.PhineasQConnection#retrieve(tdunnick.jphineas.queue.PhineasQ, int)
	 */
	public PhineasQRow retrieve(PhineasQ queue, int rowid)
	{
		ArrayList <PhineasQRow> rows = find (queue, queue.getKeyName() + "=" + rowid, 0);
		if ((rows == null) || (rows.size() == 0))
			return null;
		return rows.get(0);
	}
	
	/**
	 * Appends a queue row
	 * @param row to append
	 * @return rowid of new row or -1 if it fails
	 * @see tdunnick.jphineas.queue.PhineasQConnection#append(tdunnick.jphineas.queue.PhineasQRow)
	 */
	public int append(PhineasQRow row)
	{
		int rowid;
		PhineasQ q = row.getQueue();
		PhineasQType t = q.getType();
		synchronized (lock)
		{
			rowid = lastRow(q);
			if (rowid++ < 0)
				return -1;
			// Log.debug("appending row " + rowid);
			row.setValue(0, "" + rowid);
			StringBuffer query = new StringBuffer("insert into " + q.getTable ()
					+ " (" + q.getKeyName());
			for (int i = 1; i < t.numFields(); i++)
				query.append("," + t.getName(i));
			query.append(") values (" + rowid);
			for (int i = 1; i < t.numFields(); i++)
			{
				String s = row.getValue(i);
				if (s == null)
					query.append(",null");
				else
				  query.append(",'" + sqlEscape (s) + "'");
			}
			query.append(")");
			if (runUpdate(query.toString()) != 1)
				rowid = -1;
		}
		return rowid;
	}
	

	/**
	 * Update this row
	 * @param row to update
	 * @return rowid updated or -1 if it fails
	 * @see tdunnick.jphineas.queue.PhineasQConnection#update(tdunnick.jphineas.queue.PhineasQRow)
	 */
	public int update(PhineasQRow row)
	{
		StringBuffer query = new StringBuffer ();
		PhineasQ q = row.getQueue ();
		PhineasQType t = 	q.getType();
		for (int i = 1; i < row.numFields(); i++)
		{
			String s = row.getUpdate(i);
			if (s == null)
				continue;
			query.append(t.getName(i)+ "='" + sqlEscape(s) + "',");
		}
		int n = query.length();
		if (n-- > 0)
		{
			query.deleteCharAt(n);
			n = runUpdate("update " + row.getQueue().getTable() 	+ " set " + query.toString()
					+ " where " + q.getKeyName() + "=" + row.getRowId());
			if (n != 1)
				return -1;
		}
		return row.getRowId();
	}
}
