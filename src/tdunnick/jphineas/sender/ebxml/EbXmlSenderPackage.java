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

import tdunnick.jphineas.encryption.*;
import tdunnick.jphineas.logging.*;
import tdunnick.jphineas.mime.*;
import tdunnick.jphineas.queue.*;
import tdunnick.jphineas.xml.*;

/**
 * Construct/parse an ebXML PHINMS Mime encoded request message.
 * 
 * @author tld
 *
 */
public class EbXmlSenderPackage
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
  /** host */
  private String host = null;
	

	/**
	 * Configure an ebXML mime packager 
	 * 
	 * @param config - the Sender configuration for this Route.
	 */
	public EbXmlSenderPackage (XmlConfig config)
	{
		partyId = config.getValue("PartyId");
		hostId = config.getValue ("HostId");
		domain = config.getValue("Domain");
		cpa = config.getValue("Cpa");
		organization = config.getValue ("Organization");
		queueDirectory = config.getDirectory("QueueDirectory");
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
	 * Construct the Mime header container.  This includes the soap envelope.
	 * 
	 * @param q queue row for this message
	 * @return the Mime encoded header
	 */
	private MimeContent getHeaderContainer (PhineasQRow q)
	{
		SenderSoapEnvelope soap = new SenderSoapEnvelope();
		MimeContent part = new MimeContent ();
		part.setHeader (MimeContent.CONTENTID, "ebxml-envelope@" + organization);
		part.setContentType(MimeContent.XML);
		if (part.setBody(soap.getSoapEnvelope(q, partyId, cpa, organization, hostId)))
			return part;
		fail (q, "Failed creating ebXML envelope");
		return null;
	}
	
	/**
	 * Construct the Mime payload container.  Encrypt the payload as specified
	 * by the queue entry.
	 * 
	 * @param q  queue row for this message
	 * @return the Mime encoded payload
	 */
	private MimeContent getPayloadContainer (PhineasQRow q)
	{
		File f = new File (queueDirectory + q.getPayLoadFile());
		if (!f.canRead())
		{
			fail (q, "Can't read " + f.getAbsolutePath());
			return null;
		}
		MimeContent part = new MimeContent ();
		part.setHeader (MimeContent.CONTENTID, f.getName() 
				+ "@" + organization);
		part.setHeader(MimeContent.DISPOSITION, "attachement; name=\""
				+ f.getName() + "\"");
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
			fail (q, "Failed reading " + f.getPath() + ": " + ex.getMessage());
			return null;
		}
		
		String base = null;
		StringBuffer dn = null;	
		String path = q.getCertificateUrl();
		if (path == null)
		{
			path = q.getPublicKeyLdapAddress();
			base = q.getPublicKeyLdapBaseDn();
			dn = new StringBuffer ();
			String s = q.getPublicKeyLdapDn();
			if (s != null)
				dn.append(s);
		}
		if (path != null)
		{
			XmlEncryptor crypt = new XmlEncryptor ();
			String enc = crypt.encryptPayload(path, base, dn, payload);
			if (enc == null)
			{
				fail (q, "Failed to encrypt payload");
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
	
	/**
	 * Construct a complete ebXML request message
	 * 
	 * @param q row from sender's queue
	 * @return Mime multipart request message
	 */
	public MimeContent getMessagePackage(PhineasQRow q)
	{
		MimeContent mime = new MimeContent ();
		mime.setHeader("Host", domain);
		mime.setHeader("Connection", "Keep-Alive");
		mime.setMultiPart("type=\"text/xml\"; start=\"ebxml-envelope@" 
				+ organization + "\"");
		mime.setHeader("SOAPAction", "\"ebXML\"");
		if (!mime.addMultiPart(getHeaderContainer (q)))
			return null;
		// if there is no payload, we are done (ping, RNR poll, etc.)
		if (q.getPayLoadFile() == null)
			return mime;
		if (mime.addMultiPart(getPayloadContainer (q)))
		  return mime;
		return null;
	}
	
	/**
	 * Parse an ebXML reply container setting the appropriate queue values. In
	 * the future this would also decrypt and store any returned payloads, possibly
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
	public boolean ParseMessagePackage (PhineasQRow row, MimeContent mime)
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
		SenderSoapEnvelope env = new SenderSoapEnvelope ();
		if (!env.parseSoapEnvelope(row, partyId, cpa, new String (parts[0].getBody())))
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
}
