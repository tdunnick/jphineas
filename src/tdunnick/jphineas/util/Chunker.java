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
   * @param size of this chunk
   * @return the part
   */
  static public byte[] getBytes (File fd, int chunk, int size)
  {
  	try
  	{
  		long loc = (long) size * chunk;
  		if (loc >= fd.length())
  			return null;
  		if (loc + size > fd.length())
  			size = (int) (fd.length() - loc);
  		FileInputStream i = new FileInputStream (fd);
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
   * Count number of chunks saved
   * @param id of incoming chunks
   * @return number found
   */
  static public int chunks (String id)
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
