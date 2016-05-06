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


package tdunnick.jphineas.console.queue
;

import java.util.*;
import javax.servlet.http.*;

import tdunnick.jphineas.common.*;
import tdunnick.jphineas.config.XmlConfig;
import tdunnick.jphineas.queue.*;
import tdunnick.jphineas.logging.*;
import tdunnick.jphineas.xml.*;
import tdunnick.jphineas.util.*;

/**
 * This is the model that prepares data for display of queues
 * Key items to track for display are:
 * <ul>
 * <li>table - current table</li>
 * <li>recordId - current record </li>
 * <li>top - current top record</li>
 * <li>route - current route for sender tables</li>
 * <li>partyId - current sender party ID for receiver tables<li>
 * <li>ends - date when dashboard data display ends</li>
 * <li>days - number of days shown in dashboard</li>
 * </ul>
 * 
 * 
 * @author Thomas Dunnick
 */
public class QueueModel
{
	private String Version = "Monitor RO 0.10 08/08/2012";
	private ArrayList <Object[]> tables = new ArrayList <Object[]>();
	private PhineasQManager manager = PhineasQManager.getInstance();
	
	private final static long MS = 24*60*60*1000;
	
  public QueueModel ()
	{
		super();
	}
		
	public String getVersion()
	{
		return Version;
	}

	public void setVersion (String version)
	{
		Version = version;
	}

	/**
	 * do basic initialization... load and initialize properties and logging
	 * and refresh current configuration data
	 * 
	 * @param config properties for the console - ignored (for now...)
	 * @return true if successful
	 */
	public boolean initialize (XmlConfig config)
	{
		return refresh ();
	}

	/**
	 * clean up
	 */
	public void close ()
	{
	}

	/**
	 * Generate the data needed for the queue monitor view.
	 * 
	 * @param request received by the monitor
	 * @return a queue monitor 
	 */
	public QueueData getMonitorData (HttpServletRequest request)
	{
		
		setSession (request);
		QueueData mon = new QueueData ();	
		mon.setVersion(Version);
		if (setMonitor (mon, setSession (request)))
		  return (mon);
		return null;
	}
	
	
	/**
	 * Generate the bean needed for the dashboard view.
	 * 
	 * @param request received by the monitor
	 * @return a dashboard bean
	 */
  public DashBoardData getDashBoardData (HttpServletRequest request)
	{
		DashBoardData dash = getDashBoardSession (request);
		if (setDashBoard (dash, setSession (request)))
	    return dash;
		return null;
	}
	
	/**
	 * Return an image found in the dashboard bean.  This make use of a 
	 * bean cache so that we don't continually regenerate the data used by
	 * the chart.  Instead generate it once (in getDashBoardData above) and
	 * look it up on the browser call back requests.
	 * 
	 * @param path of image from the browser
	 * @param request from the browser
	 * @return the image
	 */
	public byte[] getChart (String path, HttpServletRequest request)
	{
		return getChart (path, getDashBoardSession (request));
	}

	/**
	 * Return an image found in the dashboard bean.  This make use of a 
	 * bean cache so that we don't continually regenerate the data used by
	 * the chart.  Instead generate it once (in getDashBoardData above) and
	 * look it up on the browser call back requests. Here assume session id
	 * is embedded in img path.
	 * 
	 * @param path to image
	 * @return the image
	 */
	public byte[] getChart (String path)
	{
		String id = path.replaceFirst("^.*_(.*)[.]png$", "$1");
		return getChart (path, getDashBoardSession (id));
	}
	
	/**
	 * Return an image found in a dashboard bean. 
	 * @param path to image
	 * @param dash bean
	 * @return the image
	 */
	public byte[] getChart (String path, DashBoardData dash)
	{
		Object[] c;
		if (path.indexOf ("bar") >= 0)
			c = dash.getBarchart ();	
		else if (path.indexOf ("line") >= 0)
			c = dash.getLinechart();
		else
			c = dash.getPiechart();
    if (c == null)
    {
    	return (new byte[0]);
    }
		return (byte[]) c[0];	
	}
	
	/************************** dashboard support *****************************/
	
	/**
	 * return a dashboard bean either from the cache, or a new one
	 * 
	 * @param request from browser used to search the cache
	 * @return a dashboard bean
	 */
	private DashBoardData getDashBoardSession (HttpServletRequest request)
	{
		return getDashBoardSession (request.getSession().getId());
	}

	/**
	 * return a dashboard bean either from the cache, or a new one
	 * 
	 * @param id from browser used to search the cache
	 * @return a dashboard bean
	 */
	private DashBoardData getDashBoardSession (String id)
	{
		DashBoardData dash = DashBoardCache.get(id);
		if (dash == null)
		{
			dash = new DashBoardData();
			dash.setVersion (Version);
		}
		return dash;
	}
	
