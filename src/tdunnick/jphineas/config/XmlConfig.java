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

package tdunnick.jphineas.config;

import java.io.File;
import java.io.InputStream;
import org.w3c.dom.*;

import tdunnick.jphineas.logging.Log;
import tdunnick.jphineas.sender.FolderProcessor;
import tdunnick.jphineas.xml.XmlContent;

/**
 * A convenience class to support the jPhineas configurations.  This provides
 * short cuts for the XML paths and conversions for empty to null values. It
 * searches first from the prefix, then the root, and finally the full tag.
 * <p>
 * jPhineas is by and large fully driven by XML configurations read by the servlet
 * and then passed to various classes.  A class only needs to know it's own 
 * relevant tags since the prefix for that class is pre-set using a "copy".
 * However, the copy's all share the same underlying XmlContent.
 * <p>
 * The overall effect are:
 * <ol>
 * <li>Reduced parameter requirements - one XmlConfig has it all</li>
 * <li>Reduced memory requirements - the XML is shared by all, not copied</li>
 * <li>Simpler and flexible access - no need for root or prefixes</li>
 * <li>Consistency - no private copies means data changes are mirrored everywhere</li>
 * </ol>
 * 
 * @author Thomas Dunnick
 *
 */
public class XmlConfig extends XmlContent
{
  Element prefix = null;  // prefix to element of interest

	/** a default directory prefix for folders and files */
  public final static String DEFAULTDIR = "DefaultDirectory";
  
 	/**
   * Load an XML document from a file and set dfltDir
   * @param file to read from
   * @return true if successful
   * @see tdunnick.jphineas.xml.XmlContent#load(java.io.File)
   */
  public boolean load (File file)
  {
  	if (!super.load(file))
  		return false;
  	return init (file);
  }
	/**
   * Load an XML document from a stream and set the root and prefix elements.
   * @param is stream to read from
   * @return true if successful
   * @see tdunnick.jphineas.xml.XmlContent#load(java.io.InputStream)
   */
  public boolean load (InputStream is)
  {
  	if (!super.load (is))
  		return false;
  	return init (null);
  }
	/**
   * Load an XML document from a string and set the root and prefix elements.
   * @param s the document as text
   * @return true if successful
   * @see tdunnick.jphineas.xml.XmlContent#load(java.io.InputStream)
   */
  public boolean load (String s)
  {
  	if (!super.load (s))
  		return false;
  	return init (null);
  }
  
  /**
   * initialize a freshly loaded configuration
   * @param f source of configuration
   * @return true if successful
   */
  boolean init (File f)
  {
  	prefix = root;
  	if ((getValue (DEFAULTDIR) == null) && (f != null))
  	{
  		// System.out.println ("Setting default directory to grandparent of " + f.getAbsolutePath());
  		setDefaultDir (f.getAbsoluteFile().getParentFile().getParentFile());
  	}
   	return init ();
  }
  
  /**
   * basic initialization
   * @return false if no default directory is set
   */
  boolean init ()
  {
  	return true;
  }
  
  /**
   * Set the default directory to this one.  It must exist
   * @param f directory to default
   * @return if successful
   */
  public boolean setDefaultDir (File f)
  {
  	if ((f == null) || !f.isDirectory())
  		return false;
  	setValue (DEFAULTDIR, f.getAbsolutePath().replace('\\', '/') + "/");
  	return true;
  }
 
  /**
   * Find the element for this path searching either the root or prefix
   * depending on the path.
   * 
   * @param path to element of interest
   */
  public Element getElement (String path)
  {
  	if (path.startsWith (rootTag))
  		return super.getElement(path);
  	return getElement (prefix, path);
  }
  
  /**
   * Search for an element with a matching pre starting at prefix and working to root
   * 
   * @param pre of tags to look for
   * @return matching element or null if not found
   */
  Element find (String pre)
  {
  	if (pre.startsWith(rootTag))
  		return super.getElement (pre);
  	Element f, e = prefix;
  	while (e != null)
		{
		  if ((f = getElement (e, pre)) != null)
		  	return f;
		  e = getParentElement (e);
		}
  	Log.debug ("Can't find " + pre);
  	return null;
  }
  
  /**
   * Search for an element with matching prefix that has a "Name" tag with this name.
   * @param pre node prefix to search
   * @param name to match
   * @return the element matched or null if fails
   */
  Element search (String pre, String name)
	{
		if (pre == null)
			return null;
		int n = 0;
		Element f;
		while ((f = getElement (prefix, pre + "[" + n++ + "]")) != null)
		{
			String s = getValue (f, "Name");
			if ((s != null) && s.equals(name))
				return f;
		}
		// Log.debug ("pre=" + pre + " name=" + name + " not found!");
		return null;
	}
   
