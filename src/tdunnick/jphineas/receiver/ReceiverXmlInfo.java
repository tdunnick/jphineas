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


import tdunnick.jphineas.xml.*;

public class ReceiverXmlInfo
{
	XmlContent xml = new XmlContent();
	
	protected ReceiverXmlInfo(XmlContent xml)
	{
		this.xml = xml;
	}

	private String getValue (String tag)
	{
		return xml.getValue(tag);
	}
	
	private void setValue (String tag, String value)
	{
		xml.setValue (tag, value);
	}
	
	private String getNamedValue (String prefix, String suffix, String name)
	{
		for (int i = 0; i < xml.getTagCount(prefix); i++)
		{
			if (xml.getValue(prefix + "[" + i + "].Name").equals(name))
				return xml.getValue(prefix + "[" + i + "]." + suffix);
		}
		return null;
	}

	private void setNamedValue (String prefix, String suffix, String name, String value)
	{
		for (int i = 0; i < xml.getTagCount(prefix); i++)
		{
			if (xml.getValue(prefix + "[" + i + "].Name").equals(name))
			{
			  xml.setValue(prefix + "[" + i + "]." + suffix, value);
			  break;
			}
		}
	}
	
	public String getEncryptionTemplate()
	{
		return getValue("Receiver.EncryptionTemplate");
	}
	
	public String getResponseTemplate()
	{
		return getValue("Receiver.ResponseTemplate");
	}

	public String getMapAction(String name)
	{
		return getNamedValue("Receiver.MapInfo.Map", "Action", name);
	}
	
	public String getMapArguments(String name)
	{
		return getNamedValue("Receiver.MapInfo.Map", "Arguments", name);
	}
	
	public String getMapDirectory(String name)
	{
		return getNamedValue("Receiver.MapInfo.Map", "Directory", name);
	}
	
	public String getMapEncryptionId(String name)
	{
		return getNamedValue("Receiver.MapInfo.Map", "Encryption.Id", name);
	}
	
	public String getMapEncryptionPassword(String name)
	{
		return getNamedValue("Receiver.MapInfo.Map", "Encryption.Password", name);
	}
	
	public String getMapEncryptionType(String name)
	{
		return getNamedValue("Receiver.MapInfo.Map", "Encryption.Type", name);
	}
	
	public String getMapEncryptionUnc(String name)
	{
		return getNamedValue("Receiver.MapInfo.Map", "Encryption.Unc", name);
	}
	
	public String getMapFilter(String name)
	{
		return getNamedValue("Receiver.MapInfo.Map", "Filter", name);
	}
	
	public String getMapQueue(String name)
	{
		return getNamedValue("Receiver.MapInfo.Map", "Queue", name);
	}
	
	public String getMapRecipient(String name)
	{
		return getNamedValue("Receiver.MapInfo.Map", "Recipient", name);
	}
	
	public String getMapRoute(String name)
	{
		return getNamedValue("Receiver.MapInfo.Map", "Route", name);
	}
	
	public String getMapService(String name)
	{
		return getNamedValue("Receiver.MapInfo.Map", "Service", name);
	}
	
	public String getOrganization()
	{
		return getValue("Receiver.Organization");
	}
	
	public String getPartyId()
	{
		return getValue("Receiver.PartyId");
	}
	
	public String getCpaDirectory()
	{
		return getValue("Receiver.CpaDirectory");		
	}

	public String getQueueClass(String name)
	{
		return getNamedValue("Receiver.QueueInfo.Queue", "Class", name);
	}
	
	public String getQueueId(String name)
	{
		return getNamedValue("Receiver.QueueInfo.Queue", "Id", name);
	}
	
	public String getQueuePassword(String name)
	{
		return getNamedValue("Receiver.QueueInfo.Queue", "Password", name);
	}
	
	public String getQueueUnc(String name)
	{
		return getNamedValue("Receiver.QueueInfo.Queue", "Unc", name);
	}
	

	public String getSoapTemplate()
	{
		return xml.getValue("Receiver.SoapTemplate");
	}

	/* setters... */
	
	public void setEncryptionTemplate(String value)
	{
		setValue("Receiver.EncryptionTemplate", value);
	}
	
	public void setResponseTemplate(String value)
	{
		setValue("Receiver.ResponseTemplate", value);
	}
	
	public void setMapAction(String name, String value)
	{
		setNamedValue("Receiver.MapInfo.Map", "Action", name, value);
	}
	
	public void setMapArguments(String name, String value)
	{
		setNamedValue("Receiver.MapInfo.Map", "Arguments", name, value);
	}
	
	public void setMapDirectory(String name, String value)
	{
		setNamedValue("Receiver.MapInfo.Map", "Directory", name, value);
	}
	
	public void setMapEncryptionId(String name, String value)
	{
		setNamedValue("Receiver.MapInfo.Map", "Encryption.Id", name, value);
	}
	
	public void setMapEncryptionPassword(String name, String value)
	{
		setNamedValue("Receiver.MapInfo.Map", "Encryption.Password", name, value);
	}
	
	public void setMapEncryptionType(String name, String value)
	{
		setNamedValue("Receiver.MapInfo.Map", "Encryption.Type", name, value);
	}
	
	public void setMapEncryptionUnc(String name, String value)
	{
		setNamedValue("Receiver.MapInfo.Map", "Encryption.Unc", name, value);
	}
	
	public void setMapFilter(String name, String value)
	{
		setNamedValue("Receiver.MapInfo.Map", "Filter", name, value);
	}
	
	public void setMapQueue(String name, String value)
	{
		setNamedValue("Receiver.MapInfo.Map", "Queue", name, value);
	}
	
	public void setMapRecipient(String name, String value)
	{
		setNamedValue("Receiver.MapInfo.Map", "Recipient", name, value);
	}
	
	public void setMapRoute(String name, String value)
	{
		setNamedValue("Receiver.MapInfo.Map", "Route", name, value);
	}
	
	public void setMapService(String name, String value)
	{
		setNamedValue("Receiver.MapInfo.Map", "Service", name, value);
	}
	
	public void setOrganization(String value)
	{
		setValue("Receiver.Organization", value);
	}
	
	public void setPartyId(String value)
	{
		setValue("Receiver.PartyId", value);
	}
	
	public void setCpaDirectory(String value)
	{
		setValue("Receiver.CpaDirectory", value);
	}
	
	public void setQueueClass(String name, String value)
	{
		setNamedValue("Receiver.QueueInfo.Queue", "Class", name, value);
	}
	
	public void setQueueId(String name, String value)
	{
		setNamedValue("Receiver.QueueInfo.Queue", "Id", name, value);
	}
	
	public void setQueuePassword(String name, String value)
	{
		setNamedValue("Receiver.QueueInfo.Queue", "Password", name, value);
	}
	
	public void setQueueUnc(String name, String value)
	{
		setNamedValue("Receiver.QueueInfo.Queue", "Unc", name, value);
	}
	

	public void setSoapTemplate(String value)
	{
		setValue("Receiver.SoapTemplate", value);
	}
}
