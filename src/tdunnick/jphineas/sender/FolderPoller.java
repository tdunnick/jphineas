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

import java.io.*;
import java.util.*;

import tdunnick.jphineas.xml.*;
import tdunnick.jphineas.config.SenderConfig;
import tdunnick.jphineas.logging.*;
import tdunnick.jphineas.util.*;

/**
 * Visit various folders in the sender's folder configurations and process any
 * candidate files found there. The configurations identify the processing class
 * and any parameters it may need to perform the work.  Simply pass the
 * file name to the process.
 * 
 * The folder poller is a single thread and doesn't attempt to process more
 * than one folder/file at a time.
 * 
 * @author Thomas L Dunnick
 *
 */
public class FolderPoller extends Pthread
{
  /** milliseconds to wait between folder sweeps */
  int pollInterval;
  /** prefix for folder maps */
  private static final String mapPrefix = "MapInfo.Map";
  /** folders to poll */
  ArrayList <FolderInfo> folders = null;
  

  /**
   * Initialize a folder poller from a sender configuration.  The poller 
   * has a set of maps that associate a given folder with a specific processor.
   * Each time a polling interval expires, it queues any files found in
   * the map's folder for it's current processor.
   * 
   * @param config containing the maps
   */
  public FolderPoller (SenderConfig config)
  {
  	super ("FolderPoller");
  	// how often we check...
  	pollInterval = config.getInt("PollInterval");
  	folders = new ArrayList <FolderInfo> ();
  	// now make an entry for each map found
  	int n = config.getMapCount ();
  	while (n-- > 0)
  	{
  		FolderInfo f = new FolderInfo ();
  		if (!f.configure (config.getMap (n)))
  			continue;
  		folders.add(f);
			Log.info("Added folder " + f.getName());
  	}
  }
  
  /*
   * run this thread - after sanity checks simply start polling
   * (non-Javadoc)
   * @see java.lang.Thread#run()
   */
	public void run()
	{
		if (pollInterval < 1)
		{
			Log.warn ("Setting default Folder Poll Interval to 30 second");
			pollInterval = 30;
		}
	  Log.info("Folder Poller starting at " + pollInterval + " second intervals");
		poll ();
		Log.info("Folder Poller exiting");
	}
	
	/**
	 * Poll a specific folder map.  Note the processor is responsible for 
	 * (re) moving and files found in the folder of this map.
	 * 
	 * @param xml configuration for this folder map
	 * @return number of files queued
	 */
	private int pollFolder (FolderInfo folder)
	{
		// Log.debug("Polling " + folder.getFolder().getAbsolutePath());
		File[] files = folder.getFolder().listFiles();
		if (files == null)
		  return (0);
		int n = 0;
		for (int i = 0; i < files.length; i++)
		{
			File f = files[i];
			if (!f.isFile())
				continue;			
			// submit this file for processing
			Log.debug("submitting " + f.getPath());
			FolderProcessor p = folder.getProcessor();
			if (p == null)
				Log.error("Can't get folder processor for " + folder.getName());
			else if (p.process (f))
				n++;
		}
		return n;
	}
	
	/**
	 * Loop through all maps, queuing files as needed.  Start with a sleep
	 * to allow everything to complete configuration
	 */
	private void poll ()
	{
		while (running() && psleep (pollInterval))
		{
			// Log.debug("Polling folder maps");
			for (int i = 0; i < folders.size(); i++)
				pollFolder (folders.get(i));
		}
	}
}
