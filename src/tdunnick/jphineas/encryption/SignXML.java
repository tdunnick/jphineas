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

package tdunnick.jphineas.encryption;

import java.io.*;
import java.util.*;
import java.security.*;
import javax.xml.crypto.*;
import javax.xml.crypto.dsig.*;
import javax.xml.crypto.dsig.dom.*;
import javax.xml.crypto.dsig.keyinfo.*;
import javax.xml.crypto.dsig.spec.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.*;
import org.w3c.dom.*;

/**
 * A couple of static functions to sign and verify XML documents.  Currently this
 * supports whole document or part identified by an Xpath ID or PATH string.
 * 
 * Note that once a document gets signed, it may not be changed in anyway or it
 * will fail to verify.  Also, it is well understood that XML DSIG is pretty
 * broken and not all that secure, but PHINMS supports it.  We'd be better off
 * with a Mime signature.
 * 
 * Yeah, this is pretty derivative... thank you Internet!
 * 
 * @author Thomas Dunnick tdunnick@wisc.edu
 *
 */
public class SignXML
{
	/**
	 * digitally sign an XML document 
	 * 
	 * @param doc to sign
	 * @param sigType how to sign, null for whole document, ID, or path to section
	 * @param path to keystore used to sign
	 * @param passwd for keystore
	 * @return signed document as a string
	 */
	public static String sign (Document doc, String sigType, String path, String passwd)
	{
		try
		{
			StringBuffer dn = new StringBuffer ();
			// Retrieve signing key
			Encryptor crypt = new Encryptor();
			KeyStore keyStore = crypt.getKeyStore(path, passwd);
			PrivateKey privateKey = (PrivateKey) crypt.getPrivateKey(keyStore, passwd, dn);
			PublicKey publicKey = (PublicKey) crypt.getPublicKey (keyStore, dn);
			return sign (doc, sigType, publicKey, privateKey);
		}
		catch (Exception e)
		{
			e.printStackTrace(System.err);
			return null;
		}
	}

	/**
	 * digitally sign an XML document - as a side effect update the document
	 * @param doc
	 * @param sigNode
	 * @param publicKey
	 * @param privateKey
	 * @return signed document as a string
	 * @throws Exception
	 */
	public static String sign (Document doc, String sigNode, PublicKey publicKey, PrivateKey privateKey) 
			throws Exception
	{
		// prepare signature factory
		String providerName = System.getProperty("jsr105Provider", "org.jcp.xml.dsig.internal.dom.XMLDSigRI");
		XMLSignatureFactory sigFactory = 
			XMLSignatureFactory.getInstance ("DOM", (Provider) Class.forName(providerName).newInstance());

		Node nodeToSign = null;
		Node sigParent = null;
		String referenceURI = null;
		XPathExpression expr = null;
		NodeList nodes;
		List <Transform> transforms = null;

		XPathFactory factory = XPathFactory.newInstance();
		XPath xpath = factory.newXPath();
		
		if ((sigNode == null) || (sigNode.length() == 0))
		{
			// signing the whole document
			// System.out.println ("Signing whole document...");
			sigParent = doc.getDocumentElement();
			referenceURI = ""; // Empty string means whole document
			transforms = Collections.singletonList (sigFactory.newTransform (Transform.ENVELOPED, 
				(TransformParameterSpec) null));
		}
		else if (sigNode.startsWith ("/"))
		{
			// signing node designated by XML path
			// System.out.println ("Signing path " + sigNode + "...");
			expr = xpath.compile(sigNode);
			nodes = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
			if (nodes.getLength() < 1)
			{
				// System.out.println("Invalid document, can't find node by PATH: "	+ sigNode);
				return null;
			}

			nodeToSign = nodes.item(0);
			sigParent = nodeToSign.getParentNode();
			referenceURI = ""; // Empty string means whole document
			transforms = new ArrayList<Transform>();
			transforms.add (sigFactory.newTransform(Transform.XPATH,	new XPathFilterParameterSpec(sigNode)));
			transforms.add (sigFactory.newTransform(Transform.ENVELOPED,	(TransformParameterSpec) null));
		}
		else
		{
			// sign node identified by this ID
			// System.out.println ("Signing id " + sigNode + "...");
			expr = xpath.compile(String.format("//*[@id='%s']", sigNode));
			nodes = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
			if (nodes.getLength() == 0)
			{
				// System.out.println("Can't find node with id: " + sigNode);
				return null;
			}

			nodeToSign = nodes.item(0);
			sigParent = nodeToSign.getParentNode();
			referenceURI = "#" + sigNode;
			/*
			 * This is not needed since the signature is alongside the signed
			 * element, not enclosed in it. transforms = Collections.singletonList(
			 * sigFactory.newTransform( Transform.ENVELOPED,
			 * (TransformParameterSpec) null ) );
			 */
		}
		// Create a Reference to the enveloped document
		Reference ref = sigFactory.newReference(referenceURI,
				sigFactory.newDigestMethod(DigestMethod.SHA1, null), transforms, null, null);

		// Create the SignedInfo
		SignedInfo signedInfo = sigFactory.newSignedInfo (sigFactory.newCanonicalizationMethod (
			CanonicalizationMethod.INCLUSIVE_WITH_COMMENTS,	(C14NMethodParameterSpec) null), 
			sigFactory.newSignatureMethod (SignatureMethod.RSA_SHA1, null), Collections.singletonList(ref));

		// Create a KeyValue containing the RSA PublicKey
		KeyInfoFactory keyInfoFactory = sigFactory.getKeyInfoFactory();
		KeyValue keyValue = keyInfoFactory.newKeyValue(publicKey);

		// Create a KeyInfo and add the KeyValue to it
		KeyInfo keyInfo = keyInfoFactory.newKeyInfo(Collections.singletonList(keyValue));

		// Create a DOMSignContext and specify the RSA PrivateKey and
		// location of the resulting XMLSignature's parent element
		DOMSignContext dsc = new DOMSignContext(privateKey, sigParent);

		// Create the XMLSignature (but don't sign it yet)
		XMLSignature signature = sigFactory.newXMLSignature (signedInfo, keyInfo);

		// Marshal, generate (and sign) the enveloped signature
		signature.sign (dsc);

		// output the resulting document
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		/* this requires W3C...
		String w3cFactory = "com.sun.org.apache.xalan.internal.xsltc.trax.TransformerFactoryImpl";
		System.setProperty("javax.xml.transform.TransformerFactory", w3cFactory);
		*/
		Transformer trans = TransformerFactory.newInstance().newTransformer ();
		trans.transform (new DOMSource(doc), new StreamResult(os));	
		return new String (os.toByteArray());
	}
	
