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
import tdunnick.jphineas.mime.*;

class MimeContentTest extends GroovyTestCase
{
	MimeContent mime = new MimeContent ();
	
	def testbody = "The quick brown fox"
	def testmsg = MimeContent.CONTENT + ": " + MimeContent.TEXT +
	   "\n\n" + testbody
	
	protected void setUp() throws Exception
	{
		super.setUp();
	}
	
	protected void tearDown() throws Exception
	{
		super.tearDown();
	}


	public final void testContentType()
	{
		mime.setContentType (mime.XML);
		assert mime.getContentType().equals (mime.XML) : "XML content type not set"
	}

	public final void testParse()
	{
		assert mime.parse (testmsg) : "Parse failed"
		assert mime.getContentType().equals (MimeContent.TEXT) : "Wrong content type"
		assert mime.getBody ().equals(testbody) : "Wrong body '" + mime.getBody() + "'"
	}
	
	public final void testToString()
	{
		mime.setBody (testbody)
		assert mime.toString() != null : "String conversion failed"
		// println (mime.toString())
	}
	
	public final void testMultiPart()
	{
		MimeContent mp = new MimeContent();
		assert mp.setMultiPart() : "Failed setting multipart"
		assert mp.getContentType().startsWith (MimeContent.MULTIPART) : "Multipart content type not set"		
		assert mp.getBoundary() != null : "Failed getting boundary"
		mime.parse (testmsg)
		assert mp.addMultiPart(mime) : "Failed adding 1st part"
		assert mp.addMultiPart(mime) : "Failed adding 2ndt part"
		mime.setBody("failed")
		assert mp.addMultiPart(mime) : "Failed adding 3rd part"
		assert mp.setBody(testbody + " died")
		MimeContent[] parts = mp.getMultiParts()
		assert parts != null : "Failed getting parts"
		assert parts.length == 3 : "Wrong number of parts, got " + parts.length
		assert mp.getBody().equals(testbody + " died") : "Body doesn't match"
		assert parts[0].getBody().equals (testbody) : "First Body parts don't match"
		assert parts[2].getBody().equals("failed") : "Last body parts don't match"
	}
}
