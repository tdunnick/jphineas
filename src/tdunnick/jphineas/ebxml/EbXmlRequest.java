package tdunnick.jphineas.ebxml;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import tdunnick.jphineas.config.RouteConfig;
import tdunnick.jphineas.encryption.XmlEncryptor;
import tdunnick.jphineas.logging.Log;
import tdunnick.jphineas.mime.MimeContent;
import tdunnick.jphineas.queue.PhineasQRow;
import tdunnick.jphineas.util.DateFmt;
import tdunnick.jphineas.xml.*;

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
  /** host */
  private String host = null;
	

	/**
	 * Configure an ebXML request
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
		SoapXml soap = new SoapXml ();
		
		soap.setFromPartyId (hostId);
		soap.setToPartyId (partyId);
		soap.setCPAId (cpa);
		soap.setConversationId ("0");
		soap.setService (q.getService());
		soap.setAction (q.getAction());
		soap.setHdrMessageId (q.getMessageId() + "@" + organization);
		soap.setTimeStamp (DateFmt.getTimeStamp(null));
	
		// pings have no body
		if (q.getAction().equals("Ping"))
			soap.delete(SoapXml.bdy);
		else
		{
			soap.setManifestReference ("cid:" + q.getPayLoadFile() + "@" + organization);
			soap.setRecordId (q.getQueueId() + "." + q.getRowId());
			soap.setDbMessageId (q.getMessageId());
			soap.setArguments (q.getArguments());
			soap.setRecipient (q.getMessageRecipient());
		}
		soap.beautify(2);
		// Log.debug("Soap content: " + soap.toString());
		
		MimeContent part = new MimeContent ();
		part.setHeader (MimeContent.CONTENTID, "ebxml-envelope@" + organization);
		part.setContentType(MimeContent.XML);
		if (part.setBody(soap.toString()))
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
		if (!parseSoapEnvelope(row, partyId, cpa, new String (parts[0].getBody())))
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
	public boolean parseSoapEnvelope (PhineasQRow q, String partyId, String cpa, String data)
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
			if (!s.startsWith(q.getMessageId() + "@"))
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
		Log.debug("setting timestamp and other response items");
		q.setMessageReceivedTime (soap.getAckTimestamp ());
		q.setResponseMessageId(soap.getHdrMessageId());
		q.setResponseArguments(soap.getArguments());
		q.setSignature ("no");
		return true;
	}	
}
