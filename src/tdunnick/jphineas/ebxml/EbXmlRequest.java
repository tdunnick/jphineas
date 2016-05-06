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

import java.io.*;

import tdunnick.jphineas.config.RouteConfig;
import tdunnick.jphineas.logging.Log;
import tdunnick.jphineas.mime.MimeContent;
import tdunnick.jphineas.queue.PhineasQRow;
import tdunnick.jphineas.util.*;
import tdunnick.jphineas.xml.*;

/**
 * Packages an ebXML request for the sender.  Note there are several "ID's" in a request
 * which are generally created by our ProcessID generator.  Most could probably be
 * recycled from the conversation ID, but we ape what PHIN-MS does and generate separate
 * unique ID's (even though they repeat when data chunking).
 * 
 * @author Thomas Dunnick tdunnick@wisc.edu
 *
 */
public class EbXmlRequest
{

	// configuration pieced needed...
	/** remote party ID */
	private String partyId = null;
	/** my party ID */
	private String hostId = null;
	/** my host domain */
	private String domain = null;
  /** route cpa */
  private String cpa = null;
  /** my organization */
  private String organization = null;
  /** queue directory */
  private String queueDirectory  = null;
  /** chunk size */
  private int chunkSize = 0;

	/**
	 * Configure an ebXML request based on the route
	 * 
	 * @param config - the Sender configuration for this Route.
	 */
	public EbXmlRequest (RouteConfig config)
	{
		partyId = config.getPartyId();
		hostId = config.getHostId();
		domain = config.getDomain();
		cpa = config.getCpa();
		organization = config.getOrganization();
		queueDirectory = config.getQueueDirectory();
		chunkSize = config.getChunkSize();
	}

	/**
	 * common failure routine for constructing messages
	 * @param row that failed
	 * @param errormsg message to record
	 */
	private void fail (PhineasQRow row, String errormsg)
	{
		row.setTransportStatus("failed");
		row.setTransportErrorCode(errormsg);
		Log.error(errormsg);
	}
	
	/**
	 * Generate the SOAP for a request.  This gets updated for chunks.
	 * @param q queue for this request
	 * @return the SOAP XML
	 */
	public SoapXml getSoapRequest (PhineasQRow q)
	{
		SoapXml soap = new SoapXml ();
		
		// soap header 
		soap.setFromPartyId (hostId);
		soap.setToPartyId (partyId);
		soap.setCPAId (cpa);
		soap.setConversationId (ProcessID.get ());
		soap.setService (q.getService());
		soap.setAction (q.getAction());
		soap.setTimeStamp (DateFmt.getTimeStamp(null));
	  soap.setHdrMessageId (ProcessID.get() + "@" + organization);
		// soap body 
		soap.setRecordId (q.getQueueId() + "." + q.getRowId());
		soap.setDbMessageId (q.getMessageId());
		soap.setArguments (q.getArguments());
		soap.setRecipient (q.getMessageRecipient());
		
		// pings have no body
		if (q.getAction().equals("Ping"))
		{
			soap.delete(SoapXml.bdy);
		}
		// soap.beautify(2);
		// Log.debug("Soap content: " + soap.toString());
    return soap;
	}
	
	/**
	 * Update this soap envelope for a specific chunk
	 * @param soap to update
	 * @param numparts to send
	 * @return true if not done chunking (the last part)
	 */
	private boolean setChunk (SoapXml soap, int numchunks)
	{
		if (numchunks < 2)
			return false;
	  String chunking = "begin";
		int chunk = 0;
		// set up chunking if this is the first chunk
		if (soap.getNumParts() == 0)
		{
			soap.setNumParts(numchunks);
		  soap.setManifestChunkResponseBlockSize(Integer.toString (chunkSize));			
		  soap.setManifestChunkRequestId(ProcessID.get());
		}
		else // set up the next chunk
		{
			chunk = soap.getPart () + 1;
			/* for whatever reasons, PHINMS doesn't want us to update chunking.  This part
			 * is strange anyway... should this be done on the receiver side?  Dumps of
			 * PHIN-MS transport suggests otherwise, so we ape what we find.
		  if (chunk + 1 < numchunks)
		  	chunking = "continue";
		  else
		  	chunking = "done";
		  */
		}
	  // set the rest of the chunking specific values
	  soap.setManifestChunking(chunking);
	  soap.setHdrMessageId (ProcessID.get() + "@" + organization);
	  soap.setPart(chunk); // the part
	  soap.setManifestChunkResponsePart("0");
	  return chunk + 1 < numchunks;
	}
	
	/**
	 * Construct the Mime header container.  This includes the soap envelope.
	 * 
	 * @param q queue row for this message
	 * @return the Mime encoded header
	 */
	private MimeContent getHeaderContainer (SoapXml soap)
	{
		MimeContent part = new MimeContent ();
		part.setHeader (MimeContent.CONTENTID, "ebxml-envelope@" + organization);
		part.setContentType(MimeContent.XML);
		if (part.setBody(soap.toString()))
			return part;
		return null;
	}
	
