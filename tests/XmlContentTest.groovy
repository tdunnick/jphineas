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

;

import groovy.util.GroovyTestCase;
import java.io.*;
import tdunnick.jphineas.xml.*;

class XmlContentTest extends GroovyTestCase
{
  def xml = """<?xml version="1.0" encoding="UTF-8"?>  	
<EncryptedData Id="ed1" Type="http://www.w3.org/2001/04/xmlenc#Element" xmlns="http://www.w3.org/2001/04/xmlenc#">
  <EncryptionMethod Algorithm="http://www.w3.org/2001/04/xmlenc#tripledes-cbc"/>
  <KeyInfo xmlns="http://www.w3.org/2000/09/xmldsig#">
    <EncryptedKey xmlns="http://www.w3.org/2001/04/xmlenc#">
      <EncryptionMethod Algorithm="http://www.w3.org/2001/04/xmlenc#rsa-1_5"/>
      <KeyInfo xmlns="http://www.w3.org/2000/09/xmldsig#">
        <KeyName>key</KeyName>
      </KeyInfo>
      <CipherData>
        <CipherValue/>
      </CipherData>
    </EncryptedKey>
  </KeyInfo>
  <CipherData>
    <CipherValue/>
  </CipherData>
</EncryptedData>"""
	
  def cpa = """<?xml version="1.0" encoding="UTF-8" standalone="no"?>
<tp:CollaborationProtocolAgreement xmlns:ds="http://www.w3.org/2000/09/xmldsig#" xmlns:tp="http://www.ebxml.org/namespaces/tradePartner/" xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:xsi="http://www.w3.org/2000/10/XMLSchema-instance" tp:cpaid="uri:phmsg-cdc-ssl" tp:version="1.2" xsi:schemaLocation="http://www.ebxml.org/namespaces/tradePartner http://ebxml.org/project_teams/trade_partner/cpp-cpa-v1_0.xsd">
  <tp:Status tp:value="proposed"/>
  <tp:Start>2001-05-20T07:21:00Z</tp:Start>
  <tp:End>2002-05-20T07:21:00Z</tp:End>
  <tp:ConversationConstraints tp:concurrentConversations="100" tp:invocationLimit="100"/>
  <tp:PartyInfo>
    <tp:PartyId tp:type="zz"/>
    <!-- sender's party id -->
    <tp:PartyRef xlink:href="http://www.cdc.gov/about.html" xlink:type="simple"/>
    <tp:Transport tp:transportId="N35">
      <tp:SendingProtocol tp:version="1.1"/>
      <tp:ReceivingProtocol tp:version="1.1"/>
      <tp:Endpoint tp:type="allPurpose" tp:uri="phmsg.cdc.gov/evalebxml/receivefile"/>
      <tp:TransportSecurity>
        <tp:Protocol/>
        <tp:CertificateRef/>
        <tp:authenticationType/>
        <!-- basic, custom, sdn, clientcert -->
        <tp:basicAuth>
          <tp:indexPage/>
          <tp:basicAuthUser/>
          <tp:basicAuthPasswd/>
        </tp:basicAuth>
      </tp:TransportSecurity>
    </tp:Transport>
  </tp:PartyInfo>
  <tp:PartyInfo>
    <tp:PartyId tp:type="zz"/>
    <!-- receiver's party id -->
    <tp:PartyRef xlink:href="http://www.cdc.gov/about.html" xlink:type="simple"/>
    <tp:Transport tp:transportId="N35">
      <tp:SendingProtocol tp:version="1.1"/>
      <tp:ReceivingProtocol tp:version="1.1"/>
      <tp:Endpoint tp:type="allPurpose" tp:uri=""/>
      <!-- tp:uri set to receiver's URL -->
      <tp:TransportSecurity>
        <tp:Protocol/>
        <!-- HTTP or HTTPS -->
        <tp:CertificateRef/>
        <tp:authenticationType/>
        <!-- basic, custom, sdn, clientcert -->
        <tp:basicAuth>
          <tp:indexPage/>
          <tp:basicAuthUser/>
          <tp:basicAuthPasswd/>
        </tp:basicAuth>
        <tp:customAuth>
          <tp:customLoginPage/>
          <tp:publicParams/>
          <tp:secretParams/>
        </tp:customAuth>
        <tp:netegrityAuth>
          <tp:sdnPassword/>
          <tp:sdnLoginPage/>
          <tp:keyStore/>
          <tp:keyStorePasswd/>
        </tp:netegrityAuth>
        <tp:clientCertAuth>
          <tp:keyStore/>
          <tp:keyStorePasswd/>
        </tp:clientCertAuth>
      </tp:TransportSecurity>
    </tp:Transport>
  </tp:PartyInfo>
  <tp:Comment xml:lang="en-us">send/receive agreement between cdc and messaging partner</tp:Comment>
</tp:CollaborationProtocolAgreement>
"""
  XmlContent xmlc = new XmlContent();
	String logPattern = "%l %p: %m%n"
	String tag = "EncryptedData.KeyInfo.EncryptedKey.KeyInfo.KeyName"

  
	protected void setUp() throws Exception
	{
		ByteArrayInputStream inp = new ByteArrayInputStream (xml.toString().getBytes())
		xmlc.load (inp);
	}

