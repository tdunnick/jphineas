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

import java.text.SimpleDateFormat;
import java.util.Date;

import tdunnick.jphineas.common.JPhineas;

/**
 * This is used to pass around and manipulate one queue entry.  The methods
 * are persistence agnostic, with the details hidden within the implementation of the
 * referenced PhineasQ.  The values are coordinated with the queue XML definition,
 * not necessarily the definition within the backing store.
 * <p>
 * Note that String getters return null by default for empty strings, just like 
 * the XmlConfig.  However this can be adjusted by noNulls...
 * 
 * @author Thomas Dunnick
 *
 */
public class PhineasQRow
{
  private PhineasQ queue;
  private PhineasQConnection conn;
  private String[] values;
  private boolean[] updates;
  private boolean noNulls = false;
  
  protected PhineasQRow (PhineasQ q)
  {
  	queue = q;
  	conn = q.getConnection();
  	int n = q.getType().numFields();
  	values = new String[n];
  	updates = new boolean[n];
  }
  
  /**
   * Copy values from a row into this one.
   * @param row
   */
  private void cpValues (PhineasQRow row)
  {
  	for (int i = 0; i < numFields(); i++)
  		values[i] = row.getValue(i);
  	resetUpdates();
   }
  
  /**
   * Set the get behavior to return either nulls or empty strings.
   * @param how if true returns string, false nulls
   * @return the previous setting
   */
  public boolean setNoNulls (boolean how)
  {
  	boolean prev = noNulls;
  	noNulls = how;
  	return (prev);
  }
  
  /**
   * compares this to row
   * @param row to compare
   * @return true if values match
   */
  public boolean equals (PhineasQRow row)
  {
  	for (int i = 0; i < numFields(); i++)
  	{
  		if (!row.getValue(i).equals(getValue(i)))
  			return false;
  	}
  	return true;
  }
  
	/**
	 * Set a queue value.
	 * 
	 * @param id of value to set
	 * @param v value to use
	 */
	public boolean setValue (int id, String v)
	{
		if ((values == null) || (id >= values.length) || (id < 0))
			return false;
		values[id] = v;
		updates[id] = true;
		return true;
	}
	
	/**
	 * Set a value in this queue.
	 * @param id value's id
	 * @param v value to set
	 * @return true if successful
	 */
	public boolean setValue (String id, String v)
	{
		return setValue (queue.getType().getFieldIndex(id), v);
	}	
	
	/**
	 * Get a queue value.
	 * 
	 * @param id of value to get
	 * @return the value
	 */
	public String getValue (int id)
	{
		if (values == null)
			return null;
		if ((id < values.length) && (id >= 0))
			return values[id];
		return noNulls ? "" : null;
	}
	
	/**
	 * Get a queue value.
	 * 
	 * @param id of value to get
	 * @return the value
	 */
	public String getValue (String id)
	{
		return getValue (queue.getType().getFieldIndex(id));
	}
	
	/**
	 * Get a queue value if it has been updated.
	 * @param index of value to get
	 * @return the value or null if not updated
	 */
	public String getUpdate (int index)
	{
    if (updates[index])
    	return getValue (index);
    return null;
	}
	
	/**
	 * Get a queue value as an integer.
	 * 
	 * @param id of value to get
	 * @param dflt value to return if fails
	 * @return the value or 0 if not found
	 */
	public int getInt (int id, int dflt)
	{
		String s = getValue (id);
		if ((s == null) || !s.matches("[0-9]+"))
			return dflt;
		return Integer.parseInt(s);
	}
	
	/**
	 * Get a queue value as an integer.
	 * 
	 * @param id of value to get
	 * @return the value or 0 if not found
	 */
	public int getInt (int id)
	{
		return getInt (id, 0);
	}
	
	/**
	 * Get a queue value as an integer.
	 * 
	 * @param id of value to get
	 * @return the value or 0 if not found
	 */
	public int getInt (String id)
	{
		return getInt (queue.getType().getFieldIndex(id));
	}
	
	/**
	 * reset all the updates to false
	 */
	public void resetUpdates ()
	{
		if (updates == null)
			return;
		for (int i = 0; i < updates.length; i++)
			updates[i] = false;
	}
	
	/**
	 * Reset all the values to null.
	 */
	public void resetValues ()
	{
		if (values == null)
			return;
		for (int i = 0; i < values.length; i++)
			values[i] = null;
		resetUpdates();
	}	
	
	/**
	 * Get number of fields in this queue
	 * @return number of fields
	 */
	public int numFields ()
	{
		return queue.getType().numFields();
	}

