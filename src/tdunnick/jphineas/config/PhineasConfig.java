package tdunnick.jphineas.config;

import java.io.File;
import java.net.InetAddress;
import org.w3c.dom.*;

import tdunnick.jphineas.logging.Log;

public class PhineasConfig extends XmlConfig
{
  boolean init ()
	{
		// Log.debug ("initialzing PhineasConfig");
 	  if (!super.init ())
   		return false;
 		if (findValue ("Domain") == null)	try
		{
			setValue (root, "Domain", InetAddress.getLocalHost().getCanonicalHostName());
		}
		catch (Exception e)
		{
			Log.error ("Can't set host domain " + e.getMessage());
			return false;
		}
		return true;
	}
	
  /**
   * Gets the "party ID" of our server
   * @return the host's party ID
   */
  public String getHostId ()
  {
  	return findValue ("HostId");
  }
  
  /**
   * Gets the domain name for our server
   * @return the domain name
   */
  public String getDomain ()
  {
  	return findValue ("Domain");
  }
  
  /**
   * Gets the organization's name for this server
   * @return the organization's name
   */
  public String getOrganization ()
  {
  	return findValue ("Organization");
  }
  
  /**
   * Gets the configuration used by the console.  This is the only other separate
   * configuration file and is referenced by the primary configuration
   * @return the configuration
   */
  public XmlConfig getConsole ()
  {
  	XmlConfig c = new XmlConfig ();
  	if (!c.load (getFile (rootTag + ".Console")))
  		return null;
  	return c;
  }
  
  /**
   * Gets the sender's part of the configuration
   * @return the sender's configuration
   */
  public SenderConfig getSender ()
  {
  	return (SenderConfig) copy (new SenderConfig(), rootTag + ".Sender");
  }
 
  /**
   * Gets the receiver's part of the configuration
   * @return the receiver's configuration
   */
  public ReceiverConfig getReceiver ()
  {
  	return (ReceiverConfig) copy (new ReceiverConfig(), rootTag + ".Receiver");
  }
  
	/**
	 * Gets the nearest trust store.  Any sub-class can have it's own separate value,
	 * or default to this "master" value.
	 * @return trust store
	 */
	public File getTrustStore ()
	{
		return getFile ("TrustStore");
	}
	
	/**
	 * Gets the nearest trust store password. It must be in the same configuration
	 * tags (sibling) as the trust store.
	 * @return the trust store password
	 */
	public String getTrustStorePassword ()
	{
		return getValue (find ("TrustStore"), "TrustStorePassword");
	}

  /**
   * Gets the nearest log configuration.  Each sub class can have it's own independent
   * log settings, or default to here.
   * @return the log configuration
   */
  public LogConfig getLog ()
  {
  	return (LogConfig) copy (new LogConfig (), find ("Log"));
  }
}
