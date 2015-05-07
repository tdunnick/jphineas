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

import tdunnick.jphineas.mime.*;
import tdunnick.jphineas.xml.*;

/**
 * This processor generates responses for ebXML PING requests.
 * 
 * @author Thomas Dunnick
 *
 */
public class PingProcessor extends ReceiverProcessor
{
  XmlConfig config = null;
  
	/**
	 * Simply save the configuration
	 * @param config to save
	 * @see tdunnick.jphineas.receiver.ReceiverProcessor#configure(tdunnick.jphineas.xml.XmlConfig)
	 */
	protected boolean configure(XmlConfig config)
	{
		this.config = config;
		return true;
	}

	/**
	 * A Ping response is simply a lone ebXML soap part with Action set to "Pong"
	 * @param request request
	 * @param parts of the request (ignored)
	 * @return mime package for the response
	 * @see tdunnick.jphineas.receiver.ReceiverProcessor#process(tdunnick.jphineas.xml.SoapXml, tdunnick.jphineas.mime.MimeContent[])
	 */
	protected MimeContent process (SoapXml request, MimeContent[] parts)
	{
		ReceiverSoapEnvelope env = new ReceiverSoapEnvelope (request);
		EbXmlReceiverPackage pkg = new EbXmlReceiverPackage (config);
		// build a soap part
		SoapXml response = env.getSoapResponse("Pong", config.getValue ("Organization"));
		return pkg.getMessagePackage(response);
	}
}
