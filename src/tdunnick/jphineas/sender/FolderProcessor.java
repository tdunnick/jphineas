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

package tdunnick.jphineas.sender;

import java.io.*;

import tdunnick.jphineas.config.FolderConfig;
import tdunnick.jphineas.xml.*;

/**
 * The processing class called by the folder poller.  This class performs whatever
 * function may be needed on the file.  The common case of course is to queue it
 * for transmission.
 * 
 * @author tld
 *
 */
public abstract class FolderProcessor
{
	/**
	 * Configure this processor
	 * @param config for this Folder 
	 * @return true if successful
	 */
	protected abstract boolean configure (FolderConfig config);
  /**
   * Do any requested processing on this Map entry
   * @param src file to process
   * @return true if successful
   */
  protected abstract boolean process (File src);
}