	/**
	* Copy this configuration to another one resetting the path/prefix
	* If it start with current root, use as is.
	* If it is null use just the root.
	* Otherwise append it.
	* @param c destination configuration
	* @param e prefix for the copy
	* @return a copy of the configuration
	*/

	public XmlConfig copy (XmlConfig c, Element e)
	{
		// System.out.println ("Making copy with default directory of " + findValue (DEFAULTDIR));
		c.setDoc (getDoc());
		if ((c.prefix = e) == null)
			c.prefix = root;
		if (!c.init ())
		{
			Log.debug("Failed initializing " + c.getClass().getName());
			return null;
		}
		return (c);
	}

  /**
   * Copy this configuration to another one resetting the prefix
   * If it start with current root, use as is. If it is null use just the root.
   * Otherwise copy from the current prefix.
	 * @param c destination configuration
   * @param pre to use
   * @return a copy of the configuration
   */

  public XmlConfig copy (XmlConfig c, String pre)
  {
  	if (pre == null)
  		return copy (c, (Element) null);
  	if (pre.length() == 0)
  		return copy (c, prefix);
  	Element e = getElement (pre);
  	if (e == null)
  	{
    	Log.debug ("Can't find " + pre);
  		return null;
  	}
  	return copy (c, e);
  }
  
  /**
   * Copy this configuration to a new one resetting the path/prefix
   * If it start with current root, use as is.
   * If it is null use just the root.
   * Otherwise append it.
   * 
   * @param pre
   * @return a copy of the configuration
   */
	public XmlConfig copy (String pre)
	{
		return copy (new XmlConfig (), pre);
	}

	/**
	 * Load a class with a prefix matching name.  The prefix normally has
	 * multiple entries, and must have a child with a "Name" tag.
	 * 
	 * @param pre to search
	 * @param name to match
	 * @return the class
	 */
	public Class <?> getClass (String pre, String name)
	{
		Element f = search (pre, name);
		if (f == null)
			return null;
		String cname = getValue (f, "Class");
		if (cname == null)
			return null;
  	try
		{
			return Class.forName(cname);
		}
		catch (ClassNotFoundException e)
		{
			Log.error("Class " + cname + " not found");
			return null;
		}
	}
	
	/**
	 * Get class instance with a prefix matching name.  The prefix normally has
	 * multiple entries, and must have a child with a "Name" tag.
	 * 
	 * @param pre to search
	 * @param name to match
	 * @return the instance
	 */
	public Object getInstance (String pre, String name)
	{
		Class <?> cf = getClass (pre, name);
		if (cf == null)
			return null;
		try
		{
			return cf.newInstance ();
		}
		catch (Exception e)
		{
			Log.error("Can't instantiate class for " + name + " - " + e.getMessage());
			return null;
		}
	}

  /**
   * return the integer value of a node.
   * first try name with prefix, then try name with root, then try name
   * @param tag of node
   * @return value or 0 if not found or not an integer
   * @see tdunnick.jphineas.xml.XmlContent#getInt(java.lang.String)
   */
  public int getInt (String tag)
  {
  	String s = getValue (tag);
  	if ((s == null) || !s.matches ("[0-9]+"))
  			return 0;
  	return Integer.parseInt(s);  	
  }
  
  /**
   * Find a tag and return its integer value.  Search from this prefix back to root.
   * @param tag to find.
   * @param dflt value if not found
   * @return the value or dflt if not found
   */
  public int findInt (String tag, int dflt)
  {
  	String s = findValue (tag);
  	if ((s == null) || !s.matches ("[0-9]+"))
  			return dflt;
  	return Integer.parseInt(s);  	
  }
  

  /**
   * Set a value based on what is currently there.  
   * If the name starts from the root, just set it.
   * Otherwise set it at our prefix.
   * @param name of xml tag
   * @param value to store
   * @see tdunnick.jphineas.xml.XmlContent#setValue(java.lang.String, java.lang.String)
   */
  public boolean setValue (String name, String value)
  {
  	boolean ok;
  	if ((root == null) || name.startsWith(rootTag))
   		ok = super.setValue(name, value);
   	else
  		ok = setValue(prefix, name, value);
  	return ok;
  }

