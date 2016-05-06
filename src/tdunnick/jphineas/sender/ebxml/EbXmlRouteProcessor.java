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

package tdunnick.jphineas.sender.ebxml;

import java.io.*;
import java.net.*;
//import org.bouncycastle.crypto.digests.*;
//import org.bouncycastle.util.encoders.*;

import tdunnick.jphineas.logging.Log;
import tdunnick.jphineas.mime.*;
import tdunnick.jphineas.sender.*;
import tdunnick.jphineas.util.SocketFactory;
import tdunnick.jphineas.queue.*;
import tdunnick.jphineas.xml.*;
import tdunnick.jphineas.config.RouteConfig;
import tdunnick.jphineas.ebxml.*;

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
	private RouteConfig config = null;
  /** route path part of URL */
  private EbXmlRequest pkg = null;
  /** place to queue outgoing files... */
  String qDirectory = null;

	public boolean configure (RouteConfig cfg)
	{
		if ((this.config = cfg) == null)
			return false;
  	pkg = new EbXmlRequest (cfg);
  	if ((qDirectory = cfg.getQueueDirectory()) == null)
  		qDirectory = "";
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
				File f = new File (qDirectory + s);
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
  	// get a soap request for this row
  	SoapXml soap = pkg.getSoapRequest(row);
  	// check for basic authentication
  	String authid = null;
  	String pw = config.getAuthenticationType();;
  	if ((pw != null) && (pw.equalsIgnoreCase("basic")))
  	{
  		authid = config.getAuthenticationId(); 
  		pw = config.getAuthenticationPassword();
  	}
   	// build a request string...
   	String req = "POST " 
   		+ config.getProtocol () + "://"
   		+ config.getHost () + ":"
   		+ config.getPort ()
   		+ config.getPath ()
   		+ " HTTP/1.1\r\n";
   	// now ready to send it off
  	try
  	{
  		Socket socket = null;
			MimeContent mime = null;
			// send all chunks over the same connection
			while ((mime = pkg.getMessagePackage(soap, row)) != null)
			{
		  	// set up a connection
				if (socket == null)
				{
		   	  if ((socket = SocketFactory.createSocket(config)) == null)
			   	{
			   		Log.error("Failed to open connection to " + config.getHost());
			   		return false;
			   	}
				}
				OutputStream out = socket.getOutputStream();
				InputStream in = socket.getInputStream();
		  	// insert BASIC authentication
			  if (authid != null)
			  	mime.setBasicAuth (authid, pw);
				Log.debug("sending EbXml request:\n" + req + mime.toString());
	  		out.write(req.getBytes());
				out.write(mime.toString().getBytes());
				out.flush();
				Log.debug("waiting for reply");
				MimeContent msg = MimeReceiver.receive(in);
				// if the remote closed the socket, then clean up
				if (!socket.isConnected() || true)
				{
					socket.close ();
					socket = null;
				}
				// then parse the reply and update our row
			  Log.debug ("response:\n" + msg.toString());
			  if (!pkg.ParseMessagePackage(row, msg, soap.getHdrMessageId()))
			  	break;
			}
			// TODO digest authentication response?
			if (socket != null)
				socket.close();
	  	return complete (row);
  	}
  	catch (Exception e)
  	{
  		Log.error("Failed sending EbXML message", e);
  	}
  	return false;
	}
}
