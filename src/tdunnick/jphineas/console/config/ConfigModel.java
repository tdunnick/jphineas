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

package tdunnick.jphineas.console.config;

import java.io.*;
import java.util.*;
import java.text.*;
import javax.servlet.http.HttpServletRequest;

import tdunnick.jphineas.config.XmlConfig;
import tdunnick.jphineas.logging.*;
import tdunnick.jphineas.xml.*;

/**
 * Creates, manages, and updates jPhineas configurations as requested by the console.
 * 
 * @author Thomas Dunnick
 *
 */
public class ConfigModel
{
	XmlConfig 
	  config = null,
	  master = null;
	File 
	  masterFile = null,
	  revisions = null;
	SimpleDateFormat dirfmt = new SimpleDateFormat ("yyyyMMddHHmmss");
	SimpleDateFormat selfmt = new SimpleDateFormat ("MMM dd, yyyy HH:mm:ss");
	
  /**
   * Read and load the configurations managed by the console.
   * @param masterName for the console.
   * @return true if successful
   */
  public boolean initialize (String masterName)
  {
  	masterFile = new File (masterName);
  	reload (master = new XmlConfig (), masterFile, null);
	 	// our config (Console) is NOT configurable... we load it ONCE here
	 	reload (config = new XmlConfig (), master.getFile ("Console"), null);
  	revisions = config.getFolder("Revisions");
  	return true;
  }
  
  /**
   * Reloads a configuration from any directory and reset to original location
   * @param x configuration to reload
   * @param c original location
   * @param dir alternate directory
   * @return true if successful
   */
  private boolean reload (XmlConfig x, File c, String dir)
  {
  	x.createDoc();
  	if (c == null)
  	{
  		Log.warn("no configuration specified");
  		return false;
  	}
  	File f = c;
  	if (dir != null)
  		f = new File (dir + c.getName());
  	if (!f.exists())
  	{
  		Log.error (f.getAbsolutePath() + " not found");
  		return false;
  	}
  	if (!x.load(f))
		{
  		Log.error ("Couldn't load " + c.getAbsolutePath());
  		return false;
		}
  	x.setDefaultDir (c);
  	// Log.debug("Reloaded " + c.getAbsolutePath());
  	return true;
  }
  
  /**
   * reload our configurations 
   * @param dir optional directory
   * @return true if successful
   */
  private boolean reload (String dir)
  {
		reload (master, masterFile, dir);
  	return true; 	
  }
  
  /**
   * Find the configuration for a tag (prefix)
   * @param tags for this configuration
   * @return the configuration.
   */
  private XmlConfig getConfig (String tags)
  {
  	/* for separate configurations...
  	if (tags.startsWith("jPhineas"))
  		return master;
  	if (tags.startsWith("Receiver"))
  		return master.copy(new XmlConfig(), "Receiver");
  	if (tags.startsWith("Sender"))
  		return master.copy (new XmlConfig(), "Sender");
  	return master.copy (new XmlConfig(), "Queues");
  	*/
  	return master;
  }
  
  /**
   * Add a tag to a prefix, replacing spaces with dots.
   * 
   * @param x configuration having tag to add
   * @param prefix to add it to
   * @return new prefix
   */
  private String addTag (XmlConfig x, String prefix)
  {
  	String t = x.getValue("Tags");
  	if (t == null)
  		return prefix;
  	t = t.replace(' ', '.');
  	if (prefix == null)
  		return t;
  	return prefix + "." + t;
  }
  
  /**
   * Get the help string for this configuration.  Reformat it for HTML use.
   * @param c configuration holding the help
   * @return HTML help string
   */
  private String getHelp (XmlConfig c)
  {
  	String h = c.getValue("Help");
  	if (h == null)
  		return "";
  	return h.replaceAll("(?s)[\\s\n\r]+"," ").replace("\"", "&quot;").replace("'", "\\'");
  }
  
  /**
   * Set the value for an input
   * @param ic the input record
   * @param prefix XML tag to the record data
   * @return true if successful
   */
  private boolean setValue (ConfigInput ic, String prefix)
  {
  	if (prefix == null)
  	{
  		if (ic.options != null)
  			ic.value = ic.options.get(0);
  		return true;
  	}
  	XmlConfig x = getConfig (prefix);
  	String tag = prefix + "." + ic.name.replace(' ', '.');
  	// Log.debug("value from " + tag);
  	ic.value = x.getValue (tag);
  	return true;
  }
  
