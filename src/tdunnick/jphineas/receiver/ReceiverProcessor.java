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
package tdunnick.jphineas.receiver;


import tdunnick.jphineas.xml.*;
import tdunnick.jphineas.config.ServiceConfig;
import tdunnick.jphineas.mime.*;

/**
 * The processing class called by the receiver based on the service/action found
 * in the request.  This class performs whatever function may be needed to fullfill
 * the request including updating the queue row.  The common case of course is to 
 * accept and store and incoming payload.
 * 
 * @author Thomas Dunnick
 *
 */
public abstract class ReceiverProcessor
{
	/**
	 * Configure this processor
	 * @param config for this Route 
	 * @return true if successful
	 */
	protected abstract boolean configure (ServiceConfig config);
  /**
   * Do any requested processing for this request and return a response
   * @param soap request to process
   * @param parts of the request
   * @return response or null if fails
   */
  protected abstract MimeContent process (SoapXml soap, MimeContent[] parts);
}
