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

public class Hl7AckMsg
{
  String ack = "";
  String code = "AA";
  String comment = "MSG OK";
  
	public Hl7AckMsg ()
	{
		super ();
		ack = null;
	}
	
	public Hl7AckMsg (String msg)
	{
		super ();
		getAck (msg, 0);
	}
	
	public void setCode (String c)
	{
		code = c;
	}
	
	public void setComment (String c)
	{
		comment = c;
	}
	
	/**
	 * get a date in HL7 format
	 * 
	 * @return formatted date
	 */
	public String now ()
	{
		SimpleDateFormat fmt = new SimpleDateFormat ("yyyyMMddHHmmss");
		return fmt.format(new Date ());		
	}
	
	public String genAck (String seg, String msa1, String msa2)
	{
		if (!seg.startsWith("MSH"))
			return "";
		// do a very simply parse
		String delim = seg.substring(3, 4);
		String[] field = seg.split("\\" + delim);
		if (field.length < 12)
			return ("");
		return ack = ("MSH" + 
				delim + field[1] + // HL7 delimiters
				delim + field[4] + // swap sending and receiving application and facility
				delim + field[5] +
				delim + field[2] + 
				delim + field[3] +
				delim + now () + // time stamp it
				delim + delim + "ACK" + delim +
				delim + field[10] +  // process ID
				delim + field[11] + // HL7 version ID
				"\rMSA" + delim + msa1 + // code
				delim + field[9] + // message ID
				delim + msa2 + "\r");		// comment
	}
	
	public String getAck (String msg, int n)
	{
		ack = "";
		if (msg == null)
			return "";
		// parse into segments
		String[] seg = msg.replace('\n', '\r').split("\r+");
		if (seg.length == 0)
			return "";
		for (int i = 0; i < seg.length; i++)
		{
			if (seg[i].startsWith("MSH"))
			{
				if (n == 0)
				  ack += genAck (seg[i], code, comment);
				else if (n-- == 1)
					return genAck (seg[i], code, comment);
			}
		}
		return ack;
	}
	
	public String toString ()
	{
		return ack;
	}
}
