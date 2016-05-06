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

import tdunnick.jphineas.config.XmlConfig;

/**
 * manages Communication Protocol Agreement (CPA)
 * 
 * @author Thomas Dunnick tdunnick@wisc.edu
 *
 */
public class CpaXml extends XmlContent
{
	/** a template for new cpa */
	public static final String template =
	"<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
	"<tp:CollaborationProtocolAgreement " +
	"	tp:cpaid=\"uri:phmsg-cdc-ssl\" tp:version=\"1.2\" " +
	"	xmlns:ds=\"http://www.w3.org/2000/09/xmldsig#\" " +
	"	xmlns:tp=\"http://www.ebxml.org/namespaces/tradePartner/\" " +
	"	xmlns:xlink=\"http://www.w3.org/1999/xlink\" " +
	"	xmlns:xsi=\"http://www.w3.org/2000/10/XMLSchema-instance\" " +
	"	xsi:schemaLocation=\"http://www.ebxml.org/namespaces/tradePartner http://ebxml.org/project_teams/trade_partner/cpp-cpa-v1_0.xsd\">" +
	"	<tp:Status tp:value=\"proposed\" />" +
	"	<tp:Start>2001-05-20T07:21:00Z</tp:Start>" +
	"	<tp:End>2002-05-20T07:21:00Z</tp:End>" +
	"	<tp:ConversationConstraints " +
	"		tp:concurrentConversations=\"100\" tp:invocationLimit=\"100\" />" +
	"	<tp:PartyInfo>" +
	"		<tp:PartyId tp:type=\"zz\"></tp:PartyId>" +
	"		<!-- sender's party id -->" +
	"		<tp:PartyRef xlink:href=\"http://www.cdc.gov/about.html\" " +
	"			xlink:type=\"simple\" />" +
	"		<tp:Transport tp:transportId=\"N35\">" +
	"			<tp:SendingProtocol tp:version=\"1.1\"> </tp:SendingProtocol>" +
	"			<tp:ReceivingProtocol tp:version=\"1.1\"> </tp:ReceivingProtocol>" +
	"			<tp:Endpoint tp:type=\"allPurpose\" " +
	"				tp:uri=\"phmsg.cdc.gov/evalebxml/receivefile\" />" +
	"			<tp:TransportSecurity>" +
	"				<tp:Protocol> </tp:Protocol>" +
	"				<tp:CertificateRef>" +
	"				</tp:CertificateRef>" +
	"				<tp:authenticationType>" +
	"				</tp:authenticationType>" +
	"				<!-- basic, custom, sdn, clientcert -->" +
	"				<tp:basicAuth>" +
	"					<tp:indexPage>" +
	"					</tp:indexPage>" +
	"					<tp:basicAuthUser>" +
	"					</tp:basicAuthUser>" +
	"					<tp:basicAuthPasswd>" +
	"					</tp:basicAuthPasswd>" +
	"				</tp:basicAuth>" +
	"			</tp:TransportSecurity>" +
	"		</tp:Transport>" +
	"	</tp:PartyInfo>" +
	"	<tp:PartyInfo>" +
	"		<!-- receiver's party id -->" +
	"		<tp:PartyId tp:type=\"zz\"></tp:PartyId>" +
	"		<tp:PartyRef xlink:href=\"http://www.cdc.gov/about.html\" xlink:type=\"simple\" />" +
	"		<tp:Transport tp:transportId=\"N35\">" +
	"			<tp:SendingProtocol tp:version=\"1.1\"> </tp:SendingProtocol>" +
	"			<tp:ReceivingProtocol tp:version=\"1.1\"> </tp:ReceivingProtocol>" +
	"			<tp:Endpoint tp:type=\"allPurpose\" tp:uri=\"\" />" +
	"			<!-- tp:uri set to receiver's URL -->" +
	"			<tp:TransportSecurity>" +
	"				<!-- HTTP or HTTPS -->" +
	"				<tp:Protocol></tp:Protocol>" +
	"				<tp:CertificateRef>" +
	"				</tp:CertificateRef>" +
	"				<!-- basic, custom, sdn, clientcert -->" +
	"				<tp:authenticationType></tp:authenticationType>" +
	"				<tp:basicAuth>" +
	"					<tp:indexPage>" +
	"					</tp:indexPage>" +
	"					<tp:basicAuthUser>" +
	"					</tp:basicAuthUser>" +
	"					<tp:basicAuthPasswd>" +
	"					</tp:basicAuthPasswd>" +
	"				</tp:basicAuth>" +
	"				<tp:customAuth>" +
	"					<tp:customLoginPage>" +
	"					</tp:customLoginPage>" +
	"					<tp:publicParams>" +
	"					</tp:publicParams>" +
	"					<tp:secretParams>" +
	"					</tp:secretParams>" +
	"				</tp:customAuth>" +
	"				<tp:netegrityAuth>" +
	"					<tp:sdnPassword>" +
	"					</tp:sdnPassword>" +
	"					<tp:sdnLoginPage>" +
	"					</tp:sdnLoginPage>" +
	"					<tp:keyStore>" +
	"					</tp:keyStore>" +
	"					<tp:keyStorePasswd>" +
	"					</tp:keyStorePasswd>" +
	"				</tp:netegrityAuth>" +
	"				<tp:clientCertAuth>" +
	"					<tp:keyStore></tp:keyStore>" +
	"					<tp:keyStorePasswd></tp:keyStorePasswd>" +
	"				</tp:clientCertAuth>" +
	"			</tp:TransportSecurity>" +
	"		</tp:Transport>" +
	"	</tp:PartyInfo>" +
	"	<tp:Comment xml:lang=\"en-us\">send/receive agreement between cdc and messaging partner</tp:Comment>" +
	"</tp:CollaborationProtocolAgreement>";
	
