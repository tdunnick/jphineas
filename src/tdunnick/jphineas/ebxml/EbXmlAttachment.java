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

package tdunnick.jphineas.ebxml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import tdunnick.jphineas.encryption.XmlEncryptor;
import tdunnick.jphineas.filter.PhineasOutputFilter;
import tdunnick.jphineas.logging.Log;
import tdunnick.jphineas.mime.MimeContent;
import tdunnick.jphineas.queue.PhineasQRow;
import tdunnick.jphineas.xml.EncryptionXml;

/**
 * manage payload attachments
 * 
 * @author Thomas Dunnick tdunnick@wisc.edu
 *
 */
public class EbXmlAttachment
{
	byte[] payload = null;
	String id = null;
	String name = null;
	
	/**
	 * default constructor for a new empty attachment
	 */
	public EbXmlAttachment ()
	{
		super ();
	}
		
	/**
	 * constructor for a mime attachment
	 * @param part of mime having the attachment
	 */
	public EbXmlAttachment (MimeContent part)
	{
		set (part);
	}
	
	/**
	 * Populates an empty attachment.  Note that encryption is done in a 
	 * separate method.
	 * 
	 * @param payload to attach
	 * @param id identifier which matches the "cid:" in the SOAP manifest
	 * @param name of this attachment, typically the file name
	 * @return true if successful
	 */
	public boolean set (byte[] payload, String id, String name)
	{
		if ((payload == null) || (id == null) || (name == null))
		  return false;
		this.payload = payload;
		this.id = id;
		this.name = name;
		return true;
	}
	
	/**
	 * Populate this attachment from Mime content
	 * @param part of Mime with the attachment
	 * @return true if successful
	 */
	public boolean set (MimeContent part)
	{
  	// check if this is a payload container
  	String pid = part.getContentId();
  	if (pid == null)
  		return false;
  	String pname = part.getDisposition();
  	if ((pname == null) || !pname.startsWith("attachment"))
  		return false;
  	int start = pname.indexOf('"');
  	int end = pname.lastIndexOf('"');
  	pname = pname.substring(start + 1, end);
  	byte[] pbody = part.decodeBody();
  	return set (pbody, pid, pname);
	}
	
	/**
	 * Generate a Mime attachment from what we have here
	 * @return the attachment or null if nothing is set
	 */
	public MimeContent get ()
	{
		if (payload == null)
			return null;
		MimeContent mime = new MimeContent ();
		mime.setContentId (id);
		mime.setDisposition ("attachement; name=\"" + name + "\"");
		// encodeBody() will set content type and encoding
		mime.encodeBody(payload);
		return mime;
	}
	
	public String toString ()
	{
		return get().toString ();
	}
	
	/**
	 * Write this payload to the file system
	 * @param f file to write
	 * @return true if successful
	 */
	public boolean savePayload (File f)
	{
		try
		{
			if (f.isDirectory())
				f = new File (f.getAbsolutePath() + "/" + name);
			FileOutputStream o = new FileOutputStream (f);
			o.write (payload);
			o.close ();
			return true;
		}
		catch (IOException ex)
		{
			Log.error ("Failed writing " + f.getPath() + ": " + ex.getMessage());
			return false;
		}
	}
	
	/**
	 * Populate the payload
	 * @param payload to use
	 */
	public void setPayload (byte[] payload)
	{
		this.payload = payload;
	}
	
  /**
   * Get the current playload
   * @return the payload
   */
  public byte[] getPayload ()
  {
  	return payload;
  }
  
  /**
   * Populate the Content-ID of the payload
   * @param id to use
   */
  public void setId (String id)
  {
  	this.id = id;
  }
  
  /**
   * Get the Content-ID of the payload
   * @return the id
   */
  public String getId ()
  {
  	return id;
  }
  
  /**
   * Populate the (file) name of the payload
   * @param name to use
   */
  public void setName (String name)
  {
  	this.name = name;
  }
  
  /**
   * Get the (file) name of the payload
   * @return the name
   */
  public String getName ()
  {
  	return name;
  }

  /**
   * Encrypt the current payload to an XML encryption format.
   * @param path to the encryption object (certificate, keystore, LDAP, etc)
   * @param base keystore/cert password or LDAP baseDN
   * @param dn for lookup in keystore or LDAP
   * @return true if successful
   */
  public boolean encrypt (String path, String base, String dn)
  {
  	if (path == null)
  		return true;
		StringBuffer dnbuf = null;
		if (dn != null)
			dnbuf = new StringBuffer (dn);
		XmlEncryptor crypt = new XmlEncryptor ();
		String enc = crypt.encryptPayload(path, base, dnbuf, payload);
		if (enc == null)
		{
			Log.error ("Failed to encrypt payload");
			return false;
		}
		payload = enc.getBytes();
  	return true;
  }
  
  /**
   * Utility to determine if this payload has been encrypted
   * @return true if encrypted
   */
  public boolean isEncrypted ()
  {
  	return new String (payload).contains("<EncryptedData");
  }
  
  /**
   * Decrypts this payload in place.  Assumes XML encryption.
   * 
   * @param cert file holding the decryption certificate
   * @param password for the certificate
   * @return true if successful
   */
  public boolean decrypt (File cert, String password)
  {	
		XmlEncryptor crypt = new XmlEncryptor ();
		byte[] p = crypt.decryptPayload (cert.getAbsolutePath(), password, password, new String (payload));
    if (p == null)
    	return false;
    payload = p;
  	return true;
  }
}
