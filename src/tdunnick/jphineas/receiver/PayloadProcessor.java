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

import java.io.File;

import tdunnick.jphineas.util.*;
import tdunnick.jphineas.config.ServiceConfig;
import tdunnick.jphineas.ebxml.*;
import tdunnick.jphineas.logging.*;
import tdunnick.jphineas.queue.*;
import tdunnick.jphineas.mime.*;
import tdunnick.jphineas.xml.*;

/**
 * This is the general purpose payload processor.
 * 
 * @author Thomas Dunnick tdunnick@wisc.edu
 *
 */
public class PayloadProcessor extends ReceiverProcessor
{
  ServiceConfig config = null;
  PhineasQ queue = null;
  
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
		if ((queue = PhineasQManager.getInstance().getQueue (s)) == null)
		{
			Log.error("Can't access queue " + s);
			return false;
		}
		return true;
	}

	/**
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
		EbXmlResponse pkg = new EbXmlResponse (config);
		//
		PhineasQRow row = getRow (request);
		// build a response package
		MimeContent response = pkg.getMessagePackage (request, "Acknowledgment");
		// build an acknowledgement part
		EbXmlAppResponse rsp = new EbXmlAppResponse ();
		// get decryption values
		String password = config.getDecryptionPassword();
		File cert = config.getDecryptionUnc();
		if ((cert != null) && !cert.canRead())
		{
			Log.error ("Can't read certificate " + cert.getAbsolutePath());
			cert = null;
		}
		// and our destination directory
  	String dir = config.getPayloadDirectory();
  	int numParts = request.getNumParts();
  	int part = request.getPart();
  	String chunkid = request.getManifestChunkRequestId();
  	File f = null;
		// decode payloads and note their responses
		for (int i = 1; i < parts.length; i++)
		{
			rsp.reset ();
			EbXmlAttachment a = new EbXmlAttachment (parts[i]);
			// set destination for chunked vs. complete message...
			if (numParts > 0)
				f = Chunker.locate(chunkid, part);
			else
			  f = new File (dir + "/" + a.getName());
	  	row.setPayloadName (a.getName());
	  	row.setLocalFileName (f.getAbsolutePath());
	  	// handle encryption
	  	boolean encrypted = a.isEncrypted();
	  	row.setEncryption(encrypted ? "yes" : "no");
			if (encrypted && (cert != null))
			{
			  if (!a.decrypt (cert, password))
			  {
		  		String s = "Could not decrypt payload for " + a.getName();
		  		Log.error (s);
	    	  rsp.set ("abnormal", s, "failure");
			  }
			}
			// Log.debug("Saving payload to " + f.getAbsolutePath());
			if (!a.savePayload(f))
	  	{
	  		String s = "Could not save payload for " + a.getName();
	  		Log.error (s);
    	  rsp.set ("abnormal", s, "failure");
	  	}
			setRowResponse (row, rsp);
			response.addMultiPart(rsp.get ());
			// check chunked requests for completed transport
			if (numParts > 0)
			{
				// don't update our queue until all chunks are received!
				if (Chunker.saved (chunkid) < numParts)
					continue;
				Log.debug("assembling " + numParts + " parts for " + a.getName ());
				Chunker.assemble(new File (dir + "/" + a.getName()), chunkid);
			}
			row.append ();
		}
		return response;
	}
	
	/**
	 * create and initialize a receiver row from a request
	 * @param request soap envelope
	 * @return a receiver row
	 */
	private PhineasQRow getRow (SoapXml request)
	{
		PhineasQRow row = queue.newRow();
		row.setMessageId(request.getHdrMessageId());
		row.setService(request.getService());
		row.setAction(request.getAction());
		row.setFromPartyId (request.getFromPartyId());
		row.setMessageRecipient (request.getRecipient());
		String s = request.getMetaData();
		row.setArguments(s);
		row.setProcessingStatus("queued");
		s = DateFmt.getTimeStamp(null);
		row.setReceivedTime (s);
		row.setLastUpdateTime (s);		
		return row;
	}
	
	boolean setRowResponse (PhineasQRow row, EbXmlAppResponse rsp)
	{
		row.setErrorCode (rsp.getStatus());
		row.setErrorMessage (rsp.getError());
		row.setApplicationStatus(rsp.getAppData());
		return true;
	}
}
