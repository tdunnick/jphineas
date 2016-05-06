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
	 * @param cache directory where replies are cached
	 */
	public ReplyCache (File cache)
	{
		if (cache == null)
			cache = new File (System.getProperty("java.io.tmpdir") + "/jphineas/");
		if (!cache.exists())
			cache.mkdirs ();
		File[] list = cache.listFiles();
		if (list == null)
		{
			Log.error(cache.getPath() + "is not a valid response cache directory");
			return;
		}
		for (int i = 0; i < list.length; i++)
			list[i].delete ();
		Log.info("Initialized reply cache at " + cache.getAbsolutePath());
		cacheDir = cache;
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
		if (cacheDir == null)
			return;
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
			Log.error("Failed writing " + fn.getAbsolutePath() 
					+ " to reply cache - " + e.getMessage());
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
	 * The reply cache key is made up from the request partyID and 
	 * the message header data messageId which is unique for each message (chunk)
	 * @param soap request
	 * @return
	 */
	private String getKey (SoapXml soap)
	{
		String key = soap.getFromPartyId() + soap.getHdrMessageId();
		return key;
	}
}
