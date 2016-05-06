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

/**
 * Data used by the JSP view to construct on TAB in the configuration GUI.  A
 * TAB typically has either sub-tabs or inputs, but not both.  However, there
 * is no restriction on this.
 * 
 * @author Thomas Dunnick tdunnick@wisc.edu
 *
 */
public class ConfigTab
{
	/** a short help message for bubble help */
  String help;
  /** the name that appears on this tab */
  String name;
  /** any sub-tabs of this tab */
  ArrayList <ConfigTab> tabs;
  /** any inputs of this tab */
  ArrayList <ConfigInput> inputs;
  
  public String getHelp () { return help; }
  public String getName () { return name; }
  public ArrayList <ConfigTab> getTabs () { return tabs; }
  public ArrayList <ConfigInput> getInputs () { return inputs; }
}
