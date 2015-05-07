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

import tdunnick.jphineas.logging.*;
import tdunnick.jphineas.queue.*;
import tdunnick.jphineas.util.*;
import tdunnick.jphineas.xml.*;

/**
 * Manages ebXML sender queue
 * 
 * @author Thomas Dunnick
 *
 */
public class EbXmlQueue
{
  /**
   * Add a queued entry.  Note the row may be pre-populated by caller with 
   * payload information, etc.
   * 
   * @param config with route/service/action/etc
   * @param row to add
   * @return true if successful
   */
  public static boolean add (XmlConfig config, PhineasQRow row)
  {
  	if ((config == null) || (row == null))
  	  return false;
		long pid = ProcessID.getNewId();
		// note our encryption if any
		String encType = config.getValue ("Encryption.Type");
		if (encType == null)
			encType = "none";
		// add the entry
		row.setMessageId ("FOLDERPOLLING-" + pid);
		row.setRouteInfo (config.getValue ("Route"));
		row.setService (config.getValue ("Service"));
		row.setAction (config.getValue ("Action"));
		row.setArguments (config.getValue ("Arguments"));
		row.setMessageRecipient (config.getValue ("Recipient"));
		row.setSignature (null);
		row.setEncryption ("yes");
		if (encType.equalsIgnoreCase ("ldap"))
		{
			row.setPublicKeyLdapAddress (config.getValue ("Encryption.Unc"));
			row.setPublicKeyLdapDn (config.getValue ("Encryption.Dn"));
			row.setPublicKeyLdapBaseDn (config.getValue ("Encryption.BaseDn"));
			// TODO id and password for secured LDAPs
		}
		else if (encType.equalsIgnoreCase ("certificate"))
		{
		  row.setCertificateUrl (config.getFile("Encryption.Unc").getAbsolutePath());
		}
		else if (encType.equalsIgnoreCase ("pbe"))
		{
			 // TODO pbe encryption...
			Log.error ("Unsupported encryption type " + encType);
			row.setEncryption("no");
		}
		else
		{
			if (!encType.equalsIgnoreCase ("none"))
			Log.error ("Unsupported encryption type " + encType);
			row.setEncryption("no");
		}
    row.setProcessingStatus ("queued");
		row.setPriority ("0");
		if (row.append() < 0)
			return false;		
		return true;
  }
  
  /**
   * Add a ping request
   * @param queue to add the request to
   * @param route to ping
   * @return true if successful
   */
  public static boolean addPing (PhineasQ queue, String route)
  {
  	if ((queue == null) || (route == null))
  		return false;
  	String xml = "<Ping><Route>" + route + "</Route>"
  	  + "<Service>urn:oasis:names:tc:ebxml-msg:service</Service>"
  	  + "<Action>Ping</Action></Ping>";
  	XmlConfig config = new XmlConfig ();
  	config.load(xml);
  	return add (config, queue.newRow());
  }
}
