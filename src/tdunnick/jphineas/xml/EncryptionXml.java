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

package tdunnick.jphineas.xml;

/**
 * This manages the encryption envelope XML.  It is identified by the Mime
 * part where the Content-ID matches the ebXML Manifest Reference href attribute.
 * (whew!).  However, it is probably sufficient to look for 
 * "Content-Disposition: attachement".
 * 
 * @author Thomas Dunnick
 *
 */
public class EncryptionXml extends XmlContent
{
	public static final String template =
		"<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
		"<EncryptedData Id=\"ed1\" Type=\"http://www.w3.org/2001/04/xmlenc#Element\" xmlns=\"http://www.w3.org/2001/04/xmlenc#\">" +
		"	 <EncryptionMethod Algorithm=\"http://www.w3.org/2001/04/xmlenc#tripledes-cbc\"/>" +
		"	 <KeyInfo xmlns=\"http://www.w3.org/2000/09/xmldsig#\">" +
		"	   <EncryptedKey xmlns=\"http://www.w3.org/2001/04/xmlenc#\">" +
		"	     <EncryptionMethod Algorithm=\"http://www.w3.org/2001/04/xmlenc#rsa-1_5\"/>" +
		"	     <KeyInfo xmlns=\"http://www.w3.org/2000/09/xmldsig#\">" +
		"	       <KeyName>key</KeyName>" +
		"	     </KeyInfo>" +
		"	     <CipherData>" +
		"	       <CipherValue/>" +
		"	     </CipherData>" +
		"	   </EncryptedKey>" +
		"	 </KeyInfo>" +
		"	 <CipherData>" +
		"	   <CipherValue/>" +
		"	 </CipherData>" +
		"</EncryptedData>";

	public static final String keyname = "EncryptedData.KeyInfo.EncryptedKey.KeyInfo.KeyName";
	public static final String keyvalue = "EncryptedData.KeyInfo.EncryptedKey.CipherData.CipherValue";
	public static final String data = "EncryptedData.CipherData.CipherValue";
	
	/**
	 * create encryption from a template
	 */
	public EncryptionXml ()
	{
		load (template);
	}
	
	/**
	 * load encryption from a string
	 * @param xml with the encryption
	 */
	public EncryptionXml (String xml)
	{
		load (xml);
	}
	
	/**
	 * Check that this is encrypted
	 * @return true if encrypted
	 */
	public boolean ok ()
	{
		return getElement ("EncryptedData") != null;
	}
	
	/**
	 * get a value
	 * @param tag for the value
	 * @return the value or empty string if null
	 */
	private String get (String tag)
	{
		String s = getValue (tag);
		if (s == null) s = "";
		return s;
	}
	
	/**
	 * get the key name (typically the certificate DN)
	 * @return the key name
	 */
	public String getKeyName ()
	{
		return get (keyname);
	}
	
	/**
	 * set the key value. Typically the symetric key encrypted by the certificate
	 * public key and base 64 encoded
	 * @param value of the key name
	 * @return true if successful
	 */
	public boolean setKeyName (String value)
	{
		return setValue (keyname, value);
	}
	
	/**
	 * get the key name (typically the certificate DN)
	 * @return the key name
	 */
	public String getKeyValue ()
	{
		return get (keyvalue);
	}
	
	/**
	 * set thge key name (certificate DN)
	 * @param value of the key name
	 * @return true if successful
	 */
	public boolean setKeyValue (String value)
	{
		return setValue (keyvalue, value);
	}
	
	/**
	 * get the key name (certificate DN)
	 * @return the key name
	 */
	public String getData ()
	{
		return get (data);
	}
	
	/**
	 * set thge key name (certificate DN)
	 * @param value of the key name
	 * @return true if successful
	 */
	public boolean setData (String value)
	{
		return setValue (data, value);
	}
}
