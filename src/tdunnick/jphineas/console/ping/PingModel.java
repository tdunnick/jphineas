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

package tdunnick.jphineas.console.ping;

import java.util.*;
import java.io.*;
import javax.servlet.http.HttpServletRequest;

import tdunnick.jphineas.xml.*;
import tdunnick.jphineas.config.RouteConfig;
import tdunnick.jphineas.config.SenderConfig;
import tdunnick.jphineas.logging.*;
import tdunnick.jphineas.queue.*;
import tdunnick.jphineas.sender.ebxml.*;

public class PingModel
{
	private SenderConfig sender = null;
  private ArrayList <String> routes = new ArrayList <String> ();
  private ArrayList <String> queues = new ArrayList <String> ();
 
	public boolean initialize (SenderConfig config)
	{
		sender = config;
		int n = sender.getRouteCount();
		for (int i = 0; i < n; i++)
		{
			RouteConfig c = sender.getRoute(i);
			routes.add (c.getName());
		}
		queues = PhineasQManager.getInstance().getQueueNames("EbXmlSndQ");
		return true;
	}
	
	public PingData getPingData (HttpServletRequest request)
	{
		String msg = pingRoute (request);
		if (msg != null)
			Log.info(msg);
		PingData p = new PingData (routes, queues);
		return (p);
	}
	
	private String pingRoute (HttpServletRequest request)
	{
		String action = request.getParameter("_action_");
		if (action == null)
			return null;
		String route = request.getParameter("Route");
		if (route == null)
			return "Route not selected";
		if (action.equals("Ping"))
		{
			String queue = request.getParameter("Queue");
			if (queue == null)
				return "Queue not selected";
			if (EbXmlQueue.addPing (PhineasQManager.getInstance().getQueue(queue), route))
				return "<ul>Ping " + route + " queued to " + queue;
			return "<ul>Ping " + route + " to " + queue + " failed";
		}
		if (action.equals("Export"))
		{
			String d = sender.getCpaDirectory();
			int r = routes.indexOf(route);
			if (r < 0)
				return "Unknown route " + route;
			RouteConfig cfg = sender.getRoute (r);
			CpaXml x = new CpaXml (cfg);
			String cpaid = cfg.getCpa();
			if (cpaid == null)
				cpaid = x.getCpaName();
			File f = new File (d + cpaid + ".xml");
			if (x.save(f))
			  return "CPA for route " + route  + " exported to " + f.getAbsolutePath();
			return "failed to export CPA for " + route;
		}
		return "Unknown action " + action;
	}
}
