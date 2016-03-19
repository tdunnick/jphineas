package tdunnick.jphineas.queue;

import java.util.*;

import tdunnick.jphineas.config.XmlConfig;
import tdunnick.jphineas.xml.*;

public abstract class PhineasQConnection
{
	XmlConfig config = null;
	/** maximum number of rows to fetch */
	int rowLimit = 15;
	
	public void configure (XmlConfig cfg)
	{
		config = cfg;
	}
	
	/**
	 * limit number of rows returned
	 * @param l the limit
	 */
	public void setRowLimit (int l)
	{
		rowLimit = l;
	}
	
	/**
	 * get the current limit for rows returned 
	 * @return the limit
	 */
	public int getRowLimit ()
	{
		return rowLimit;
	}
	
	/**
	 * opens a connection and prepare it for use. This may
	 * optionally create the connection if it doesn't already exist.
	 */
	protected abstract boolean open (PhineasQ queue);
	/**
	 * close this connection.  Should only be called by the connection manager.
	 * @return true if successful
	 */
	protected abstract boolean close ();
	/**
	 * flush any data held by this connection to backing storage.
	 * @return true if successful
	 */
	public abstract boolean flush ();
	/**
	 * Append a row to this connection.
	 * @param row to append
	 * @return new row id
	 */
	public abstract int append (PhineasQRow row);
  /**
   * Update this row in this connection.  The update may result in a new rowid for
   * some types of connections.
   * @param row to update
   * @return rowid if successful, or -1 if fails.
   */
  public abstract int update (PhineasQRow row);
  /**
   * Remove a row in this connection.  This may not be a valid operation for all
   * connections.
   * @param queue affected
   * @param rowid to remove
   * @return true if successful.
   */
  public abstract boolean remove (PhineasQ queue, int rowid);
  /**
   * Retrieve a row in this connection.
   * @param queue affected
   * @param rowid to retrieve
   * @return true if successful.
   */
  public abstract PhineasQRow retrieve (PhineasQ queue, int rowid);
  /**
   * Retrieve rows that match this SQL like where clause
   * @param queue affected
   * @param constraint on field determined by queue Type
   * @param recordId to start search with
   * @return matched rows
   */
  public abstract ArrayList <PhineasQRow> findByRecordId (PhineasQ queue, String constraint, int recordId);
  /**
   * Retrieve rows that match this SQL like where clause
   * @param queue affected
   * @param start date to search from
   * @param end date to search to
   * @return matched rows
   */
  public abstract ArrayList <PhineasQRow> findByDate (PhineasQ queue, long start, long end);
  /**
   * Retrieve rows that match this processing status.  Only consider non-empty fields.
   * @param queue affected
   * @param status to search on
   * @return matched rows
   */
  public abstract ArrayList <PhineasQRow> findProcessingStatus (PhineasQ queue, String status);
	/**
	 * Get the distinct for one or more fields
   * @param queue affected
	 * @param field list separated by comma's
	 * @return the list
	 */
	public abstract ArrayList <String> findDistinct (PhineasQ queue, String field);
  /**
   * Gets the number of rows in this connection.
   * @param queue affected
   * @return number of rows or -1 if fails.
   */
  public abstract int numRows (PhineasQ queue);
	/**
	 * get the row ID for the last entry in this queue
	 * @param queue to search
	 * @return the last row id
	 */
	public abstract int lastRow(PhineasQ queue);
}
