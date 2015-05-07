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

class XmlConfigTest extends GroovyTestCase
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
	
  XmlConfig xmlc = null;
	String logPattern = "%l %p: %m%n"
	String tag = "EncryptedData.KeyInfo.EncryptedKey.KeyInfo.KeyName"

 
	protected void setUp () throws Exception
	{
		xmlc = new XmlConfig ();
		xmlc.load (xml);
	}
	protected void tearDown() throws Exception
	{
		super.tearDown();
	}

	public final void testGetValue()
	{
		// println "'" + xmlc.getValue(tag) + "'"
		assert xmlc.getValue (tag).equals("key") : "tag value not retrieved"
	}

	public final void testSetValue ()
	{
		xmlc.setValue (tag, "foobar");
		assert xmlc.getString(tag).equals("foobar") : "tag value not set"
		xmlc.setValue (tag + "[3]", "the third")
		assert xmlc.getString(tag + "[3]").equals("the third") : "index value not set"
		assert xmlc.getString (tag + "[0]").equals("foobar") : "zero index corrupted"
		// println "'" + xmlc.getValue(tag + "[1]") + "'"
		assert xmlc.getValue(tag + "[1]") == null : "wrong length for empty tag"
		assert xmlc.getTagCount(tag) == 4 : "incorrect tag count"
	}
	
	public final void testAttribute ()
	{
	  assert xmlc.getAttribute ("KeyInfo", "xmlns") != null : "attribute Id not found"
	  assert xmlc.getAttribute ("KeyInfo", "xmlns").equals("http://www.w3.org/2000/09/xmldsig#") : "attribute Id didn't match"
		assert xmlc.getAttribute (tag, "id2") == null : "returned invalid attribute"
	}
	
	public final void testNS ()
	{
		String xname = "examples/soap_defaults.xml"
		String xtag = "soap-env:Header.eb:MessageHeader.eb:From.eb:PartyId"
		File f = new File (xname);
		xmlc.load (f);
		assert xmlc.getValue(xtag).equals("FROMPARTYID") : "didn't match value"
		assert xmlc.getAttribute(xtag, "eb:type").equals("zz") : "didn't match attribute"
	}
	
}
