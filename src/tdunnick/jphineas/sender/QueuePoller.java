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

import tdunnick.jphineas.config.FolderConfig;
import tdunnick.jphineas.config.SenderConfig;
import tdunnick.jphineas.logging.*;
import tdunnick.jphineas.queue.*;
import tdunnick.jphineas.util.*;
import tdunnick.jphineas.xml.*;

/**
 * The Queue poller periodically checks transport queues for PhinmsQRow records to send.
 * It adds record ready to send to a priority queue. A set of helper
 * threads pull records off that queue as they find them.  The transport
 * queues are listed in Sender.MapInfo.Map[].Queue
 * 
 * @author Thomas Dunnick
 *
 */
public class QueuePoller extends Pthread
{
	/** polling interval */
	int pollInterval = 30;
  /** list of send queues */
  ArrayList <PhineasQ> sendQ = null;
  /** list of active helper threads */
  QueueThread[] queueThreads = null;
  /** our queue of rows to send */
  PriorityBlockingQ queue = null;
  /** list of active route processors */
  HashMap <String, RouteInfo> routes = null;

	/**
	 * Configures a set of sender queues, a priority blocking queue, 
	 * and set of threads to service them.
	 * @param config of the sender
	 */
	public QueuePoller (SenderConfig config)
	{
		super ("QueuePoller");
		pollInterval = config.getPollInterval ();
		// scan folder's for send queue, get a manager, and add them to our list
		sendQ = new ArrayList <PhineasQ> ();
		int n = config.getMapCount();
		while (n-- > 0)
		{
			FolderConfig cfg = config.getMap(n);
			String qname = cfg.getQueue ();
			PhineasQ sq = PhineasQManager.getInstance().getQueue(qname);
			if (sq == null)
			{
				Log.error("Couldn't get queue manager for " + qname);
				continue;
			}					
			if (!sendQ.contains(sq))
				sendQ.add(sq);			
		}
  	// now make an entry for each route map found
		routes = new HashMap <String, RouteInfo> ();
		n = config.getRouteCount();
  	while (n-- > 0)
  	{
  		RouteInfo r = new RouteInfo ();
  		if (!r.configure (config.getRoute (n)))
  			continue;
  		routes.put (r.getName(), r);
			Log.info("Added folder " + r.getName());
  	}
  	int m = config.getMaxThreads ();
		queue = new PriorityBlockingQ (m);
		queueThreads = new QueueThread[m];
		for (int i = 0; i < m; i++)
		{
			queueThreads[i] = new QueueThread(queue, routes);
			queueThreads[i].setName("QueueThread");
		}
	}
	
	/**
	 * Put anything that is in attempted status back to queued for next restart
	 * @return true if successful
	 */
	private boolean resetQueues ()
	{
		for (int i = 0; i < sendQ.size(); i++)
		{
			ArrayList <PhineasQRow> rows = sendQ.get(i).findProcessingStatus ("attempted");
			if (rows == null)
				continue;
			for (int j = 0; j < rows.size(); j++)
			{
				PhineasQRow row = rows.get(j);
				row.setProcessingStatus("queued");
				if (row.update() < 0)
					Log.error ("failed to reset row " + row.getValue(0) + " for senders");
			}
		}				
		return true;
	}
	
	/*
	 * Starts the queue processors (senders), and poll the queues.
	 * 
	 * (non-Javadoc)
	 * @see java.lang.Thread#run()
	 */
	public void run()
	{
		Log.info("Queue Poller starting " + queueThreads.length + " senders at " 
				+ pollInterval + " second intervals");
		for (int i = 0; i < queueThreads.length; i++)
			queueThreads[i].start ();
		try
		{
		  poll();
		}
		catch (InterruptedException e)
		{
			Log.info("Queue poller interrupted");
		}
	  // clean up and die
		queue.clear ();
		Log.info ("Waiting for queue senders to exit...");
		for (int i = 0; i < queueThreads.length; i++)
			queueThreads[i].quit();
		resetQueues ();
		Log.info("Queue Poller exiting");
	}
	
	/**
	 * Check queues for any entries that are "queued" and place them on
	 * the priority blocking queue for a sender (processor) to pick up.
	 * This starts with a sleep interval so that the rest of the startup
	 * has time to complete.
	 * 
	 */
	private void poll () throws InterruptedException
	{
		while (psleep(pollInterval))
		{
			for (int i = 0; i < sendQ.size(); i++)
			{
				ArrayList <PhineasQRow> rows = sendQ.get(i).findProcessingStatus ("queued");
				if (rows == null)
					continue;
				for (int j = 0; j < rows.size(); j++)
				{
					// note the priority queue will ignore duplicates
					// and all updates are made by the processors
					PhineasQRow row = rows.get(j);
					int p = row.getInt("PRIORITY");
					if (queue.put(row, p))
						Log.debug("added row " + row.getValue(0) + " for senders");
				}
			}				
		}
	}
}
