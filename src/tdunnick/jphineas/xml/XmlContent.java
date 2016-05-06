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

package tdunnick.jphineas.xml;

import java.io.*;
import java.util.*;
import org.w3c.dom.*;

import javax.xml.parsers.*;
import javax.xml.transform.*; 
import javax.xml.transform.dom.DOMSource; 
import javax.xml.transform.stream.StreamResult;

/** 
 * A simplified narrow view of XML documents implemented using the DOM model.
 * Access is accomplished using (Properties like) name strings, where individual
 * XML tag names are separated by dots, and repeated names are accessed as arrays.  
 * For example consider...
 * <pre>
 * 
 * <foo>
 *   <bar>
 *     <junk>stuff</junk>
 *   </bar>
 *   <bar>
 *     <junk>crap</junk>
 *   </bar>
 *   <bar>
 *     <junk>nothing</junk>
 *   </bar>
 * </foo>
 * 
 * </pre>
 * In this case getValue("foo.bar[1].junk") would return "crap".  Note the
 * first name can never be indexed since it is the root.
 * 
 * "Values" are set and retrieved as text nodes, with only one logical "Value"
 * per names element path.
 * 
 * @author tld
 * 
 */
public class XmlContent
{
	private Document doc = null;
  private boolean beautify = false;
  protected Element root = null;
  protected String rootTag = null;

  /**
   * Gets the currently loaded document (for external use)
   * @return document
   */
  public Document getDoc()
	{
		return doc;
	}

	/**
	 * Sets the currently loaded document (for external use)
	 * @param doc to set
	 */
	public void setDoc(Document doc)
	{
		this.doc = doc;
		root = null;
		rootTag = null;
		beautify = false;
		if (doc != null)
			root = doc.getDocumentElement();
		if (root != null)
			rootTag = root.getTagName();
	}
	
	/**
	 * Create a document from scratch.
	 * @return true if successful
	 */
	public boolean createDoc ()
	{
		setDoc (null);
		try
		{
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			setDoc (builder.newDocument());
			return true;
		}
		catch (Exception e)
		{
			// Log.error("Failed loading XML: " + e.getMessage());
		}
		return false;
	}
	
	/**
   * Load an XML document from a stream. 
   * @param is stream to read from
   * @return true if successful
   */
  public boolean load (InputStream is)
  {
    setDoc (null);
  	if (is == null)
  		return false;
  	try
		{
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setNamespaceAware(true); // needed for digital signatures
			DocumentBuilder builder = factory.newDocumentBuilder();
			setDoc (builder.parse(is));
			return true;
		}
  	catch (Exception e)
  	{
  		// Log.error("Failed loading XML: " + e.getMessage());
  	}
  	return false;
  }
  
	/**
   * Load an XML document from a file.
   * @param f file to read from
   * @return true if successful
   */
  public boolean load (File f)
  {
  	if (f == null)
  		return false;
  	boolean ok = false;
  	try
  	{
  		FileInputStream is = new FileInputStream (f);
  	  ok = load (is);
  		is.close();
  	}
  	catch (IOException e)
  	{
  		// Log.error("Failed opening " + f.getPath() + ": " + e.getMessage());
  	}
  	return ok;
  }
  
  /**
   * Load an XML document from it's string representation
   * @param s with the document
   * @return the document
   */
  public boolean load (String s)
  {
  	if (s == null)
  		return false;
  	ByteArrayInputStream is = new ByteArrayInputStream (s.getBytes());
  	return load (is);
  }
  
