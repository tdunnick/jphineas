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
import tdunnick.jphineas.sender.*;
import tdunnick.jphineas.xml.*;

class RouteInfoTest extends GroovyTestCase
{
	def confg = """<?xml version="1.0" encoding="UTF-8"?>
	<Sender>
	  <RouteInfo>
	    <Route>
	      <Name>foo</Name>
	      <PartyId>fooparty</PartyId>
	      <Cpa>foocpa</Cpa>
	      <Host>foohost</Host>
	      <Path>foopath</Path>
	      <Port>100</Port>
	      <Protocol>HTTP</Protocol>
	      <Timeout>100</Timeout>
	      <Retry>10</Retry>
	      <Authentication>
	        <Type>keystore</Type>
	        <Id>keyid</Id>
	        <Password>keypass</Password>
	        <Unc>keypath</Unc>
	      </Authentication>
        <Packager>test.TestPackager</Packager>
	    </Route>
		  <Route>
			  <Name>bar</Name>
			  <Processor>tdunnick.jphineas.sender.file.FileRouteProcessor</Processor>
			  <PartyId>barid</PartyId>
			  <Cpa>barcpa</Cpa>
			  <Host>barhost</Host>
			  <Path>barpath</Path>
			  <Port>200</Port>
			  <Protocol>HTTPs</Protocol>
			  <Timeout>200</Timeout>
			  <Retry>20</Retry>
			  <Authentication>
			    <Type>cert</Type>
			    <Unc>certpath</Unc>
			  </Authentication>
		    <Packager>test.TestPackager</Packager>
		</Route>
	  </RouteInfo>
	</Sender>"""
	
	XmlConfig cfg = null;
	RouteInfo inf = new RouteInfo ();
	
	protected void setUp() throws Exception
	{
		cfg = new XmlConfig ()
		cfg.load (confg.toString())
		LogConfig dflt = Log.getLogConfig();
		dflt.setLogLevel(LogConfig.DEBUG)
		dflt.setLogLocal(true)
		dflt.setLogStream (null)
	}
	
	public final void testSetRouteInfo()
	{
		// assert cfg.getValue ("Name") != null : "failed loading test configuration"
		assert inf.configure (cfg.copy ("RouteInfo.Route[0]")): "Failed 1st configuration"
		assert inf.getName().equals("foo") : "name doesn't match foo"
		assert inf.getTimeout() == 100 : "timeout doesn't match 100"
		assert inf.getRetry() == 10 : "retry doesn't match 10"
		assert inf.getProcessor() != null : "no processor loaded"
		assert inf.getProcessor().getClass().getName().equals("tdunnick.jphineas.sender.ebxml.EbXmlRouteProcessor") :  "default processor incorrect"

		assert inf.configure (cfg.copy ("RouteInfo.Route[1]")): "Failed 2nd configuration"
		assert inf.getName().equals("bar") : "name doesn't match bar"
		assert inf.getTimeout() == 200 : "timeout doesn't match 200"
		assert inf.getRetry() == 20 : "retry doesn't match 20"
		assert inf.getProcessor () != null : "no processor loaded"
		assert inf.getProcessor().getClass().getName().equals("tdunnick.jphineas.sender.file.FileRouteProcessor") : "expected File processor"
	}
}
