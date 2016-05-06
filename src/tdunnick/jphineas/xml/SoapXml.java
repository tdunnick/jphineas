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

package tdunnick.jphineas.xml;

import tdunnick.jphineas.common.JPhineas;

/**
 * Management of the SOAP part of an ebXML package.  All the "magic" xml tags
 * are contained here.
 * <p>
 * The soap Mime part is identifed by Content-ID "<ebxml-envelope@cdc.gov>"
 * The soap is divided into sections as follows:
 * <ul>
 * <li>MessageHeader - the soap-env:Envelope.soap-env:Header.eb:MessageHeader</li>
 * <li>Acknowledgment - the eb:Acknowledgement part of the MessageHeader</li>
 * <li>MessageData - the eb:MessageData part of the MessageHeader</li>
 * <li>MessageBody - the soap-env:Envelope.soap-env:Body</li>
 * <li>Manifest - the eb:Manifest part of the MessageBody</li>
 * <li>DatabaseInfo - the MetaData.DatabaseInfo part of the Manifest</li>
 * </ul>
 * 
 * @author Thomas Dunnick
 *
 */
public class SoapXml extends XmlContent
{
	/** a template for new content */
	public static final String template =
		"<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
		"<soap-env:Envelope xmlns:SOAP=\"http://schemas.xmlsoap.org/soap/envelope/\" " +
		"xmlns:eb=\"http://www.oasis-open.org/committees/ebxml-msg/schema/msg-header-2_0.xsd\" " +
		"xmlns:soap-env=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" " +
		"xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " +
		"xsi:schemaLocation=\"http://schemas.xmlsoap.org/soap/envelope/ http://www.oasis-open.org/committees/ebxml-msg/schema/envelope.xsd\" >" +
		"		<soap-env:Header>" +
		"			<eb:MessageHeader soap-env:mustUnderstand=\"1\" " +
		"				eb:version=\"2.0\" >" +
		"				<eb:From>" +
		"					<eb:PartyId eb:type=\"zz\"></eb:PartyId>" +
		"				</eb:From>" +
		"				<eb:To>" +
		"					<eb:PartyId eb:type=\"zz\"></eb:PartyId>" +
		"				</eb:To>" +
		"				<eb:CPAId></eb:CPAId>" +
		"				<eb:ConversationId></eb:ConversationId>" +
		"				<eb:Service eb:type=\"string\"></eb:Service>" +
		"				<eb:Action></eb:Action>" +
		"				<eb:MessageData>" +
		"					<eb:MessageId></eb:MessageId>" +
		"					<eb:Timestamp></eb:Timestamp>" +
		"				</eb:MessageData>" +
		"			</eb:MessageHeader>" +
		"			<eb:AckRequested soap-env:actor=\"urn:oasis:names:tc:ebxml-msg:actor:toPartyMSH\" " +
		"				soap-env:mustUnderstand=\"1\" eb:signed=\"false\" eb:version=\"2.0\" ></eb:AckRequested>" +
		"		</soap-env:Header>" +
		"		<soap-env:Body>" +
		"			<eb:Manifest eb:version=\"2.0\">" +
		"				<eb:Reference xlink:href=\"cid:EPOCHSHORT_EPOCHPLUS@ORGNAME.messaging.com\" " +
		"					xlink:type=\"simple\"></eb:Reference>" +
		"				<MetaData xmlns=\"http://www.cdc.gov/manifest/databaseinfo\" " +
		"					soap-env:mustUnderstand=\"0\">" +
		"					<DatabaseInfo soap-env:mustUnderstand=\"0\">" +
		"						<RecordId soap-env:mustUnderstand=\"0\"></RecordId>" +
		"						<MessageId></MessageId>" +
		"						<Arguments></Arguments>" +
		"						<MessageRecipient></MessageRecipient>" +
		"					</DatabaseInfo>" +
		"				</MetaData>" +
		"				<ResponseChunking xmlns=\"http://www.cdc.gov/manifest/responsechunking\" " +
		"					soap-env:mustUnderstand=\"0\"></ResponseChunking>" +
		"				<PeerVersion xmlns=\"http://www.cdc.gov/manifest/databaseinfo\"></PeerVersion>" +
		"			</eb:Manifest>" +
		"		</soap-env:Body>" +
		"</soap-env:Envelope>";
	
