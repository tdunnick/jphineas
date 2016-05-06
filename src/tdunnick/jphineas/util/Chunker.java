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

import java.io.*;

import org.jfree.util.Log;

/**
 * A simple set of static methods to implement file chunking.  Incoming chunks 
 * are placed in a directory and named by the part and id. Note that id's should
 * be unique (no checking is done). Outgoing chunks are generated as needed from
 * the source file.
 * 
 * @author Thomas Dunnick tdunnick@wisc.edu
 *
 */
public class Chunker
{
	static File chunkDir = null;
	
	/**
	 * Sets the folder where incoming chunks live
	 * @param dir for chunks
	 */
	static public void setDir (File dir)
	{
		chunkDir = dir;
	}
	
  /**
   * gets the location for a specific chunk
   * @param id unique to this set of chunks
   * @param chunk of the id
   * @return location of the chunk
   */
  static public File locate (String id, int chunk)
  {
  	return new File (chunkDir.getAbsolutePath() + "/" + chunk + "_" + id);
  }
  
  /**
   * gets the data for a specific chunk
   * @param fd location of file getting chunked
   * @param chunk of this file
   * @param size of this chunk, if zero return whole file
   * @return the part or null if it fails
   */
  static public byte[] getBytes (File fd, int chunk, int size)
  {
  	try
  	{
  		long loc = (long) size * chunk;
  		// return empty chunk when file is exhausted
  		if ((loc >= fd.length()) || (loc < chunk))
  			return new byte[0];
  		// set size to maximum available
  		if ((size == 0) || (loc + size > fd.length()))
  			size = (int) (fd.length() - loc);
  		FileInputStream i = new FileInputStream (fd);
  		// read from last location
  		if (loc > 0)
  			i.skip (loc);
   		byte[] buf = new byte [size];
  		i.read (buf);
  		i.close ();
  		return buf;
  	}
  	catch (IOException e)
  	{
  		return null;
  	}
  }
  
  /**
   * Assemble incoming chunks to a file.  Assumed to be ordered and
   * beginning with chunk '0'.
   * 
   * @param fd destination file for chunks
   * @param id of these chunks
   * @return number of chunks assembled
   */
  static public int assemble (File fd, String id)
  {
  	try
  	{
	  	File t = new File (chunkDir.getAbsolutePath() + "/" + id);
	  	FileOutputStream o = new FileOutputStream (t);
	  	int chunk = 0;
	  	while (true)
	  	{
	  		File s = locate (id, chunk++);
	  		if (!s.canRead())
	  			break;
	  		FileInputStream i = new FileInputStream (s);
	  		byte[] buf = new byte [(int) s.length()];
	  		i.read (buf);
	  		i.close ();
	  		o.write(buf);
	  		s.delete ();
	   	}
	  	o.close ();
	  	t.renameTo(fd);
	  	return chunk;
  	}
  	catch (IOException e)
  	{
  		return 0;
  	}
  }
  
  /**
   * return number of chunks needed for this file
   * @param f to check
   * @param size of a chunk
   * @return number of chunks or 0 if not chunked
   */
  static public int needed (File f, int size)
  {
  	long l = f.length();
  	if ((size == 0) || (l < size))
  		return 0;
  	int n = (int) (l / size);
  	if (l % size != 0)
  		n++;
  	return n;
  }
  
  /**
   * Count number of chunks saved
   * @param id of incoming chunks
   * @return number found
   */
  static public int saved (String id)
  {
  	int chunk = -1;
  	File s = null;
  	do
  	{
  		s = locate (id, ++chunk);
  	}
  	while (s.canRead ());
  	return chunk;
  }
}
