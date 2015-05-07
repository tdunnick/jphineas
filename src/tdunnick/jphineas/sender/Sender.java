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

package tdunnick.jphineas.sender;

import java.io.*;
import java.net.*;
import javax.servlet.*;
import javax.servlet.http.*;

import tdunnick.jphineas.common.*;
import tdunnick.jphineas.logging.*;
import tdunnick.jphineas.queue.*;
import tdunnick.jphineas.sender.ebxml.EbXmlQueue;
import tdunnick.jphineas.xml.*;

/**
 * The jPhineas sender servlet.  This uses a single configuration file to 
 * set up Templates and the QueueManager.  It first fills in any needed 
 * defaults and performs some sanity checks.  Then starts a FolderPoller and 
 * QueuePoller Pthreads to handle outgoing messages.
 * 
 * @author Thomas Dunnick
 * 
 */
public class Sender extends HttpServlet
{
	private static String status = "stopped";
	/*
	 * identifiers found in our XML configuration file - note this follows
	 * receiver.xml conventions, with our own additions.
	 */
	// servlet configuration
	private static String configName = null;
	// folder poller
	private static FolderPoller folderPoller = null;
	// a queue poller
	private static QueuePoller queuePoller = null;
	// the queue manager
	private static PhineasQManager manager = PhineasQManager.getInstance();
	
	/**
	 * do basic initialization needed for this servlet to start
	 * 
	 * @return true if successful
	 */
	public static boolean startup ()
	{
		String s = null;
		if (configName == null)
		{
			Log.error ("startup with no configuration");
			return false;
		}
		
		// add bouncy castle to the security providers for missing algorithms
	  java.security.Security.addProvider (
	  		new org.bouncycastle.jce.provider.BouncyCastleProvider());

		// load the configuration
	  XmlConfig master = new XmlConfig ();
		if (!master.load(new File (configName)))
		{
			status = "failed loading " + configName;
			return false;
		}
		XmlConfig config = new XmlConfig ();
		if (!config.load(master.getFile("Sender")))
		{
			status = "Failed loading configuration from " 
				+ master.getValue ("Sender");
			return false;
		}
		// copy in globals values
		config.setValue("HostId", master.getValue ("HostId"));
		config.setValue("Domain", master.getValue ("Domain"));
		config.setValue("Organization", master.getValue ("Organization"));
		// configure logging
		Log.xmlLogConfig(config.copy("Log"));
		Log.info("Starting sender servlet...");
		if (config.getValue("Domain") == null)
		{
			try
			{
			  config.setValue("Domain", InetAddress.getLocalHost().getCanonicalHostName());
			}
			catch (Exception e)
			{
				Log.warn("Can't set Sender.Host " + e.getMessage());
			}
		}
		if (config.getInt("PollInterval") < 1)
		{
			Log.warn ("Setting default PollInterval to 30");
			config.setValue("PollInterval", "30");
		}
		if (config.getInt("MaxThreads") < 1)
		{
			Log.warn ("Setting default MaxThreads to 3");
			config.setValue ("MaxThreads", "3");
		}
		if (config.getFolder("QueueDirectory") == null)
		{
			s = System.getProperty ("java.io.tmpdir");
			if (s == null)
			{
				Log.error("Can't set QueueDirectory");
				return false;
			}
			Log.warn ("Setting default QueueDirectory to " + s);
			config.setValue("QueueDirectory", s);
		}
		// start the folder poller
		folderPoller = new FolderPoller (config);
		folderPoller.setName ("FolderPoller");
		folderPoller.start();
		// start the queue poller
		queuePoller = new QueuePoller (config);
		queuePoller.setName("QueuePoller");
		queuePoller.start ();
		status = "running";
		return (true);
	}
	
	/**
	 * shutdown this service
	 */
	public static void shutdown ()
	{
		// shut down the folder and queue poller
		if (folderPoller != null)
		{
		  folderPoller.quit ();
		  folderPoller = null;
		}
		if (queuePoller != null)
		{
			queuePoller.quit ();
			queuePoller = null;
		}
		status = "stopped";
	}
	
	/**
	 * A GET should be interactive, so send a brief message back to the browser
	 * to indicate we are alive and well.
	 * 
	 * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException
	{
		String msg = JPhineas.getHtml () + "<p>Sender " + status + "</p>";
		/*
		 * for debugging purposes, check for a Ping request
		String route = req.getParameter("Ping");
		if (route != null)
		{
			if (EbXmlQueue.addPing (manager.getQueue("HsqlSendQ"), route))
				msg += "<ul>Ping " + route + " queued to HsqlSendQ";
			else
				msg += "<ul>Ping " + route + " to HsqlSendQ failed";
		}
		 */
		msg = "<html><body>" + msg + "</body></html>";
		resp.getOutputStream().write (msg.getBytes());
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException
	{
		doGet (req, resp);
	}

	/**
	 * Clean up and die...
	 * @see javax.servlet.GenericServlet#destroy()
	 */
	public void destroy()
	{
		shutdown ();
		Log.info("Sender exiting...");
 		super.destroy();
	}

	/**
	 * set up our thread name, load the configuration, initialize, and go...
	 * @throws exception if this doesn't work
	 * @see javax.servlet.GenericServlet#init()
	 */
	public void init() throws ServletException
	{
		Thread.currentThread().setName("Sender");
	  configName = getServletContext().getInitParameter("Configuration");
		// Log.fine ("Configuration file=" + configFile);
		if (!startup())
		{
			throw new ServletException ("Fatal error: " + status + " jPhineas Sender");
		}
		Log.info ("jPhineas Sender Ready");
	}
}
