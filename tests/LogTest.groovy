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

;

import groovy.util.GroovyTestCase;
import tdunnick.jphineas.logging.*;

class LogTest extends GroovyTestCase
{
	LogConfig dflt = Log.getLogConfig();
	OutputStream out = new ByteArrayOutputStream ();
	
	protected void setUp() throws Exception
	{
		dflt.setLogLevel (LogConfig.DEBUG)
		dflt.setLogStream (out)
		dflt.setLogLocal (true)
	}
	
	protected void tearDown() throws Exception
	{
		//println (out.toString())
		super.tearDown();
	}
	
	public final void testGetLogConfig()
	{
		assert dflt != null : "can't get default configuration"
	}
	
	public final void testSetLogConfigString()
	{
	}
	
	public final void testSetLogConfigLogConfig()
	{
	}
	
	
	public final void testDebug()
	{
		Log.debug ("debugging ok")
		assert out.toString().length() > 0 : "debugging failed"
		// print out.toString();
	}
	
	public final void testErrorStringException()
	{
	}
	
	public final void testErrorString()
	{
		Log.debug ("error ok")
		assert out.toString().length() > 0 : "errors failed"
	}
	
	public final void testWarn()
	{
		Log.debug ("warn ok")
		assert out.toString().length() > 0 : "warnings failed"
	}
	
	public final void testInfo()
	{
		Log.debug ("inf ok")
		assert out.toString().length() > 0 : "info failed"
	}
}
