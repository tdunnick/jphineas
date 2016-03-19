package tdunnick.jphineas.config;

import java.io.File;

public class ReceiverConfig extends PhineasConfig
{
	private final static String servicePath = "ServiceInfo.Service";
	
	boolean init ()
	{
		super.init ();
		if (forceFolder ("PayloadDirectory", System.getProperty ("java.io.tmpdir")) == null)
			return false;
		if (forceFolder ("CpaDirectory", "CPA/") == null)
			return false;
		if (forceFolder ("CacheDirectory", System.getProperty ("java.io.tmpdir")) == null)
			return false;
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
}
