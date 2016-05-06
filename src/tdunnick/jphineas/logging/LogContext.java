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

import java.util.*;
import java.text.SimpleDateFormat;
import java.io.*;

/**
 * A logging configuration for Phineas.
 * Yeah, log4j is really nice, but this gives us our own embedded control
 * @author tld
 *
 */
public class LogContext
{
	public static final int OFF = 0;
	public static final int ERROR = OFF + 1;
	public static final int WARN = ERROR + 1;
	public static final int INFO = WARN + 1;
	public static final int DEBUG = INFO + 1;
	
	/** id assigned to this log */
	private String logId = Thread.currentThread().getName();
	/** log output stream */
  private OutputStream logStream = System.out;
  /** log output level */
  private int logLevel = INFO;
  /** log location in source */
  private boolean logLocal = false;
  /** log file name */
  private String logName = null;
  /** number of days to keep logs */
  private int logDays = 0;
  /** our rolling log timer */
  private LogRoller logRoller = null;
  /** a cache of log messages */
  private ArrayList <String> msgs = new ArrayList <String> ();
  /** size of our cache */
  private int msgsize = 256;
  
  /**
   * Generate the name of a rolled log base on last date modified.
   * @param f file to roll
   * @return rolled File
   */
  private File getRollFile (File f)
  {
  	SimpleDateFormat fmt = new SimpleDateFormat ("yyyyMMdd");
  	String s = fmt.format (new Date(f.lastModified()));
  	return new File (f.getPath() + "." + s);
  }
  
  /**
   * place a message in the log
   * @param msg
   */
  public void log (String msg)
  {
  	msgs.add(msg);
  	while (msgs.size() > msgsize)
  		msgs.remove(0);
  	try
  	{
    	logStream.write(msg.getBytes());  		
  	}
  	catch (IOException e)
  	{
  		System.err.print(msg);
  	}
  }
  
  /**
   * get our message cache in reverse order
   * @return the log cache
   */
  public ArrayList <String> getLog ()
  {
  	ArrayList <String> m = new ArrayList <String> (msgs);
  	Collections.reverse(m);
  	return m;  	
  }
 
  /**
   * roll this log and open a new log stream
   * @return true if successful
   */
  public boolean rollLog ()
  {
  	// don't roll standard streams
  	if ((logStream == System.out) || (logStream == System.err))
  		return true;
  	if (logStream != null)
  		close ();
  	if (logName == null)
  		return false;
  	boolean append = false;
  	File f = new File (logName);
  	if (f.exists())
  	{
  		if (logDays < 0)
  		{
  			f.delete();
  		}
  		else if (logDays > 0)
  		{
  			File d = new File (f.getParent());
  			File[] versions = d.listFiles(new LogFilter (f.getName(), logDays));
  			for (int i = 0; i < versions.length; i++)
  				versions[i].delete();
  			File r = getRollFile (f);
  			if (r.exists())
  				append = true;
  			else
  			  f.renameTo(r);
  		}
  		else
  			append = true;
  	}
  	try
  	{
  	  logStream = new FileOutputStream (f, append);
  	}
  	catch (IOException e)
  	{
  		System.err.println ("Can't open " + f.getPath());
  		logStream = null;
  		return false;
  	}
  	return true;
  }
  
  /**
   * flush and close this log
   * @return true if successful
   */
  public boolean close ()
  {
  	if ((logStream == System.out) || (logStream == System.err) || (logStream == null))
  	  return true;
		if (logRoller != null)
			logRoller.interrupt();
		logRoller = null;
  	try
  	{
	  	logStream.flush();
	  	logStream.close();
	  	logStream = null;
  	}
  	catch (IOException e)
  	{
  		return false;
  	}
  	return true;
  }
  
	/* (non-Javadoc)
	 * @see java.lang.Object#finalize()
	 */
	protected void finalize() throws Throwable
	{
		close ();
	}

	/**
	 * get the identifier for this log
	 * @return the logId
	 */
	public String getLogId()
	{
		return logId;
	}

	/**
	 * set the identifier for this log
	 * @param logId the logId to set
	 */
	public void setLogId(String logId)
	{
		this.logId = logId;
	}

