/*
 *  Copyright (c) 2015 Thomas Dunnick (https://mywebspace.wisc.edu/tdunnick/web)
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
import java.io.*;

import tdunnick.jphineas.logging.Log;

/**
 * Generates the error object for display to user.
 * 
 * @author Thomas Dunnick
 *
 */
public class ErrorData
{
	private ArrayList <String> message = new ArrayList <String> ();
	  
  private boolean loadErrors (String fn)
  {
  	Log.debug ("Reading " + fn);
  	try
  	{
  		BufferedReader inp = new BufferedReader (new FileReader (fn));
  		String s;
  		while ((s = inp.readLine()) != null)
  		{
  			if (s.indexOf ("ERROR") >= 0)
  			{
  				s = s.replace ("<", "&lt;");
  				message.add (0, s.replace (">", "&gt;"));
  			}
  		}
  		inp.close();
  	}
  	catch (IOException e)
  	{
  		Log.error ("Can't read error from " + fn + " - " + e.getMessage());
  		return false;
  	}
  	return true;
  }
  
  public ArrayList <String> getMessage ()
  {
  	if (message.size() == 0)
  		message.add ("Unknown Error");
  	return message;
  }
  
  public void setMessage (ArrayList <String> m)
  {
  	message = m;
  }
}
