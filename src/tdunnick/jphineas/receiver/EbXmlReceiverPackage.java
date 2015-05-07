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

package tdunnick.jphineas.receiver;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Constructor;

import tdunnick.jphineas.logging.Log;
import tdunnick.jphineas.queue.*;
import tdunnick.jphineas.mime.*;
import tdunnick.jphineas.xml.*;
import tdunnick.jphineas.encryption.*;
import tdunnick.jphineas.filter.*;

/**
 * Package up ebXML responses to senders
 * 
 * @author Thomas Dunnick
 *
 */
public class EbXmlReceiverPackage
{
	// Service configuation
	XmlConfig config = null;
	// configuration pieces needed...
  /** a filter */
	Constructor <?> filter = null; 
 
	/**
	 * Configure an ebXML mime packager 
	 * 
	 * @param config - the Sender configuration for this Route.
	 */
	public EbXmlReceiverPackage (XmlConfig config)
	{
		this.config = config;
		String s = config.getValue("Filter.Class");
		if (s != null) try
		{
			Class<?> cf = Class.forName(s);
			if (!PhineasOutputFilter.class.isAssignableFrom(cf))
				Log.error(s + " is not a PhineasInputFilter");
			else
			  filter = cf.getConstructor(InputStream.class);
		}
		catch (Exception e)
		{
			Log.error("Couldn't load filter " + s, e);
		}		
	}


	/**
	 * Create and return the Mime header container for this response
	 * @param soap to package
	 * @return Mime header
	 */
	public MimeContent getHeaderContainer (XmlContent soap)
	{
		// build a soap part
		MimeContent part = new MimeContent ();
		part.setHeader (MimeContent.CONTENTID, 
				"ebxml-envelope@" + config.getValue("Organization"));
		part.setContentType(MimeContent.XML);
		if (!part.setBody (soap.toString()))
		{
			Log.error("Failed creating soap response");
			return null;
		}
		return part;
	}
	
	/**
	 * Create and return an ebXML application response part.
	 * Note that the PHINMS naming conventions differ between the response XML,
	 * the sender's queue, and the receiver's queue (sheesh), but we'll stay with
	 * that for compatibility.
	 * 
	 * @param status of the application
	 * @param error of the application
	 * @param appdata of the application
	 * @return a Mime response
	 */
	public MimeContent getAppResponseContainer (String status, String error, 
			String appdata, PhineasQRow row)
	{
		ResponseXml resp = new ResponseXml ();
		row.setErrorCode (status);
		resp.setStatus(status);
		row.setErrorMessage (error);
		resp.setError (error);
		row.setApplicationStatus(appdata);
		resp.setAppData (appdata);
		MimeContent part = new MimeContent ();
		part.setHeader (MimeContent.CONTENTID, "<statusResponse@cdc.gov>");
		part.setContentType(MimeContent.XML);
		if (!part.setBody (resp.toString()))
		{
			Log.error("Failed creating response xml");
			return null;
		}
		return part;		
	}
	
	public MimeContent getPayloadContainer ()
	{
		return null;
	}
	
  /**
   * Generate a default response package for this soap header
   * @param soap header container
   * @return (partial) response
   */
  public MimeContent getMessagePackage (SoapXml soap)
  {
		// now finish off the reply
		MimeContent mime = new MimeContent ();
		mime.setHeader("Host", config.getValue("Domain"));
		mime.setHeader("Connection", "Keep-Alive");
		mime.setMultiPart("type=\"text/xml\"; start=\"ebxml-envelope@" 
				+ config.getValue("Organization") + "\"");
		mime.setHeader("SOAPAction", "\"ebXML\"");
		// add the header
		mime.addMultiPart(getHeaderContainer(soap));
		return mime;
  }
  
  public MimeContent parsePayloadContainer (MimeContent part, PhineasQRow row)
  {
  	// check if this is a payload container
  	String s = part.getHeader ("Content-Disposition");
  	if ((s == null) || !s.startsWith("attachment"))
  		return null;
  	// assume all goes well
  	MimeContent appResponse = getAppResponseContainer ("success", "none", "none", row);
  	// get the payload directory and file name
  	String dir = config.getDirectory("PayloadDirectory");
  	String filename = s.replaceFirst ("^.*name=\"([^\"]*).*$", "$1");
  	Log.debug("payload file name=" + filename);
  	if (filename.equals(s))
  	{
  		// make up a name...
    	appResponse = getAppResponseContainer ("abnormal", "missing file name", "warning",row);
    	try
    	{
	    	File f = File.createTempFile(dir + "Unknown", "");
	    	filename = f.getName();
    	}
    	catch (IOException e)
    	{
    	  Log.error (s = "Could not create payload file");
    	  return getAppResponseContainer ("abnormal", "can't create file", "failure", row);
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
    	return getAppResponseContainer ("abnormal", s, "failure", row);
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
					appResponse = getAppResponseContainer ("abnormal", s, "warning", row);
				}
			}
			// write the filtered data to the queue directory
			out.write(payload);
			out.close();
		}
		catch (IOException e)
		{
			Log.error(s = "Can't save payload to " + dir + filename, e);
	  	appResponse = getAppResponseContainer ("abnormal", s, "failure", row);
		}
  	return appResponse;
  }
}
