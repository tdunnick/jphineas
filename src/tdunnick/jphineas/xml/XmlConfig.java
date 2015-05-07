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

package tdunnick.jphineas.xml;

import java.io.File;
import java.io.InputStream;
import org.w3c.dom.*;

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
  private Element prefix = null;  // prefix to element of interest
  private Element root = null;	// root of document
  private String dfltDir = null;

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
  	reset (file);
  	return true;
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
  	reset (null);
  	return true;
  }
  
  /**
   * re-set the root and default prefix elements for this configuration
   */
  public void reset (File file)
  {
   	prefix = root = getDoc().getDocumentElement();
  	// the default directory for file paths is parent of this file's folder
   	File d = null;
    dfltDir = getValue (prefix, DEFAULTDIR);
  	if ((dfltDir != null) && (dfltDir.length() > 0))
  	{
  		d = new File (dfltDir);
  		if (!d.isDirectory())
  			d = null;
  	}
  	if ((d == null) && (file != null))
    	d = file.getParentFile().getParentFile();
  	if ((d != null) && d.isDirectory())
  		dfltDir = d.getAbsolutePath() + "/";
  	else
  		dfltDir = "";
  }
    
  /**
   * Make a copy of this configuration with a new prefix.
   * If it start with current root, use as is.
   * If it is null use just the root.
   * Otherwise append it.
   * @param pre to use
   * @return a copy of the configuration
   */
  public XmlConfig copy (String pre)
  {
  	XmlConfig c = new XmlConfig ();
  	c.setDoc(getDoc ());
  	c.root = root;
  	if (pre == null)
  		c.prefix = root;
  	else if (pre.length() == 0)
  		c.prefix = prefix;
  	else
  	{
  		if (pre.startsWith(root.getTagName()))
	   		c.prefix = getElement(pre);
	  	else
	  		c.prefix = getElement(prefix, pre);
  	}
  	c.dfltDir = dfltDir;
  	return (c);
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
   * Set a value based on what is currently there.  The order is opposite of
   * get's...
   * If the name starts from the root, just set it.
   * If a matching value is currently at our root, then set it.
   * Otherwise set it at our prefix.
   * @param name of xml tag
   * @param value to store
   * @see tdunnick.jphineas.xml.XmlContent#setValue(java.lang.String, java.lang.String)
   */
  public boolean setValue (String name, String value)
  {
  	boolean ok;
  	if (name.startsWith(root.getTagName() + "."))
   		ok = super.setValue(name, value);
  	else if (getValue (root, name) != null)
  		ok = setValue(root, name, value);
  	else
  		ok = setValue(prefix, name, value);
  	return ok;
  }

  /**
   * return a string value of a node
   * first try name with prefix, then try name with root, then try name
   * converting empty string to null
   * @param tag name of node
   * @return the string value or null if no value found
   * @see tdunnick.jphineas.xml.XmlContent#getValue(java.lang.String)
   */
  public String getValue (String tag)
  {
		if (tag.endsWith(DEFAULTDIR))
			return dfltDir;
  	String s = null;
 		s = getValue (prefix, tag);
  	if (s == null)
  		s = getValue (root, tag);
    if (s == null)
    	s = super.getValue(tag);
    if ((s == null) || (s.length () == 0))
    	return null;
  	return s;
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
   * first try name with prefix, then try name with root, then try name
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
  	Element e = getElement (prefix, name);
  	if (e == null)
  	{
  		e = getElement(root, name);
  		if (e == null)
  			e = getElement(name);
  	}
  	if (e == null)
  		return null;
  	String s = e.getAttribute(attrib);
  	if ((s != null) && (s.length() > 0))
  	  return s;
  	return null;
  }
  
  /**
   * get a count of tags matching name
   * first try name with prefix, then try name with root, then try name
   * @param tag to match
   * @return number of matches
   * @see tdunnick.jphineas.xml.XmlContent#getTagCount(java.lang.String)
   */
  public int getTagCount (String tag)
  {
  	int n = 0;
 		n = getTagCount(prefix, tag);
  	if (n == 0)
  		n = getTagCount(root, tag);
  	if (n == 0)
  		n = super.getTagCount (tag);
  	return n;
  }

	/**
	 * Find an expected directory and return it's folder.  If it is not
	 * an absolute path, tack on the dfltDir prefix.
	 * 
	 * @param tag of folder tag
	 * @return the directory File or null if not a directory
	 */
	public File getFolder (String tag)
	{
  	String s = getValue (tag);
  	if (s == null)
  		return null;
  	File d = new File (s);
  	if (!(d.isDirectory() || d.isAbsolute()))
  		d = new File (dfltDir + s);
  	if (d.isDirectory() || !d.exists())
  		return d;
  	return null;
	}
	
	/**
	 * Force an expected directory and return it's path
	 * include trailing '/'
	 * 
	 * @param tag of folder tag
	 * @return the directory path
	 */
	public String getDirectory (String tag)
	{
  	File d = getFolder (tag);
  	if (d != null)
   	  return d.getAbsolutePath() + "/";
  	return "";
	}
	
	/**
	 * return a file object for this tag, prefixed with dfltDir if needed.
	 * @param tag to file
	 * @return the File or null if it doesn't exit
	 */
	public File getFile (String tag)
	{
		String s = getValue (tag);
		if (s == null)
			return null;
		File f = new File (s);
		if (!(f.isFile() || f.isAbsolute()))
			f = new File (dfltDir + s);
		if (!f.isFile())
			return null;
		return f;
	}
}
