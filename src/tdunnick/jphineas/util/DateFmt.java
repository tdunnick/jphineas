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

package tdunnick.jphineas.util;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateFmt
{
	static SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

	/**
	 * Construct a PHINMS style timestamp
	 * @param d date to use, or null for now
	 * @return the timestamp
	 */
	public static String getTimeStamp (Date d)
	{
		if (d == null)
			d = new Date();
		return fmt.format(d);
	}
	
	/**
	 * Construct a PHINMS style timestamp
	 * @param t timestamp to use, or null for now
	 * @return the timestamp
	 */
	public static String getTimeStamp (long t)
	{
		return getTimeStamp (new Date (t));
	}
	
	/**
	 * Parse a PHINMS style date
	 * @param s date to parse
	 * @return date/time
	 */
	public static long getTime (String s)
	{
		try
		{
		  return fmt.parse(s).getTime();
		}
		catch (Exception e)
		{
			return Long.MIN_VALUE;
		}
	}
}
