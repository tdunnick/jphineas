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

import java.util.*;
import tdunnick.jphineas.logging.*;
import tdunnick.jphineas.queue.*;
import tdunnick.jphineas.util.*;

/**
 * A Queue PThread.
 * 
 * This loops checking a priority queue of PhineasQRow records to process.  When
 * it finds one it calls a processor based on the Route.  There are MaxThreads
 * copies of this active, all sharing the same priority queue.
 * 
 * @author tld
 *
 */
public class QueueThread extends Pthread
{
	/** the queue of rows to send */
	private PriorityBlockingQ queue = null;
	/** a list of known routes */
	private HashMap <String,RouteInfo> routes = null;
	
	
	/**
	 * Get configuration and the queue
	 * @param q blocking queue of PhineasQRow objects
	 * @param r a list of routes to service
	 */
	public QueueThread (PriorityBlockingQ q, HashMap <String,RouteInfo> r)
	{
		super ("QueueThread");
		queue = q;
		routes = r;
	}
		 	  
	public void run()
	{
		Log.info("Starting");
		if (queue == null)
		{
			Log.error ("Sender queue not specified");
			return;
		}
		while (processRows ());
		Log.info("Exiting");
	}
	
	/**
	 * A common point of failure, that updates the queue and logs a message.
	 * 
	 * @param r
	 * @param msg
	 */
	private void fail (PhineasQRow r, String msg)
	{
		r.setProcessingStatus("failed");
		r.setTransportStatus (msg);
		Log.error(msg);
	}
	
	/**
	 * Send one queued file
	 * @return false if this thread should exit
	 */
	private boolean processRows ()
	{
		PhineasQRow r;
		// get the next row to process from our queue - this may block
		try
		{
		  r = (PhineasQRow) queue.take();
		}
		catch (InterruptedException e)
		{
			Log.debug ("Sender interrupted");
			return false;
		}
		if (!running ()) 
			return false;
		if (r == null)
		{
			Log.error("Sender Queue empty");
			return false;
		}
		// note we are attempting to transmit this file
		r.setProcessingStatus ("attempted");
		r.setLastUpdateTime(DateFmt.getTimeStamp(null));
		if (r.update() < 0)
		{
			Log.error("Can't update row status");
			return false;
		}
		RouteInfo route = routes.get(r.getRouteInfo());
		if (route == null)
		{
			fail (r, "Can't get Route information for " + r.getRouteInfo());
			return (running() && (r.update() >= 0));
		}
 	  Log.debug("attempting to process message");
  	int retries = 0;
  	while (running() && !route.getProcessor().process (r))
  	{
  		if ((route.getTimeout() < 1) || (retries++ >= route.getRetry()))
  		{
  			fail (r, "Retries expired for " + r.getRouteInfo());
  			break;
  		}
  		// TODO exponential backoff???
  		if (!psleep (route.getTimeout()))
  			return false;
   	}
		r.setLastUpdateTime(DateFmt.getTimeStamp(null));
		return (running() && (r.update() >= 0));
	}
}