	/**
	 * Validate a signed XML document
	 * @param xml to verify
	 * @return true if it verified
	 */
	public static boolean verify (String xml)
	{
  	try
		{
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setNamespaceAware(true); // needed for digital signatures
			return verify (factory.newDocumentBuilder().parse(new ByteArrayInputStream (xml.getBytes())));
		}
  	catch (Exception e)
  	{
  		// Log.error("Failed loading XML: " + e.getMessage());
  	}
  	return false;
	}
	
	/**
	 * Validate a signed XML document
	 * @param doc to verify
	 * @return true if it verified
	 */
	public static boolean verify (Document doc)
	{
		if (doc == null)
			return false;
		// Find Signature element
		NodeList nl = doc.getElementsByTagNameNS(XMLSignature.XMLNS, "Signature");
		if (nl.getLength() == 0)
		{
			// System.err.println ("Cannot find Signature element");
			return false;
		}
		try
		{
			// Create a DOM XMLSignatureFactory that will be used to unmarshal the
			// document containing the XMLSignature
			String providerName = System.getProperty("jsr105Provider",
					"org.jcp.xml.dsig.internal.dom.XMLDSigRI");
			XMLSignatureFactory fac = XMLSignatureFactory.getInstance("DOM",
					(Provider) Class.forName (providerName).newInstance ());
	
			// check all the signatures...
			for (int i = 0; i < nl.getLength(); i++)
			{
				// Create a DOMValidateContext and specify a KeyValue KeySelector
				// and document context
				DOMValidateContext valContext = new DOMValidateContext (new KeyValueKeySelector(), nl.item(i));
	
				// unmarshal the XMLSignature
				XMLSignature signature = fac.unmarshalXMLSignature (valContext);
	
				// try a validation
				if (!signature.validate (valContext))
				{
					/*
					System.out.println(String.format("Signature %s failed core validation", i));					
					boolean sv = signature.getSignatureValue().validate(valContext);
					System.out.println(String.format("Signature %s validation status: %s", i, sv));
					// check the validation status of each Reference
					Iterator <?> it = signature.getSignedInfo().getReferences().iterator();
					for (int j = 0; it.hasNext(); j++)
					{
						boolean refValid = ((Reference) it.next()).validate(valContext);
						System.out.println(String.format("Signature %s ref['%s'] validity status: %s", i, j, refValid));
					}
					*/
					return false;
				}
				// System.out.println(String.format("Signature %s passed core validation", i));				
			}	
			return true;
		}
		catch (Exception e)
		{
			e.printStackTrace(System.err);
			return false;
		}
	}
	
	/**
	 * KeySelector which retrieves the public key out of the KeyValue element and
	 * returns it. NOTE: If the key algorithm doesn't match signature algorithm,
	 * then the public key will be ignored.
	 */
	private static class KeyValueKeySelector extends KeySelector
	{
		public KeySelectorResult select(KeyInfo keyInfo,
				KeySelector.Purpose purpose, AlgorithmMethod method,
				XMLCryptoContext context) throws KeySelectorException
		{
			if (keyInfo == null)
			{
				throw new KeySelectorException("Null KeyInfo object!");
			}
			SignatureMethod sm = (SignatureMethod) method;
			List <?> list = keyInfo.getContent();

			for (int i = 0; i < list.size(); i++)
			{
				XMLStructure xmlStructure = (XMLStructure) list.get(i);
				if (xmlStructure instanceof KeyValue)
				{
					PublicKey pk = null;
					try
					{
						pk = ((KeyValue) xmlStructure).getPublicKey();
					}
					catch (KeyException ke)
					{
						throw new KeySelectorException(ke);
					}

					// make sure algorithm is compatible with method
					if (algEquals(sm.getAlgorithm(), pk.getAlgorithm()))
					{
						return new SimpleKeySelectorResult(pk);
					}
				}
			}
			throw new KeySelectorException("No KeyValue element found!");
		}

		// @@@FIXME: this should also work for key types other than DSA/RSA
		static boolean algEquals(String algURI, String algName)
		{
			if (algName.equalsIgnoreCase("DSA")
					&& algURI.equalsIgnoreCase(SignatureMethod.DSA_SHA1))
			{
				return true;
			}
			else if (algName.equalsIgnoreCase("RSA")
					&& algURI.equalsIgnoreCase(SignatureMethod.RSA_SHA1))
			{
				return true;
			}
			else
			{
				return false;
			}
		}
	}

	private static class SimpleKeySelectorResult implements KeySelectorResult
	{
		private PublicKey pk;

		SimpleKeySelectorResult(PublicKey pk)
		{
			this.pk = pk;
		}
		public Key getKey()
		{
			return pk;
		}
	}	
}
