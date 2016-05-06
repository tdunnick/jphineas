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
 * A filter can be injected into an input or output stream in order to modify
 * data in transit.  By writing a filter and identifying it in the configuration
 * one can dynamically modify the behavior of the jPhineas transport.  A filter
 * is configured from the same configuration that identifies it.
 * <p>
 * Both XML encryption and decryption filters are included here as examples,
 * since this activity is normally handled directly by the ebXML processors.
 */

package tdunnick.jphineas.filter;