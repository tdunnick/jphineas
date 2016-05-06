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

package tdunnick.jphineas.config;

import java.io.File;

import tdunnick.jphineas.logging.Log;
import tdunnick.jphineas.sender.RouteProcessor;

public class RouteConfig extends SenderConfig
{
	boolean init ()
	{
		// Log.debug ("initialzing RouteConfig");
		if (!super.init ())
			return false;
		String[] tags = 
		{ 
			"Name", "Host", "Path", "Port", "Protocol"
		};
		for (int i = 0; i < tags.length; i++)
		{
			if (getValue (tags[i]) == null)
			{
				Log.error ("Route Map missing value for " + tags[i]);
				return false;
			}
		}
		if (getValue ("Cpa") == null)
		{
			String s = getPartyId();
			if (s != null)
				setValue ("Cpa", s + '.' + getHostId());
		}
		return true;
	}
	
	public String getName ()
	{
		return getValue ("Name");
	}
	
	public RouteProcessor getProcessor ()
	{
		return (RouteProcessor) getInstance (".ProcessorInfo.Processor", getValue ("Processor"));
	}
	
	public String getPartyId ()
	{
		return getValue ("PartyId");
	}
	
	public String getCpa ()
	{
		return getValue ("Cpa");
	}
	
	public String getHost ()
	{
		return getValue ("Host");
	}
	
	public String getPath ()
	{
		return getValue ("Path");
	}
	
	public int getPort ()
	{
		return getInt ("Port");
	}
	
	public String getProtocol ()
	{
		return getValue ("Protocol");
	}
	
	public int getRetry ()
	{
		return findInt ("Retry", 3);
	}
	
	public int getTimeout ()
	{
		return getInt ("Timeout", 30);
	}
	
	public String getAuthenticationType ()
	{
		return getValue ("Authentication.Type");
	}
	
	public String getAuthenticationId()
	{
		return getValue ("Authentication.Id");
	}
	
	public String getAuthenticationPassword()
	{
		return getValue ("Authentication.Password");
	}
	
	public File getAuthenticationUnc ()
	{
		return getFile ("Authentication.Unc");
	}
	
	public String getAuthenticationBaseDn ()
	{
		return getValue ("Authentication.BaseDn");
	}
	
	public String getAuthenticationDn ()
	{
		return getValue ("Authentication.Dn");
	}
	
	public File getTrustStore ()
	{
		return getFile ("TrustStore");
	}
	
	public String getTrustStorePassword ()
	{
		return findValue ("TrustStorePassword");
	}
  
	public int getChunkSize ()
	{
		return getInt ("ChunkSize");
	}
}
