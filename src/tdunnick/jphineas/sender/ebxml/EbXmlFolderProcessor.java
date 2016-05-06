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

package tdunnick.jphineas.sender.ebxml;

import java.io.*;
import java.lang.reflect.*;

import tdunnick.jphineas.config.FolderConfig;
import tdunnick.jphineas.filter.*;
import tdunnick.jphineas.logging.*;
import tdunnick.jphineas.queue.*;
import tdunnick.jphineas.sender.*;
import tdunnick.jphineas.util.*;
import tdunnick.jphineas.xml.*;

/**
 * Visit various folders in the sender's map configuration and queue up 
 * any candidates for transmission.  Perform any needed encryption when the
 * payload get's queued.
 * 
 * @author tld
 *
 */
public class EbXmlFolderProcessor extends FolderProcessor
{	
	FolderConfig config = null;
	
	PhineasQ queue = null;		// this folder's queue
	Constructor <?> filter = null; // a filter
	
	protected boolean configure (FolderConfig config)
	{
		this.config = config;
		
		if ((queue = PhineasQManager.getInstance().getQueue(config.getQueue())) == null)
		  return false;	
		filter = config.getFilter();
		return true;
	}
	

	/**
	 * Queue one file. The gets a unique process ID to uniquely identify the file
	 * and the message.  The file is first moved to the processed folder.  Since the
	 * move is atomic this takes care of any file system race conditions.  If a filter
	 * has been specified (for example an HL7 transform) it is loaded and the file is 
	 * then read/filtered to the Sender's temp folder for transmission.  Lastly the queue
	 * gets updated.
	 * 
	 * @param src file to queue
	 * @return true if successful
	 */
	protected boolean process (File src)
	{
		Log.debug("Processing " + src.getPath() + " for " + config.getName());
		// get a unique name
		String fname = src.getName() + "." + ProcessID.get();
		// and move it to processed
		File dst = new File (config.getProcessed().getPath() + "/" + fname);
		// if the move fails, another process still has it open, probably the writer!
		// this is done FIRST to prevent race conditions
		if (!src.renameTo(dst))
		{
			Log.warn("Can't move " + src.getPath() + " to " + dst.getPath());
			return false;
		}
		
		// filter and encrypt
		try
		{
			InputStream in = new FileInputStream (dst);
			/* TODO add any user filter to this input
			if (filter != null)
			{
				try
				{
					PhineasInputFilter c_in = (PhineasInputFilter) filter.newInstance(in);
					c_in.configure (config.copy ("Filter"));
					in = c_in;
				}
				catch (Exception e)
				{
					Log.error("Couldn't load filter for " + config.getName(), e);
					return false;
				}
			}
			*/
			/*
			// could add an encryption filter to this input instead of using RouteProcessor
			// note our encryption if any
			String encType = config.getValue ("Encryption.Type").toLowerCase();
			if (encType.equals("none"))
				encType = null;
		
			if (encType != null)
			{
				EncryptorInputFilter c_in = new EncryptorInputFilter (in);
				c_in.configure (config.copy ("Encryption"));
				in = c_in;
			}
			*/

			// write the filtered data to the queue directory
			dst = new File (config.getQueueDirectory () + fname);
			OutputStream out = new FileOutputStream (dst);
			int c;
			while ((c = in.read()) >= 0)
				out.write(c);
			in.close();
			out.close();
		}
		catch (IOException e)
		{
			Log.error("Can't queue " + src.getPath() + " to " + dst.getPath(), e);
			return false;
		}
		
		// add the entry
		PhineasQRow r = queue.newRow();
		// with payload set
		r.setPayLoadFile (fname);
		r.setDestinationFileName (src.getName());
		// TODO figure out best place to put ACK path in queue
		r.setResponseFileName(config.getAcknowledged().getPath() + "/" + fname);
		return EbXmlQueue.add(config, r);
	}
}
