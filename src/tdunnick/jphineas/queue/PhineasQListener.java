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

package tdunnick.jphineas.queue;

import java.io.File;

import javax.servlet.*;

import tdunnick.jphineas.config.XmlConfig;
import tdunnick.jphineas.logging.Log;


/**
 * This is a simple listener used to (re)configure the PhineasQManager.
 * It expects a context parameter for the queue configuration:
 * 
 * <pre>
 *   &lt;context-param&gt;<br>
 *     &lt;param-name&gt;DBUSER&lt;/param-name&gt;
 *     &lt;param-value&gt;pankaj&lt;/param-value&gt;
 *   &lt;/context-param&gt;
 * </pre>
 * 
 * @author Thomas Dunnick tdunnick@wisc.edu
 *
 */
public class PhineasQListener implements ServletContextListener
{
	public void contextDestroyed(ServletContextEvent ev)
	{
		PhineasQManager.getInstance().close();
	}
	
	public void contextInitialized(ServletContextEvent ev)
	{
		ServletContext ctx = ev.getServletContext ();
		String configname = ctx.getInitParameter("Configuration");
		PhineasQManager.getInstance().configure(configname);
	}

}
