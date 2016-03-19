package tdunnick.jphineas.config;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Constructor;

import tdunnick.jphineas.filter.PhineasInputFilter;
import tdunnick.jphineas.logging.Log;
import tdunnick.jphineas.sender.FolderProcessor;

public class FolderConfig extends SenderConfig
{

	boolean init ()
	{
		if (!super.init ())
			return false;
		String[] tags = 
		{ 
			"Name", "Source", "Processor", "Processed", "Acknowledged",
			"Route", "Service", "Action", "Queue"
		};
		for (int i = 0; i < tags.length; i++)
		{
			if (getValue (tags[i]) == null)
			{
				Log.error ("Folder Map missing value for " + tags[i]);
				return false;
			}
		}
		return true;
	}
	
	public String getName ()
	{
		return getValue ("Name");
	}
	
	public File getSource ()
	{
		return getFolder ("Source");
	}
	
	public FolderProcessor getProcessor ()
	{
	  return (FolderProcessor) getInstance (".ProcessorInfo.Processor", getValue ("Processor"));
	}
	
	public Constructor <?> getFilter ()
	{
		Class<?> cf = getClass ("FilterInfo.Filter", getValue ("Filter"));
		if (cf == null)
			return null;
  	try
		{
			if (!PhineasInputFilter.class.isAssignableFrom(cf))
			{
				Log.error(cf.getName() + " is not a PhineasInputFilter");
				return null;
			}
			return cf.getConstructor(InputStream.class);
		}
		catch (Exception e)
		{
			Log.error("Couldn't load filter " + cf.getName(), e);
			return null;
		}		
	}

	public File getProcessed ()
	{
		return getFolder ("Processed");
	}
	
	public File getAcknowledged ()
	{
		return getFolder ("Acknowledged");
	}
	
	public String getRoute ()
	{
		return getValue ("Route");
	}
	
	public String getService ()
	{
		return getValue ("Service");
	}
	
	public String getAction ()
	{
		return getValue ("Action");
	}
	
	public String getArguments ()
	{
		return getValue ("Arguments");
	}
	
	public String getRecipient ()
	{
		return getValue ("Recipient");
	}

	public String getEncryptionType ()
	{
		return getValue ("Encryption.Type");
	}
	
	public String getEncryptionId ()
	{
		return getValue ("Encryption.Id");
	}

	public String getEncryptionPassword ()
	{
		return getValue ("Encryption.Password");
	}

	/**
	 * Note PHINMS has a pretty goofy URL encoding for files that looks like...
	 * file:///C|/path/to/file  where 'C' is the drive letter and the '|' replaces the colon.
	 * A Java File.toURL() would look more like file:/C:/path/to/file.  Here we will punt
	 * for the time being... heck, this isn't even a UNC!
	 * 
	 * @return UNC to file
	 */
	public String getEncryptionUnc ()
	{
		String k = "Encryption.Unc";
		File f = getFile (k);
		if (f != null)
			return f.getAbsolutePath();
		return getValue (k);
	}
	
	public String getEncryptionBaseDn ()
	{
		return getValue ("Encryption.BaseDn");
	}
	
	public String getEncryptionDn ()
	{
		return getValue ("Encryption.Dn");
	}

	public String getQueue ()
	{
		return getValue ("Queue");
	}
}
