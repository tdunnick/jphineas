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

package tdunnick.jphineas.console.queue;

import tdunnick.jphineas.util.*;

public class DashBoardCache
{
  private static Cache cache = new Cache();
  
  private DashBoardCache ()
  {
  }
  
  public static synchronized void put (String id, DashBoardData d)
  {
	  cache.put(id, d, 5);
  }
  
  public static synchronized DashBoardData get (String id)
  {
  	return (DashBoardData) cache.get(id); 
  }
}
