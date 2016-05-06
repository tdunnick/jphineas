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

import java.io.IOException;
import java.io.OutputStream;

import org.jfree.util.Log;

import tdunnick.jphineas.encryption.XmlEncryptor;
import tdunnick.jphineas.xml.XmlContent;

/**
 * Perform encryption as a OutputStream filter.  Pass just the <Encryption>
 * part of the sender's configuration with the template added
 * for the corresponding map.
 * 
 * @author tld
 *
 */
public class EncryptorOutputFilter extends PhineasOutputFilter
{

	public EncryptorOutputFilter(OutputStream arg0)
	{
		super(arg0);
	}

	protected boolean process() throws IOException
	{
		// get need encryption parameters
		if (config == null)
			throw new IOException ("Encryption not configured");
		String keystore = config.getValue ("Unc");
		String storePass = config.getValue ("Password");
		String id = config.getValue ("Id");
		StringBuffer dn = new StringBuffer ();
		if (id != null)
			dn.append(id);
		byte[] b = buf.toByteArray();
		if (b.length == 0)
			return false;
		Log.debug("encrypting: " + new String (b));
		// do the encryption
		XmlEncryptor crypt = new XmlEncryptor ();
		String payload = crypt.encryptPayload(keystore, storePass, dn, b);
		if (payload == null)
			throw new IOException ("Encryption failed");
		buf.reset();
		buf.write (payload.getBytes());
		return true;
	}

}