	/** the prefix to header data in the envelope */
	public static final String hdr = "soap-env:Envelope.soap-env:Header.eb:MessageHeader.";
	/** the prefix to ack message data */
	public static final String data = hdr + "eb:MessageData.";
	/** the prefix to acknowledgments */
	public static final String ack = "soap-env:Envelope.soap-env:Header.eb:Acknowledgment.";
	/** the prefix to error messages */
	public static final String error = "soap-env:Envelope.soap-env:Header.eb:ErrorList";
	/** the prefix to the body data in the envelope */
	public static final String bdy = "soap-env:Envelope.soap-env:Body.";
	/** the prefix to the manifest */
	public static final String manifest = bdy + "eb:Manifest.";
	/** the prefix to database data in the envelop */
	public static final String dbinf = manifest + "MetaData.DatabaseInfo.";
	
	/**
	 * construct an empty SOAP 
	 */
	public SoapXml ()
	{
		load (template);
		setValue (manifest + "PeerVersion", JPhineas.name + " " + JPhineas.revision);
	}
	
	/**
	 * construct SOAP from data
	 */
	public SoapXml (String xml)
	{
		load (xml);
	}
	
	/**
	 * Checks to see if we really have soap by looking for the root
	 * @return true if this is soap
	 */
	public boolean ok ()
	{
		return (getElement ("soap-env:Envelope") != null);
	}
	
	/**
	 * gets a SOAP value for a path
	 * @param path to the value
	 * @return the value or empty string if null
	 */
	private String get (String path)
	{
		String s = getValue (path);
		if (s == null) 
			s = "";
		return s;
	}
	
	/************************** MessageHeader **********************************/
	
	/**
	 * Get the service from the MessageHeader
	 * @return the service
	 */
	public String getService ()
	{
		return get (hdr + "eb:Service");
	}
	
	/**
	 * Set the service in the MessageHeader
	 * @param value of the service
	 * @return true if successful
	 */
	public boolean setService (String value)
	{
		return setValue (hdr + "eb:Service", value);
	}
	
	/**
	 * Get the action from the MessageHeader
	 * @return the service
	 */
	public String getAction ()
	{
		return get (hdr + "eb:Action");
	}

	/**
	 * Set the action in the MessageHeader
	 * @param value of the action
	 * @return true if successful
	 */
	public boolean setAction (String value)
	{
		return setValue (hdr + "eb:Action", value);
	}
	
	/**
	 * Get the CPA ID from the MessageHeader
	 * @return the service
	 */
	public String getCPAId ()
	{
		return get(hdr + "eb:CPAId");
	}
	
	/**
	 * Set the CPA ID in the MessageHeader
	 * @param value of the CPA ID
	 * @return true if successful
	 */
	public boolean setCPAId (String value)
	{
		return setValue (hdr + "eb:CPAId", value);
	}
	
	/**
	 * Get the local Party ID from the MessageHeader
	 * @return the service
	 */
	public String getFromPartyId ()
	{
		return get(hdr + "eb:From.eb:PartyId");
	}
	
	/**
	 * Set the local Party ID in the MessageHeader
	 * @param value of the local Party ID
	 * @return true if successful
	 */
	public boolean setFromPartyId (String value)
	{
		return setValue (hdr + "eb:From.eb:PartyId", value);
	}
	
	/**
	 * Get the remote Party ID from the MessageHeader
	 * @return the service
	 */
	public String getToPartyId ()
	{
		return get(hdr + "eb:To.eb:PartyId");
	}

	/**
	 * Set the remote Party ID in the MessageHeader
	 * @param value of the remote Party ID
	 * @return true if successful
	 */
	public boolean setToPartyId (String value)
	{
		return setValue (hdr + "eb:To.eb:PartyId", value);
	}
	
	/**
	 * Get the remote Conversation ID from the MessageHeader
	 * @return the service
	 */
	public String getConversationId ()
	{
		return get(hdr + "eb:ConversationId");
	}

	/**
	 * Set the remote Conversation ID in the MessageHeader
	 * @param value of the remote Conversation ID
	 * @return true if successful
	 */
	public boolean setConversationId (String value)
	{
		return setValue (hdr + "eb:ConversationId", value);
	}
	
	/************************** Manifest ***************************************/
	
	/**
	 * Get the reference from the manifest
	 * @return the service
	 */
	public String getManifestReference ()
	{
		String s = getAttribute (manifest + "eb:Reference", "xlink:href");
		if ((s != null) && (s.length() == 0))
			s = null;
		return s;
	}