  /**
   * Save an XML document to a stream
   * @param n node in the document
   * @param os stream to save to
   * @param fragment true if this is a fragment (no XML declaration)
   * @return true if successful
   */
  public boolean save (Node n, OutputStream os, boolean fragment)
	{
  	if ((doc == null) || (os == null))
  		return false;
  	try
		{
			Transformer tFormer = TransformerFactory.newInstance().newTransformer();
			// Output Types (text/xml/html)
			tFormer.setOutputProperty(OutputKeys.METHOD, "xml");
			if (beautify)
			  tFormer.setOutputProperty(OutputKeys.INDENT, "yes");
			if (fragment)
				tFormer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
			Source source = new DOMSource(n);
			Result result = new StreamResult(os);
			tFormer.transform(source, result);
			return true;
		}
		catch (Exception e)
		{
  		// Log.error("Failed saving XML: " + e.getMessage());
			// Log.error("Failed to save XML", e);
		}
		return false;
	}
  /**
   * Save an XML document to a stream
   * @param os stream to save to
   * @return true if successful
   */
   
  public boolean save (OutputStream os)
  {
  	return save (doc, os, false);
  }
  
  /**
   * Save an XML document to a file
   * @param f file to save to
   * @return true if successful
   */
  
  public boolean save (File f)
  {
  	boolean ok = false;
  	try
  	{
  		FileOutputStream os = new FileOutputStream (f);
  	  ok = save (os);
  		os.close();
  	}
  	catch (IOException e)
  	{
  		// Log.error("Failed opening " + f.getPath() + ": " + e.getMessage());
  	}
  	return ok;
  }
  
  /**
   * get the Element that is a parent of this node
   * @param n child node
   * @return parent element
   */
  protected Element getParentElement (Node n)
  {
  	if (n == null) return null;
  	while ((n = n.getParentNode ()) != null)
  	{
  		if (n.getNodeType() == Node.ELEMENT_NODE)
  			break;
  	}
  	return (Element) n;
  }
  
  /**
   * Finds a child in this element matching the name. 
   * 
   * @param e element to search
   * @param name of child to find
   * @return element matching or null if not found
   */
  private Element findElement (Element e, String name)
  {
  	if ((e == null) || (name == null) || (e.getNodeType() != Node.ELEMENT_NODE))
  		return null;
  	NodeList l = e.getChildNodes();
  	int count = 0;
  	int i = name.lastIndexOf('[');
  	if (i > 0)
  	{
  		count = Integer.parseInt(name.substring(i+1, name.length() - 1));
  		name = name.substring(0, i);
  	}
  	for (i = 0; i < l.getLength(); i++)
  	{
  		Node n = l.item(i);
  		if (n.getNodeType() != Node.ELEMENT_NODE)
  			continue;
  		if (n.getNodeName().equals(name) && (count-- < 1))
  			return (Element) n;
  	}
  	// System.out.println ("Couldn't match " + name + " (" + count + ")");
  	return null;
  }
  
  /**
   * Finds an element in the element subtree matching the names.  Empty
   * tags indicate a parent including names that start with a dot.
   * 
   * @param e element subtree
   * @param names to match
   * @return the matching element or null if not found
   */
  protected Element getElement (Element e, String names)
  {
  	if (names.length() == 0)
  	  return e;
		String[] name = names.split("[.]");
		
		for (int i = 0; (i < name.length) && (e != null); i++)
		{
			if (name[i].length() == 0)
				e = getParentElement (e);
			else
			  e = findElement (e, name[i]);
		}
		return e;  		
  }
  
  /**
   * Finds an element in the document matching the names.  Ignore redundent
   * "dots" in the name (e.g. "foo.bar.stuff" == "foo.bar..stuff..."
   * 
   * @param names to find
   * @return element for that tag
   */
  public Element getElement (String names)
  {
		if ((doc == null) || (names == null) || (names.length() == 0))
			return null;
		String rootName = names;
		int dot = names.indexOf(".");
		if (dot > 0)
		{
			rootName = names.substring(0, dot);
			names = names.substring (dot + 1);
	  }
	  else
	  	names = "";
		if (!rootName.equals(rootTag))
		{
			// System.out.println ("Root does not match " + names);
			return null;
		}
		return getElement (root, names);
  }
  