	/**
	 * Construct the Mime payload container.  Encrypt the payload as specified.
	 * If we are chunking, get the next chunk and update the soap.
	 * 
	 * @param q  queue row for this message
	 * @return the Mime encoded payload
	 */
	private MimeContent getPayloadContainer (SoapXml soap, PhineasQRow q)
	{
		// if all done chunking, then do nothing
		if (soap.getPart() + 1 == soap.getNumParts())
			return null;
		File f = new File (queueDirectory + q.getPayLoadFile());
		String ref = q.getPayLoadFile();
		if (setChunk (soap, Chunker.needed(f, chunkSize)))
			ref = Integer.toString(soap.getPart()) + "_" + soap.getConversationId();
	  soap.setManifestReference("cid:" + ref + "@" + organization);

		if (!f.canRead())
		{
			fail (q, "Can't read " + f.getAbsolutePath());
			return null;
		}
		EbXmlAttachment p = new EbXmlAttachment ();
		p.setPayload(Chunker.getBytes (f, soap.getPart(), chunkSize));
		p.setId(ref + "@" + organization);
		p.setName(ref);
		String path = q.getCertificateUrl();
		if (path == null)
		{
			path = q.getPublicKeyLdapAddress();
			String base = q.getPublicKeyLdapBaseDn();
			String dn = q.getPublicKeyLdapDn();
			p.encrypt(path, base, dn);
		}
	  // Log.debug("Payload part: " + p.get().toString());
		return p.get ();
	}
	
	/**
	 * Construct a complete ebXML request message
	 * 
	 * @param q row from sender's queue
	 * @return Mime multipart request message
	 */
	public MimeContent getMessagePackage(SoapXml soap, PhineasQRow q)
	{
		MimeContent payload = null;
		if ((q.getPayLoadFile() != null) &&	((payload = getPayloadContainer (soap, q)) == null))
			return null;
		MimeContent hdr = getHeaderContainer (soap);
		if (hdr == null)
		{
			fail (q, "Failed creating ebXML envelope");
			return null;
		}		
		soap.beautify(2);
		// Log.debug("Soap request: " + soap.toString());
		
		// now package it all up
		MimeContent mime = new MimeContent ();
		mime.setHeader("Host", domain);
		mime.setHeader("Connection", "Keep-Alive");
		mime.setMultiPart("type=\"text/xml\"; start=\"ebxml-envelope@" 
				+ organization + "\"");
		mime.setHeader("SOAPAction", "\"ebXML\"");
		mime.addMultiPart(hdr);
		if (payload != null)
			mime.addMultiPart (payload);
		return mime;
	}
	
	/**
	 * Parse an ebXML reply container setting the appropriate queue values. 
   * TODO decrypt and store any returned payloads, possibly
	 * using a processor of some type.
	 * 
	 * @param row row to update
	 * @param mime from receiver
	 * @return true if successful
	 */
	private boolean parseReply (PhineasQRow row, MimeContent mime)
	{
		ResponseXml xml = new ResponseXml (mime.getBody());

		if (!xml.ok())
			return false;
		// update the queue
		row.setApplicationStatus (xml.getStatus());
		row.setApplicationErrorCode (xml.getError());
		row.setApplicationResponse (xml.getAppData());
		return true;
	}

	/**
	 * Parses each part of the reply from the receiver.  Set the 
	 * transport and application status.
	 * 
	 * @param row row to update
	 * @param mime received
	 * @return true if successful
	 */
	public boolean ParseMessagePackage (PhineasQRow row, MimeContent mime, String id)
	{
		if (mime == null)
		{
			Log.debug ("mime was null");
			return false;
		}
		MimeContent[] parts = mime.getMultiParts();
		if ((parts == null) || (parts.length == 0))
		{
			Log.debug("no mime parts found");
			return false;
		}
		Log.debug ("found " + parts.length + " parts");
		if (!parseSoapEnvelope(row, partyId, cpa, new String (parts[0].getBody()), id))
		{
			Log.debug("Failed parsing soap envelope");
			return false;
		}
		// well, we really only expect one for now...
		for (int i = 1; i < parts.length; i++)
		{
			parseReply (row, parts[i]);
		}
		row.setTransportStatus ("success");
		row.setTransportErrorCode("none");
		return true;
	}
	
	/**
	 * Checks a return envelope, setting the application status and return true
	 * if all looks good.
	 * 
	 * @param q row being processed
	 * @param partyId of the remote receiver
	 * @param cpa used for the connection
	 * @param data received from the receiver
	 * @return true if all checks out
	 */
	private boolean parseSoapEnvelope (PhineasQRow q, String partyId, String cpa, String data, String id)
	{
		SoapXml soap = new SoapXml (data);
		if (!soap.ok ())
		{
			Log.error("Failed parsing reply");
			return false;
		}
		soap.beautify (2);
		Log.debug ("Parsing Soap envelope:\n" + soap.toString());
		String e = soap.getError (0);
		if (e != null)
		{
			Log.error ("ebXML error " + e);
			return false;
		}
		if (!soap.getFromPartyId().equals (partyId))
		{
			Log.error("Incorrect route in reply");
			return false;
		}
		if (!soap.getCPAId().equals(cpa))
		{
			Log.error("Incorrect cpa in reply");
			return false;
		}			
		if (!soap.getService().equals("urn:oasis:names:tc:ebxml-msg:service"))
		{
			Log.error("Unexpected service");
			return false;
		}
		String action = soap.getAction();
		if (action.equals("Acknowledgment"))
		{
			String s = soap.getAckRefToMessageId();
			if (!s.equals (id))
			{
				Log.error("Incorrect message reference ID " + s);
				return false;
			}
		}
		else if (!action.equals ("Pong"))
		{
			Log.error("Unexpected response action " + action);
			return false;
		}
		// Log.debug("setting timestamp and other response items");
		q.setMessageReceivedTime (soap.getAckTimestamp ());
		q.setResponseMessageId(soap.getHdrMessageId());
		q.setResponseArguments(soap.getArguments());
		q.setSignature ("no");
		return true;
	}	
}
