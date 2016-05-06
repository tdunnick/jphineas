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

package tdunnick.jphineas.filter;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

import tdunnick.jphineas.config.XmlConfig;
import tdunnick.jphineas.xml.*;

/**
 * This is a generic filter intended to be configured by XML.  
 * It gobbles up ALL of the input when configured, processes it 
 * to a storage buf, and then doles it out as requested.
 * 
 * @author tld
 *
 */
public abstract class PhineasInputFilter extends FilterInputStream
{
  private int bufpos = 0;
  private int bufmark = 0;
  protected byte[] buf = null;
 
	public PhineasInputFilter(InputStream in)
	{
		super(in);
	}
	
	public int read() throws IOException
	{
		if (buf == null)
			throw new IOException ("Encryption filter not configured");
		if (available () > 0)
			return (int) buf[bufpos++];
		return -1;
	}
	
	public int read(byte[] b, int off, int len) throws IOException
	{
		int c, i = 0;
		while ((i < len) && ((c = read()) >= 0))
		  b[off + i++] = (byte) c;
		return i;
	}
	
	public int read(byte[] b) throws IOException
	{
		return read (b, 0, b.length);
	}

	public int available() throws IOException
	{
		if (buf == null)
			throw new IOException ("Encryption filter not configured");
		return buf.length - bufpos;
	}

	public synchronized void mark(int readlimit)
	{
		bufpos = bufmark;
	}

	public boolean markSupported()
	{
		return true;
	}

	public synchronized void reset() throws IOException
	{
		bufpos = bufmark;
	}

	public long skip(long n) throws IOException
	{
		if (buf == null)
			throw new IOException ("Encryption filter not configured");
		if (n > available())
			n = available();
		bufpos += n;
		return n;
	}	
	
	/**
	 * convenience method for sub-classes that need the whole buffer up
	 * front (most do)
	 * 
	 * @return buffer size or -1 if it fails
	 */
	protected long fillBuf ()
	{
		try
		{
			buf = new byte[in.available()];
			return in.read(buf);
		}
		catch (IOException e)
		{
			buf = null;
			return -1;
		}
	}
	
	/**
	 * Here is where all the work gets done
	 * @param cfg used for configuration
	 * @return true if successful
	 */
	public abstract boolean configure (XmlConfig cfg);

}
