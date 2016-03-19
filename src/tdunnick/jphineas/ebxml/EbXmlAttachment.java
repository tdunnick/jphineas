/*
 *  Copyright (c) 2015 Thomas Dunnick (https://mywebspace.wisc.edu/tdunnick/web)
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
	
	public EbXmlAttachment ()
	{
		super ();
	}
	
	public EbXmlAttachment (byte[] payload, String id, String name)
	{
		set (payload, id, name);
	}
	
	public EbXmlAttachment (File f, String id)
	{
		set (f, id);
	}
	
	public EbXmlAttachment (MimeContent part)
	{
		set (part);
	}
	
	public boolean set (File f, String id)
	{
		if (!loadPayload (f))
			return false;
		this.id = id;
		return true;
	}
	
	public boolean set (byte[] payload, String id, String name)
	{
		if ((payload == null) || (id == null) || (name == null))
		  return false;
		this.payload = payload;
		this.id = id;
		this.name = name;
		return true;
	}
	
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
	
	public MimeContent get ()
	{
		MimeContent mime = new MimeContent ();
		mime.setContentId (id);
		// mime.setEncoding(MimeContent.BASE64);
		// mime.setContentType(MimeContent.OCTET);
		mime.setDisposition ("attachement; name=\"" + name + "\"");
		mime.encodeBody(payload);
		return mime;
	}
	
	public String toString ()
	{
		return get().toString ();
	}
	
	public boolean loadPayload (File f)
	{
		if (!f.canRead())
		{
			Log.error ("Can't read " + f.getAbsolutePath());
			return false;
		}
		byte[] p = null;
		try
		{
			FileInputStream is = new FileInputStream (f);
			p = new byte[is.available()];
			is.read(p);
			is.close();
		}
		catch (IOException ex)
		{
			Log.error ("Failed reading " + f.getPath() + ": " + ex.getMessage());
			return false;
		}
		payload = p;
		name = f.getName();
		return true;
	}
	
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
	
	public void setPayload (byte[] payload)
	{
		this.payload = payload;
	}
	
  public byte[] getPayload ()
  {
  	return payload;
  }
  
  public void setId (String id)
  {
  	this.id = id;
  }
  
  public String getId ()
  {
  	return id;
  }
  
  public void setName (String name)
  {
  	this.name = name;
  }
  
  public String getName ()
  {
  	return name;
  }

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
  
  public boolean isEncrypted ()
  {
  	return new String (payload).contains("<EncryptedData");
  }
  
  public boolean decrypt (File cert, String password)
  {	
		XmlEncryptor crypt = new XmlEncryptor ();
		byte[] p = crypt.decryptPayload (cert.getAbsolutePath(), password, password, new String (payload));
    if (p == null)
    	return false;
    payload = p;
  	return true;
  }
  
	/**
	public MimeContent getPayload (File f, String organization, 
			String path, String base, String dn)
	{
		if (!f.canRead())
		{
			Log.error ("Can't read " + f.getAbsolutePath());
			return null;
		}
		byte[] payload = null;
		try
		{
			FileInputStream is = new FileInputStream (f);
			payload = new byte[is.available()];
			is.read(payload);
			is.close();
		}
		catch (IOException ex)
		{
			Log.error ("Failed reading " + f.getPath() + ": " + ex.getMessage());
			return null;
		}
		return getPayload (payload, f, organization, path, base, dn);
	}

	public MimeContent getPayload (byte[] payload, File f, String organization, 
			String path, String base, String dn)
	{
		MimeContent part = new MimeContent ();
		part.setHeader (MimeContent.CONTENTID, f.getName() 
				+ "@" + organization);
		part.setHeader(MimeContent.DISPOSITION, "attachement; name=\""
				+ f.getName() + "\"");
		if (path != null)
		{
			StringBuffer dnbuf = null;
			if (dn != null)
				dnbuf = new StringBuffer (dn);
			XmlEncryptor crypt = new XmlEncryptor ();
			String enc = crypt.encryptPayload(path, base, dnbuf, payload);
			if (enc == null)
			{
				Log.error ("Failed to encrypt payload");
				return null;
			}
		  part.setContentType(MimeContent.XML);
		  part.setBody(enc);
		}
		else
		{
		  part.setContentType(MimeContent.OCTET);
		  part.encodeBody(payload);
		}
	  // Log.debug("Payload part: " + part.toString());
		return part;
	}

  public MimeContent parsePayloadContainer (MimeContent part)
  {
  	// check if this is a payload container
  	String s = part.getHeader ("Content-Disposition");
  	if ((s == null) || !s.startsWith("attachment"))
  		return null;
  	// assume all goes well
  	MimeAppResponse rsp = new MimeAppResponse ();
  	rsp.set ("success", "none", "none");
  	// get the payload directory and file name
  	String dir = config.getDirectory("PayloadDirectory");
  	String filename = s.replaceFirst ("^.*name=\"([^\"]*).*$", "$1");
  	Log.debug("payload file name=" + filename);
  	if (filename.equals(s))
  	{
  		// make up a name...
  		rsp.set ("abnormal", "missing file name", "warning");
    	try
    	{
	    	File f = File.createTempFile(dir + "Unknown", "");
	    	filename = f.getName();
    	}
    	catch (IOException e)
    	{
    	  Log.error (s = "Could not create payload file");
    	  return rsp.get ("abnormal", "can't create file", "failure");
    	}
  	}
  	// then get the payload, assume it is not encrypted
  	byte[] payload;
		row.setEncryption("no");
 	// check for xml encryption 	
  	if (part.getContentType().equals(MimeContent.XML))
  	{
  		// TODO check type for alternate decryption methods
   	  XmlEncryptor crypt = new XmlEncryptor ();
  		String password = config.getValue ("Decryption.Password");
  		File cert = config.getFile("Decryption.Unc");
  		if ((cert == null) || !cert.canRead())
  		{
  			Log.error ("Can't read certificate " + config.getValue("Decryption.Unc"));
  			payload = part.decodeBody();
  			// note we are saving an encrypted payload
  	 		row.setEncryption("yes");
 		}
  		else
  		{
  	    payload = crypt.decryptPayload(cert.getAbsolutePath(), password, password, part.getBody());
  		}
  	}
  	else
  	{
  	  payload = part.decodeBody();
  	}
  	row.setPayloadName (filename);
  	row.setLocalFileName (dir + filename);
  	if (payload == null)
  	{
  		Log.error (s = "Could not decode payload for " + filename);
    	return rsp.get ("abnormal", s, "failure");
  	}
  	// save the file using a filter if given
		try
		{
			OutputStream out = new FileOutputStream (dir + filename);
			// add any user filter to this input
			if (filter != null)
			{
				try
				{
					PhineasOutputFilter c_out = (PhineasOutputFilter) filter.newInstance(out);
					c_out.configure (config.copy ("Filter"));
					out = c_out;
				}
				catch (Exception e)
				{
					Log.error(s = "Couldn't load filter for " + config.getValue("Name"), e);
					rsp.set ("abnormal", s, "warning");
				}
			}
			// write the filtered data to the queue directory
			out.write(payload);
			out.close();
		}
		catch (IOException e)
		{
			Log.error(s = "Can't save payload to " + dir + filename, e);
	  	rsp.set ("abnormal", s, "failure");
		}
  	return rsp.get();
  }
  */
}
