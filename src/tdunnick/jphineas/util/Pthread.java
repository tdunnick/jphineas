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

package tdunnick.jphineas.util;

import tdunnick.jphineas.logging.Log;

/**
 * Some extensions to our threads for managing graceful exits
 * 
 * @author Thomas L Dunnick
 *
 */
public class Pthread extends Thread
{
	/**
	 * all Pthreads get names and default log configuration inherited from parent
	 * @param name
	 */
	public Pthread (String name)
	{
		super();
		this.setName(name);
		Log.setLogConfig (this, null);
	}
	
  /**
   * @return true if thread is stopped
   */
  public boolean stopped ()
  {
  	return !running ();
  }
  
  /**
   * @return true if thread is running
   */
  public boolean running ()
  {
  	return this.isAlive();
  }

  /**
   * Sleep for a number of second or until interrupted.
   * 
   * @param seconds to sleep
   * @return false if interrupted
   */
  protected boolean psleep (int seconds)
  {
  	if (Thread.currentThread() != this)
  		return false;
  	try
  	{
   		Thread.sleep (seconds * 1000);
  	}
  	catch (InterruptedException e)
  	{
  		Log.debug("Sleep interrupted!");
  		return false;
  	}
  	return true;
  }
  
	/**
	 * quit this thread nicely, waiting for it to die
	 */
	public void quit ()
	{
		try
		{
			while (this.getState() == Thread.State.RUNNABLE)
				sleep (100);
			switch (this.getState()) 
			{
				case TERMINATED :
				case NEW : return;
				default :
					break;
			}
			this.interrupt();
		  this.join ();
		}
		catch (Exception e)
		{
			Log.error ("Failed to join " + this.getClass().getName());
		}
	}
}
