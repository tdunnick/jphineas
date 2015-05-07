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


package tdunnick.jphineas.console;

import java.io.*;
import java.net.*;
import javax.servlet.*;
import javax.servlet.http.*;

import tdunnick.jphineas.queue.*;
import tdunnick.jphineas.receiver.Receiver;
import tdunnick.jphineas.sender.Sender;
import tdunnick.jphineas.console.config.*;
import tdunnick.jphineas.console.queue.*;
import tdunnick.jphineas.console.logs.*;
import tdunnick.jphineas.console.ping.*;
import tdunnick.jphineas.logging.*;
import tdunnick.jphineas.xml.*;

/**
 * This is the controller for the jPhineas console.  Managed functions are mapped
 * to HTML pages in web.xml.  Request URL's are examined for the desired page,
 * and a model is called to deliver a domain object which is subsequently passed
 * to a JSP view.
 * <p>
 * The console configuration includes links to sender and receiver configurations,
 * as well as meta data used to manage jPhineas configuration screens/pages.
 * 
 * @author Thomas Dunnick
 *
 */
public class Console extends HttpServlet
{
	private static final long serialVersionUID = 1L;
	private static XmlConfig config = null;
	private static String status = "stopped";
	private QueueModel mon = null;
	private ConfigModel cfg = null;
	private LogModel logs = null;
	private PingModel ping = null;
	

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public Console()
	{
		super();
	}
	
	/**
	 * do basic initialization needed for this servlet
	 * 
	 * @return true if successful
	 */
	private boolean initialize ()
	{
		String conf = getServletContext().getInitParameter("Configuration");
		config = new XmlConfig ();
		if (!config.load(new File (conf)))
		{
			status = "failed loading " + conf;
			return false;
		}
		Log.xmlLogConfig (config.copy("Log"));
		Log.info("Console starting...");
		// instantiate our queue and configuration models
		cfg = new ConfigModel ();
		if (!cfg.initialize (conf))
			return false;
		mon = new QueueModel ();
		if (!mon.initialize(config))
			return false;
		ping = new PingModel ();
		if (!ping.initialize (config))
			return false;
		logs = new LogModel ();
		return true;	
	}

	/**
	 * @see Servlet#init(ServletConfig)
	 */
	public void init() throws ServletException
	{
		Thread.currentThread().setName("Console");
		// logger.fine ("Configuration file=" + configFile);
		if (!initialize())
		{
			throw (new ServletException ("Fatal error: error initializing jPhineas Console"));
		}
		status = "ready";
		Log.info ("jPhineas Console Ready");
	}

	/**
	 * @see Servlet#destroy()
	 */
	public void destroy()
	{
		Log.info("Console Exiting...");
		super.destroy();
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException
	{
		doPost (request, response);
	}

	/**
	 * Examine the request URL and dispatch a model and view based on it.
	 * 
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException
	{
		String s = request.getRequestURI();
		String f = null;
		Thread.currentThread().setName("Console");

		Log.debug ("request path=" + s);
		Object o = null;
		
		if (s.contains (".png"))
		{
			byte[] img = mon.getChart(s);
			if (img != null)
			{
				// Log.debug ("img size=" + img.length);
				response.setHeader("Content-Type", "image/png");
				response.setHeader("Content-Length", Integer.toString (img.length));
				OutputStream out = response.getOutputStream();
				out.write(img);
				out.close();
				return;
			}
		}
		
		if (s.contains ("restart.html"))
		{
			Receiver.shutdown();
			Sender.shutdown();
			PhineasQManager.getInstance().restart ();
			Sender.startup();
			Receiver.startup();
			initialize ();
			String r = request.getHeader("referer");
			if (r != null)
			{
				URL url = new URL (r);
				s = url.getPath();
			}
		}

		if (s.contains ("dashboard.html"))
		{
			if ((o = mon.getDashBoardData (request)) != null)
			{
				request.setAttribute("dashboard", o);
				f = "/views/dashboard.jsp";
			}
		}
		else if (s.contains("queues.html"))
		{
			mon.updateQueue(request);
			if ((o = mon.getMonitorData(request)) != null)
			{
				request.setAttribute("queues", o);
				f = "/views/queues.jsp";
			}
		}
		else if (s.contains ("config.html"))
		{
			cfg.updateConfig(request);
			if ((o = cfg.getConfigData(request)) != null)
			{
				request.setAttribute("config", o);
				f = "/views/config.jsp";
			}
		}
		else if (s.contains ("logs.html"))
		{
			if ((o = logs.getLogData (request)) != null)
			{
				request.setAttribute ("logs", o);
				f = "/views/logs.jsp";
			}
		}
		else if (s.contains ("ping.html"))
		{
			if ((o = ping.getPingData (request)) != null)
			{
				request.setAttribute ("ping", o);
				f = "/views/ping.jsp";
			}
		}
		else if (s.contains("error") || (f == null))
		{
			request.setAttribute("error", new ErrorData ());
			f = "/views/error.jsp";
		}
		Log.debug ("dispatching to " + f);
		RequestDispatcher view = request.getRequestDispatcher(f);
		view.forward(request, response);
	}
}
