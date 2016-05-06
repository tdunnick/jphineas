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

package tdunnick.jphineas.queue;

import java.util.*;

import tdunnick.jphineas.config.XmlConfig;
import tdunnick.jphineas.xml.*;

/**
 * The queue type holds what amounts to meta-data for a type of queue.  The
 * actual persistence implementation is hidden from jPhineas outside of this
 * package.  
 * 
 * @author Thomas Dunnick
 *
 */
public class PhineasQType
{
	/** name of this type */
	private String typeName = null;
	/** mapping from id's to field #'s */
	private HashMap <String,Integer> idMap = null;
	/** local copy of field names */
	private String[] names = null;

	public PhineasQType (XmlConfig config)
	{
		typeName = config.getValue("Name");
		int n = config.getTagCount ("Field");
		idMap = new HashMap <String,Integer> (n + n / 3);
		names = new String[n];
		for (int i = 0; i < n; i++)
		{
			String tag = "Field[" + i + "]";
			String id = config.getAttribute(tag, "id");
			if ((id != null) && (id.length() > 0))
			{
				idMap.put(id, Integer.valueOf(i));
			  names[i] = config.getValue(tag);
			}
		}
	}
	
	/**
	 * get the name for this type
	 * @return the name of this type
	 */
	public String getType ()
	{
		return typeName;
	}
	
	/**
	 * Get the field name for a queue value. Useful for dB updates.
	 * 
	 * @param index of value
	 * @return name of that value
	 */
	public String getName (int index)
	{
		if (names == null)
			return null;
		if ((index < numFields()) && (index >= 0))
			return names[index];
		return null;		
	}
		
	/**
	 * Maps a field ID (used internally) to a dB field name (used by persistence)
	 * @param id to map
	 * @return field name or null if not found
	 */
	public String getName (String id)
	{
		return getName (getFieldIndex (id));
	}
	
	/**
	 * get a list of all of the field names
	 * @return the names
	 */
	public String[] getNames ()
	{
		return names;
	}
	
	/**
	 * Get a list of ID's ordered by their index
	 * @return array of queue ID's
	 */
	public String[] getIds ()
	{
		Iterator <String> it = idMap.keySet().iterator();
		String[] ids = new String [idMap.size()];
		while (it.hasNext())
		{
			String k = it.next ();
			ids[idMap.get(k).intValue()] = k;
		}
		return ids;
	}
	
	/**
	 * Get the value index for a given field id
	 * @param n name to use
	 * @return index to value or -1 if not found
	 */
	public int getFieldIndex (String n)
	{
		if ((idMap == null) || (n == null))
			return -1;
		Integer i = idMap.get(n);
		if (i == null)
			return -1;
		return i.intValue();
	}

	/**
	 * gets the number of (named) fields in this queue
	 * @return number of fields
	 */
	public int numFields ()
	{
		return names.length;
	}	
}
