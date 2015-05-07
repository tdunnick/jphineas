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



import junit.framework.Test;
import junit.framework.TestSuite;

public class PhineasTests
{
	public static Test suite()
	{
		TestSuite suite = new TestSuite("Phineas tests");
		suite.addTestSuite(ByteArrayTest.class);
		suite.addTestSuite(EncryptorTest.class);
		suite.addTestSuite(XmlContentTest.class);
		suite.addTestSuite(XmlConfigTest.class);
		suite.addTestSuite(LogTest.class);
		suite.addTestSuite(EncryptorFilterTest.class);
		suite.addTestSuite(MimeContentTest.class);
		suite.addTestSuite(MimeReceiverTest.class);
		suite.addTestSuite(PhineasQTest.class);
		suite.addTestSuite(PriorityBlockingQTest.class);
		suite.addTestSuite(RouteInfoTest.class);
		suite.addTestSuite(SoapXmlTest.class);
		return suite;
	}
}