  /**
   * Adds an element name (and any needed siblings) to a given element.
   * and return it.
   * 
   * @param e element to add tag to 
   * @param name to add
   * @return element added
   */
  private Element addENode (Element e, String name)
	{
  	if (e == null)
  		return null;
		NodeList l = e.getChildNodes();
		Element el = null;
		int count = 0;
		int i = name.lastIndexOf('[');
		if (i > 0)
		{
			count = Integer.parseInt(name.substring(i + 1, name.length() - 1));
			name = name.substring(0, i);
		}
		for (i = 0; i < l.getLength(); i++)
		{
			Node n = l.item(i);
			if (n.getNodeType() != Node.ELEMENT_NODE)
				continue;
			if (n.getNodeName().equals(name))
				count--;
		}
		while (count-- >= 0)
		{
			el = doc.createElement(name);
			e.appendChild(el);
		}
		return el;
	}
  
  /**
   * Adds all the needed elements for names for an element subtree.  Ignore redundant
   * "dots" in the name (e.g. "foo.bar.stuff" == "foo.bar..stuff..."
   * @param e place in subtree
   * @param names to add
   * @return element for the added (or found) tag
   */
 
  private Element addElement (Element e, String names)
  {
  	if ((e == null) || (names == null))
  		return null;
		String[] name = names.split("[.]");
		int i;
		for (i = 0; i < name.length; i++)
		{
			if (name[i].length() == 0)
				continue;
			Element e2 = findElement (e, name[i]);
			if (e2 == null)
				break;
			e = e2;
		}
		while (i < name.length)
		{
			if (name[i].length() > 0)
				e = addENode (e, name[i]);
			i++;
		}
		return e;  		
  }
  
  /**
   * Adds all the needed elements for names.  Ignore redundant
   * "dots" in the name (e.g. "foo.bar.stuff" == "foo.bar..stuff..."
   * @param names to add
   * @return element for the added (or found) tag
   */
  private Element addElement (String names)
  {
		if ((names == null) || (names.length() == 0))
			return null;
		if ((doc == null) && !createDoc ())
			return null;
		String rootName = names;
		int dot = names.indexOf(".");
		if (dot > 0)
		{
			rootName = names.substring(0, dot);
			names = names.substring (dot + 1);
	  }
	  else
	  	names = "";
		if (root == null)
		{
		  root = doc.createElement(rootName);
			doc.appendChild(root);
			rootTag = rootName;
		}
		else if (!rootName.equals(rootTag))
		{
			// Log.debug ("Root does not match " + names);
			return null;
		}
		return addElement (root, names);
 }
    
  /**
   * Gets the text values for children of this node.
   * All text nodes for the named element are trimmed, concatenated by a 
   * space and returned as the value.
   * @param n node to use
   * @return text value or null if the node doesn't exist
   */
  protected String getValue (Node n)
  {
  	if (n == null)
  		return null;
    NodeList l = n.getChildNodes();
    String s = "";
    for (int i = 0; i < l.getLength(); i++)
    {
    	n = l.item(i);
    	if (n.getNodeType() == Node.TEXT_NODE)
     		s = s.trim () + " " + n.getNodeValue();
    }
    return s.trim ();  	
  }
  
  /**
   * Gets the "value" for names from an element subtree.  
   * All text nodes for the named element
   * are trimmed, concatenated by a space and returned as the value.
   * @param e element to search
   * @param names to match
   * @return value of name or null if not found
   */
  protected String getValue (Element e, String names)
  {
  	return getValue (getElement (e, names));
  }
  
  /**
   * Gets the "value" for names.  All text nodes for the named element
   * are trimmed, concatenated by a space and returned as the value.
   * @param names of value desired
   * @return value of name or null if not found
   */
  public String getValue (String names)
	{
    return getValue (getElement (names));
	}
  
  /**
   * Get node value as an integer.
   * @param names to match
   * @return value or 0 if not found
   */
  public int getInt (String names)
  {
  	return getInt (names, 0);
  }
  
