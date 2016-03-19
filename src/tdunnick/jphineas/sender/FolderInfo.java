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
