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
package tdunnick.jphineas.sender;

import java.io.*;
import java.net.*;
import java.security.*;
import javax.net.ssl.*;

import tdunnick.jphineas.logging.Log;
import tdunnick.jphineas.xml.*;

/**
 * Set up a trust manager for the sender's certificate authority and creates
 * open sockets based on the protocol with read timeouts pre-set.
 * 
 * @author Thomas Dunnick
 *
 */
public class SenderSocketFactory
{
	/** cache the CA and trust managers */
	private static String certificateAuthority = "";
	private static TrustManager[] senderTrustManagers = null;

	/**
	 * This builds a set of trustmanagers for the given certificate authority
	 * @param name of the JKS authority file
	 * @param password for the authority
	 * @return trust managers or null if it fails
	 */
	 private static TrustManager[] getTrustManagers (File file, String password)
	 {
		 String name;
		 if (file == null) // check system properties for a default...
		 {
			 name = System.getProperty("javax.net.ssl.trustStore");
			 if (name != null)
				 file = new File (name);
			 password = System.getProperty("javax.net.ssl.trustStorePassword");
		 }
		 name = file.getAbsolutePath();
		 // sanity checks...
		 if ((file == null) || (password == null))
			 return null;
		 // if in our cache we are done
		 if (name.equals(certificateAuthority))
			 return senderTrustManagers;
		 // load the CA and create a set of trust managers
		 try
		 {
			 System.out.println ("CA " + name + " " + password);
		   KeyStore ks = KeyStore.getInstance ("JKS"); // assumes java keystore format
		   ks.load (new FileInputStream (file), password.toCharArray());
		   TrustManagerFactory tf = 
		  	 TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
		   tf.init(ks);		   
		   certificateAuthority = name;
		   return senderTrustManagers = tf.getTrustManagers();
		 }
		 catch (Exception e)
		 {
			 Log.error("Failed loading CA " + name, e);
			 return null;
		 }
	 }
	 
	 /**
	  * Open and return a socket for use by the sender.  This used the Sender/Route
	  * configuration to determine the host, etc. and set up SSL if needed.
	  * 
	  * @param config for this Sender's Route
	  * @return a socket ready to use or null if it fails
	  */
	 public static Socket createSocket (XmlConfig config)
	 {
		 String host = config.getValue ("Host");
		 String protocol = config.getValue("Protocol");
		 int port = config.getInt ("Port");
		 int timeout = config.getInt("Timeout") * 1000;
		 if ((host == null) || (protocol == null) || (port == 0))
		 {
			 Log.error("Route missing connection information (Host, Protocol, Port)");
			 return null;
		 }
		 try
		 {
			 if (protocol.equalsIgnoreCase("http"))
			 {
				 Socket socket = new Socket (host, port);
				 socket.setSoTimeout(timeout);
				 return socket;
			 }
			 // SSL - load up the CA trust managers
			 getTrustManagers (config.getFile ("TrustStore"), 
					 config.getValue("TrustStorePassword"));
			 // then build a key manager if client certification is used
			 KeyManager[] km = null;
			 String type = config.getValue("Authentication.Type");
			 if ((type != null) && type.equals("clientcert"))
			 {
				 File cert = config.getFile ("Authentication.Unc");
				 String password = config.getValue("Authentication.Password");
				 if ((cert == null) || (password == null))
				 {
					 Log.error("Route Authentication mis-configured");
					 return null;
				 }

				 KeyStore ks = KeyStore.getInstance("PKCS12"); // assumes PKCS format
				 InputStream kin = new FileInputStream (cert);
				 ks.load(kin, password.toCharArray());
				 kin.close ();
				 KeyManagerFactory kf = KeyManagerFactory.getInstance("SunX509");
				 kf.init(ks, password.toCharArray());
				 km = kf.getKeyManagers();
			 }
			 // now set up an SSL context
			 SSLContext ctx = SSLContext.getInstance("TLS");
			 ctx.init(km, senderTrustManagers, new SecureRandom ());
			 // and create the socket
			 SSLSocketFactory sf = ctx.getSocketFactory();
			 SSLSocket sock = (SSLSocket) sf.createSocket(host, port);
			 sock.setSoTimeout(timeout);
			 sock.startHandshake();
			 return sock;
		 }
		 catch (Exception e)
		 {
			 Log.error("Unable to connect to " + protocol + "://" + host + ":" + port
					 + " - " + e.getMessage());
			 return null;
		 }	 		 
	 }
}
