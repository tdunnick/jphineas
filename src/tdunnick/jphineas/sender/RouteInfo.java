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

package tdunnick.jphineas.sender;

import tdunnick.jphineas.config.RouteConfig;
import tdunnick.jphineas.logging.Log;
import tdunnick.jphineas.xml.*;

/**
 * A domain class to parse and provide route information to various parts
 * of the sender from the sender's configuration.
 * 
 * @author tld
 *
 */
public class RouteInfo
{
  private String name = null;
  /** route package class */
  private RouteProcessor processor = null;
  /** route timeout in seconds */
  private int timeout = 0;
  /** route retries */
  private int retry = 0;
    
  public boolean configure (RouteConfig config)
	{
  	if (config == null)
  	{
  		Log.error("NULL configuration");
  		return false;
  	}
  	name = config.getName ();
  	timeout = config.getTimeout();
  	retry = config.getRetry ();
  	if (((processor = config.getProcessor()) != null) && processor.configure (config))
  		return true;
  	Log.error("Route missing processor");
  	return false;
	}
  
  protected String getName ()
  {
  	return name;
  }
  
  protected int getTimeout ()
  {
  	return timeout;
  }
  
  protected int getRetry ()
  {
  	return retry;
  }

  protected RouteProcessor getProcessor ()
  {
  	return processor;
  }
}
