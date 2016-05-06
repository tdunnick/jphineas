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

package tdunnick.jphineas.common;

public class JPhineas
{
  public static final String name = "jPhineas";
  public static final String revision = "1.02 alpha";
  public static final String updated = "May 6, 2016";
  public static final String copyright = "&copy; 2015,2016 Thomas L Dunnick" +
    " - all rights reserved";
  public static final String projectUrl = "https://github.com/tdunnick/jphineas";
  public static final String homeUrl = "https://mywebspace.wisc.edu/tdunnick/web";
  
  public static String getHtml ()
  {
  	return name + " " + revision + " " + updated + "<br/>"
  	  + "Copyright " + copyright + "<br/>"
  	  + "<a href=\"" + projectUrl + "\">" + projectUrl + "</a><br/>"
  	  + "<a href=\"" + homeUrl + "\">" + homeUrl + "</a><br/>";
  }

}