	/**
	 * Get the queue to which this row belongs
	 * @return the queue belonging to this object
	 */
	public PhineasQ getQueue ()
	{
		return queue;
	}
	// convenience functions normally provided by the queue itself
	/**
	 * Get the identifier used for this queue
	 * @return identifier
	 */
	public String getQueueId ()
	{
		return queue.getName();
	}	
		
	/**
	 * removes this row from the queue
	 * @return true if successful
	 * @see tdunnick.jphineas.queue.PhineasQConnection#remove(tdunnick.jphineas.queue.PhineasQ, int)
	 */
	public boolean remove ()
	{
		return conn.remove(queue, getRowId());
	}
	
	/**
	 * retrieve a row from the queue in this row
	 * @param rowid to retrieve
	 * @return true if successful
	 */
	public boolean retrieve (int rowid)
	{
		PhineasQRow row = conn.retrieve (queue, rowid);
		if (row == null)
			return false;
		cpValues (row);
		return true;
	}
	
	/**
	 * update this row
	 * @return number of rows updated
	 */
	public int update ()
	{
		return conn.update(this);
	}
	
	/**
	 * append this row
	 * @return number of rows appended
	 */
	public int append ()
	{
		return (conn.append(this));
	}
	
	/**
	 * Force the row ID to a value
	 * @param row
	 */
	protected void setRowId (int row)
	{
		values[0] = "" + row;
	}
	/**
	 * Convenience method to get row id as integer.  Note this assumes the first
	 * field is ALWAYS the row ID.
	 * @return rowID or -1 if not valid.
	 */
	public int getRowId ()
	{
		return getInt (0, -1);
	}	

	// Most queues share some common attributes.  Here are their setter/getters.
	
	/**
	 * Set the MESSAGEID if it exists
	 * @param v value to set
	 * @return true if successful
	 */
	public boolean setMessageId (String v)
	{
		return setValue ("MESSAGEID", v);
	}
	
	/**
	 * Get the MESSAGEID if it exists
	 * @return the ID or null if there is none
	 */
	public String getMessageId ()
	{
		return getValue ("MESSAGEID");
	}
	
	/**
	 * Set the PAYLOADFILE if it exists
	 * @param v value to set
	 * @return true if successful
	 */
	public boolean setPayLoadFile (String v)
	{
		return setValue ("PAYLOADFILE", v);
	}
	
	/**
	 * Get the PAYLOADFILE if it exists
	 * @return the ID or null if there is none
	 */
	public String getPayLoadFile ()
	{
		return getValue ("PAYLOADFILE");
	}
	
	/**
	 * Set the ROUTEINFO if it exists
	 * @param v value to set
	 * @return true if successful
	 */
	public boolean setRouteInfo (String v)
	{
		return setValue ("ROUTEINFO", v);
	}
	
	/**
	 * Get the ROUTEINFO if it exists
	 * @return the ID or null if there is none
	 */
	public String getRouteInfo ()
	{
		return getValue ("ROUTEINFO");
	}
	
	/**
	 * Set the SERVICE if it exists
	 * @param v value to set
	 * @return true if successful
	 */
	public boolean setService (String v)
	{
		return setValue ("SERVICE", v);
	}
	
	/**
	 * Get the SERVICE if it exists
	 * @return the ID or null if there is none
	 */
	public String getService ()
	{
		return getValue ("SERVICE");
	}
	
	/**
	 * Set the ACTION if it exists
	 * @param v value to set
	 * @return true if successful
	 */
	public boolean setAction (String v)
	{
		return setValue ("ACTION", v);
	}
	
	/**
	 * Get the ACTION if it exists
	 * @return the ID or null if there is none
	 */
	public String getAction ()
	{
		return getValue ("ACTION");
	}
	
	/**
	 * Set the ARGUMENTS if it exists
	 * @param v value to set
	 * @return true if successful
	 */
	public boolean setArguments (String v)
	{
		return setValue ("ARGUMENTS", v);
	}
	
	/**
	 * Get the ARGUMENTS if it exists
	 * @return the ID or null if there is none
	 */
	public String getArguments ()
	{
		return getValue ("ARGUMENTS");
	}
	
	/**
	 * Set the MESSAGERECIPIENT if it exists
	 * @param v value to set
	 * @return true if successful
	 */
	public boolean setRecipient (String v)
	{
		return setValue ("MESSAGERECIPIENT", v);
	}
	
	/**
	 * Get the MESSAGERECIPIENT if it exists
	 * @return the ID or null if there is none
	 */
	public String getRecipient ()
	{
		return getValue ("MESSAGERECIPIENT");
	}
	
