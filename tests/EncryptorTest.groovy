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
import tdunnick.jphineas.logging.*;
import tdunnick.jphineas.encryption.*;

class EncryptorTest extends GroovyTestCase
{
	Encryptor crypt;
	String ksname = "tests/test.pfx"
	String kspass = "changeit"
	String certname = "tests/test.der"
	String dn = "CN=test.slh.wisc.edu, OU=WSLH, O=UW, L=Madison, ST=Wisconsin, C=US"
	String testdata = "The quick grey fox jumped over the lazy dogs"
	
	protected void setUp() throws Exception
	{
		/*
		LogConfig dflt = Log.getLogConfig();
		dflt.setLogLevel(LogConfig.DEBUG)
		dflt.setLogLocal(true)
		*/
		crypt = new Encryptor ();
	}
	public final void testGetKeyStore ()
	{
		def k = crypt.getKeyStore (ksname, kspass)
		assert k != null : "Failed to load " + ksname
	}
	
	public final void testGetAlias ()
	{
		def ks = crypt.getKeyStore (ksname, kspass)
		String alias = crypt.getAlias (ks, null)
		assert alias != null : "Failed to get alias for null DN"
		alias = crypt.getAlias (ks, new StringBuffer (dn))
		assert alias != null : "Failed to get alias for " + dn
	}
	
	public final void testGetPrivateKey ()
	{
		def key = crypt.getPrivateKey (ksname, kspass, null)
		assert key != null : "Failed to get private key for null DN"
	  key = crypt.getPrivateKey (ksname, kspass, new StringBuffer (dn))
		assert key != null : "Failed to get private key for " + dn
	}
	
	public final void testGetPublicKey ()
	{
		def pdn = new StringBuffer()
		def key = crypt.getKeyStoreKey (ksname, kspass, null)
		assert key != null : "Failed to get public key for null DN"
	  key = crypt.getKeyStoreKey (ksname, kspass, new StringBuffer (dn))
		assert key != null : "Failed to get public key for " + dn
	  key = crypt.getDerKey (certname, pdn)
		assert key != null : "Failed to get public key from " + certname
		assert pdn.toString().equals(dn) : "DN didn't match " + pdn.toString()
	}
	
	public final void testGetLdapKey ()
	{
		def pdn = new StringBuffer ()
		def key = crypt.getLdapKey ("directory.verisign.com:389", 
  			"O=Centers for Disease Control and Prevention", "cn=cdc phinms", pdn)
  	assert key != null : "Failed to get LDAP public key for cdc phinms"
  	// println pdn.toString()
	}
	
	public final void testEncrypt ()
	{
		def key = crypt.getKeyStoreKey (ksname, kspass, null)
		def data = crypt.encrypt (testdata.getBytes(), key)
		assert data != null : "Failed encryption"		
	}
	
	public final void testDecrypt ()
	{
		def key = crypt.getDerKey (certname, null)
		String data = crypt.encrypt (testdata.getBytes(), key)
		key = crypt.getPrivateKey (ksname, kspass, null)
		byte[] ds = crypt.decrypt (data, key)
		assert ds != null : "Failed RSA decryption"
		assert new String(ds).equals(testdata) : "RSA Decryption " + new String(ds) + " didn't match"
		key = crypt.generateDESKey();
		data = crypt.encrypt (testdata.getBytes(), key)
		ds = crypt.decrypt (data, key)
		assert ds != null : "Failed DES decryption"
		assert new String(ds).equals(testdata) : "DES Decryption " + new String(ds) + " didn't match"
	}
}
