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

package tdunnick.jphineas.console;

import java.util.*;
import tdunnick.jphineas.logging.*;

/**
 * Generates the error object for display to user.
 * 
 * @author Thomas Dunnick
 *
 */
public class ErrorData
{
	private ArrayList <String> message = new ArrayList <String> ();
	  
  private boolean getLogErrors ()
  {
  	HashMap <String, LogContext> configs = Log.getLoggers();
  	Iterator <String> it = configs.keySet().iterator();
  	while (it.hasNext())
  	{
  		String k = it.next();
  		LogContext cfg = configs.get (k);
	  	ArrayList <String> log = cfg.getLog();
	  	for (int i = 0; i < log.size(); i++)
	  	{
	  		String e = log.get(i);
	  		if (e.contains ("ERROR"))
	  			message.add(e);
	  	}
  	}
  	if (message.size () == 0)
  		return false;
  	// sort by date and reverse
  	String[] a = message.toArray(new String[0]);
  	Arrays.sort (a);
  	message.clear();
  	int i = a.length;
  	while (i-- > 0)
  		message.add (a[i]);
  	return true;
  }
  
  public ArrayList <String> getMessage ()
  {
  	if ((message.size() == 0) && !getLogErrors ())
  		message.add ("Unknown Error");
  	return message;
  }
  
  public void addMessage (String m)
  {
  	message.add (m);
  }
  
  public void setMessage (ArrayList <String> m)
  {
  	message = m;
  }
}