  /**
   * Get a list of all the child names and values for this name
   * @param name of node with values
   * @return array of child node name/value pairs
   */
  public String[][] getChildren (String name)
  {
  	HashMap <String, Integer> names = new HashMap <String, Integer> ();
  	ArrayList <String[]> values = new ArrayList <String[]> ();
  	Node node = getElement (name);
  	if (node != null)
  		node = node.getFirstChild();
  	while (node != null)
  	{
  		if (node.getNodeType() == Node.ELEMENT_NODE)
  		{
  			String n = node.getNodeName ();
  			int i = 0;
  			if (names.containsKey(n))
  			  i = names.get(n).intValue() + 1;
  			names.put(n, new Integer (i));
  			if (i > 0) n += "[" + i + "]";
	  		String[] v = { n, getValue (node)};
	  		values.add (v);
  		}
  		node = node.getNextSibling();
  	}
  	return values.toArray(new String[0][]);
  }
  
  /**
   * Get node value as an integer.
   * @param names to match
   * @param dflt value if non-numeric or not found
   * @return value or dflt if not found
   */
  public int getInt (String names, int dflt)
  {
  	String s = getValue (names);
  	if ((s == null) || !s.matches("[0-9]+"))
  	  return dflt;
  	return Integer.parseInt(s);
  }

  private boolean setValue (Node e, String value)
  {
  	if (e == null)
  		return false;
    NodeList l = e.getChildNodes();
    String s = null;
    for (int i = 0; i < l.getLength(); i++)
    {
    	Node n = l.item(i);
    	if (n.getNodeType() == Node.TEXT_NODE)
    	{
    		e.removeChild(n);
    		i--;
    	}
    }
  	Node n = doc.createTextNode(value);
 		e.appendChild(n);
  	return true;
  }
  
  /**
   * Sets the "value" for names at element subtree.  The corresponding element has all of its
   * text nodes removed and replace by this single node value.  A null
   * value is set as an empty string.
   * 
   * @param e subtree
   * @param names desired to set
   * @param value to set
   * @return true if successful
   */
  protected boolean setValue (Element e, String names, String value)
  {
  	if (value == null)
  		value = "";
  	return setValue (addElement (e, names), value); 	
  }
  
 
  /**
   * Sets the "value" for names.  The corresponding element has all of its
   * text nodes removed and replace by this single node value.  A null
   * value is set as an empty string.
   * 
   * @param names desired to set
   * @param value to set
   * @return true if successful
   */
  public boolean setValue (String names, String value)
  {
  	if (value == null)
  		value = "";
  	return setValue (addElement (names), value);
  }
  
  /**
   * Delete all the children of a element at names.
   * 
   * @param names element to leave childless
   * @return true if successful
   */
  public boolean deleteChildren (String names)
  {
  	Element e = getElement (names);
  	if (e == null)
  		return false;
  	Node n = null;
  	while ((n = e.getFirstChild()) != null)
  		e.removeChild(n);
  	return true;
  }
  
  /**
   * Delete this element and all it's children
   * 
   * @param names element to delete
   * @return true if successful
   */
  public boolean delete (String names)
  {
  	Element e = getElement (names);
  	if (e == null)
  		return false;
  	Node n = e.getParentNode();
  	if (n == null)
  		return false;
  	n.removeChild(e);
  	return true;
  }
  
  /**
   * Get an attribute for names
   * @param names list of interest
   * @param attrib of attribute
   * @return attribute value of null if tag not found
   */
  public String getAttribute (String names, String attrib)
  {
  	if (attrib == null)
  		return null;
  	Element e = getElement (names);
  	if (e == null)
  		return null;
  	String a = e.getAttribute(attrib);
  	if (a.length() == 0) a = null;
  	return a;
  }
  
  /**
   * Set an attribute for names.  The element must exist.  A null value
   * is set as an empty string.
   * @param names list of interest
   * @param attrib of attribute to set
   * @param value of attribute to set
   * @return true if successful
   */
  public boolean setAttribute (String names, String attrib, String value)
  {
  	if (attrib == null)
  	  return false;
  	if (value == null)
  		value = "";
  	Element e = addElement (names);
  	if (e == null)
  		return false;
  	e.setAttribute(attrib, value);
  	return true;
  }
  