	/**
	 * set a dashboard bean with all the data needed by the view, based on 
	 *  the session.  This includes the data, images, statistics, etc.
	 * 
	 * @param dash to set
	 * @param session used by this dash
	 * @return true if successful.
	 */
	private boolean setDashBoard (DashBoardData dash, HttpSession session)
	{
		// start out by pulling and setting session values
		String table = getAttribute(session, "table");
		if (table == null)
			table = getTransportName ();
		if (table == null)
		{
			Log.error("No transport table specified or found");
			return false;
		}
    dash.setSender(isTransport (table));
		int days = getInt(getAttribute(session, "days"));
		long ends = getDate(getAttribute(session, "ends"));
		dash.setTables(tables);
		dash.setConstraint(getAttribute(session, "constraint"));
		dash.setTable(table);
		if (days == 0)
			days = 365;
		dash.setDays(days);
		dash.setEnds(ends);
		// get our current stats and create the associate images
		setDashStats (dash, table, ends, days);
		Charts chart = new Charts();
		chart.getPieChart(dash);
		chart.getBarChart(dash);
		chart.getLineChart(dash);
		// finally save it in our cache for image recall
		DashBoardCache.put(session.getId(), dash);
		return true;
	}

	/**
	 * query the database to get the raw dashboard bean statistics
	 * 
	 * @param dash bean to set
	 * @param table to query
	 * @param ends when last item appears in statistics
	 * @param days statistics coveres
	 * @return
	 */
	private boolean setDashStats (DashBoardData dash, String table, 
			long ends, long days)
	{
		String constraintName = "ROUTEINFO";
		String dateName = "MESSAGERECEIVEDTIME";
		if (!isTransport (table))
		{
			constraintName = "FROMPARTYID";
			dateName = "RECEIVEDTIME";
		}
		long interval = days * MS;
		long start = ends - interval;
		PhineasQ queue = manager.getQueue(table);
		ArrayList <PhineasQRow> rows = queue.findByDate (start, ends);
		if (rows == null)
		{
			Log.debug("No rows found for date range "  + start + " to " + ends);
			return false;
		}
	  ArrayList <String[]> data = new ArrayList <String[]> ();
	  String constraintValue = dash.getConstraint();
		int n = 0, min = Integer.MAX_VALUE, max = 0, total = 0;
		interval /= 5;
		if (interval < MS)
			dash.setInterval("" + (interval * 24 / MS) + " hour");
		else if (interval / MS < 30)
			dash.setInterval("" + (interval / MS) + " day");
		else if (interval / MS < 150)
			dash.setInterval("" + (interval / (MS * 7)) + " week");
		else
			dash.setInterval("" + (interval / (MS * 30)) + " month");
		start += interval;
		int sz = rows.size ();
		for (int i = 0; i < sz; i++)
		{
			PhineasQRow r = rows.get (i);
	  	String s = r.getValue(constraintName);
	  	if (s == null)
	  	{
	  		Log.warn ("Record " + r.getRowId() + " missing " 
	  				+ constraintName + " in " + table);
	  		continue;
	  	}
	  	long t = DateFmt.getTime (r.getValue(dateName));
	  	if (t > start)
	  	{
	  		if (min > n) min = n;
	  		if (max < n) max = n;
	  		total += n;
	  		n = 0;
	  		start += interval;
	  	}
	  	String d = "" + t;
	  	data.add (new String[]{s,d});
	  	if ((constraintValue == null) || constraintValue.equals(s))
	  	  n++;
		}
		if (min > n) min = n;
		if (max < n) max = n;
		total += n;
	  dash.setStats(data);
	  dash.setMin (min);
	  dash.setMax(max);
	  dash.setTotal(total);
		return true;
	}
	
  /************************* queue monitor ********************************/
	
	/**
	 * set the data in the queue monitor bean needed for display based on
	 * criteria found in the session.
	 * 
	 * @param mon bean to set
	 * @param session for the request
	 * @return true if successful
	 */
	private boolean setMonitor (QueueData mon, HttpSession session)
	{
		String s;
		mon.setTables(tables);
		if ((s = getAttribute(session, "table")) == null)
			s = getTransportName(); 
		mon.setTable(s);
		mon.setConstraint(getAttribute(session, "constraint"));
		mon.setTop(getInt(getAttribute (session, "top")));
		mon.setRecordId(getInt(getAttribute (session, "recordId")));
		return (getData (mon));
	}
	
