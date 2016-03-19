package tdunnick.jphineas.config;

import tdunnick.jphineas.logging.*;

public class SenderConfig extends PhineasConfig
{
	private final static String mapPath = "MapInfo.Map";
	private final static String routePath = "RouteInfo.Route";
	
	boolean init ()
	{
		// Log.debug ("initialzing SenderConfig");
		if (!super.init ())
			return false;
		if (forceFolder ("QueueDirectory", System.getProperty ("java.io.tmpdir")) == null)
		{
			Log.error("Can't force QueueDirectory");
			return false;
		}
		if (forceFolder ("CpaDirectory", "CPA/") == null)
		{
			Log.error("Can't force CpaDirectory");
			return false;
		}
		return true;
	}
	
	public int getPollInterval ()
	{		
		return findInt ("PollInterval", 30);
	}
	
	public int getMaxThreads()
	{
		return findInt ("MaxThreads", 3);
	}
	
	public String getQueueDirectory ()
	{
		return getDirectory ("QueueDirectory");
	}
	
	public String getCpaDirectory ()
	{
		return getDirectory ("CpaDirectory");
	}
	
	public int getMapCount ()
	{
		return getTagCount (mapPath);
	}
	
	public FolderConfig getMap (int n)
	{
		return (FolderConfig) copy (new FolderConfig (), mapPath + "[" + n + "].");
	}
	
	public int getRouteCount ()
	{
		return getTagCount (routePath);
	}
	
	public RouteConfig getRoute (int n)
	{
		return (RouteConfig) copy (new RouteConfig (), routePath + "[" + n + "].");
	}	
}
