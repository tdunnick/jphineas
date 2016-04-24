package tdunnick.jphineas.config;

import java.io.File;

import tdunnick.jphineas.util.Chunker;

public class ReceiverConfig extends PhineasConfig
{
	private final static String servicePath = "ServiceInfo.Service";
	private final static String routePath = ".Sender.RouteInfo.Route";
	
	boolean init ()
	{
		super.init ();
		if (forceFolder ("PayloadDirectory", System.getProperty ("java.io.tmpdir")) == null)
			return false;
		if (forceFolder ("CpaDirectory", "CPA/") == null)
			return false;
		if (forceFolder ("CacheDirectory", System.getProperty ("java.io.tmpdir")) == null)
			return false;
		Chunker.setDir(getCacheFolder());
		return true;
	}
	
	public String getPayloadDirectory ()
	{
		return getDirectory ("PayloadDirectory");
	}
	
	public String getCpaDirectory ()
	{
		return getDirectory ("CpaDirectory");
	}
	
	public File getCacheFolder ()
	{
		return getFolder ("CacheDirectory");
	}
	
	public int getServiceCount ()
	{
		return getTagCount (servicePath);
	}
	
	public ServiceConfig getService (int n)
	{
		return (ServiceConfig) copy (new ServiceConfig (), servicePath + "[" + n + "].");
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
