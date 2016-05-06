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

package tdunnick.jphineas.console.logs;

import java.util.*;
import javax.servlet.http.*;
import tdunnick.jphineas.logging.*;

public class LogModel
{
  public LogData getLogData (HttpServletRequest request)
  {
  	LogData logs = new LogData ();
  	HashMap <String, LogContext> configs = Log.getLoggers ();
  	ArrayList <LogContext> lc = new ArrayList <LogContext> ();
  	Iterator <String> it = configs.keySet().iterator();
  	while (it.hasNext())
  	{
  		LogContext l = configs.get (it.next());
  		if (lc.contains(l))
  		{
  			// System.out.println ("*** Skipping log " + l.getLogId());
  			continue;
  		}
  		// System.out.println ("*** Adding log " + l.getLogId());
  		lc.add(l);
  	}
  	logs.logs = lc;
  	return logs;
  }
}
