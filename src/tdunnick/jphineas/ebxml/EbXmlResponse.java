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

import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.util.Date;

import tdunnick.jphineas.util.*;
import tdunnick.jphineas.config.XmlConfig;
import tdunnick.jphineas.filter.PhineasOutputFilter;
import tdunnick.jphineas.logging.Log;
import tdunnick.jphineas.mime.MimeContent;
import tdunnick.jphineas.xml.*;

public class EbXmlResponse
{
	// Service configuation
	XmlConfig config = null;
	// configuration pieces needed...
  /** a filter */
	Constructor <?> filter = null; 
 
	/**
	 * Configure an ebXML mime packager 
	 * 
	 * @param config - the Sender configuration for this Route.
	 */
	public EbXmlResponse (XmlConfig config)
	{
		this.config = config;
		/* TODO fix this filter reference
		XmlConfig c = config.copy (new XmlConfig(), "Filter", config.getValue ("Filter"));
		String s = c.getValue("Class");
		if (s != null) try
		{
			Class<?> cf = Class.forName(s);
			if (!PhineasOutputFilter.class.isAssignableFrom(cf))
				Log.error(s + " is not a PhineasInputFilter");
			else
			  filter = cf.getConstructor(InputStream.class);
		}
		catch (Exception e)
		{
			Log.error("Couldn't load filter " + s, e);
		}	
		*/	
	}

	/**
	 * Construct an ebXML SOAP response envelope
	 * @param request incoming soap request
	 * @param action to set
	 * @return an ebXML SOAP envelope
	 */
	public SoapXml getSoapResponse (SoapXml request, String action)
	{
		Date now = new Date();
		String organization = config.getValue ("Organization");
		SoapXml soap = new SoapXml ();
		soap.setFromPartyId (request.getToPartyId());
		soap.setToPartyId (request.getFromPartyId());
		soap.setCPAId ("");
		soap.setConversationId (request.getConversationId());
		soap.setService ("urn:oasis:names:tc:ebxml-msg:service");
		soap.setAction (action);
		soap.setHdrMessageId ("" +	now.getTime() + "@" + organization);
		soap.setTimeStamp (DateFmt.getTimeStamp(now));
		soap.setRefToMessageId (0,	"statusResponse@" + organization);
		soap.setRefToMessageId (1, request.getHdrMessageId());
		soap.setAckTimestamp (DateFmt.getTimeStamp(now));
		soap.setAckRefToMessageId (request.getHdrMessageId());
		soap.setManifestReference ("cid:statusResponse@" + organization);
		soap.beautify(2);
		return soap;
	}

	/**
	 * Create and return the response Mime header container for this request
	 * @param request used to generate the response
	 * @param action to include in the response
	 * @return Mime header
	 */
	public MimeContent getHeaderContainer (SoapXml request, String action)
	{
		return getHeaderContainer (getSoapResponse (request, action));
	}
	
	/**
	 * Create and return the Mime header container for this response
	 * @param soap response to package
	 * @return Mime header
	 */
	public MimeContent getHeaderContainer (SoapXml soap)
	{
		// build a soap part
		MimeContent part = new MimeContent ();
		part.setHeader (MimeContent.CONTENTID, 
				"ebxml-envelope@" + config.getValue("Organization"));
		part.setContentType(MimeContent.XML);
		if (!part.setBody (soap.toString()))
		{
			Log.error("Failed creating soap response");
			return null;
		}
		return part;
	}

  /**
   * Generate a default response package for this soap header
   * @param request soap used to build response
   * @param action to include in response
   * @return (partial) response
   */
  public MimeContent getMessagePackage (SoapXml request, String action)
  {
  	return getMessagePackage (getHeaderContainer(request, action));
  }
  
  /**
   * Generate a default response package for this soap header
   * @param soap response header container
   * @return (partial) response
   */
  public MimeContent getMessagePackage (MimeContent soap)
  {
		// now finish off the reply
		MimeContent mime = new MimeContent ();
		mime.setHeader("Host", config.getValue("Domain"));
		mime.setHeader("Connection", "Keep-Alive");
		mime.setMultiPart("type=\"text/xml\"; start=\"ebxml-envelope@" 
				+ config.getValue("Organization") + "\"");
		mime.setHeader("SOAPAction", "\"ebXML\"");
		// add the header
		mime.addMultiPart(soap);
		return mime;
  }
}
