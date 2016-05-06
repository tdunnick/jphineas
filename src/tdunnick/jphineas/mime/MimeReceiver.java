/*
 *  Copyright (c) 2015-2016Thomas Dunnick (https://mywebspace.wisc.edu/tdunnick/web)
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

package tdunnick.jphineas.mime;

import java.io.*;
import java.util.*;
import javax.servlet.http.HttpServletRequest;

import tdunnick.jphineas.logging.*;

/**
 * Read an incoming message in Mime format.  This may be coming from either
 * an HTTP request or an HTTP response stream
 * 
 * @author Thomas Dunnick
 *
 */
public class MimeReceiver
{
  /**
   * Read and parse incoming MIME content from an HTTP request.  This relies on
   * end of file, multipart boundary, or Content-Length to determine end of message.
   * 
   * @param req request object with headers and input stream
   * @return the content or null if a problem occurs
   */
	public static MimeContent receive (HttpServletRequest req) throws IOException
	{
		InputStream is = req.getInputStream();
  	StringBuffer buf = new StringBuffer();
 		int len = Integer.MAX_VALUE; // mime message length
 		String boundary = null;
 		// copy headers into buf
 		Enumeration <?> names = req.getHeaderNames();
 		while (names.hasMoreElements())
 		{
 			String n = (String) names.nextElement();
 			Enumeration <?> values = req.getHeaders(n);
 			while (values.hasMoreElements())
 			{
 				String v = (String) values.nextElement();
 				// check for boundary and length
 				if ((boundary == null) && v.contains("boundary="))
				{
					boundary = v.replaceFirst("^.*boundary=\"*([^\";]*).*$", "--$1--");
					Log.debug ("Boundary=" + boundary);
				}
 				else if (n.equalsIgnoreCase ("content-length"))
 				{
 					len = Integer.parseInt(v);
 					Log.debug("Length=" + len);
 					continue;
 				}
 				buf.append(n + ": " + v + "\n");
			}
 		}
 		buf.append("\n");
		return receiveBody (is, buf, boundary, len);
	}
	
  /**
   * Read and parse incoming MIME content from a (HTTP response) stream.  This relies on
   * end of file, multipart boundary, or Content-Length to determine end of message.
   * 
   * @param is input stream to read
   * @return the content or null if a problem occurs
   */
  public static MimeContent receive (InputStream is) throws IOException
  {
  	StringBuffer buf = new StringBuffer();
 		int c, // current char read
  		len = Integer.MAX_VALUE, // mime message length
  		line = 0; // index to current line read
		String boundary = null;
		// read and parse the header
		while ((c = is.read()) >= 0)
		{
 			// ignore carrage returns
			if (c == '\r')
				continue;
			if (c == '\n') // end of line, check for length and boundary
			{
				if (buf.length() == line)
				{
					buf.append('\n');
					break;
				}
				String l = buf.substring(line);
				if (l.toLowerCase().startsWith("content-length: "))
				{
					len = Integer.parseInt(buf.substring(line + 16));
					Log.debug ("Len=" + len + " for " + l + " at " + buf.length());
				}
				else if (l.contains("boundary="))
				{
					boundary = l.replaceFirst("^.*boundary=\"*([^\";]*).*$", "--$1--");
					Log.debug ("Boundary=" + boundary);
				}
				line = buf.length() + 1;
			}
		  buf.append ((char) c);
		}
		return receiveBody (is, buf, boundary, len);
  }
  
  /**
   * Read the body of a Mime request from a stream.  It may be either 
   * multipart or length delimited. 
   * 
   * @param is stream to read from
   * @param buf to store to (including headers)
   * @param boundary for multipart if given
   * @param len of body
   * @return the mime content
   * @throws IOException
   */
  private static MimeContent receiveBody (InputStream is, StringBuffer buf, 
  		String boundary, int len) throws IOException
  {
  	int c, line = buf.length();
		while ((c = is.read()) >= 0)
		{
 			// ignore carrage returns
		  if (c != '\r')
			  buf.append ((char) c);
	  	// if no boundary, test for len
	  	if (boundary == null)
	  	{
	  		if (len-- == 0)
		  		break;
	  	}
		  // otherwise test for a boundary
	    else	if ((c == '-') && buf.substring(line).equals(boundary))
			  break;
			// note if we are starting a new line
			if (c == '\n')
				line = buf.length();
		}
  	MimeContent mime = new MimeContent ();
  	if (mime.parse(buf.toString()))
  		return mime;
  	throw new IOException ("Failed parsing mime: " + buf.toString());
  }
}
