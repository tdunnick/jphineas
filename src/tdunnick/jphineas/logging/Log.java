/*
 *  Copyright (c) 2015 Thomas Dunnick (https://mywebspace.wisc.edu/tdunnick/web)
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

import tdunnick.jphineas.xml.*;


/**
 * A logging configuration for jPhineas. Yeah, log4j is really nice, 
 * but this gives us our own embedded control and automatic config selection 
 * by thread name.
 * 
 * @author Thomas L Dunnick
 *
 */
public class Log
{
	/** logger configurations */
	private static HashMap <String,LogConfig> loggers = new HashMap<String, LogConfig>();
	/** last configuration used */
	private static LogConfig config = new LogConfig();
	
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
	public static LogConfig getLogConfig() 
	{
		String s = getThreadName(false);
		// System.err.println (s + " log configuration");
		if (loggers.containsKey(s))
			return loggers.get(s);
		// new thread defaults to last configuration set or added
		setLogConfig (config);
		return config;
	}
  
  /**
   * sets a logging configuration for this thread
   * 
   * @param cfg to add
   */
  public static void setLogConfig (LogConfig cfg)
  {
  	// if (cfg.rollLog())
  	  loggers.put(getThreadName(false), config = cfg);
  }
  
  /**
   * Add a logging configuration using xml configuration
   * 
   * @param props xml
   */
  public static void xmlLogConfig (XmlConfig props)
  {
		LogConfig cfg = new LogConfig ();
		cfg.setLogId(Thread.currentThread().getName());
 		cfg.setLogName(props.getValue("LogName"));
	  cfg.setLogLevel(props.getValue("LogLevel"));
	  cfg.setLogDays(props.getValue("LogDays"));
	  cfg.setLogLocal(props.getValue("LogLocal"));
	  setLogConfig (cfg);
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
			if (n.equals (ignore))
				continue;
		  n += "." + stack[i].getMethodName();
			return n + "() " + stack[i].getLineNumber();
		}
  	return "";
  }
  
  /**
   * get the thread name for logging purposes
   * @return the thread name
   */
  private static String getThreadName (boolean logging)
  {
  	Thread t = Thread.currentThread();
  	String s = t.getName();
  	if (logging)
  		s += "[" + t.getId() + "]";
  	return s;
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
  	LogConfig cfg = getLogConfig ();
  	if ((cfg.getLogLevel() < level) ||  
  			(cfg.getLogLevel() == LogConfig.OFF) ||
  			(cfg.getLogStream() == null))
  		return;
  	SimpleDateFormat fmt = new SimpleDateFormat ("yyyy-MM-dd HH:mm:ss ");
  	StringBuffer buf = new StringBuffer (fmt.format (new Date()));
  	buf.append(getThreadName(true));
  	buf.append(" ");
  	switch (level)
		{
			case LogConfig.DEBUG:
				buf.append("DEBUG: ");
				break;
			case LogConfig.ERROR:
				buf.append("ERROR: ");
				break;
			case LogConfig.INFO:
				buf.append("INFO: ");
				break;
			case LogConfig.WARN:
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
   * get a list of loggers
   * @return the loggers
   */
  public static HashMap <String, LogConfig> getLoggers ()
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
  	log (LogConfig.INFO, m);
  }
  
  /**
   * Warning logging, identified by 'WARN'
   * 
   * @param m message to log.
   */
  public static void warn (String m)
  {
  	log (LogConfig.WARN, m);
  }
  
 /**
  * Error logging, identified by 'ERROR'
  * 
  * @param m message to log.
  */
  public static void error (String m)
  {
  	log (LogConfig.ERROR, m);
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
  	log (LogConfig.DEBUG, m);
  }
}