	public static final String cpa = "tp:CollaborationProtocolAgreement";
	public static final String comment = cpa + ".tp:Comment"; 
	public static final String sender = cpa + ".tp:PartyInfo[0].tp:PartyId";
	public static final String receiver = cpa + ".tp:PartyInfo[1].tp:PartyId";
	public static final String transport =cpa + ".tp:PartyInfo[1].tp:Transport";
	public static final String endpoint = transport + ".tp:Endpoint";
	public static final String security =transport + ".tp:TransportSecurity";
	public static final String protocol = security + ".tp:Protocol";
	public static final String authtype = security + ".tp:authenticationType";
	public static final String basicauth = security + ".tp:basicAuth";
	public static final String customauth = security + ".tp:customAuth";
	public static final String sdnauth = security + ".tp:netegrityAuth";
	public static final String certauth = security + ".tp:clientCertAuth";
	
	/**
	 * create a CPA from a Sender Route
	 * @param route
	 */
	public CpaXml (XmlConfig route)
	{
		load (template);
		setValue (sender, route.getValue("HostId"));
		setValue (receiver, route.getValue("PartyId"));
		String s = route.getValue("Host") + ":" + route.getValue("Port") 
		  + route.getValue("Path");
		setAttribute (endpoint, "tp:uri", s);
		setValue (protocol, route.getValue ("Protocol").toUpperCase());
		s = route.getValue("Authentication.Type");
		if (s == null)
			s = "none";
		setValue (authtype, s);
		String id = route.getValue ("Authentication.Id");
		String pw = route.getValue ("Authentication.Password");
		String unc = route.getValue ("Authentication.Unc");
		pw = "SECRET!";
		if (s.equalsIgnoreCase("basic"))
		{
			setValue (basicauth + ".tp:basicAuthUser", id);
			setValue (basicauth + ".tp:basicAuthPasswd", pw);
		}
		else if (s.equalsIgnoreCase("sdn"))
		{
			setValue (sdnauth + ".tp:sdnLoginPage", unc);
			setValue (sdnauth + ".tp:sdnPassword", pw);			
			setValue (sdnauth + ".tp:keyStore", unc);
			setValue (sdnauth + ".tp:keyStorePasswd", pw);			
		}
		else if (s.equalsIgnoreCase("clientcert"))
		{
			setValue (certauth + ".tp:keyStore", unc);
			setValue (certauth + ".tp:keyStorePasswd", pw);
		}
		s = "send/receive agreement between " + route.getValue ("HostId")
		  + " and " + route.getValue ("Name");
		setValue (comment, s);
	}
	
	/**
	 * Get the preferred name for this CPA.  Note this may vary from that set in the Route!
	 * @return preferred name
	 */
	public String getCpaName ()
	{
		return getValue (receiver) + "." + getValue (sender);
	}
}
