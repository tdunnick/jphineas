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

import java.io.*;
import groovy.util.GroovyTestCase;
import tdunnick.jphineas.mime.*;
import tdunnick.jphineas.logging.*;

class MimeReceiverTest extends GroovyTestCase
{
	String testbody = "The quick brown fox"
	String testmsg = MimeContent.CONTENT + ": " + MimeContent.TEXT +
		   "\n\n" + testbody
		
	 protected void setUp() throws Exception
	 {
		 LogConfig dflt = Log.getLogConfig();
		 dflt.setLogLevel(LogConfig.DEBUG)
		 dflt.setLogLocal(true)
		 dflt.setLogStream (null);
	 }
		 
	public final void testReceive()
	{
		MimeContent m = new MimeContent ()
		m.parse (testmsg);
		String s = m.toString().replace("\n", "\r\n")
    // println ("'" + s + "'")
		ByteArrayInputStream is = new ByteArrayInputStream (s.getBytes());
	  MimeContent m2 = MimeReceiver.receive (is)
		assert m2 != null : "Parse failed"
		assert m2.getBody ().equals (testbody) : "Body '" + m2.getBody() + "' doesn't match"	
		m2.reset();
		m2.setMultiPart();
		m2.addMultiPart (m);
		m2.addMultiPart (m);
		m2.addMultiPart (m);
		s = m2.toString().replace("\n", "\r\n")
		is = new ByteArrayInputStream (s.getBytes())
		m2 = MimeReceiver.receive (is)
		assert m2 != null : "Multipart Parse failed"
		assert m2.getMultiParts() != null : "Failed parsing multiparts " + m2.toString()
		assert m2.getMultiParts().length == 3 : "Incorrect number of multiparts"
		assert m2.getMultiParts()[2].getBody().equals(testbody) : "Multipart body '" + m2.getMultiParts()[2].getBody() + "'does't match"
	}
}
