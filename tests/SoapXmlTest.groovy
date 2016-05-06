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

import groovy.util.GroovyTestCase;
import tdunnick.jphineas.xml.*;

class SoapXmlTest extends GroovyTestCase
{
	String xml = "<soap-env:Envelope xmlns:SOAP=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:eb=\"http://www.oasis-open.org/committees/ebxml-msg/schema/msg-header-2_0.xsd\" xmlns:soap-env=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://schemas.xmlsoap.org/soap/envelope/ http://www.oasis-open.org/committees/ebxml-msg/schema/envelope.xsd\"><soap-env:Header><eb:MessageHeader soap-env:mustUnderstand=\"1\" eb:version=\"2.0\"><eb:From><eb:PartyId eb:type=\"zz\">2.16.840.1.114222.4.3.2.2.3.1.28</eb:PartyId></eb:From><eb:To><eb:PartyId eb:type=\"zz\">jPhineas</eb:PartyId></eb:To><eb:CPAId>jPhineas.2.16.840.1.114222.4.3.2.2.3.1.28</eb:CPAId><eb:ConversationId>1427375065656</eb:ConversationId><eb:Service eb:type=\"string\">defaultservice</eb:Service><eb:Action>defaultaction</eb:Action><eb:MessageData><eb:MessageId>1427375066000@shrek.eau.wi.charter.com</eb:MessageId><eb:Timestamp>2015-03-26T08:04:26</eb:Timestamp></eb:MessageData></eb:MessageHeader><eb:SyncReply soap-env:actor=\"http://schemas.xmlsoap.org/soap/actor/next\" soap-env:mustUnderstand=\"1\" eb:version=\"2.0\"></eb:SyncReply></soap-env:Header><soap-env:Body><eb:Manifest eb:version=\"2.0\"><eb:Reference xlink:href=\"cid:index-1.html.1427375055703@shrek.eau.wi.charter.com\" xlink:type=\"simple\"></eb:Reference><MetaData xmlns=\"http://www.cdc.gov/manifest/databaseinfo\" soap-env:mustUnderstand=\"0\"><DatabaseInfo soap-env:mustUnderstand=\"0\"><RecordId soap-env:mustUnderstand=\"0\">1194</RecordId><MessageId>FOLDERPOLLING-31efd986e718056937a652eda926680037dd80381427375055796</MessageId><Arguments></Arguments><MessageRecipient></MessageRecipient></DatabaseInfo></MetaData><ResponseChunking xmlns=\"http://www.cdc.gov/manifest/responsechunking\" soap-env:mustUnderstand=\"0\"></ResponseChunking><PeerVersion xmlns=\"http://www.cdc.gov/manifest/databaseinfo\">2.7.00</PeerVersion></eb:Manifest></soap-env:Body></soap-env:Envelope>"
  String metadata = "<Manifest><MetatData><DatabaseInfo><RecordId>1194</RecordId><MessageId>FOLDERPOLLING-31efd986e718056937a652eda926680037dd80381427375055796</MessageId><Arguments/><MessageRecipient/></DatabaseInfo></MetatData></Manifest>"
  void testGetMetaData ()
	{
		SoapXml s = new SoapXml (xml)
		String m = s.getMetaData ();
		assert m != null : "Failed getting metadata"
		assert m.equals (metadata) : "Expected meta data doesn't match - " + m
	}
}
