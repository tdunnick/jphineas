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

package tdunnick.jphineas.logging;

import java.text.SimpleDateFormat;
import java.util.*;
import java.io.*;

import tdunnick.jphineas.config.LogConfig;
import tdunnick.jphineas.xml.*;


/**
 * A logging configuration for jPhineas. Yeah, log4j is really nice, 
 * but this gives us our own embedded control.  The desired behavior is
 * to provide specific configurations by ID from either servlets or optionally
 * worker threads.  Otherwise threads inherit configurations from their parents.
 * 
 * Log configurations are mapped by their ID.  As a hack, they are also mapped
 * by thread (name).  When a log is configured, the configuring thread is noted. When a 
 * logging request is made, the appropriate configuration is selected by thread.
 * If this is the first time for that thread, it defaults to the last configuration
 * made.  Note that multiple thread may answer to the same name!
 * 
 * @author Thomas L Dunnick
 *
 */
public class Log
{
	/** logger configurations - mapped by Log ID */
	private static HashMap <String,LogContext> loggers = new HashMap<String, LogContext>();
	/** logger configurations - mapped by Thread Name */
	private static HashMap <String,LogContext> threadmap = new HashMap <String, LogContext>();
	/** last configuration used */
	private static LogContext config = new LogContext();
	
  /**
   * This is a singleton shared by all classes
   */
  private Log ()
  {
  }
    
 	/**
	 * gets the log configuration for the current thread. 
	 * If none exists use the last one set.
	 * 
	 * @return the configuration
	 */
	public static LogContext getLogConfig() 
	{
		String s = Thread.currentThread().getName();
		// System.err.println (s + " log configuration");
		if (threadmap.containsKey(s))
			return threadmap.get(s);
		// new thread defaults to last configuration set or added
		setLogConfig (Thread.currentThread(), config.getLogId());
		return config;
	}
  
	/**
	 * force the configuration for a thread - user current configuration if
	 * no ID is given.
	 * 
	 * @param id of configuration to force
	 * @return true if successful
	 */
	public static boolean setLogConfig (Thread t, String id)
	{
		if (id != null)
		{
		  LogContext cfg = loggers.get (id);
			if (cfg == null)
			{
				// if not our default, this is a boo boo
				if (!id.equals (config.getLogId()))				
				  return false;
				// otherwise add it to the map
				loggers.put(id, config);
			}
			else
				config = cfg;
		}
		threadmap.put(t.getName(), config);
		return true;
	}
  
  /**
   * Add a logging configuration using xml configuration.  Note this will
   * replace an existing configuration with the same ID.
   * 
   * @param props xml
   * @return the log ID
   */
  public static String configure (LogConfig props)
  {
  	if (props == null)
  		return getLogConfig().getLogId();
		LogContext cfg = null;
		String id = props.getLogId();
		if (id == null)
			id = Thread.currentThread().getName();
		if ((cfg = loggers.get(id)) == null)
		{
	  	// System.out.println("*** Creating context for " + id);
	  	cfg = new LogContext ();
			cfg.setLogId(id);
	 	  loggers.put(id, cfg);
		}
		// System.out.println ("*** Updating context for " + id);
 		cfg.setLogName(props.getLogName());
	  cfg.setLogLevel(props.getLogLevel());
	  cfg.setLogDays(props.getLogDays());
	  cfg.setLogLocal(props.getLogLocal());
 	  threadmap.put(Thread.currentThread().getName(), config = cfg);
	  return id;
  }

  /**
   * use the thread's stack trace to determine logging location.
   * 
   * @return class and line where logging was performed.
   */
  private static String getCallerLocation ()
  {
  	
		StackTraceElement[] stack = Thread.currentThread().getStackTrace();
		// skip our Log class and...
		String ignore = Log.class.getName();
		// our own and the Thread call in the stack, so start at 2
		for (int i = 2; i < stack.length; i++)
		{
			String n = stack[i].getClassName();
			if (n.startsWith (ignore))
				continue;
		  n += "." + stack[i].getMethodName();
			return n + "() " + stack[i].getLineNumber();
		}
  	return "";
  }
  
  /**
   * The general logging function.  Only log for requested levels and include
   * locations if requested.
   * 
   * @param level of logging requested
   * @param m message to log
   */
  private static void log (int level, String m)
  {
  	LogContext cfg = getLogConfig ();
  	if ((cfg.getLogLevel() < level) ||  
  			(cfg.getLogLevel() == LogContext.OFF) ||
  			(cfg.getLogStream() == null))
  		return;
  	SimpleDateFormat fmt = new SimpleDateFormat ("yyyy-MM-dd HH:mm:ss ");
  	StringBuffer buf = new StringBuffer (fmt.format (new Date()));
  	String n = Thread.currentThread().getName();
  	if ((n == null) || (n.length() == 0))
  		n = cfg.getLogId();
  	buf.append(n + "[" + Thread.currentThread().getId() + "] ");
  	switch (level)
		{
			case LogContext.DEBUG:
				buf.append("DEBUG: ");
				break;
			case LogContext.ERROR:
				buf.append("ERROR: ");
				break;
			case LogContext.INFO:
				buf.append("INFO: ");
				break;
			case LogContext.WARN:
				buf.append("WARN: ");
				break;
		}
  	buf.append(m);
  	if (cfg.isLogLocal())
  		buf.append(" - " + getCallerLocation ());
  	buf.append("\n");
  	cfg.log (buf.toString());
  }

  /**
   * close current logs and reset logging
   */
  public static void close ()
  {
  	Iterator <String> it = loggers.keySet().iterator();
  	config = null;
  	while (it.hasNext ())
  	{
  		String k = it.next();
  		LogContext cfg = loggers.get(k);
  		cfg.close();
  	}
  	loggers.clear();
  	threadmap.clear();
  	config = new LogContext();
  }
  
  /**
   * get a list of loggers
   * @return the loggers
   */
  public static HashMap <String, LogContext> getLoggers ()
  {
  	return loggers;
  }
  
  /**
   * General information logging, identified by 'INFO'
   * 
   * @param m message to log.
   */
  public static void info (String m)
  {
  	log (LogContext.INFO, m);
  }
  
  /**
   * Warning logging, identified by 'WARN'
   * 
   * @param m message to log.
   */
  public static void warn (String m)
  {
  	log (LogContext.WARN, m);
  }
  
 /**
  * Error logging, identified by 'ERROR'
  * 
  * @param m message to log.
  */
  public static void error (String m)
  {
  	log (LogContext.ERROR, m);
  }
  
  /**
   * Exception logging, identified by 'ERROR'.  Provide the localized message,
   * or a stack trace if this is a generic exception (like NULL object).
   * 
   * @param m message to log.
   * @param e an exception
   */
  public static void error (String m, Exception e)
  {
  	String msg = "";
    if (e == null)
    {
    	msg = "null Exception????";
    }
    else
    {
    	msg = e.getMessage();
      if ((msg == null) || (msg.length() == 0))
      {
      	ByteArrayOutputStream b = new ByteArrayOutputStream ();
      	e.printStackTrace(new PrintStream (b));
      	msg = b.toString();
      }
    }
   	error (m + ", " + msg);
  }
  
  /**
   * Debugging, identified by 'DEBUG'
   * 
   * @param m message to log.
   */
  public static void debug (String m)
  {
  	log (LogContext.DEBUG, m);
  }
}