	/**
	 * query the database and retrieve the information needed by the
	 * queue view for display including a summary table and details for the
	 * selected record.
	 * 
	 * @param mon to fill in with the data
	 * @return true if successful
	 */
	private boolean getData (QueueData mon)
	{
		String t = mon.getTable();
		if (t == null)
			return false;
		PhineasQ queue = manager.getQueue(t);
		// get the field names for this queue
		ArrayList <String> fieldnames = 
			new ArrayList <String> (Arrays.asList(queue.getType().getNames()));
		// set the list of field names shown for the queue and details
		mon.setRowfields (fieldnames);
	  mon.setFields (fieldnames);
		// get the selected records
		ArrayList <PhineasQRow> rows = 
			queue.findByRecordId (mon.getConstraint(), mon.getTop());
		// and set them in the DAO
	  setMonitorRows (mon, rows);
	  // figure out a good value for backing up to previous rows
		int n = queue.lastRow();
		if ((mon.top > 0) && (mon.top < n))
		{
			if (mon.prev <= 0)
				mon.prev = mon.top + rows.size () - 1;
			if (mon.prev > n)
				mon.prev = n;
			Log.debug ("prev=" + mon.prev + " last=" + n);
		}
		else
			mon.prev = 0;
	  return true;
	}
	
  /**
   * move data from a JDBC result set into queue monitor bean
   * @param mon bean to update
   * @param res result set from JDBC
   * @return true if successful
   */
  private boolean setMonitorRows (QueueData mon, ArrayList <PhineasQRow> res)
  {
  	// the field shown for the rows table and details - currently match
		ArrayList <String> detailfields = mon.getFields();
		ArrayList <String> rowfields = mon.getRowfields();
		// the data for the rows table
	  ArrayList <ArrayList <String>> rows = new ArrayList <ArrayList <String>> ();
	  // the display class for each row
	  ArrayList <String> rowClass = new ArrayList <String> ();
	  boolean istransport = isTransport (mon.getTable());
	  String colname = "";
	  int n = res.size();
	  // hack for detail field size
	  int numfields = rowfields.size();
	  // build the table rows noting the record ID for detail display
	  for (int rownum = 0; rownum < n; rownum++)
		{
	  	String s = null;
	  	int recordId = 0;
	  	// the values for one row
	  	ArrayList <String> values = new ArrayList <String> ();
			// get the next row and add it to the rows
			PhineasQRow r = res.get (rownum);
			for (int i = 0; i < numfields; i++)
			{
				// get the current field and value and add it to the record
				// NOTE this assumes rowfields line up with the data...
				colname = rowfields.get(i);
				String v = r.getValue(i);
				values.add (v);
			}
		  // add these values to our table rows
			rows.add (values);
			// set the status for display class ok, queued, attempted, or failed
			if (istransport)
			{
				s = r.getProcessingStatus();
				if (s == null)
					s = "queued";
				if (s.equals("done"))
				{
					s = r.getTransportStatus();
					if ((s == null) || !s.equals("success"))
						s = "failed";
					else
					{
						s = r.getApplicationErrorCode();
						if ((s == null) || !s.equals ("none"))
							s = "warning";
						else
						  s = "ok";
					}
				}
				else if (!(s.equals ("queued") || s.equals("attempted")))
					s = "failed";
			}
			else
			{
				s = r.getErrorMessage();
				if ((s == null) || !s.equals ("none"))
					s = "failed";
				else
					s = "ok";
			}
			rowClass.add (s);
			
			// if no record ID is set, use first one found
			recordId = r.getRowId();
		  if (mon.getRecordId() == 0)
		  	mon.setRecordId (recordId);
		  // if this is the selected record, set the details
		  if (mon.getRecordId() == recordId)
		  {
		  	ArrayList <String> record = new ArrayList <String> ();
		  	int numdetails = detailfields.size ();
				// NOTE this assumes detailfields line up with the data...
		  	for (int i = 0; i < numdetails; i++)
		  	{
		  	  record.add(r.getValue(i));
		  	}
		  	mon.setRecord (record);
		  	if (istransport)
		  	{
		  	  s = r.getTransportStatus();
		  		if ((s != null) && !s.equalsIgnoreCase("success"))
		  			mon.resend = true;
		  	}
		  }
		}
	  // Log.debug("set " + rows.size() + " monitor queue rows");
	  mon.setRows(rows);
	  mon.setRowClass(rowClass);
  	return true;
  }
 	
	/**
	 * set our list of sender and receiver tables, including routes for
	 * the sender and party ID's for the receiver.
	 * @return true if successful
	 */
	private boolean refresh ()
	{
		tables.clear();
		return (getTransport() && getWorkers ());
	}

