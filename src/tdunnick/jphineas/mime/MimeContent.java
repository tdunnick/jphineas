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

package tdunnick.jphineas.mime;

import java.util.*;
import org.bouncycastle.util.encoders.*;

/**
 * Mime content management.
 * 
 * @author Thomas Dunnick
 *
 */
public class MimeContent
{
	private StringBuffer buf = new StringBuffer ();
	
	/**  MIME content type header tag */
	public static final String CONTENT = "Content-Type";
	/**  MIME text value for CONTENT */
	public static final String TEXT = "text/plain";
	/**  MIME xml value for CONTENT */
	public static final String XML = "text/xml";
	/** MIME octet stream for CONTENT */
	public static final String OCTET = "Application/Octet-Stream";
	/**  MIME multipart value for CONTENT */
	public static final String MULTIPART = "multipart/related";	
	
	/**  MIME encoding header tag */
	public static final String ENCODING = "Content-Transfer-Encoding";
	/**  MIME base64 value for ENCODING */
	public static final String BASE64 = "base64";
	/** MIME quoted printable value for ENCODING */
	public static final String QUOTED = "quoted/printable";

	/** MIME content length header tag */
	public static final String LENGTH = "Content-Length";
  /** MIME Content ID header tag */
	public static final String CONTENTID = "Content-ID";
	/** MIME Message ID Header tag */
	public static final String MESSAGEID = "Message-ID";
	/** MIME disposition tag */
	public static final String DISPOSITION = "Content-Disposition";
	/** current MIME headers */
	private ArrayList <String> headers = new ArrayList <String>();
	
	
	/**
	 * create an empty MIME content with TEXT CONTENT and empty the body 
	 */
	public MimeContent()
	{
		reset ();
	}

	/**
	 * create MIME content as parsed from a message
	 * @param m message to parse
	 */
	public MimeContent (String m)
	{
		parse (m);
	}
	
	/**
	 * reset the MIME content with TEXT CONTENT and empty the body 
	 */
	public void reset ()
	{
		headers.clear();
		setHeader ("MIME-Version", "1.0");
		buf.setLength(0);
	}
	
	/**
	 * If the header starts with HTTP, then this is the response from
	 * a request and parse out the code.
	 * @return HTTP response code or -1 if not a response
	 */
	public int getHTTPCode ()
	{
		if (headers.size() < 1)
			return -1;
		String s = headers.get (0);
		if (s.startsWith("HTTP"))
		{
			s = s.replaceFirst("^.* ([0-9]+) .*$", "$1");
		  return Integer.parseInt(s);
		}
		return -1;
	}
	/**
	 * Set the MIME CONTENT type
	 * @param id of CONTENT
	 */
	public void setContentId (String id)
	{	
			setHeader (CONTENTID, id);
	}
	
	/**
	 * Gets the current CONTENT type
	 * @return type of content
	 */
	public String getContentId ()
	{
		return getHeader (CONTENTID);
	}
	

	/**
	 * Set the MIME CONTENT type
	 * @param type of CONTENT
	 */
	public void setContentType (String type)
	{	
		if (type.startsWith (MULTIPART))
			setMultiPart ();
		else
			setHeader (CONTENT, type);
	}
	
	/**
	 * Gets the current CONTENT type
	 * @return type of content
	 */
	public String getContentType ()
	{
		return getHeader (CONTENT);
	}
	
	/**
	 * Sets the MIME encoding.  Default is none (no encoding).
	 * @param enc encode to use
	 */
	public void setEncoding (String enc)
	{
		setHeader (ENCODING, enc);
	}
	
	/**
	 * Gets the current MIME encoding
	 * @return encoding or null if none is set
	 */
	public String getEncoding ()
	{
		return getHeader (ENCODING);
	}
	
	/**
	 * Sets the MIME dispostion.
	 * @param d disposition to use
	 */
	public void setDisposition (String d)
	{
		setHeader (DISPOSITION, d);
	}
	
	/**
	 * Gets the current MIME disposition
	 * @return encoding or null if none is set
	 */
	public String getDisposition ()
	{
		return getHeader (DISPOSITION);
	}

	
	/**
	 * Set BASIC authentication
	 * TODO set digest authentication
	 * 
	 * @param uid - user id
	 * @param password - user password
	 */
	public void setBasicAuth (String uid, String password)
	{
		if ((uid == null) || (password == null))
			return;
		byte[] encoded = Base64.encode ((uid + ":" + password).getBytes());
		setHeader ("Authorization", "Basic " + new String (encoded));
	}
	
