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

import java.util.ArrayList;

import tdunnick.jphineas.common.JPhineas;

public class PingData
{
	/** title in the navigation bar */
  String title = "CPA Export/Ping";
  /** revision in the navigation bar */
  String version = JPhineas.revision;
  /** routes to ping */
  ArrayList <String> routes = new ArrayList <String> ();
  /** queues to use */
  ArrayList <String> queues = new ArrayList <String> ();
  
  public PingData (ArrayList <String> routes, ArrayList <String> queues)
  {
  	this.routes = routes;
  	this.queues = queues;
  }
  
	public String getTitle()
	{
		return title;
	}
	public String getVersion()
	{
		return version;
	}
	public ArrayList<String> getRoutes()
	{
		return routes;
	}
	public ArrayList<String> getQueues()
	{
		return queues;
	}
}