	/**
	 * get the list of worker (receiver) queues.
	 * each queue includes a list of party ID's 
	 * 
	 * @return true if successful
	 */
	private boolean getWorkers ()
	{
		ArrayList <String> q = manager.getQueueNames("EbXmlRcvQ");
    for (int i = 0; i < q.size(); i++)
		{
    	String n = q.get(i);
  		PhineasQ queue = manager.getQueue(n);
  		ArrayList <String> r = queue.findDistinct("FROMPARTYID");
  		if (r == null)
  			r = new ArrayList <String> ();  			
			Object[] entry = { n, r };
			tables.add (entry);
		}
		Log.debug("added " + q.size() + " worker (receiver) queues");
		return true;
	}
	
	/**
	 * load a list sender queue - each queue includes a list of routes
	 * @return true if successful
	 */
	private boolean getTransport ()
	{
		ArrayList <String> q = manager.getQueueNames("EbXmlSndQ");
    for (int i = 0; i < q.size(); i++)
		{
    	String n = q.get(i);
  		PhineasQ queue = manager.getQueue(n);
  		ArrayList <String> r = queue.findDistinct("ROUTEINFO");
  		if (r == null)
  			r = new ArrayList <String> ();
			Object[] entry = { n, r };
			tables.add (entry);
		}
		Log.debug("added " + q.size() + " transport (sender) queues");
		return true;
	}
	
	/**
	 * extract the sender's transport queue name from our cheap object above
	 * @return
	 */
	private String getTransportName ()
	{
		if ((tables == null) || (tables.size() == 0))
			return null;
		return (String) tables.get(0)[0];
	}
	
	/**
	 * return true if table is a transport table
	 */
	private boolean isTransport (String n)
	{
		return manager.getQueue(n).getType().getType().contains("SndQ");
	}

	/**
	 * safe integer parse
	 * @param value to parse
	 * @return 0 if fails
	 */
	private int getInt (String value)
	{
		try
		{
			return (Integer.parseInt(value));
		}
		catch (Exception e)
		{
			return 0;
		}
	}
	
	/**
	 * safe date parse
	 * @param value of date in seconds since the EPOCH
	 * @return the seconds since the EPOCH or now if fails
	 */
	private long getDate (String value)
	{
		java.util.Date d = new java.util.Date();
;
		if ((value != null) && value.matches("[0-9]+"))	try
		{
			d = new java.util.Date (Long.parseLong(value));
		}
		catch (Exception e)
		{
			Log.error ("date value: " + value);
		}
		return d.getTime();
	}
	
	/**
	 * get a session object as string
	 */
	private String getAttribute (HttpSession session, String name)
	{
		Object o = session.getAttribute(name);
		if (o == null)
			return null;
		return (String) o;
	}

	/******************** session data management *******************/
	
	/**
	 * Add any request parameters to the session data.  Reset sub-parameter -
	 * for example a change in the table voids everything else.  However,
	 * we set sub-paramters last so they can still be included on a RESTful
	 * URL
	 * 
	 * @param request
	 * @return
	 */
	private HttpSession setSession (HttpServletRequest request)
	{
		HttpSession session = request.getSession();
		// these are the parameters from the rest line that we record
		String[] parm = { "table", "constraint", "top", "recordId", "days", "ends" };
		for (int i = 0; i < parm.length; i++)			
		{
			String v = request.getParameter(parm[i]);
			if ((v == null) || (v.length() == 0))
				continue;
			Log.debug("setting session " + parm[i] + "=" + v);
			session.setAttribute (parm[i], v);
			if (i == 2) // "top"
			{
				session.setAttribute ("recordId", v);
			}
			else if (i < 2) // "table" or "constraint"
			{
				session.removeAttribute("recordId");
				session.removeAttribute("top");
				if (i == 0) // "table"
					session.removeAttribute("constraint");
			}
		}
		return session;
	}
	
	/**
	 * Makes any requested updated for deletion or resends.  Table name
	 * must be in REST or session parameters
	 * @param request for the page
	 * @return true if successful
	 */
	public boolean updateQueue (HttpServletRequest request)
	{
		String table = (String) request.getParameter("table");
		if (table == null)
		{
			HttpSession session = request.getSession();
			table = (String) session.getAttribute("table");
		}
		if ((table == null) || (table.length() == 0))
		  return true;
		String s = request.getParameter("delete");
		if ((s != null) && (s.length() > 0))
		{
			int r = Integer.parseInt (s);
			PhineasQ q = manager.getQueue(table);
			if (q == null)
				return false;
			Log.info("removing record " + r + " from " + table);
			return q.remove(r);
		}
		s = request.getParameter("resend");
		if ((s != null) && (s.length() > 0))
		{
			int r = Integer.parseInt (s);
			PhineasQ q = manager.getQueue(table);
			if (q == null)
				return false;
			PhineasQRow row = q.retrieve(r);
			row.setProcessingStatus("queued");
			if (row.update() < 0)
				return false;
			Log.info("Re-queued record " + r + " in " + table);
		}
		return true;
	}
}