	/**
	 * Set the ENCRYPTION if it exists
	 * @param v value to set
	 * @return true if successful
	 */
	public boolean setEncryption (String v)
	{
		return setValue ("ENCRYPTION", v);
	}
	
	/**
	 * Get the ENCRYPTION if it exists
	 * @return the ID or null if there is none
	 */
	public String getEncryption ()
	{
		return getValue ("ENCRYPTION");
	}
	
	/**
	 * Set the PROCESSINGSTATUS if it exists
	 * @param v value to set
	 * @return true if successful
	 */
	public boolean setProcessingStatus (String v)
	{
		return setValue ("PROCESSINGSTATUS", v);
	}
	
	/**
	 * Get the PROCESSINGSTATUS if it exists
	 * @return the ID or null if there is none
	 */
	public String getProcessingStatus ()
	{
		return getValue ("PROCESSINGSTATUS");
	}
	
	/**
	 * Set the TRANSPORTSTATUS if it exists
	 * @param v value to set
	 * @return true if successful
	 */
	public boolean setTransportStatus (String v)
	{
		return setValue ("TRANSPORTSTATUS", v);
	}
	
	/**
	 * Get the TRANSPORTSTATUS if it exists
	 * @return the it or null if there is none
	 */
	public String getTransportStatus ()
	{
		return getValue ("TRANSPORTSTATUS");
	}
	
	/**
	 * Set the TRANSPORTERRORCODE if it exists
	 * @param v value to set
	 * @return true if successful
	 */
	public boolean setTransportErrorCode (String v)
	{
		return setValue ("TRANSPORTERRORCODE", v);
	}
	
	/**
	 * Get the TRANSPORTERRORCODE if it exists
	 * @return the it or null if there is none
	 */
	public String getTransportErrorCode ()
	{
		return getValue ("TRANSPORTERRORCODE");
	}
	
	/**
	 * Set the APPLICATIONSTATUS if it exists
	 * @param v value to set
	 * @return true if successful
	 */
	public boolean setApplicationStatus (String v)
	{
		return setValue ("APPLICATIONSTATUS", v);
	}
	
	/**
	 * Get the APPLICATIONSTATUS if it exists
	 * @return the it or null if there is none
	 */
	public String getApplicationStatus ()
	{
		return getValue ("APPLICATIONSTATUS");
	}
	
	/**
	 * Set the APPLICATIONERRORCODE if it exists
	 * @param v value to set
	 * @return true if successful
	 */
	public boolean setApplicationErrorCode (String v)
	{
		return setValue ("APPLICATIONERRORCODE", v);
	}
	
	/**
	 * Get the APPLICATIONERRORCODE if it exists
	 * @return the it or null if there is none
	 */
	public String getApplicationErrorCode ()
	{
		return getValue ("APPLICATIONERRORCODE");
	}

	/**
	 * Set the APPLICATIONRESPONSE if it exists
	 * @param v value to set
	 * @return true if successful
	 */
	public boolean setApplicationResponse (String v)
	{
		return setValue ("APPLICATIONRESPONSE", v);
	}
	
	/**
	 * Get the APPLICATIONRESPONSE if it exists
	 * @return the it or null if there is none
	 */
	public String getApplicationResponse ()
	{
		return getValue ("APPLICATIONRESPONSE");
	}
	
	/**
	 * Set the MESSAGERECIPIENT if it exists
	 * @param v value to set
	 * @return true if successful
	 */
	public boolean setMessageRecipient (String v)
	{
		return setValue ("MESSAGERECIPIENT", v);
	}
	
	/**
	 * Get the MESSAGERECIPIENT if it exists
	 * @return the it or null if there is none
	 */
	public String getMessageRecipient ()
	{
		return getValue ("MESSAGERECIPIENT");
	}
	
	/**
	 * Set the MESSAGERECEIVEDTIME if it exists
	 * @param v value to set
	 * @return true if successful
	 */
	public boolean setMessageReceivedTime (String v)
	{
		return setValue ("MESSAGERECEIVEDTIME", v);
	}
	
	/**
	 * Get the MESSAGERECEIVEDTIME if it exists
	 * @return the it or null if there is none
	 */
	public String getMessageReceivedTime ()
	{
		return getValue ("MESSAGERECEIVEDTIME");
	}
	
	/**
	 * Set the SIGNATURE if it exists
	 * @param v value to set
	 * @return true if successful
	 */
	public boolean setSignature (String v)
	{
		return setValue ("SIGNATURE", v);
	}
	