	protected void tearDown() throws Exception
	{
		super.tearDown();
	}

	public final void testLoad()
	{
		assert xmlc.getDoc() != null : "Failed to load xml"
		assert xmlc.createDoc() : "document creation failed"
	}

	public final void testSave()
	{
		ByteArrayOutputStream outp = new ByteArrayOutputStream ();
		assert xmlc.save(outp) : "Save failed"
	}
	
	public final void testGetValue()
	{
		// println "'" + xmlc.getValue(tag) + "'"
		assert xmlc.getValue (tag).equals("key") : "tag value not retrieved"
	}

	public final void testSetValue ()
	{
		xmlc.setValue (tag, "foobar");
		assert xmlc.getValue(tag).equals("foobar") : "tag value not set"
		xmlc.setValue (tag + "[3]", "the third")
		assert xmlc.getValue(tag + "[3]").equals("the third") : "index value not set"
		assert xmlc.getValue(tag + "[0]").equals("foobar") : "zero index corrupted"
		// println "'" + xmlc.getValue(tag + "[1]") + "'"
		assert xmlc.getValue(tag + "[1]").length() == 0 : "wrong length for empty tag"
		assert xmlc.getTagCount(tag) == 4 : "incorrect tag count"
		xdump (xmlc)
	}
	
	public final void testAttribute ()
	{
	  assert xmlc.setAttribute (tag, "id", "foobar") : "couldn't set attribute"
	  assert xmlc.getAttribute (tag, "id").equals("foobar") : "attribute didn't match"
		assert xmlc.getAttribute (tag, "id2").length() == 0 : "returned invalid attribute"
	}
	
	public final void testNS ()
	{
		String xname = "examples/soap_defaults.xml"
		String xtag = "soap-env:Envelope.soap-env:Header.eb:MessageHeader.eb:From.eb:PartyId"
		FileInputStream inp = new FileInputStream (xname);
		assert xmlc.load (inp) : "Couldn't load " + xname
		assert xmlc.getValue(xtag).equals("FROMPARTYID") : "didn't match value"
		assert xmlc.getAttribute(xtag, "eb:type").equals("zz") : "didn't match attribute"
		xdump (xmlc)
	}
	
	public final void testToString ()
	{
		xdump (xmlc)
		def s = xmlc.toString()
		assert s != null : "failed to save to string"
		// println s
		xmlc.beautify (0);
		s = xmlc.toString(xmlc.getElement ("EncryptedData.KeyInfo"), true)
		assert s != null : "failed to element save to string"
		//println s
	}
	
	public final void testBeautify ()
	{
		XmlContent xml = new XmlContent ();
		assert xml.load (cpa) : "can't load cpa"
		xml.beautify (2);
		String s = xml.toString().replace ("\r", "")
		assert s.equals (cpa) : "Expected CPA not matched"
	}
	
	void xdump (XmlContent x)
	{
		ByteArrayOutputStream outp = new ByteArrayOutputStream ();
		x.beautify (-1)
		assert x.save(outp) : "Save failed"
		// println outp.toString()
	}
}