  /**
   * Return a string value of a node. The special tag for the default 
   * directory is handled separately
   * 
   * @param tag name of node
   * @return the string value or null if no value found
   * @see tdunnick.jphineas.xml.XmlContent#getValue(java.lang.String)
   */
  public String getValue (String tag)
  {
		String s = getValue (getElement (tag));
		if ((s != null) && (s.length() > 0))
		  return s;
		// Log.debug("Nothing found at " + tag);
		return null;
  }
  
  /**
   * Find a tag and return its value.  Search from this prefix back to root.
   * Empty tags are ignored. Tag must have a value!
   * @param tag to find.
   * @return the value or null if not found
   */
  public String findValue (String tag)
  {
  	String s = getValue (tag);
  	if (s != null)
  		return s;
   	Element f, e = prefix;
  	while (e != null)
		{
		  if (((s = getValue (e, tag)) != null) && (s.length() > 0))
		  	return s;
		  e = getParentElement (e);
		}
  	Log.debug ("Can't find " + tag);
  	return null;
  }
  
  /**
   * return a string value of a node
   * first try name with prefix, then try name with root, then try name
   * converting null to empty string
   * @param tag name of node
   * @return the string value
   */
  public String getString (String tag)
  {
  	String s = getValue (tag);
  	if (s == null) return "";
  	return s;
  }
  
  /**
   * return a string attribute of a node
   * first try name with prefix, then try parent nodes to the root
   * converting empty string to null
   * @param name of node
   * @param attrib to fetch
   * @return value if found
   * @see tdunnick.jphineas.xml.XmlContent#getAttribute(java.lang.String, java.lang.String)
   */
  public String getAttribute (String name, String attrib)
  {
   	if (attrib == null)
  		return null;
  	Element e = getElement (name);
  	if (e == null)
  		return null;
  	String s = e.getAttribute (attrib);
  	if ((s != null) && (s.length() > 0))
  	  return s;
  	return null;
  }
  
  /**
   * get a count of tags matching name
   * first try name with prefix, then try parent nodes to the root
   * @param tag to match
   * @return number of matches
   * @see tdunnick.jphineas.xml.XmlContent#getTagCount(java.lang.String)
   */
  public int getTagCount (String tag)
  {
  	if (tag == null)
  		return 0;
  	Element e = getParentElement (getElement (tag));
  	if (e == null)
  		return 0;  	 	
  	int i = tag.lastIndexOf('.');
  	if (i > 0)
  		tag = tag.substring(i + 1);
  	return getTagCount (e, tag);
  }

  /**
   * get a count of tags for this prefix matching tag
   * @param tag to match
   * @return count of matches
   */
  public int getPrefixCount (String tag)
  {
  	return getTagCount (prefix, tag);
  }
  
  /**
   * Return the full path to this directory, forcing it if need be
   * @param tag with path to directory
   * @return the path
   */
  public String getDirectory (String tag)
  {
  	File f = getFolder (tag);
  	if (f == null)
  		return null;
  	return f.getAbsolutePath() + "/";
  }
  
	/**
	 * return a directory file object for this tag, prefixed with dfltDir if needed.
	 * create the directory if it doesn't already exist
	 * 
	 * @param tag of folder tag
	 * @return the directory File or null if not a directory
	 */
	public File getFolder (String tag)
	{
  	String s = findValue (tag);
  	if (s == null)
  		return null;
  	File d = new File (s);
  	if (!(d.isDirectory() || d.isAbsolute()))
  	{
  		String dir = findValue (DEFAULTDIR);
  		// System.out.println ("Found default folder " + dir);
  		if (dir == null)
  			return null;
   		d = new File (dir + s);		
  	}
   	if (!d.exists ())
  		d.mkdirs();
   	if (d.isDirectory())
  		return d;
  	return null;
	}
	
	/**
	 * Force a folder to exist, using a default value if needed
	 * @param tag for the folder
	 * @param dflt value if not set
	 * @return the folder
	 */
	File forceFolder (String tag, String dflt)
	{
		// System.out.println ("Forcing " + tag + " to " + dflt);
		if (findValue (tag) == null)
			setValue (tag, dflt);
		return getFolder (tag);
	}
	
	/**
	 * return a file object for this tag, prefixed with dfltDir if needed.
	 * @param tag to file
	 * @return the File or null if it is not readable
	 */
	public File getFile (String tag)
	{
		String s = findValue (tag);
		if (s == null)
			return null;
		File f = new File (s);
		if (!(f.canRead() || f.isAbsolute()))
			f = new File (findValue (DEFAULTDIR) + s);
		if (!f.canRead ())
			return null;
		return f;
	}
	
}
