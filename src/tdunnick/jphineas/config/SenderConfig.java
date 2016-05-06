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
