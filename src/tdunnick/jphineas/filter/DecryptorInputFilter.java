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
import tdunnick.jphineas.util.*;
import tdunnick.jphineas.xml.*;

/**
 * Perform decryption as a InputStream filter.  The xml configuration
 * should contain Encryption.Unc (path to keystore/cert) and
 * Encryption.Password (password for keystore AND private key)
 * 
 * @author tld
 *
 */
public class DecryptorInputFilter extends PhineasInputFilter
{

	public DecryptorInputFilter(InputStream in)
	{
		super(in);
	}
	
	public boolean configure(XmlConfig config)
	{
		// get need encryption parameters
		if (config == null)
			return false;
		String keystore = config.getValue ("Unc");
		String storePass = config.getValue ("Password");
		String keyPass = config.getValue("Password");
		if (fillBuf() < 0)
			return false;
		String payload = new String (buf);
		// do the encryption
		XmlEncryptor crypt = new XmlEncryptor ();
		buf = crypt.decryptPayload(keystore, storePass, keyPass, payload);
		return buf != null;
	}	
}
