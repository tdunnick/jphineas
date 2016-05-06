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


/**
 * The jPhineas Receiver only (currently) handles ebXML requests.  However, it
 * is designed to accommodate future HTTP style request protocols.  Non HTTP
 * protocols (for example MLLP) will require their own TCP listeners (independent
 * of the J2EE container hosting jPhineas).
 * <p>
 * Incoming ebXML requests are dispatched to processors based on their Service/Action
 * mappings.  Initially processors exist for ebXML Ping and Payload requests.  Future
 * processors will include PHINMS style Receiver Servlet support and possibly
 * Route Not Read (implemented in PHINMS as a Receiver Servlet).
 */

package tdunnick.jphineas.receiver;