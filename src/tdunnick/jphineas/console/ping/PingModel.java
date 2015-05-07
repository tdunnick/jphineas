package tdunnick.jphineas.console.ping;

import java.util.*;
import java.io.*;
import javax.servlet.http.HttpServletRequest;

import tdunnick.jphineas.xml.*;
import tdunnick.jphineas.logging.*;
import tdunnick.jphineas.queue.*;
import tdunnick.jphineas.sender.ebxml.*;

public class PingModel
{
	private XmlConfig sender = new XmlConfig ();
  private ArrayList <String> routes = new ArrayList <String> ();
  private ArrayList <String> queues = new ArrayList <String> ();
 
	public boolean initialize (XmlConfig config)
	{
		File f = config.getFile ("Sender");
		if ((f == null) || !sender.load(f))
		{
			Log.error ("Can't load Sender Configuration");
			return false;
		}
		int n = sender.getTagCount("RouteInfo.Route");
		for (int i = 0; i < n; i++)
		{
			String tag = "RouteInfo.Route[" + i + "].";
			routes.add (sender.getValue(tag + "Name"));
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
			String cpaDir = sender.getDirectory ("CpaDirectory");
			if (cpaDir.length() == 0)
				cpaDir = sender.getDirectory (XmlConfig.DEFAULTDIR) + "CPA/";
			File f = new File (cpaDir);
			if (!f.exists())
			  f.mkdirs();
			int r = routes.indexOf(route);
			if (r < 0)
				return "Unknown route " + route;
			CpaXml x = new CpaXml (sender.copy("RouteInfo.Route[" + r + "]"));
			f = new File (cpaDir + x.getCpaName() + ".xml");
			if (x.save(f))
			  return "CPA for route " + route  + " exported to " + f.getAbsolutePath();
			return "failed to export CPA for " + route;
		}
		return "Unknown action " + action;
	}
}
