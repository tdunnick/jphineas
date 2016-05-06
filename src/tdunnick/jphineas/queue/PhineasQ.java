/*
 *  Copyright (c) 2015-2016-2016 Thomas Dunnick (https://mywebspace.wisc.edu/tdunnick/web)
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
import tdunnick.jphineas.logging.*;

/**
 * A PhineasQ is shared among all users of that queue.  It provides the glue
 * that binds a connection, queue type to a specific queue.
 * 
 * @author Thomas Dunnick
 *
 */
/**
 * @author user
 *
 */
public class PhineasQ
{
	/** queue name */
	String name = null;
	/** connection table */
	String table = null;
	/** table type */
	PhineasQType type = null;
	/** a connection */
	PhineasQConnection conn = null;
	
	/**
	 * Get the name of this queue
	 * @return it's name
	 */
	public String getName ()
	{
		return name;
	}
	
	/**
	 * Get the type of queue
	 * @return type name
	 */
	public PhineasQType getType ()
	{
		return type;
	}
	
	/**
	 * get the connection for this queue
	 * @return the connection
	 */
	public PhineasQConnection getConnection ()
	{
		return conn;
	}
	
	/**
	 * Get the table name for this queue
	 * @return table name
	 */
	public String getTable ()
	{
		return table;
	}
		
	/**
	 * Get an empty row for this queue
	 * @return the new row
	 */
	public PhineasQRow newRow ()
	{
		return new PhineasQRow (this);
	}
	
	/**
	 * convenience method for getting...
	 * @return the field name of a primary key
	 */
	public String getKeyName ()
	{
		return type.getName(0);
	}
	
	/**
	 * convenience method for getting...
	 * @return the number of fields
	 */
	public int numFields ()
	{
		return type.numFields();
	}
	
	/**
	 * get the row ID for the last entry in this queue
	 * @return the last row id or -1 if it fails
	 */
	public int lastRow()
	{
		return conn.lastRow(this);
	}

	/**
	 * limit number of rows returned
	 * @param l the limit
	 */
	public void setRowLimit (int l)
	{
		conn.setRowLimit (l);
	}
	
	/**
	 * get the current limit for rows returned 
	 * @return the limit
	 */
	public int getRowLimit ()
	{
		return conn.getRowLimit();
	}
	

	/**
	 * look up data by date
	 * @param start date
	 * @param end date
	 * @return rows found
	 */
	public ArrayList <PhineasQRow> findByDate (long start, long end)
	{
		return conn.findByDate (this, start, end);
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
	 */
	public ArrayList <PhineasQRow> findByRecordId (String constraint, int recordId)
	{
		return conn.findByRecordId (this, constraint, recordId);
	}
	
	/**
	 * look up all the records for a given processing status
	 * @param status to search on
	 * @return rows found
	 */
	public ArrayList <PhineasQRow> findProcessingStatus (String status)
	{
		return conn.findProcessingStatus (this, status);
	}
	
	/**
	 * Get the distinct for one or more fields
	 * @param field list separated by comma's
	 * @return the list
	 */
	public ArrayList <String> findDistinct (String field)
	{
		return conn.findDistinct (this, field);
	}
	
	/**
	 * removes a row from the queue
	 * @param rowid to remove
	 * @return true if successful
	 * @see tdunnick.jphineas.queue.PhineasQConnection#remove(tdunnick.jphineas.queue.PhineasQ, int)
	 */
	public boolean remove (int rowid)
	{
		return conn.remove(this, rowid);
	}
	
	/**
	 * returns a row from this queue
	 * @param rowid to return
	 * @return the row or null if row is not found
	 * @see tdunnick.jphineas.queue.PhineasQConnection#retrieve(tdunnick.jphineas.queue.PhineasQ, int)
	 */
	public PhineasQRow retrieve (int rowid)
	{
		return conn.retrieve(this, rowid);
	}
	
	/**
	 * Configure this queue from relevant chunks of xml in the master queue.
	 * Do not allow a queue to be re-configured.  Once and done.
	 * @param name of queue
	 * @param table for this queue
	 * @param conn connection for this queue
	 * @param type queue type
	 * @return true if successful
	 */
	protected boolean configure (String name, String table, PhineasQConnection conn, PhineasQType type)
	{
		this.conn = conn;
		this.type = type;
		this.name = name;
		this.table = table;
		if (conn.open (this))
			return true;
		Log.error("Failed to open configuration");
		return false;
	}
}