  /**
   * Get a count of elements matching names
   * 
   * @param e element subtree
   * @param names to count
   * @return number of matching elements
   */
  protected int getTagCount (Element e, String names)
  {
  	int cnt = 0;
  	while (getElement (e, names + "[" + cnt + "]") != null)
  		cnt++;
  	return cnt;
  }
 
  /**
   * Get a count of elements matching names
   * 
   * @param names to count
   * @return number of matching elements
   */
  public int getTagCount (String names)
  {
  	int cnt = 0;
  	while (getElement (names + "[" + cnt + "]") != null)
  		cnt++;
  	return cnt;
  }
  
  /**
   * Create an indent string for formatting
   * @param sz of then indentation in spaces
   * @return the indent text
   */
  private String getIndent (int sz)
  {
  	if (sz < 0)
  		return "";
  	StringBuffer buf = new StringBuffer("\n");
  	while (sz-- > 0)
  		buf.append(" ");
  	return buf.toString();
  }
  
  /**
   * Adjust the indent for child elements and format each to it's
   * own line. If the tabsz is < 0, run everything together.  Run
   * recursively over all children.  Note this could conceivably have an
   * uncaught DOMException
   * 
   * @param parent to beautify
   * @param tabsz to indent
   * @param level to indent
   */
  private void beautify (Node parent, int tabsz, int level)
  {
  	if (parent == null)
  		return;
  	// boolean needindent = true;
  	String indent = "";
  	if (tabsz > 0)
  		indent = getIndent (tabsz * level);
  	// Log.debug("beautifing element " + parent.getNodeName());
  	NodeList l = parent.getChildNodes();
  	// run through children 
		for (int i = 0; i < l.getLength(); i++)
		{			
			Node n = l.item(i);
			// if we are at a text node, trim it
			if (n.getNodeType() == Node.TEXT_NODE)
			{
				String s = n.getNodeValue().trim();
				// remove empty nodes
				if (s.length() == 0)
				{
					parent.removeChild(n);
					i--;
					continue;
				}
				n.setNodeValue(s);
				// if only one text node, we are done!
				if (l.getLength() == 1)
					return;
			}
			// add an indent node...
			Node tn = doc.createTextNode(indent);
			parent.insertBefore(tn, n);
			// get next node
			n = l.item(++i);
			// recurse for non-text nodes, beautify all the children
			if (n.getNodeType() != Node.TEXT_NODE)
				beautify(n, tabsz, level + 1);
		}
		if ((l.getLength () == 0) || (tabsz < 1))
			return;
		indent = getIndent (tabsz * --level);
		Node n = doc.createTextNode (indent);
		parent.appendChild(n);
  }
  
  /**
   * Adjust text nodes between tags that don't have "values" to help beautify
   * the resulting XML.  This assumes that only text nodes that are single
   * children actually have interesting data.  If the tabsz <= 0 run everything
   * together (packed XML).
   * 
   * @param tabsz size of indent for each tag set
   */
  public void beautify (int tabsz)
  {
  	if (doc == null)
  		return;
		beautify (root, tabsz, 1);
		doc.normalize();
		// note if transform indent needed in save above
		beautify = (tabsz > 0);
  }
  
  
  /**
   * get the String representation of the Element
   * @param n node in document
   * @param fragment true if this is a fragment (no XML declaration)
   * @return the formatted XML
   */
  public String toString (Node n, boolean fragment)
  {
  	ByteArrayOutputStream os = new ByteArrayOutputStream ();
  	if (save (n, os, fragment))
  		return os.toString();
  	return null;
  }
 
  /**
   * get the String representation of the named node
   * @param names node in document
   * @param fragment true if this is a fragment (no XML declaration)
   * @return the formatted XML
   */  
  public String toString (String names, boolean fragment)
  {
  	return toString (getElement (names), fragment);
  }
  
  /**
   * give the String representation of this XML
   * @return the formatted XML
   */
  public String toString ()
  {
  	return toString (doc, false);
  }
}
