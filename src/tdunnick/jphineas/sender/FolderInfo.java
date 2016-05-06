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

import tdunnick.jphineas.config.FolderConfig;
import tdunnick.jphineas.logging.Log;
import tdunnick.jphineas.xml.*;

/**
 * General information expected by the folder poller.  This is typically extended
 * for specific maps
 * 
 * @author Thomas Dunnick
 *
 */
public class FolderInfo
{
  protected String name = null; // name of this map
  protected File folder = null; // folder being consumed
  protected FolderProcessor processor = null; // processor for this map

  public boolean configure (FolderConfig config)
	{
  	if (config == null)
  	{
  		Log.error("NULL configuration");
  		return false;
  	}
  	if ((name = config.getName()) == null)
  	{
  		Log.error("Folder entry not named");
  		return false;
 		}
  	if ((folder = config.getSource ()) == null)
   	{
  		Log.error("Can't get Source folder");
  		return false;
  	}
  	processor = config.getProcessor();
  	if (processor == null)
  	{
  		Log.error ("Failed loading folder processor for " + name);
  		return false;
 		}
  	if (!processor.configure (config))
  	{
  		Log.error ("Failed configuring folder processor for " + name);
  		return false;
  	}
		return true;
	}
  
  protected String getName ()
  {
  	return name;
  }
  
  protected File getFolder ()
  {
  	return folder;
  }
  
  protected FolderProcessor getProcessor ()
  {
  	return processor;
  }
}
