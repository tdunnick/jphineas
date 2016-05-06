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

import tdunnick.jphineas.encryption.XmlEncryptor;
import tdunnick.jphineas.xml.XmlContent;

/**
 * Perform decryption as an OutputStream filter.  The xml configuration
 * should contain Encryption.Unc (path to keystore/cert) and
 * Encryption.Password (password for keystore AND private key)
 * 
 * @author tld
 *
 */
public class DecryptorOutputFilter extends PhineasOutputFilter
{
	public DecryptorOutputFilter(OutputStream out)
	{
		super(out);
	}

	protected boolean process() throws IOException
	{
		String payload = buf.toString();
		if (payload.length() == 0)
			return false;
		// get need encryption parameters
		if (config == null)
			throw new IOException ("Decryption not configured");
		String keystore = config.getValue ("Encryption.Unc");
		String storePass = config.getValue ("Encryption.Password");
		String keyPass = config.getValue("Encryption.Password");
		// do the decryption
		XmlEncryptor crypt = new XmlEncryptor ();
		byte[] b = crypt.decryptPayload(keystore, storePass, keyPass, payload);
		if (b == null)
			throw new IOException ("Decryption failed");
		buf.reset();
		buf.write (b);
		return true;
	}
}
