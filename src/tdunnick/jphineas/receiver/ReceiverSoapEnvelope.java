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

import java.text.SimpleDateFormat;
import java.util.Date;

import tdunnick.jphineas.logging.*;
import tdunnick.jphineas.queue.*;
import tdunnick.jphineas.xml.*;
import tdunnick.jphineas.mime.*;

/**
 * The manages the ebXML soap request and packaging 
 * 
 * @author Thomas Dunnick
 *
 */
public class ReceiverSoapEnvelope
{

  /** the request */
	private SoapXml request = null;
	
	public ReceiverSoapEnvelope (SoapXml soap)
	{
		request = soap;
	}
	
	/**
	 * Construct a PHINMS style timestamp
	 * @return the timestamp
	 */
	private String getTimeStamp (Date d)
	{
		SimpleDateFormat ts = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		return ts.format(d);
	}
	
	/**
	 * Construct an ebXML SOAP response envelope
	 * @param action to set
	 * @param organization 
	 * @return an ebXML SOAP envelope
	 */
	public SoapXml getSoapResponse (String action, String organization)
	{
		Date now = new Date();
		SoapXml soap = new SoapXml ();
		soap.setFromPartyId (request.getToPartyId());
		soap.setToPartyId (request.getFromPartyId());
		soap.setCPAId ("");
		soap.setConversationId ("" + now.getTime());
		soap.setService ("urn:oasis:names:tc:ebxml-msg:service");
		soap.setAction (action);
		soap.setHdrMessageId ("" +	now.getTime() + "@" + organization);
		soap.setTimeStamp (getTimeStamp(now));
		soap.setRefToMessageId (0,	"statusResponse@" + organization);
		soap.setRefToMessageId (1, request.getHdrMessageId());
		soap.setAckTimestamp (getTimeStamp(now));
		soap.setAckRefToMessageId (request.getHdrMessageId());
		soap.setManifestReference ("cid:statusResponse@" + organization);
		soap.beautify(2);
		return soap;
	}
	
	public boolean updateQueue (PhineasQRow q)
	{
		Log.debug ("Updating queue");
		q.setValue("FROMPARTYID", request.getFromPartyId());
		q.setValue("SERVICE", request.getService ());
		q.setValue("ACTION", request.getAction ());
		String s = request.getHdrMessageId();
		if (s != null)
			s = s.replaceFirst("^.*@","");
		q.setValue("MESSAGEID", s);
		q.setValue("ARGUMENTS", request.getArguments());
		q.setValue("MESSAGERECIPIENT", request.getRecipient());
		return true;
	}
}
