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

import java.io.File;

import groovy.util.GroovyTestCase;
import tdunnick.jphineas.logging.*;
import tdunnick.jphineas.xml.XmlContent;
import tdunnick.jphineas.encryption.*;

class SignXMLTest extends GroovyTestCase
{
	String ksname = "tests/test.pfx"
	String kspass = "changeit"
	String certname = "tests/test.der"
	String dn = "CN=test.slh.wisc.edu, OU=WSLH, O=UW, L=Madison, ST=Wisconsin, C=US"
		
	public final void testSign ()
	{
		assert signit (null) : "failed signing document"
		assert signit ("acct") : "failed signing id 'acct'"
		assert signit ("/PatientRecord/Account") : "failed signing path '/PatientRecord/Account'"
	}
	
	public final void testVerify ()
	{
		File f = new File ("tests/SignFile.xml");
		XmlContent xml = new XmlContent ();
		assert xml.load (f) : "failed loading " + f.getAbsolutePath();
		assert !SignXML.verify (xml.getDoc()) : "Unsigned document was verified!"
		f  = new File ("tests/SignFile_path.xml");
		assert xml.load (f) : "failed loading " + f.getAbsolutePath();
		assert SignXML.verify (xml.getDoc()) : "Signed document was not verified!"
	}
	
	boolean signit (String path)
	{
		File f = new File ("tests/SignFile.xml");
		XmlContent xml = new XmlContent ();
		assert xml.load (f) : "failed loading " + f.getAbsolutePath();
		// println (xml.toString());
		String s = SignXML.sign (xml.getDoc(), path, ksname, kspass);
		if (s == null)
		  return false;
		xml.load (s);
		return (SignXML.verify (s) && SignXML.verify (xml.getDoc()))
	}
}