	/**
	 * Get the SIGNATURE if it exists
	 * @return the it or null if there is none
	 */
	public String getSignature ()
	{
		return getValue ("SIGNATURE");
	}
	
	/**
	 * Set the PUBLICKEYLDAPADDRESS if it exists
	 * @param v value to set
	 * @return true if successful
	 */
	public boolean setPublicKeyLdapAddress (String v)
	{
		return setValue ("PUBLICKEYLDAPADDRESS", v);
	}
	
	/**
	 * Get the PUBLICKEYLDAPADDRESS if it exists
	 * @return the it or null if there is none
	 */
	public String getPublicKeyLdapAddress ()
	{
		return getValue ("PUBLICKEYLDAPADDRESS");
	}
	
	/**
	 * Set the PUBLICKEYLDAPDN if it exists
	 * @param v value to set
	 * @return true if successful
	 */
	public boolean setPublicKeyLdapDn (String v)
	{
		return setValue ("PUBLICKEYLDAPDN", v);
	}
	
	/**
	 * Get the PUBLICKEYLDAPDN if it exists
	 * @return the it or null if there is none
	 */
	public String getPublicKeyLdapDn ()
	{
		return getValue ("PUBLICKEYLDAPDN");
	}

	/**
	 * Set the PUBLICKEYLDAPBASEDN if it exists
	 * @param v value to set
	 * @return true if successful
	 */
	public boolean setPublicKeyLdapBaseDn (String v)
	{
		return setValue ("PUBLICKEYLDAPBASEDN", v);
	}
	
	/**
	 * Get the PUBLICKEYLDAPBASEDN if it exists
	 * @return the it or null if there is none
	 */
	public String getPublicKeyLdapBaseDn ()
	{
		return getValue ("PUBLICKEYLDAPBASEDN");
	}

	/**
	 * Set the CERTIFICATEURL if it exists
	 * @param v value to set
	 * @return true if successful
	 */
	public boolean setCertificateUrl (String v)
	{
		return setValue ("CERTIFICATEURL", v);
	}
	
	/**
	 * Get the CERTIFICATEURL if it exists
	 * @return the it or null if there is none
	 */
	public String getCertificateUrl ()
	{
		return getValue ("CERTIFICATEURL");
	}
	
	/**
	 * Set the DESTINATIONFILENAME if it exists
	 * @param v value to set
	 * @return true if successful
	 */
	public boolean setDestinationFileName (String v)
	{
		return setValue ("DESTINATIONFILENAME", v);
	}
	
	/**
	 * Get the DESTINATIONFILENAME if it exists
	 * @return the it or null if there is none
	 */
	public String getDestinationFileName ()
	{
		return getValue ("DESTINATIONFILENAME");
	}
	/**
	 * Set the RESPONSEMESSAGEID
	 * @param v value to set
	 * @return true if successful
	 */
	public boolean setResponseMessageId (String v)
	{
		return setValue ("RESPONSEMESSAGEID", v);
	}
	
	/**
	 * Get the RESPONSEMESSAGEID if it exists
	 * @return the it or null if there is none
	 */
	public String getResponseMessageId ()
	{
		return getValue ("RESPONSEMESSAGEID");
	}
	
	/**
	 * Set the RESPONSEARGUMENTS
	 * @param v value to set
	 * @return true if successful
	 */
	public boolean setResponseArguments (String v)
	{
		return setValue ("RESPONSEARGUMENTS", v);
	}
	
	/**
	 * Get the RESPONSEARGUMENTS if it exists
	 * @return the it or null if there is none
	 */
	public String getResponseArguments ()
	{
		return getValue ("RESPONSEARGUMENTS");
	}
	
	/**
	 * Set the RESPONSEFILENAME
	 * @param v value to set
	 * @return true if successful
	 */
	public boolean setResponseFileName (String v)
	{
		return setValue ("RESPONSEFILENAME", v);
	}
	
	/**
	 * Get the RESPONSEFILENAME if it exists
	 * @return the it or null if there is none
	 */
	public String getResponseFileName ()
	{
		return getValue ("RESPONSEFILENAME");
	}
	
	/**
	 * Set the RESPONSELOCALFILE
	 * @param v value to set
	 * @return true if successful
	 */
	public boolean setResponseLocalFile (String v)
	{
		return setValue ("RESPONSELOCALFILE", v);
	}
	
	/**
	 * Get the RESPONSELOCALFILE if it exists
	 * @return the it or null if there is none
	 */
	public String getResponseLocalFile ()
	{
		return getValue ("RESPONSELOCALFILE");
	}
	
