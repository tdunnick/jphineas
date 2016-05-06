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

import java.util.*;
import tdunnick.jphineas.common.*;

/**
 * This is the data object passed to the configuration JSP and includes the
 * expected TABS and INPUTS for the GUI.
 * 
 * @author Thomas Dunnick
 *
 */
public class ConfigData
{
	/** title in the navigation bar */
  String title = "Configuration";
  /** revision in the navigation bar */
  String version = JPhineas.revision;
  /** tabs for this screen */
  ArrayList <ConfigTab> tabs = new ArrayList <ConfigTab> ();
  
	public String getTitle()
	{
		return title;
	}
	public void setTitle(String title)
	{
		this.title = title;
	}
	public String getVersion()
	{
		return version;
	}
	public void setVersion(String version)
	{
		this.version = version;
	}
	public ArrayList <ConfigTab> getTabs()
	{
		return tabs;
	}
	public void setTabs (ArrayList <ConfigTab> tabs)
	{
		this.tabs = tabs;
	}
}
