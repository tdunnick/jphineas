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

package tdunnick.jphineas.sender.file;

import java.io.*;

import tdunnick.jphineas.config.RouteConfig;
import tdunnick.jphineas.logging.*;
import tdunnick.jphineas.queue.*;
import tdunnick.jphineas.sender.*;
import tdunnick.jphineas.xml.*;

public class FileRouteProcessor extends RouteProcessor
{
	private String host = null;
	private String path = null;
	
	public boolean configure (RouteConfig config)
	{
		host = config.getHost();
		path = config.getPath();
		return true;
	}
	
	public boolean process (PhineasQRow row)
	{
		if (row == null)
			return false;
		try
		{
			FileOutputStream out = new FileOutputStream("//" + host + path);
			FileInputStream in = new FileInputStream(row.getPayLoadFile());
			int c;
			while ((c = in.read()) >= 0)
				out.write(c);
			in.close();
			out.close();
			row.setTransportStatus ("success");
			row.setTransportErrorCode("none");
		}
		catch (IOException e)
		{
			Log.error("Couldn't send file", e);
			return false;
		}
		return true;
	}
}
