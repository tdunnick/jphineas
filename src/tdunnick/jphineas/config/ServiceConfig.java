package tdunnick.jphineas.config;

import java.io.File;

import tdunnick.jphineas.logging.Log;
import tdunnick.jphineas.receiver.*;

public class ServiceConfig extends ReceiverConfig
{
	boolean init ()
	{
		if (!super.init ())
			return false;
		String[] tags = 
		{ 
			"Name", "Service", "Processor", "Action", "Queue"
		};
		for (int i = 0; i < tags.length; i++)
		{
			if (getValue (tags[i]) == null)
			{
				Log.error ("Service Map missing value for " + tags[i]);
				return false;
			}
		}
		return true;
	}
	
	public String getName ()
	{
		return getValue ("Name");
	}
	
	public ReceiverProcessor getProcessor ()
	{
	  return (ReceiverProcessor) getInstance (".ProcessorInfo.Processor", getValue ("Processor"));
	}
	
	public String getService ()
	{
		return getValue ("Service");
	}
	
	public String getAction ()
	{
		return getValue ("Action");
	}

	public String getQueue()
	{
		return getValue ("Queue");
	}
	
	public String getDecryptionType ()
	{
		return getValue ("Decryption.Type");
	}

	public String getDecryptionId ()
	{
		return getValue ("Decryption.Id");
	}

	public String getDecryptionPassword ()
	{
		return getValue ("Decryption.Password");
	}

	public File getDecryptionUnc ()
	{
		return getFile ("Decryption.Unc");
	}

	public RouteConfig getRoute ()
	{
		// TODO get the route for this service
		return null;
	}
}
