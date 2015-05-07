package tdunnick.jphineas.receiver;

import java.io.*;
import tdunnick.jphineas.logging.*;
import tdunnick.jphineas.mime.*;
import tdunnick.jphineas.util.Cache;
import tdunnick.jphineas.xml.SoapXml;

/**
 * Cached replies from the receiver.  This prevents duplicates from getting
 * processed and improves response time for lost replies.  The reply itself
 * is kept on disk, so that huge replies (RNR) replies can be held as long as needed.
 * 
 * @author Thomas Dunnick tdunnick@wisc.edu
 *
 */
public class ReplyCache extends Cache
{
	/** the directory holding replies */
	File cacheDir = null;
	/** lifetime in cache - default 12 hours */
	int lifetime = (60 * 60 * 12);
	
	/**
	 * initializes a reply cache, removing and left overs and create cache
	 * directory as needed
	 * 
	 * @param cacheDir directory where replies are cached
	 */
	public ReplyCache (File cacheDir)
	{
		if (cacheDir == null)
			cacheDir = new File (System.getProperty("java.io.tmpdir") + "/jphineas/");
		this.cacheDir = cacheDir;
		if (!cacheDir.isAbsolute())
			cacheDir.mkdirs ();
		File[] list = cacheDir.listFiles();
		if (list != null)
		{
			for (int i = 0; i < list.length; i++)
				list[i].delete ();
		}
		Log.info("Initialized reply cache at " + cacheDir.getAbsolutePath());
	}
	
	/**
	 * Set lifetime for the cache and return the old value
	 * @param seconds to live in cache - default is 12 hours
	 * @return the old lifetime
	 */
	public int setLifetime (int seconds)
	{
		int old = lifetime;
		lifetime = seconds;
		return old;
	}
	
	/**
	 * Check the cache for a reply to this request
	 * @param soap request
	 * @return reply or null if not found
	 */
	public MimeContent get(SoapXml soap)
	{
		File fn = (File) super.get(getKey (soap));
		if (fn == null)
			return null;
		try
		{
			byte[] buf = new byte[(int) fn.length()];
			FileInputStream i = new FileInputStream (fn);
			i.read (buf);
			i.close ();
			return new MimeContent (new String (buf));
		}
		catch (IOException e)
		{
			Log.error("Failed reading " + fn.getAbsolutePath() + " from reply cache");
			super.remove(getKey (soap));
			return null;
		}
	}
	
	/**
	 * Add a reply to this request
	 * @param soap request
	 * @param data reply
	 */
	public void put (SoapXml soap, MimeContent data)
	{
		String key = getKey (soap);
		// if already cached we are done
		if (super.get(key) != null)
		  return;
		File fn = new File (cacheDir.getAbsoluteFile() + "/" + key.replaceAll("\\W+", "_"));
		try
		{
			FileOutputStream o = new FileOutputStream (fn);
			o.write(data.toString().getBytes());
			o.close ();
		  super.put(key, fn, lifetime);
		}
		catch (IOException e)
		{
			Log.error("Failed writing " + fn.getAbsolutePath() + " to reply cache");
			return;
		}
	}
	
	/**
	 * called by the cache cleaner to remove an entry - here we delete
	 * the cached file.
	 * @param key to the cache entry
	 * @see tdunnick.jphineas.util.Cache#remove(java.lang.String)
	 */
	protected void remove(String key)
	{
		File fn = (File) super.get (key);
		fn.delete();
		super.remove (key);
	}

	/**
	 * The reply cache key is made up from the request partyID and messageId
	 * TODO addition ID's for chunks, etc.
	 * @param soap request
	 * @return
	 */
	private String getKey (SoapXml soap)
	{
		String key = soap.getFromPartyId() + soap.getDbMessageId();
		return key;
	}
}
