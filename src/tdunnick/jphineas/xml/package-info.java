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
 * jPhineas includes utility classes for managing all things XML.
 * The underlying object is a {@link org.w3c.dom.Document W3C Document}.
 * <p>
 * The {@link tdunnick.jphineas.xml.XmlContent XmlContent} class provides a simplified
 * view of a document with access methods using syntax similar to properties.  XML tags
 * can be combined using dot separators and indexed like an array using square
 * brackets enclosing a number to form a "path" to a given node (value).  
 * Thus a single node in the XML can be read, created, or written with a single 
 * method call.
 * <p>
 * Additional classes exist for creating and accessing XML that is associated with
 * ebXML including SOAP, encryption, responses, etc.  These classes include the
 * "template" XML and methods to expose needed parts.  This segregates the specific
 * tag names to the XML of interest.
 * 
 */
package tdunnick.jphineas.xml;