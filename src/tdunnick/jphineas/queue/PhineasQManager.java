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

package tdunnick.jphineas.queue;

import java.util.*;
import java.io.*;

import tdunnick.jphineas.config.LogConfig;
import tdunnick.jphineas.config.XmlConfig;
import tdunnick.jphineas.logging.*;
import tdunnick.jphineas.xml.*;
/**
 * Doles out PhineasQ's which may be shared by multiple threads.  Queues are identified
 * in a single configuration file, so that all processes share the same set of 
 * queues (identifiers).  The queue manager is a singleton.
 * 
 * @author Thomas Dunnick tdunnick@wisc.edu
 *
 */
public class PhineasQManager
{
	/** the managers */
  private static PhineasQManager manager = new PhineasQManager();
  /** our configuration file */
  private String configName = null;
  /** our synchronization lock */
  private Object lock = new Object ();
  /** the currently active/open queues */
  private HashMap <String,PhineasQ> queues = new HashMap <String,PhineasQ>();
  /** connections */
  private HashMap <String,PhineasQConnection> connections = new HashMap <String,PhineasQConnection>();
  /** types */
  private HashMap <String,PhineasQType> types = new HashMap <String,PhineasQType> ();
  /** our logging context */
  String logId = null;
  
  /**
   * Singleton constructor.
   */
  private PhineasQManager ()
  {
  }
  
  /**
   * Singleton instance request
   * @return the singleton
   */
  public static PhineasQManager getInstance ()
  {
  	return manager;
  }
  
  /**
   * reload the configuration and restart all the connections
   * @return true if successful
   */
  public boolean restart ()
  {
  	if (configName == null)
  		return false;
  	close ();
  	return configure (configName);
  }
  
  /**
   * Configure the manager.
   * @param name of configuration file
   * @return true if successful
   */
  boolean configure (String name)
  {
		// if no name given we are done
  	if (name == null)
  	{
  		Log.error("Missing configuration");
  		return (false);
  	}
  	synchronized (lock)
  	{
	  	XmlConfig config = new XmlConfig (); 	
	  	if (!config.load (new File (name)))
	  	{
	  		Log.error("Couldn't load queue configuration " + name);
	  		return false;
	  	}
	  	config = config.copy ("Queues");
			logId = Log.configure ((LogConfig) config.copy (new LogConfig (), "Log"));
	  	// load our type configurations
	  	int n = config.getTagCount("TypeInfo.Type");
	  	if (n == 0)
	  	{
	     	Log.error("Configuration " + name + " missing Type(s)");
	    	return false;  		
	  	}
	  	for (int i = 0; i < n; i++)
	  	{
	  		XmlConfig c = config.copy("TypeInfo.Type[" + i + "]");
	  		String s = c.getValue ("Name");
	  		if (types.containsKey (s))
	  			continue;
	  		types.put (s, new PhineasQType (c));
	  		// Log.debug ("added type " + s + " with " + c.getTagCount("Field") + " fields");
	  	}
	  	// load connections
	  	n = config.getTagCount("ConnectionInfo.Connection");
	  	if (n == 0)
	  	{
	  		Log.error("Configuration " + name + " missing Connection(s)");
	  		return false;
	  	}
	  	for (int i = 0; i < n; i++)
			{
				XmlConfig c = config.copy("ConnectionInfo.Connection[" + i + "]");
				String s = c.getValue("Name");
				if (connections.containsKey (s))
					continue;
	
				// load an instance of this queue
				try
				{
					Class <?> qclass = Class.forName(c.getValue("Class"));
					PhineasQConnection conn = (PhineasQConnection) qclass.newInstance();
					conn.configure(c);
					connections.put(s, conn);
				}
				catch (ClassNotFoundException e)
				{
					Log.error("Class '" + c.getValue("Class") + "' not found");
					continue;
				}
				catch (Exception e)
				{
					Log.error("Class '" + c.getValue("Class") + "' not accessible", e);
					continue;					
				}
			}
	  	// load queues
	  	n = config.getTagCount("QueueInfo.Queue");
	  	if (n == 0)
	  	{
	  		Log.warn("Configuration " + name + " missing Queue(s)");
	  		return false;
	  	}
	  	for (int i = 0; i < n; i++)
			{
				addQueue (config.copy("QueueInfo.Queue[" + i + "]"));
			}
			Log.debug("Queue configuation: " 
					+ config.getTagCount("TypeInfo.Type") + " types, "
					+ config.getTagCount("ConnectionInfo.Connection") + " connections, "
					+ config.getTagCount("QueueInfo.Queue") + " queues");
			configName = name;
	  	return true;
  	}
  }

  /**
   * Get a list of all the (valid) queues
   * @param type of queue to list, or null for all
   * @return a list of queues
   */
  public ArrayList <String> getQueueNames (String type)
  {
  	synchronized (lock)
  	{
	  	ArrayList <String> qnames = new ArrayList <String> ();
	  	Iterator <String> it = queues.keySet().iterator();
	  	while (it.hasNext ())
	  	{
	  		String name = it.next();
	  		if (type != null)
	  		{
		  		String t = queues.get(name).type.getType();
		  		if (!t.contains (type))
		  			continue;
	  		}
	  		qnames.add (name);
	  	}
	  	return qnames;
  	}
  }
  
  /**
   * Get a queue.
   * 
   * @param name of queue to get
   * @return the queue
   */
  public PhineasQ getQueue (String name)
  {
  	synchronized (lock)
		{
			return queues.get(name);
		}
  }

  /**
   * shut down all queues
   */
  void close ()
  {
  	synchronized (lock)
  	{
  		Log.debug ("Close request...");
	  	Iterator <PhineasQConnection> it = connections.values().iterator();
	  	while (it.hasNext())
	  	{
	  		PhineasQConnection c = it.next();
  			c.close();
	  	}
	  	connections.clear();
	  	queues.clear();
	  	types.clear ();
  	}
	}  
  
  protected void finalize() throws Throwable
	{
  	close ();
	}
  
  /**
   * Add this queue to the configuration
   * @param qconfig
   * @return
   */
  private boolean addQueue (XmlConfig qconfig)
  {
  	// if already named, skip it
		String name = qconfig.getValue("Name");
		if (queues.containsKey(name))
			return false;
		// check the type
		PhineasQType qtype = types.get(qconfig.getValue ("Type"));
		if (qtype == null)
		{
			Log.error("Can't find queue type " + qconfig.getValue ("Type"));
			return false;
		}
		// check the connection
		PhineasQConnection conn = connections.get(qconfig.getValue("Connection"));
		if (conn == null)
		{
			Log.error("Can't find connection " + qconfig.getValue("Connection"));
			return false;
		}
		// configure the queue
		PhineasQ q = new PhineasQ ();
		if (!q.configure (name, qconfig.getValue("Table"), conn, qtype))
		{
			Log.error("Failed configuring queue " + name);
			return false;
		}
		// add it to our list
		queues.put(name, q);
		Log.info("Queue " + name + " successfully configured");
		return true;
  }
}
