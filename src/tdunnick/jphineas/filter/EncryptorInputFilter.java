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

package tdunnick.jphineas.filter;

import java.io.*;

import tdunnick.jphineas.config.XmlConfig;
import tdunnick.jphineas.encryption.XmlEncryptor;
import tdunnick.jphineas.xml.*;

/**
 * Perform encryption as a InputStream filter.  Pass just the <Encryption>
 * part of the sender's configuration with the template added
 * for the corresponding map.
 * 
 * @author tld
 *
 */
public class EncryptorInputFilter extends PhineasInputFilter
{

	public EncryptorInputFilter(InputStream in)
	{
		super(in);
	}
	
	public boolean configure(XmlConfig config)
	{
		// get need encryption parameters
		if (config == null)
			return false;
		String keystore = config.getString ("Unc");
		String storePass = config.getString ("Password");
		StringBuffer dn = new StringBuffer (config.getString ("Id"));
		if (fillBuf() < 0)
			return false;
		// do the encryption
		XmlEncryptor crypt = new XmlEncryptor ();
		String payload = crypt.encryptPayload(keystore, storePass, dn, buf);
		if (payload == null)
			return false;
		buf = payload.getBytes();
		return true;
	}	
}