  /**
   * Creates a set of inputs for this configuration.
   * @param c configuration identifying these inputs
   * @param prefix to the data for these inputs
   * @return a list of inputs
   */
  private ArrayList <ConfigInput> getInputs (XmlConfig c, String prefix)
  {
  	int n = c.getPrefixCount("Input");
  	if (n == 0)
  		return null;
  	ArrayList <ConfigInput> inputs = new ArrayList <ConfigInput> ();
  	// add a hidden field for the prefix
  	ConfigInput input;
  	if (prefix != null)
  	{
  		input = new ConfigInput ("_prefix_", "", "hidden", null, null, null);
	  	input.value = prefix;
	  	inputs.add(input);
  	}
   	// Log.debug("Found " + n + " inputs");
  	for (int i = 0; i < n; i++)
  	{
      XmlConfig ic = c.copy("Input[" + i + "]");
      String tags = ic.getValue("Tags");
      String name = ic.getValue("Name");
      if ((tags == null) && (name == null))
      	continue;
      input = new ConfigInput (tags, name, ic.getValue("Type"),
      	ic.getValue("Width"), getHelp (ic), ic.getValue ("Options"));
      String ref = ic.getValue("Ref");
      if (ref != null)
      	input.options = getRefOptions (ref, ic.getValue("Match"));
      setValue (input, prefix);
      inputs.add (input);      
  	}
  	String options = "OK Cancel";
  	if (prefix == null)
  		options = "Save Revert Cancel";
  	else if (prefix.endsWith("]"))
  		options = "OK Delete Cancel";
  	inputs.add (new ConfigInput ("_action_", "", "submit", null, null, options));
   	return inputs;
  }
  
  /**
   * Get the names for options to a data reference.  The special reference to "Revisions"
   * fetches a list of versions from a revision folder.
   * @param ref to XML data names
   * @return space delimited list of names
   */
  private ArrayList <String> getRefOptions (String ref, String match)
  {
  	if (ref.equals("Revisions"))
  		return getRevisionOptions ();
  	ArrayList <String> l = new ArrayList <String> ();
  	ref = ref.replace(' ', '.');
  	// Log.debug("Getting references to " + ref);
  	XmlConfig x = getConfig (ref);
  	if (x == null)
  	{
  		Log.error ("No configuration for " + ref);
  		return l;
  	}
  	int n = x.getPrefixCount(ref);
  	for (int i = 0; i < n; i++)
  	{
  		String s = x.getValue(ref + "[" + i + "].Name");
  		if ((s == null) || ((match != null) && !s.contains (match)))
  			continue;
  		l.add(s);
  	}
  	return l;
  }
 
  /**
   * Get the names for options to a data reference.
   * @param ref to XML data names
   * @return space delimited list of names
   */
  private ArrayList <String> getRevisionOptions ()
  {
  	ArrayList <String> l = new ArrayList <String> ();
  	l.add ("Current");
 	  // Log.debug("Getting revisions");
  	if ((revisions == null) || !revisions.isDirectory())
  		return l;
  	File[] rev = revisions.listFiles();
  	int n = rev.length;
  	while (n-- > 0)
  	{
  		if (!rev[n].isDirectory())
  			continue;
  		try
  		{
  			String s = selfmt.format(dirfmt.parse (rev[n].getName()));
  			l.add (s);
  		}
  		catch (Exception e)
  		{
  			Log.error("Can't parse revision " + rev[n].getName());
  		}
  	}
  	return l;
  }

  /**
   * Creates the tabs for a set of configuration items, for example routes.
   * Each tab is named by the item (a route, etc.) and holds the inputs for 
   * that item.
   * 
   * @param c configuration for this set of tabs
   * @param prefix for XML data of that set
   * @return set of tabs for that set
   */
  private ArrayList <ConfigTab> getSet (XmlConfig c, String prefix)
  {
  	ArrayList <ConfigTab> tabs = new ArrayList <ConfigTab> ();
  	c = c.copy("Set");
  	prefix = addTag (c, prefix);
  	XmlConfig x = getConfig (prefix);
  	int n = x.getPrefixCount(prefix);
  	for (int i = 0; i < n; i++)
  	{
  		String tags = prefix + "[" + i + "]";
  		ConfigTab tab = new ConfigTab ();
  		tab.name = x.getValue(tags + ".Name");
  		tab.inputs = getInputs (c, tags);
  		tab.tabs = null;
  		tabs.add(tab); 		
  	}
  	ConfigTab tab = new ConfigTab ();
  	tab.name = "NEW";
  	tab.inputs = getInputs (c, prefix + "[" + n + "]");
  	tabs.add(tab);
  	return tabs;
  }
  
