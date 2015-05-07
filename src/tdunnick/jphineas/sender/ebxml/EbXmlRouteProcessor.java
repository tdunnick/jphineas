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

package tdunnick.jphineas.sender.ebxml;

import java.io.*;
import java.net.*;
//import org.bouncycastle.crypto.digests.*;
//import org.bouncycastle.util.encoders.*;

import tdunnick.jphineas.logging.Log;
import tdunnick.jphineas.mime.*;
import tdunnick.jphineas.sender.*;
import tdunnick.jphineas.queue.*;
import tdunnick.jphineas.xml.*;

/**
 * This is the route processor for ebXML (standard PHINMS) outgoing messages.
 * It gets called from a QueueThread when a message is found to send.  It uses
 * the Route's configuration, along with queue information to package the 
 * payload, make a connection, and send the message.  It updates the queue
 * based on the response (or lack of).
 * 
 * @author user
 *
 */
public class EbXmlRouteProcessor extends RouteProcessor
{
	private XmlConfig config = null;
  /** route path part of URL */
  private EbXmlSenderPackage pkg = null;

	public boolean configure (XmlConfig config)
	{
		if ((this.config = config) == null)
			return false;
  	pkg = new EbXmlSenderPackage (config);
		return true;
	}
	 
	/**
	 * Completes the processing of request including saving the ACK and
	 * removing the temporary file.
	 * 
	 * @param row to complete
	 * @return true if successful
	 */
	private boolean complete (PhineasQRow row)
	{
		// save the acknowledgment
		// TODO decide better place to identify ACK path in the queue
		String s = row.getResponseFileName();
		if ((s != null) && (s.length() > 0))
		{
			row.setResponseFileName("");
			boolean noNulls = row.setNoNulls(true);
			String r = "transportStatus=" + row.getTransportStatus() + "\n"
			  + "transportError=" + row.getTransportErrorCode() + "\n"
			  + "applicationStatus=" + row.getApplicationStatus() + "\n"
			  + "applicationError=" + row.getApplicationErrorCode() + "\n"
			  + "applicationData=" + row.getApplicationResponse() + "\n"
			  + "responseMessageId=" + row.getResponseMessageId() + "\n"
			  + "responseArguments=" + row.getResponseArguments() + "\n"
			  + "responseLocalFile=" + row.getResponseLocalFile() + "\n"
			  + "responseFileName=" + row.getResponseFileName() + "\n"
			  + "responseSignature=" + row.getResponseMessageSignature() + "\n"
			  + "responseMessageOrigin=" + row.getResponseMessageOrigin() + "\n";
			row.setNoNulls(noNulls);
			try
			{
				FileOutputStream out = new FileOutputStream (s);
				out.write(r.getBytes());
				out.close();			
			}
			catch (IOException e)
			{
				Log.error("Failed writing acknowledgment to " + s, e);
			}
		}
		// remove the cached file if transport succeeded
		if (row.getTransportStatus().equalsIgnoreCase("success"))
		{
			s = row.getPayLoadFile();
			if (s != null)
			{
				File f = new File (config.getDirectory("QueueDirectory") + s);
				if (!f.delete())
					Log.error("Couldn't delete cached file " + f.getAbsolutePath());
			}
		}
		// note we are done with this record
  	row.setProcessingStatus("done");
  	Log.debug("Send completed");
		return true;
	}
	
	/**
	 * The ebXML processor is responsible for creating an ebXML request, opening
	 * the appropriate connection including any needed authentication,
	 * and processing the response.
	 * 
	 * @param row with entry to send and update
	 * @return true if successful with row updates staged
	 * @see tdunnick.jphineas.sender.RouteProcessor#process(tdunnick.jphineas.queue.PhineasQRow)
	 */
	public boolean process (PhineasQRow row)
	{
  	if (row == null)
  		return false;
  	// create our outgoing message
  	MimeContent mime = pkg.getMessagePackage(row);
  	if (mime == null)
  		return complete (row);
  	String req = config.getValue("Authentication.Type");
  	// insert BASIC authentication
  	if ((req != null) && (req.equalsIgnoreCase("basic")))
  	{
  		mime.setBasicAuth(config.getValue("Authentication.Id"), 
  				config.getValue("Authentication.Password"));
  	}
  	// set up a connection and response handler
   	Socket socket = SenderSocketFactory.createSocket(config);
   	if (socket == null)
   		return false;
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
			MimeContent msg = MimeReceiver.receive(in);
			// TODO digest authentication response?
			out.close();
			in.close();
			socket.close();
			// finally parse the reply and update our row
		  Log.debug ("response:\n" + msg.toString());
		  if (pkg.ParseMessagePackage(row, msg))
		  	return complete (row);
  	}
  	catch (Exception e)
  	{
  		Log.error("Failed sending EbXML message", e);
  	}
  	return false;
	}
}
