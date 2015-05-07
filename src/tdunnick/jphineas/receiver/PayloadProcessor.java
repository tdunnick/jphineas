package tdunnick.jphineas.receiver;

import tdunnick.jphineas.util.*;
import tdunnick.jphineas.logging.*;
import tdunnick.jphineas.queue.*;
import tdunnick.jphineas.mime.MimeContent;
import tdunnick.jphineas.xml.*;

public class PayloadProcessor extends ReceiverProcessor
{
  XmlConfig config = null;
  PhineasQ queue = null;
  
	/**
	 * Simply save the configuration
	 * @param config to save
	 * @see tdunnick.jphineas.receiver.ReceiverProcessor#configure(tdunnick.jphineas.xml.XmlConfig)
	 */
	protected boolean configure(XmlConfig config)
	{
		this.config = config;
		String s = config.getValue("Queue");
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
	protected MimeContent process(SoapXml request, MimeContent[] parts)
	{
		ReceiverSoapEnvelope env = new ReceiverSoapEnvelope (request);
		EbXmlReceiverPackage pkg = new EbXmlReceiverPackage (config);
		//
		PhineasQRow row = getRow (request);
		// build a soap part
		SoapXml soap = env.getSoapResponse("Acknowledgment", config.getValue ("Organization"));
		// save our application responses
		MimeContent[] appResponse = new MimeContent[parts.length];
		// decode payloads and note their responses
		for (int i = 1; i < parts.length; i++)
		{
			appResponse[i] = pkg.parsePayloadContainer(parts[i], row);
			if (appResponse[i] != null)
			  row.append ();
		}
		// build a response part
		MimeContent response = pkg.getMessagePackage(soap);
		// add the responses
		for (int i = 1; i < appResponse.length; i++)
		{
			if (appResponse[i] != null)
			{
				response.addMultiPart(appResponse[i]);
			}
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
}