  /**
   * Parses through the configuration xml building various input tabs
   * based on items found there.
   * 
   * @param c the part of the configuration having tabs
   * @param prefix to the XML values of those tabs
   * @return a list of tabs for user configuration
   */
  private ArrayList <ConfigTab> getTabs (XmlConfig c, String prefix)
  {
  	ArrayList <ConfigTab> tabs = new ArrayList <ConfigTab> ();
  	int n = c.getPrefixCount("Tab");
  	// Log.debug("Found " + n + " tabs for prefix " + prefix);
  	if (n == 0)
  	{
  		if (c.getValue("Set.Tags") != null)
  			return getSet (c, prefix);
  		return null;
  	}
  	for (int i = 0; i < n; i++)
  	{
  		XmlConfig t = c.copy("Tab[" + i + "]");
  		ConfigTab tab = new ConfigTab ();
  		String tag = addTag (t, prefix);
  		tab.name = t.getValue("Name");
  		if (tab.name == null)
  			continue;
  		tab.help = getHelp (t);
  		tab.inputs = getInputs (t, tag);
  		// Log.debug("Getting Tab " + i + " for " + tab.name);
  		tab.tabs = getTabs (t, tag);
  		tabs.add(tab);
  	}
  	return tabs;
  }
  
  /**
   * Build the data used by config.jsp
   * @param request from the user
   * @return configuration data
   */
  public ConfigData getConfigData (HttpServletRequest request)
  {
  	ConfigData data = new ConfigData ();
  	data.tabs = getTabs (config.copy("Config"), null);
    return data;
  }
  
  /**
   * Updates, stores, and recalls jPhineas configurations.
   * 
   * @param request for the configuration changes
   * @return true if successful
   */
  public boolean updateConfig (HttpServletRequest request)
  {
  	String action = request.getParameter("_action_");
  	if ((action == null) || action.equals("Cancel"))
  	  return true;
  	if (action.equals("OK")) // update with these values
  	{
  		String prefix = request.getParameter("_prefix_");
  		if (prefix == null)
  		{
  			Log.error ("Missing prefix for update");
  			return false;
  		}
  		XmlConfig x = getConfig (prefix);
    	Enumeration <?> names = request.getParameterNames ();
    	while (names.hasMoreElements())
    	{
    		String n = (String) names.nextElement();
    		if (n.startsWith("_")) // skip internal stuff
    			continue;
    		String[] values = request.getParameterValues(n);
    		for (int i = 0; i < values.length; i++)
    		{
    			Log.debug ("updating  " + prefix + " " + n + ":" + values[i]);
    			x.setValue (prefix + "." + n.replace(' ', '.'), values[i]);
    		}    			
    	}
  	}
  	else if (action.equals ("Delete")) // delete this tab's entry
  	{
  		String prefix = request.getParameter("_prefix_");
  		XmlConfig x = getConfig (prefix);
  		Log.debug("Deleting " + prefix);
  		if (x.delete(prefix))
  			return true;
  		Log.error ("Can't delete " + prefix);
  		return false;  		
  	}
  	else if (action.equals("Save")) // save this configuration
  	{
  		String now = dirfmt.format(new Date ());
  		// for separate directories...
  		File d = new File (revisions.getAbsolutePath() + "/" + now);
  		if (!d.mkdirs())
  		{
  			Log.error ("Can't create revision " + d.getAbsolutePath());
  			return false;
  		}
  		Log.debug ("Save revision " + d.getAbsolutePath());
  		if ((masterFile != null) && masterFile.exists())
  		{
  			masterFile.renameTo(new File (d.getAbsolutePath() + "/" + masterFile.getName()));
    		master.beautify(2);
    		master.save(masterFile);
  		}
  		/* for single configuration... 
  		masterFile.renameTo(new File (revisions.getAbsoluteFile() + "/" 
  				+ masterFile.getName() + "." + now));
  		*/
  		return true;  		
  	}
  	else if (action.equals ("Revert")) // revert to identified configuration
  	{
  		String rev = request.getParameter("Revision");
  		Log.debug("Revison " + rev);
  		if (rev.equals("Current"))
  			rev = null;
  		else try
  		{
  			rev = revisions.getAbsolutePath() + "/" + dirfmt.format (selfmt.parse(rev)) + "/";
  		}
  		catch (Exception e)
  		{
  			Log.error("Can't parse revision " + rev);
  			return false;
  		}
  		
  		Log.debug("Revert to " + (rev == null ? "Current" : rev));
  		reload (rev);
  		return true;
  	}
  	/* debugging...
  	Enumeration <?> names = request.getParameterNames ();
  	StringBuffer buf = new StringBuffer ("Returned parameters:\n");
  	while (names.hasMoreElements())
  	{
  		String n = (String) names.nextElement();
  		String[] values = request.getParameterValues(n);
  		for (int i = 0; i < values.length; i++)
  			buf.append ("  " + n + ":" + values[i] + "\n"); 			
  	}
  	Log.debug(buf.toString());
  	*/
  	return true;
  }
}
