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

import java.io.*;

import tdunnick.jphineas.config.XmlConfig;
import tdunnick.jphineas.xml.*;

/**
 * A general purpose output filter.  Subclasses need to implements process ()
 * which filters the buffered output on close and may override configure
 * in order to check input paramters (if any).
 * 
 * Note flush() does nothing, nothing goes out until close.
 * 
 * @author tld
 *
 */
public abstract class PhineasOutputFilter extends FilterOutputStream
{
	protected XmlConfig config = null;
  protected ByteArrayOutputStream buf = new ByteArrayOutputStream ();

  public PhineasOutputFilter(OutputStream out)
	{
		super(out);
	}

	public void write(byte[] b, int off, int len) throws IOException
	{
		buf.write (b, off, len);
	}

	public void write(byte[] b) throws IOException
	{
		buf.write(b);
	}

	public void write(int b) throws IOException
	{
		buf.write(b);
	}

	public void close() throws IOException
	{
		if (process ())
			out.write(buf.toByteArray());		
		out.close();
	}

	public void flush() throws IOException
	{
	}
	
	/**
	 * Set up everything needed to process on close. Over ride if you want to
	 * check the configuration out ahead of time.
	 * 
	 * @param config processing configuration, usually as XML
	 * @return true if successful
	 */
	public boolean configure (XmlConfig config)
	{
		this.config = config;
		return true;
	}
	/**
	 * Do the actual data filtering.  Update the buffer for writing.
	 * @return true if successful
	 * @throws IOException
	 */
	protected abstract boolean process () throws IOException;
}