	/**
	 * Set an arbitrary MIME header. If the value is null,
	 * remove the header. Surround ID's with angle brackets as required.
	 * 
	 * @param name of MIME tag
	 * @param value to set
	 */
	public void setHeader (String name, String value)
	{
		setHeader (name, value, -1);
	}
	
	/**
	 * Set the header at a specific position.  If the value is null,
	 * remove the header.  If the position is negative append the header.
	 * Surround ID's with angle brackets as required.
	 * 
	 * @param name of header item
	 * @param value of header item
	 * @param position in header list
	 */
	public void setHeader (String name, String value, int position)
	{
		if (name == null)
			return;
		// ID's must be surrounded with angle brackets
		if (name.equalsIgnoreCase (CONTENTID) || name.equalsIgnoreCase(MESSAGEID))
		{
			if ((value != null) && !value.matches("<.*>"))
				value = "<" + value + ">";
		}
		int i = getHeaderIndex (name);
		if (value == null)
		{
			if (i >= 0)
				headers.remove(i);
			return;
		}
		String v = name + ": " + value;
		if ((position < 0) || (position == i))
		{
			if (i < 0)
			  headers.add (v);
			else
				headers.set(i, v);
		}
		else
		{
			if (i >= 0)
				headers.remove(i);
			if (position < headers.size())
				headers.add (position, v);
			else
				headers.add(v);
		}
	}
	
	/**
	 * Get an arbitrary MIME header value, removing angle brackets from ID's
	 * 
	 * @param name of MIME tag
	 * @return value of tag
	 */
	public String getHeader (String name)
	{
		int i = getHeaderIndex (name);
		if (i < 0)
			return "";
		String s = headers.get (i).replaceFirst("^.*: *", "");
		// remove angle brackets for content or message id's
		if (name.equalsIgnoreCase(CONTENTID) || name.equalsIgnoreCase (MESSAGEID))
			s = s.substring(1, s.length() -1);
		return s;
	}
	
	/**
	 * Gets the headers index for a tag 
	 * @param name of tag
	 * @return index or -1 if not found
	 */
	private int getHeaderIndex (String name)
	{
		name = name.toLowerCase();
		for (int i = 0; i < headers.size(); i++)
		{
			if (headers.get(i).toLowerCase().startsWith(name))
				return i;
		}
		return -1;
	}
	
	/**
	 * Get a list of MIME headers including tags and values
	 * @return the list of headers
	 */
	public String[] getHeaders ()
	{
		String[] h = new String[headers.size()];
		return headers.toArray(h);
	}
	
	/**
	 * Set the body of the MIME message
	 * @param body to set it to
	 * @return true if successful
	 */
	public boolean setBody (String body)
	{
		int p = -1;
		String boundary = getBoundary();
		if (boundary != null)
			p = buf.indexOf("\n--" + boundary);
		if (p > 0)
			buf.delete(0, p);
		else if (p < 0)
			buf.setLength(0);
		buf.insert(0, body);
		return true;
	}
	
	/**
	 * Gets the body of the MIME message
	 * @return the message body
	 */
	public String getBody ()
	{
		int p = -1;
		String boundary = getBoundary();
		if (boundary != null)
			p = buf.indexOf("\n--" + boundary);
		if (p > 0)
			return buf.substring(0, p);
		return buf.toString();
	}
	
	/**
	 * Set encoding to base64 and encode the binary body
	 * @param body to encode
	 * @return true if successful
	 */
	public boolean encodeBody (byte[] body)
	{
		setEncoding (BASE64);
		setContentType (MimeContent.OCTET);
		setBody (new String (Base64.encode(body)));
		return false;
	}
	
	/**
	 * Gets a binary body, decoding if necessary
	 * @return the decoded body
	 */
	public byte[] decodeBody ()
	{
		if (getEncoding().equalsIgnoreCase(BASE64))
			return Base64.decode(buf.toString().getBytes());
		return buf.toString().getBytes();
	}
	
	/**
	 * parse the body of this content as POST parameters 
	 * @return table of paramters
	 */
	public HashMap <String, String> getParams ()
	{
		HashMap <String, String> arg = new HashMap <String, String> ();
		String[] t = getBody().split("[&]");
		for (int i = 0; i < t.length; i++)
		{
			String[] a = t[i].split("=");
			if (a.length == 2)
				arg.put(a[0].trim(), a[1].trim());
		}
		return arg;
	}
	
