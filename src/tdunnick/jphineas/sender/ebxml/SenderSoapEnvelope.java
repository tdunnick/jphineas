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

import tdunnick.jphineas.util.*;
import tdunnick.jphineas.logging.*;
import tdunnick.jphineas.queue.*;
import tdunnick.jphineas.xml.*;

/**
 * Construct/parse ebXML SOAP envelope, which is found in the first 
 * container of the ebXML Mime formatted request message.
 * 
 * TODO parse responses to RNR polls
 * 
 * @author tld
 *
 */
public class SenderSoapEnvelope
{
	/**
	 * parse the PID from the message ID in a queue row
	 * @param q row to parse
	 * @return
	 */
	private String getPid (PhineasQRow q)
	{
		String mid = q.getMessageId();
		int dash = mid.lastIndexOf('-');
		if (dash > 0)
			return mid.substring(dash + 1);
		return mid;
	}
	/**
	 * Construct an ebXML SOAP envelope for the current data
	 * @param q queue row used for construction
	 * @param partyId of remote receiver
	 * @param cpa for this connection
	 * @param organization for this message
	 * @param fromId my party id
	 * @return an ebXML SOAP envelope
	 */
	public String getSoapEnvelope(PhineasQRow q, 
			String partyId, String cpa, String organization, String fromId)
	{
		String pid = getPid (q);
		SoapXml soap = new SoapXml ();
		
		soap.setFromPartyId (fromId);
		soap.setToPartyId (partyId);
		soap.setCPAId (cpa);
		soap.setConversationId (pid);
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
		Log.debug("Soap content: " + soap.toString());
		return soap.toString();
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
