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
import tdunnick.jphineas.logging.*;

/**
 * Data used by the JSP view to render inputs in the configuration GUI
 * 
 * @author Thomas Dunnick tdunnick@wisc.edu
 *
 */
public class ConfigInput
{
	/** the prompt for the input */
  String prompt;
  /** the name used to identify the input in the form */
  String name;
  /** the input type (e.g. "text", etc) */
  String type;
  /** the current default value */
  String value;
  /** a short message for help bubbles that follow the mouse */
  String help;
  /** the input width (when important */
  int width;
  /** option lists for select or radio inputs */
  ArrayList <String> options;
  
  
	/**
	 * Almost all of the work is done in construction.  Only the value and
	 * occasionally the options are updated by the model.
	 * 
	 * @param name of the input
	 * @param prompt for the input
	 * @param type of the input (e.g. "text", etc)
	 * @param width for the input (optional)
	 * @param help for the input help bubble (optional)
	 * @param options used for select lists and radio buttons
	 */
	public ConfigInput(String name, String prompt, String type,	
			String width, String help, String options)
	{
		this.name = name;
		if (prompt == null)
			prompt = this.name;
		this.prompt = prompt;
		if (type.equals ("dir") || type.equals("file"))
			type = "text";
		this.type = type;
		this.value = "";
		this.help = help;
		this.width = 0;
		if (width != null)
		{
			try
			{
				this.width = Integer.parseInt (width);
			}
			catch (Exception e)
			{
				Log.error ("bad input width for " + name);
			}
		}		
		if (options != null)
			this.options = new ArrayList <String>(Arrays.asList(options.split (" +")));
		else
		  this.options = null;
	}
	
	public String getPrompt()
	{
		return prompt;
	}
	public String getName()
	{
		return name;
	}
	public String getType()
	{
		return type;
	}
	public String getValue()
	{
		return value;
	}
	public int getWidth()
	{
		return width;
	}
	public String getHelp()
	{
		return help;
	}
	public ArrayList<String> getOptions()
	{
		return options;
	}
  
}
