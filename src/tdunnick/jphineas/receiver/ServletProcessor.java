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

package tdunnick.jphineas.receiver;

import java.util.*;
import java.io.*;
import java.net.Socket;

import tdunnick.jphineas.util.SocketFactory;
import tdunnick.jphineas.config.ServiceConfig;
import tdunnick.jphineas.encryption.XmlEncryptor;
import tdunnick.jphineas.logging.*;
import tdunnick.jphineas.mime.*;
import tdunnick.jphineas.xml.*;
import tdunnick.jphineas.ebxml.*;

/**
 * The servlet process passes payload processing on to an HTTP servlet, and returns
 * it's response to the sender.
 * 
 * @author Thomas Dunnick tdunnick@wisc.edu
 *
 */
public class ServletProcessor extends ReceiverProcessor
{
  ServiceConfig config = null;
  
	/**
	 * Simply save the configuration
	 * @param config to save
	 * @see tdunnick.jphineas.receiver.ReceiverProcessor#configure(tdunnick.jphineas.config.ServiceConfig)
	 */
	protected boolean configure(ServiceConfig config)
	{
		this.config = config;
		String s = config.getQueue();
		if (s == null)
		{
			Log.error ("Receiver Queue not specified");
			return false;
		}
		return true;
	}

	/**
	 * Decode the payload if possible, and send it along with the meta data as an HTTP
	 * request to a message processor.  Decode it's response and return that to the 
	 * sender as ebXML
	 * 
	 * A Payload response decodes and stores any payload, and includes a part 
	 * for the application response for each payload found.
	 * 
	 * @param request request
	 * @param parts of the request (ignored)
	 * @return mime package for the response
	 * @see tdunnick.jphineas.receiver.ReceiverProcessor#process(tdunnick.jphineas.xml.SoapXml, tdunnick.jphineas.mime.MimeContent[])
	 */
	protected MimeContent process (SoapXml request, MimeContent[] parts)
	{
		// send the request
		MimeContent[] app_parts = appRequest (makeRequest (request, parts));
		// build a response part
		EbXmlResponse pkg = new EbXmlResponse (config);
		MimeContent response = pkg.getMessagePackage(request, "Acknowledgment");
		// parse the response parameters in the first part to the soap envelope
		HashMap <String, String> arg = app_parts[0].getParams();
		EbXmlAppResponse rsp = new EbXmlAppResponse ();
		rsp.set(arg.get ("status"), arg.get("error"), arg.get("appdata"));
		response.addMultiPart (rsp.get());
		// add the app payloads to this response
		for (int i = 1; i < parts.length; i++)
			response.addMultiPart(app_parts[i]);
		return response;
	}
	
	/**
	 * Sends the mime request to a servlet and return the response.
	 * @param mime request to send
	 * @return the response mime parts
	 */
	MimeContent[] appRequest (MimeContent mime)
	{
  	String req = config.getValue("Authentication.Type");
  	// insert BASIC authentication
  	if ((req != null) && (req.equalsIgnoreCase("basic")))
  	{
  		mime.setBasicAuth(config.getValue("Authentication.Id"), 
  				config.getValue("Authentication.Password"));
  	}
  	// set up a connection and response handler
   	Socket socket = SocketFactory.createSocket (config.getRoute());
   	if (socket == null)
   		return null;
   	// build a request string...
   	req = "POST " 
   		+ config.getValue("Protocol") + "://"
   		+ config.getValue("Host") + ":"
   		+ config.getValue("Port")
   		+ config.getValue("Path") 
   		+ " HTTP/1.1\r\n";
   	// now ready to send it off
  	try
  	{
			Log.debug("sending EbXml request:\n" + req + mime.toString());
			OutputStream out = socket.getOutputStream();
			InputStream in = socket.getInputStream();
  		out.write(req.getBytes());
			out.write(mime.toString().getBytes());
			out.flush();
			Log.debug("waiting for reply");
			MimeContent msg = MimeReceiver.receive (in);
			// TODO digest authentication response?
			out.close();
			in.close();
			socket.close();
			// finally parse the reply and update our row
		  Log.debug ("response:\n" + msg.toString());
		  return msg.getMultiParts();
  	}
  	catch (Exception e)
  	{
  		Log.error("Failed sending EbXML message", e);
  	}
		return null;
	}
	
	/**
	 * Create a servlet request from the senders request
	 * @param parts of the sender's mime request
	 * @return the mime request package for the servlet
	 */
	MimeContent makeRequest (SoapXml request, MimeContent[] parts)
	{
  	// create our outgoing message
  	MimeContent mime = new MimeContent ();
		mime.setHeader("Host", config.getValue("Domain"));
		mime.setMultiPart("type=\"text/xml\"; start=\"parameters\"");
		mime.setHeader ("user_key", ""); // certificate SN
		
		// create the main post part
		MimeContent p = new MimeContent ();
		p.setHeader (MimeContent.CONTENTID, "<parameters>");
		p.setContentType(MimeContent.TEXT);
		p.setBody("from=" + request.getFromPartyId()
				+ "&manifest=" + request.getMetaData()
				+ "&service=" + request.getService()
				+ "&action=" + request.getAction());
		mime.addMultiPart(p);
		for (int i = 1; i < parts.length; i++)
			mime.addMultiPart(getBody (parts[i]));
		return mime;
	}
	
	/**
	 * decrypt and repackage a payload body for the servlet if possible
	 * 
	 * @param part to decrypt
	 * @return part for body
	 */
	MimeContent getBody (MimeContent part)
	{
	 	// check for xml encryption 	
  	if (part.getContentType().equals(MimeContent.XML))
  	{
  		// TODO check type for alternate decryption methods
   	  XmlEncryptor crypt = new XmlEncryptor ();
  		String password = config.getValue ("Decryption.Password");
  		File cert = config.getFile("Decryption.Unc");
  		if ((cert != null) && cert.canRead())
  		{
  	    byte[] payload = crypt.decryptPayload(cert.getAbsolutePath(), password, password, part.getBody());
  			MimeContent p = new MimeContent ();
  			p.setHeader (MimeContent.CONTENTID, "<payload>");
   		  p.setContentType(MimeContent.OCTET);
   			p.setHeader (MimeContent.ENCODING, MimeContent.BASE64);
  			p.setHeader(MimeContent.DISPOSITION, part.getHeader(MimeContent.DISPOSITION));
  		  p.encodeBody(payload);
  			return p;
  		}
  	}
  	return part;
	}
}
