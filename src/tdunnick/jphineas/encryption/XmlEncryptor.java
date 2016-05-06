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

package tdunnick.jphineas.encryption;

import java.io.*;
import java.security.*;

import tdunnick.jphineas.logging.*;
import tdunnick.jphineas.xml.*;

/**
 * A PHINMS payload container consists of an XML wrapper holding information about
 * how the encryption was done and a payload.  This assumes triple DES encryption
 * of the payload using an RSA encrypted key designated by a distinguished name
 * from the wrapper.
 * 
 * @author tld
 *
 */
public class XmlEncryptor
{	
	/**
	 * Encrypt and return an XML payload as a string.  Note the XML is 
	 * initially obtained from Template.  For PEM encoded certificates, the 
	 * password is not needed (normally a public cert) and the DN is normally
	 * obtained from the certificate.
	 *   
	 * @param path to the RSA encryption keystore or certficate
	 * @param passwd for the keystore or LDAP BaseDN
	 * @param dn distinguished name for the key to use or LDAP CN
	 * @param payload data to encrypt
	 * @return XML wrapped payload
	 */
	public String encryptPayload (String path, String passwd, StringBuffer dn, byte[] payload)
	{
		if ((path == null) || (payload == null))
			return null;
		Log.debug ("encryption path=" + path);
		if (dn == null)
			dn = new StringBuffer ();
		EncryptionXml xml = new EncryptionXml ();
		Encryptor crypt = new Encryptor ();
		Key key = crypt.generateDESKey();
		xml.setData (crypt.encrypt(payload, key));
		Key pkey;
		if (!new File (path).exists()) // assume this is an LDAP
		{
			String cn = dn.toString();
			dn.setLength(0);
			pkey = crypt.getLdapKey(path, passwd, cn, dn);
		}
		else if ((passwd == null) || (passwd.length() == 0)) // a binary cert
		{
			dn.setLength(0);
			pkey = crypt.getDerKey(path, dn);
		}
		else // a java keystore
		{
		  pkey = crypt.getKeyStoreKey(path, passwd, dn);
		}
		if ((dn == null) || (dn.length() == 0))
		{
			Log.error("Certificate DN not found");
			return null;
		}
		xml.setKeyName (dn.toString());
		xml.setKeyValue(crypt.encrypt(key.getEncoded(), pkey));
		ByteArrayOutputStream out = new ByteArrayOutputStream ();
		try
		{
		  xml.save(out);
		}
		catch (Exception e)
		{
			Log.error("Can't store payload: " + e.getMessage());
			return null;
		}
		return out.toString();
	}

	/**
	 * Decrypt a payload wrapped in XML.  If the payload is not XML encrypted
	 * we don't do anything and just return the payload.
	 * 
	 * @param path to decryption keystore
	 * @param storepass to keystore
	 * @param keypass and entry
	 * @param payload string, optionally encrypted in XML
	 * @return binary payload data or null if it fails
	 */
	public byte[] decryptPayload (String path, String storepass, 
			String keypass, String payload)
	{
		EncryptionXml xml = new EncryptionXml (payload);
		if (!xml.ok ())
		{
			Log.debug ("payload is not encrypted");
			return payload.getBytes();
		}
		String dn = xml.getKeyName ();
		if (dn == null)
		{
			Log.error("Payload key name not found");
			return null;
		}
		String ckey = xml.getKeyValue();
		if (ckey == null)
		{
			Log.error("Payload encryption key not found");
			return null;
		}
		String data = xml.getData ();
		if (data == null)
		{
			Log.error("Payload data not found");
			return null;
		}
		return decryptData (path, storepass, keypass, new StringBuffer(dn), ckey, data);
	}
	
	/**
	 * Decrypt data using a DES key encrypted by an RSA key from a keystore.
	 * 
	 * @param path to the keystore
	 * @param storepass for the keystore 
	 * @param keypass for the private key
	 * @param dn distinguished name of the keystore entry
	 * @param ckey encrypted DES key
	 * @param data to decrypt
	 * @return the decrypted data
	 */
	private byte[] decryptData (String path, String storepass, 
			String keypass, StringBuffer dn, String ckey, String data)
	{
		Encryptor crypt = new Encryptor ();
	  Log.debug("getting RSA key");
		Key key = crypt.getPrivateKey(path, storepass, keypass, dn);
		Log.debug("getting DES key");
    key = crypt.decryptKey(ckey, key, Encryptor.DES_TRANSFORM);
    Log.debug("decrypting payload");
    return crypt.decrypt(data, key);
	}
}
