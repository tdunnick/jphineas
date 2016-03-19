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