	/**
	 * Set the reference in the manifest
	 * @param value of the reference
	 * @return true if successful
	 */
	public boolean setManifestReference (String value)
	{
		return setAttribute (manifest + "eb:Reference", "xlink:href", value);
	}
	
	/**
	 * Get the chunking request id from the manifest
	 * @return the request id
	 */
	public String getManifestChunkRequestId ()
	{
		String s = getValue (manifest + "ResponseChunking.RequestId");
		if ((s != null) && (s.length() == 0))
			s = null;
		return s;
	}

	/**
	 * Set the chunking request id in the manifest
	 * @param value of the request id
	 * @return true if successful
	 */
	public boolean setManifestChunkRequestId (String value)
	{
		return setValue (manifest + "ResponseChunking.RequestId", value);
	}
	
	/**
	 * Get the chunking request id from the manifest
	 * @return the ResponseBlockSize
	 */
	public String getManifestChunkResponseBlockSize ()
	{
		String s = getValue (manifest + "ResponseChunking.ResponseBlockSize");
		if ((s != null) && (s.length() == 0))
			s = null;
		return s;
	}

	/**
	 * Set the chunking request id in the manifest
	 * @param value of the ResponseBlockSize
	 * @return true if successful
	 */
	public boolean setManifestChunkResponseBlockSize (String value)
	{
		return setValue (manifest + "ResponseChunking.ResponseBlockSize", value);
	}
	
	/**
	 * Get the chunking request id from the manifest
	 * @return the ResponseBlockSize
	 */
	public String getManifestChunkResponsePart()
	{
		String s = getValue (manifest + "ResponseChunking.ResponsePart");
		if ((s != null) && (s.length() == 0))
			s = null;
		return s;
	}

	/**
	 * Set the chunking request id in the manifest
	 * @param value of the ResponseBlockSize
	 * @return true if successful
	 */
	public boolean setManifestChunkResponsePart (String value)
	{
		return setValue (manifest + "ResponseChunking.ResponsePart", value);
	}

	/**
	 * Get the Chunking from the manifest
	 * @return the Chunking
	 */
	public String getManifestChunking ()
	{
		String s = getValue (manifest + "ResponseChunking.Chunking");
		if ((s != null) && (s.length() == 0))
			s = null;
		return s;
	}

	/**
	 * Set Chunking in the manifest
	 * @param value of the Chunking ('begin', 'continue', 'done')
	 * @return true if successful
	 */
	public boolean setManifestChunking (String value)
	{
		return setValue (manifest + "ResponseChunking.Chunking", value);
	}
	
  /**
   * Check if we are done chunking.,
   * @return true if all chunks are done
   */
  public boolean done ()
  {
  	String s = getManifestChunking ();
  	return ((s == null) || (s.equals("done")));
  }


	/*********************************** MessageData ****************************/
	
	/**
	 * Get the message ID from the message data
	 * @return the message ID
	 */
	public String getHdrMessageId ()
	{
		return get(data + "eb:MessageId");
	}

	/**
	 * Set the message ID in the message data
	 * @param value of the message ID
	 * @return true if successful
	 */
	public boolean setHdrMessageId (String value)
	{
		return setValue (data + "eb:MessageId", value);
	}
	
	/**
	 * Get the time stamp from the message data
	 * @return the time stamp
	 */
	public String getTimeStamp ()
	{
		return get(data + "eb:TimeStamp");
	}

	/**
	 * Set the time stamp in the message data
	 * @param value of the time stamp
	 * @return true if successful
	 */
	public boolean setTimeStamp(String value)
	{
		return setValue (data + "eb:TimeStamp", value);
	}
	
	/**
	 * Get the RefToMessageId from the message data
	 * @param index to the reference
	 * @return the RefToMessageId
	 */
	public String getRefToMessageId (int index)
	{
		return get(data + "eb:RefToMessageId[" + index + "]");
	}

	/**
	 * Set the RefToMessageId in the message data
	 * @param index to the reference
	 * @param value of the message ID
	 * @return true if successful
	 */
	public boolean setRefToMessageId(int index, String value)
	{
		return setValue (data + "eb:RefToMessageId[" + index + "]", value);
	}
	
	
	/****************************** DatabaseInfo *********************************/
	
	/**
	 * Get the message ID from the database info
	 * @return the message ID
	 */
	public String getDbMessageId ()
	{
		return get(dbinf + "MessageId");
	}

	/**
	 * Set the message ID in the database info
	 * @param value of the message ID
	 * @return true if successful
	 */
	public boolean setDbMessageId (String value)
	{
		return setValue (dbinf + "MessageId", value);
	}
	
