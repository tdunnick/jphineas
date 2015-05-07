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