	/**
	 * Set the body with a map of POST parameters
	 * @param params
	 */
	public void setParams (HashMap <String, String> params)
	{
		StringBuffer buf = new StringBuffer ();
		Iterator <String> it = params.keySet().iterator();
		while (it.hasNext())
		{
			String k = it.next();
			buf.append(k + "=" + params.get (k) + "&");
		}
		setBody (buf.substring(0, buf.length() - 1));
	}
	
	public boolean setMultiPart ()
	{
		return setMultiPart (null);
	}
	
	/**
	 * Set the MIME content to be multipart and create a boundary
	 * based on the time and a hashcode
	 * @return true if successful
	 */
	public boolean setMultiPart (String args)
	{
		if (buf.length() > 0)
			return false;
		Date d = new Date ();
		String boundary = "_Part_" + d.getTime() + "_" + d.hashCode();
		if (args != null)
			setHeader (CONTENT, MULTIPART + "; boundary=\"" + boundary + "\"; " + args);
		else
			setHeader (CONTENT, MULTIPART + "; boundary=\"" + boundary + "\"");
		return true;
	}
	
	/**
	 * Gets the text used to separate multipart MIME messages.
	 * @return boundary or null if not a multipart message
	 */
	public String getBoundary ()
	{
		String b = getHeader (CONTENT);
		int i = b.indexOf("boundary=");
		if (i < 0)
			return null;
		i += 9;
		int e = -1;
		if (b.charAt(i) == '"')
		{
			i++;
			e = b.indexOf('"', i);
		}
		else
		  e = b.indexOf(";", i);
		if (e < 0)
		  return b.substring (i);
		return b.substring (i, e);
	}
	
	/**
	 * Get a list of MIME content for a multipart message
	 * @return the list or null if not multipart
	 */
	public MimeContent[] getMultiParts ()
	{
		String boundary = getBoundary ();
		if (boundary == null)
			return null;
		int s = buf.indexOf("--" + boundary + "\n");
		if (s < 0)
			return null;
		else
			s += 3 + boundary.length();
		int e = buf.indexOf("\n--" + boundary + "--", s);
		if (e < 0)
			return null;
		String[] parts = buf.substring(s, e).split("(?s)\n--" + boundary + "\n");
		MimeContent[] mparts = new MimeContent[parts.length];
		for (int i = 0; i < parts.length; i++)
			mparts[i] = new MimeContent (parts[i]);
		return mparts;
	}
	
	/**
	 * Add MIME content to a multipart message
	 * @param m
	 * @return true if successful
	 */
	public boolean addMultiPart (MimeContent m)
	{
		if (m == null)
			return false;
		String boundary = getBoundary ();
		if (boundary == null)
			return false;
		boundary = "\n--" + boundary + "--\n";
		int l = buf.indexOf (boundary);
		if (l < 0)
		{
			buf.append(boundary.substring (0, boundary.length() -3) + "\n");
		}
		else
		{
			l += boundary.length() - 3;
			buf.delete(l, l + 2);
		}
		buf.append(m.toString() +  boundary);
		return true;
	}
	
	/**
	 * Get actual current content length (use getHeader(LENGTH) for "logical")
	 * 
	 * @return actual content length
	 */
	public int getContentLength ()
	{
		return buf.length();
	}
	
	/**
	 * Get the MIME message
	 * @return the formatted message
	 */
	public String toString ()
	{
		if (getHeader(CONTENT).length() == 0)
			setHeader (CONTENT, TEXT, 1);
		setHeader (LENGTH, "" + buf.length());
		StringBuffer hd = new StringBuffer ();
		for (int i = 0; i < headers.size(); i++)
			hd.append(headers.get(i) + "\n");
		return hd.toString() + "\n" + buf.toString();
	}
	
	/**
	 * Parse and store a formated MIME message
	 * @param msg to parse
	 */
	public boolean parse (String msg)
	{
		reset ();
		int hdrlen = msg.indexOf("\n\n");
		if (hdrlen < CONTENT.length())
			return false;
		String[] header = msg.substring(0, hdrlen).split ("(?s)\n");
		if (header.length < 1)
			return false;
		for (int i = 0; i < header.length; i++)
		{
			headers.add(header[i]);
		}
		buf.append(msg.substring(hdrlen + 2));
		return true;
	}
}
