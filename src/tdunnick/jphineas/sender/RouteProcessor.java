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
import tdunnick.jphineas.config.RouteConfig;
import tdunnick.jphineas.queue.*;

/**
 * The processing class called by the queue poller (thread).  This class performs whatever
 * function may be needed on the queue row.  The common case of course is to transmit
 * the payload to some destination
 * 
 * @author tld
 *
 */
public abstract class RouteProcessor
{
	/**
	 * Configure this processor
	 * @param config for this Route 
	 * @return true if successful
	 */
	protected abstract boolean configure (RouteConfig config);
  /**
   * Do any requested processing on this queue row
   * @param row to process
   * @return true if successful
   */
  protected abstract boolean process (PhineasQRow row);
}
