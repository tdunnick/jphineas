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
