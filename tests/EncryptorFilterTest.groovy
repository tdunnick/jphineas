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
import tdunnick.jphineas.logging.*;
import tdunnick.jphineas.xml.*;
import tdunnick.jphineas.filter.*;
import tdunnick.jphineas.common.*
import tdunnick.jphineas.config.XmlConfig;

class EncryptorFilterTest extends GroovyTestCase
{
	String payloadName = "testpayload.xml"
	String derxml = "<Encryption><Unc>tests/test.der</Unc>" +
	"<Password></Password><Id></Id></Encryption>"
	String pfxxml = "<Encryption><Unc>tests/test.pfx</Unc>" +
	"<Password>changeit</Password></Encryption>"
	String dn = "CN=test.slh.wisc.edu, OU=WSLH, O=UW, L=Madison, ST=Wisconsin, C=US"
	String testdata = "The quick grey fox jumped over the lazy dogs"
	
	protected void setUp() throws Exception
	{
		/*
		LogConfig dflt = Log.getLogConfig();
		dflt.setLogLevel(LogConfig.DEBUG)
		dflt.setLogLocal(true)
		*/
		java.security.Security.addProvider (
			new org.bouncycastle.jce.provider.BouncyCastleProvider());
	}
	
	XmlConfig getcfg (String xml, String pre)
	{
		XmlConfig cfg = new XmlConfig ();
		cfg.load (xml);
		return (cfg.copy (pre));
	}

	public final void testEncryptionInputFilter ()
	{
		ByteArrayInputStream b_in = new ByteArrayInputStream (testdata.getBytes())
		EncryptorInputFilter c_in = new EncryptorInputFilter (b_in)
		assert c_in.configure(getcfg (derxml, "Encryption")) : "failed configuration"
	  // println new String (c_in.buf)
		FileOutputStream f_out = new FileOutputStream (payloadName)
		int c;
		while ((c = c_in.read()) >= 0)
			f_out.write ((byte) c);
		c_in.close()
		f_out.close ()		
	}

	public final void testEncryptionOutputFilter ()
	{
    FileOutputStream f_out = new FileOutputStream (payloadName + "2")
		EncryptorOutputFilter c_out = new EncryptorOutputFilter (f_out)
		c_out.configure (getcfg (derxml, "Encryption"))
		c_out.write(testdata.getBytes())
		c_out.close ()
		
	}
		
	public final void testDecryptionFilter ()
	{
		File f = new File (payloadName + "2");
		assert f.canRead() : "No encryption payload"
		InputStream f_in = new FileInputStream (f);
		DecryptorInputFilter d_in = new DecryptorInputFilter (f_in);
		assert d_in.configure (getcfg (pfxxml, "Encryption")) : "failed configuration"
		byte[] b = new byte[d_in.available()]
		d_in.read(b)
		d_in.close ();
		assert new String (b).equals (testdata) : "payload didn't match"
		f.delete ();
	}

	public final void testDecryptionOutputFilter ()
	{
		File f = new File (payloadName)
		InputStream f_in = new FileInputStream (f);
		byte[] b = new byte[f_in.available()]
		f_in.read (b);
		f_in.close ();
		ByteArrayOutputStream b_out = new ByteArrayOutputStream ();
		DecryptorOutputFilter d_out = new DecryptorOutputFilter(b_out)
		d_out.configure (getcfg (pfxxml, "Encryption"))
		d_out.write (b)
		d_out.close ()
		assert b_out.toString().equals(testdata) : "payload didn't match"
		f.delete()
	}

}