	/**
	 * Get the recipient from the database info
	 * @return the recipient
	 */
	public String getRecipient()
	{
		return get(dbinf + "MessageRecipient");
	}

	/**
	 * Set the recipient in the database info
	 * @param value of the recipient
	 * @return true if successful
	 */
	public boolean setRecipient (String value)
	{
		return setValue (dbinf + "MessageRecipient", value);
	}
	
	/**
	 * Get the arguments from the database info
	 * @return the arguments
	 */
	public String getArguments()
	{
		return get(dbinf + "Arguments");
	}
	
	/**
	 * Set the marguments in the database info
	 * @param value of the arguments
	 * @return true if successful
	 */
	public boolean setArguments (String value)
	{
		return setValue (dbinf + "Arguments", value);
	}
	
	/**
	 * Get the record ID from the database info
	 * @return the record ID
	 */
	public String getRecordId()
	{
		return get(dbinf + "RecordId");
	}
	
	/**
	 * Set the record ID in the database info
	 * @param value of the record ID
	 * @return true if successful
	 */
	public boolean setRecordId(String value)
	{
		return setValue (dbinf + "RecordId", value);
	}
	
	/**
	 * Get the current part for chunked requests
	 * @return the chunk's part
	 */
	public int getPart ()
	{
		return getInt (dbinf + "Part");
	}
	
	/**
	 * Set the current part for a chunked request
	 * @param part for this chunk
	 * @return true if successful
	 */
	public boolean setPart (int part)
	{
		return setValue (dbinf + "Part", "" + part);
	}
	
	/**
	 * Get the current part for chunked requests
	 * @return the chunk's part
	 */
	public int getNumParts ()
	{
		return getInt (dbinf + "NumParts");
	}
	
	/**
	 * Set the current part for a chunked request
	 * @param parts for this chunk
	 * @return true if successful
	 */
	public boolean setNumParts (int parts)
	{
		return setValue (dbinf + "NumParts", "" + parts);
	}
	
	/****************************** Acknowledgment *********************************/
	
	/**
	 * Get the acknowledgment time stamp from the Acknowledgment 
	 * @return the acknowledgment time stamp
	 */
	public String getAckTimestamp ()
	{
		return get(ack + "eb:Timestamp");
	}

	/**
	 * Set the acknowledgment time stamp in the Acknowledgment
	 * @param value of the acknowledgment time stamp
	 * @return true if successful
	 */
	public boolean setAckTimestamp (String value)
	{
		return setValue (ack + "eb:Timestamp", value);
	}
	
	/**
	 * Get the acknowledgment message reference id from the Acknowledgment
	 * @return the acknowledgment message reference id
	 */
	public String getAckRefToMessageId()
	{
		return get(ack + "eb:RefToMessageId");
	}

	/**
	 * Set the acknowledgment message reference id in the Acknowledgment
	 * @param value of the acknowledgment message reference id
	 * @return true if successful
	 */
	public boolean setAckRefToMessageId (String value)
	{
		return setValue (ack + "eb:RefToMessageId", value);
	}
	
	/******************************** error messages **************************************/
	public String getError (int n)
	{
		String path = error + "[" + n + "].eb:Error";
		String code = getAttribute(path, "eb:errorCode");
		if (code == null)
			return null;
		return code + " - " + getValue (path);
	}
	
	public boolean setError (int n, String code, String msg)
	{
		setValue (error + "[" + n + "].eb.Error", msg);		
		setAttribute(error + "[" + n + "].eb:Error", "eb:errorCode", code);		
		setAttribute(error + "[" + n + "].eb:Error", "eb:severity", "Error");
		if (getAttribute (error, "eb:highestSeverity") == null)
		{
			setAttribute (error, "eb:highestSeverity", "Error");
			setAttribute (error, "soap-env:mustUnderstand", "1");
		}
		return true;
	}
	
	/****************************** misc ************************************************/
	/**
	 * Get all of the MetaData from a request
	 */
	public String getMetaData ()
	{
		String pre = "Manifest.MetatData.DatabaseInfo.";
		XmlContent xml = new XmlContent ();
		xml.setValue(pre + "RecordId", getRecordId());
		xml.setValue(pre + "MessageId", getDbMessageId());
		xml.setValue(pre + "Arguments", getArguments());
		xml.setValue(pre + "MessageRecipient", getRecipient());
		xml.beautify (0);
		return xml.toString().replaceFirst ("^.*[?]>", "");
	}

}
