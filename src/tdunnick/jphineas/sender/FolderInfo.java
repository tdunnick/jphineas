package tdunnick.jphineas.sender;

import java.io.*;

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

  public boolean configure (XmlConfig config)
	{
  	if (config == null)
  	{
  		Log.error("NULL configuration");
  		return false;
  	}
  	if ((name = config.getValue ("Name")) == null)
  	{
  		Log.error("Folder entry not named");
  		return false;
 		}
  	if ((folder = config.getFolder ("Source")) == null)
   	{
  		Log.error("Can't get Source folder");
  		return false;
  	}
  	String p = config.getValue("Processor");
   	try
		{
			Class<?> cf = Class.forName(p);
			if (!FolderProcessor.class.isAssignableFrom(cf))
				Log.error(p + " is not a FolderProcessor");
			else
			{
				processor = (FolderProcessor) cf.newInstance();
				if (processor.configure (config))
				  return true;
			}
		}
		catch (ClassNotFoundException e)
		{
			Log.error("Filter " + p + " not found");
		}
		catch (InstantiationException e)
		{
			Log.error("Can't create new instance of " + p);
		}
		catch (IllegalAccessException e)
		{
			Log.error("Can't access new instance of " + p);
		}
  	return false;
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
