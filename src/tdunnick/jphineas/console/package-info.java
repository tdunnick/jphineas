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
 * jPhineas features a web based console to perform the following functions:
 * <ul>
 * <li>Monitor and Manage Queues</li>
 * <li>Graphically Monitor Statistics - a Dashboard</li>
 * <li>Ping Configured Routes</li>
 * <li>Examine Recent Logged Events</li>
 * <li>Manage Configuration</li>
 * <li>Restart Services</li>
 * <li>Provide General Information</li>
 * </ul>
 * <p>
 * The console servlet "controller" is mapped to a HTML page in web.xml for 
 * each of the above functions. A "model" is called, based on the URL, and 
 * is expected to return a data structure. This data is then embedded in the 
 * request object and passed to a corresponding JSP "view" to complete the 
 * MVC paradigm.
 */

package tdunnick.jphineas.console;