	/**
	 * Set the RESPONSEMESSAGEORIGIN
	 * @param v value to set
	 * @return true if successful
	 */
	public boolean setResponseMessageOrigin (String v)
	{
		return setValue ("RESPONSEMESSAGEORIGIN", v);
	}
	
	/**
	 * Get the RESPONSEMESSAGEORIGIN if it exists
	 * @return the it or null if there is none
	 */
	public String getResponseMessageOrigin ()
	{
		return getValue ("RESPONSEMESSAGEORIGIN");
	}
	
	/**
	 * Set the RESPONSEMESSAGESIGNATURE
	 * @param v value to set
	 * @return true if successful
	 */
	public boolean setResponseMessageSignature(String v)
	{
		return setValue ("RESPONSEMESSAGESIGNATURE", v);
	}
	
	/**
	 * Get the RESPONSEMESSAGESIGNATURE if it exists
	 * @return the it or null if there is none
	 */
	public String getResponseMessageSignature ()
	{
		return getValue ("RESPONSEMESSAGESIGNATURE");
	}
	
	/**
	 * Set the PRIORITY if it exists
	 * @param v value to set
	 * @return true if successful
	 */
	public boolean setPriority (String v)
	{
		return setValue ("PRIORITY", v);
	}
	
	/**
	 * Get the PRIORITY if it exists
	 * @return the it or null if there is none
	 */
	public String getPriority ()
	{
		return getValue ("PRIORITY");
	}
	
	/************************ receiver unique **********************************/
	
	/**
	 * Set the FROMPARTYID if it exists
	 * @param v value to set
	 * @return true if successful
	 */
	public boolean setFromPartyId (String v)
	{
		return setValue ("FROMPARTYID", v);
	}
	
	/**
	 * Get the FROMPARTYID if it exists
	 * @return the it or null if there is none
	 */
	public String getFromPartyId ()
	{
		return getValue ("FROMPARTYID");
	}
	/**
	 * Set the RECEIVEDTIME if it exists
	 * @param v value to set
	 * @return true if successful
	 */
	public boolean setReceivedTime (String v)
	{
		return setValue ("RECEIVEDTIME", v);
	}
	
	/**
	 * Get the RECEIVEDTIME if it exists
	 * @return the it or null if there is none
	 */
	public String getReceivedTime ()
	{
		return getValue ("RECEIVEDTIME");
	}
	
	/**
	 * Set the LASTUPDATETIME if it exists
	 * @param v value to set
	 * @return true if successful
	 */
	public boolean setLastUpdateTime (String v)
	{
		return setValue ("LASTUPDATETIME", v);
	}
	
	/**
	 * Get the LASTUPDATETIME if it exists
	 * @return the it or null if there is none
	 */
	public String getLastUpdateTime ()
	{
		return getValue ("LASTUPDATETIME");
	}
	
	/**
	 * Set the PAYLOADNAME if it exists
	 * @param v value to set
	 * @return true if successful
	 */
	public boolean setPayloadName (String v)
	{
		return setValue ("PAYLOADNAME", v);
	}
	
	/**
	 * Get the PAYLOADNAME if it exists
	 * @return the it or null if there is none
	 */
	public String getPayloadName ()
	{
		return getValue ("PAYLOADNAME");
	}

	/**
	 * Set the LOCALFILENAME if it exists
	 * @param v value to set
	 * @return true if successful
	 */
	public boolean setLocalFileName (String v)
	{
		return setValue ("LOCALFILENAME", v);
	}
	
	/**
	 * Get the LOCALFILENAME if it exists
	 * @return the it or null if there is none
	 */
	public String getLocalFileName ()
	{
		return getValue ("LOCALFILENAME");
	}
	/**
	 * Set the ERRORCODE if it exists
	 * @param v value to set
	 * @return true if successful
	 */
	public boolean setErrorCode (String v)
	{
		return setValue ("ERRORCODE", v);
	}
	
	/**
	 * Get the ERRORCODE if it exists
	 * @return the it or null if there is none
	 */
	public String getErrorCode ()
	{
		return getValue ("ERRORCODE");
	}
	/**
	 * Set the ERRORMESSAGE if it exists
	 * @param v value to set
	 * @return true if successful
	 */
	public boolean setErrorMessage (String v)
	{
		return setValue ("ERRORMESSAGE", v);
	}
	
	/**
	 * Get the ERRORMESSAGE if it exists
	 * @return the it or null if there is none
	 */
	public String getErrorMessage ()
	{
		return getValue ("ERRORMESSAGE");
	}
}