	/**
	 * get the output stream for this log
	 * @return the out
	 */
	public OutputStream getLogStream()
	{
		return logStream;
	}
	/**
	 * set the output stream for this log
	 * @param out the out to set
	 */
	public void setLogStream (OutputStream out)
	{
		this.logStream = out;
	}
	/**
	 * get the current logging level
	 * @return the logLevel
	 */
	public int getLogLevel()
	{
		return logLevel;
	}
	/**
	 * set the logging level - values are off, error, warn, info, or debug
	 * @param level the logLevel to set
	 */
	public void setLogLevel(String level)
	{
		int l;
		if (level == null)
			return;
		level = level.toLowerCase();
		if (level.equals("off")) l = OFF;
		else if (level.equals("error")) l = ERROR;
		else if (level.equals("warn")) l = WARN;
		else if (level.equals("info")) l = INFO;
		else if (level.equals("debug")) l = DEBUG;
		else return;		
		setLogLevel (l);
	}
	
	/**
	 * set the logging level
	 * @param logLevel the logLevel to set
	 */
	public void setLogLevel(int logLevel)
	{
		this.logLevel = logLevel;
	}
	/**
	 * get the current log name
	 * @return the logName
	 */
	public String getLogName()
	{
		return logName;
	}
	/**
	 * set the current log name 
	 * @param logName the logName to set
	 */
	public void setLogName(String logName)
	{
		this.logName = logName;
	}
	/**
	 * get the number of days logs are archived
	 * @return the logRolling
	 */
	public int getLogDays()
	{
		return logDays;
	}
	/**
	 * set the number of days logs are archived
	 * 0 = append current log
	 * -1 = restart current log daily
	 * @param days the number of logs to keep
	 */
	public void setLogDays(int days)
	{
		this.logDays = days;
		if (logDays == 0)
		{
			if (logRoller != null)
				logRoller.interrupt();
			logRoller = null;
		}
		else
		{
			logRoller = new LogRoller (this);
			logRoller.start();
		}
	}
	
	/**
	 * set the number of days logs are archived
	 * 0 = append current log
	 * -1 = restart current log daily
	 * @param days the number of logs to keep
	 */
	public void setLogDays(String days)
	{
		try
		{
			int d = Integer.parseInt(days);
			setLogDays (d);
		}
		catch (Exception e)
		{
			// forget about it...
		}
	}

	/**
	 * Are we logging source code location?
	 * @return true if logging location
	 */
	public boolean isLogLocal()
	{
		return logLocal;
	}

	/**
	 * For source code location logging
	 * @param logLocal true if source code location wanted
	 */
	public void setLogLocal(boolean logLocal)
	{
		this.logLocal = logLocal;
	}
	
	/**
	 * For source code location logging
	 * @param local "true" if source code location wanted
	 */
	public void setLogLocal (String local)
	{
		if (local == null) return;
		if (local.equalsIgnoreCase("true"))
			setLogLocal (true);
		else
			setLogLocal (false);
	}
	
}

/**
 * The filter used to select files to be deleted when logs are rolled.
 * @author tld
 *
 */
class LogFilter implements FileFilter
{
	/** milliseconds per day */
	private static final long msPerDay = 24 * 60 * 60 * 1000;

	private String prefix;
	private long age;
	LogFilter (String name, long days)
	{
		prefix = name;
		this.age = new Date().getTime() - (days * msPerDay);
	}
	public boolean accept(File f)
	{
		return f.getName().startsWith(prefix) && f.lastModified() > age;
	}	
}


/**
 * A thread to roll logs once per day.
 * 
 * @author Thomas L Dunnick
 *
 */
class LogRoller extends Thread
{
	/** milliseconds per day */
	private static final long msPerDay = 24 * 60 * 60 * 1000;
	private LogContext config;

	protected LogRoller (LogContext c)
	{
		config = c;
	}
	public synchronized void start()
	{
		try
		{
			for (;;)
			{
				// wait until roll over time... now midnight
				// TODO configure roll over time in LogConfig
				Calendar cal = Calendar.getInstance();
				long now = cal.getTimeInMillis() + cal.get (Calendar.ZONE_OFFSET);
				wait (msPerDay - now % msPerDay);
				config.rollLog();
			}
		}
		catch (Exception e)
		{
			// when interrupted, we quit
		}
	}	
}
