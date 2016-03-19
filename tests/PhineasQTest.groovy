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

import java.io.File;

import groovy.util.GroovyTestCase;
import tdunnick.jphineas.logging.*;
import tdunnick.jphineas.queue.*;

class PhineasQTest extends GroovyTestCase
{
  PhineasQManager manager;
  def qinfo = """
<Queues>
  <TypeInfo>
  <DefaultDirectory>.</DefaultDirectory>
  <Type>
    <Name>EbXmlSndQ</Name>
  	<Field id="RECORDID">RECORDID</Field>
	  <Field id="MESSAGEID">MESSAGEID</Field>
	  <Field id="PAYLOADFILE">PAYLOADFILE</Field>
	  <Field id="DESTINATIONFILENAME">DESTINATIONFILENAME</Field>
	  <Field id="ROUTEINFO">ROUTEINFO</Field>
	  <Field id="SERVICE">SERVICE</Field>
	  <Field id="ACTION">ACTION</Field>
	  <Field id="ARGUMENTS">ARGUMENTS</Field>
	  <Field id="MESSAGERECIPIENT">MESSAGERECIPIENT</Field>
	  <Field id="MESSAGECREATIONTIME">MESSAGECREATIONTIME</Field>
  	<Field id="ENCRYPTION">ENCRYPTION</Field>
  	<Field id="SIGNATURE">SIGNATURE</Field>
  	<Field id="PUBLICKEYLDAPADDRESS">PUBLICKEYLDAPADDRESS</Field>
  	<Field id="PUBLICKEYLDAPBASEDN">PUBLICKEYLDAPBASEDN</Field>
  	<Field id="PUBLICKEYLDAPDN">PUBLICKEYLDAPDN</Field>
  	<Field id="CERTIFICATEURL">CERTIFICATEURL</Field>
  	<Field id="PROCESSINGSTATUS">PROCESSINGSTATUS</Field>
  	<Field id="TRANSPORTSTATUS">TRANSPORTSTATUS</Field>
  	<Field id="TRANSPORTERRORCODE">TRANSPORTERRORCODE</Field>
	  <Field id="APPLICATIONSTATUS">APPLICATIONSTATUS</Field>
  	<Field id="APPLICATIONERRORCODE">APPLICATIONERRORCODE</Field>
	  <Field id="APPLICATIONRESPONSE">APPLICATIONRESPONSE</Field>
	  <Field id="MESSAGESENTTIME">MESSAGESENTTIME</Field>
	  <Field id="MESSAGERECEIVEDTIME">MESSAGERECEIVEDTIME</Field>
	  <Field id="RESPONSEMESSAGEID">RESPONSEMESSAGEID</Field>
	  <Field id="RESPONSEARGUMENTS">RESPONSEARGUMENTS</Field>
	  <Field id="RESPONSELOCALFILE">RESPONSELOCALFILE</Field>
	  <Field id="RESPONSEFILENAME">RESPONSEFILENAME</Field>
	  <Field id="RESPONSEMESSAGEORIGIN">RESPONSEMESSAGEORIGIN</Field>
	  <Field id="RESPONSEMESSAGESIGNATURE">RESPONSEMESSAGESIGNATURE</Field>
	  <Field id="PRIORITY">PRIORITY</Field>
  </Type>
  <Type>
    <Name>EbXmlRcvQ</Name>
   	<Field id="RECORDID">RECORDID</Field>
  	<Field id="MESSAGEID">MESSAGEID</Field>
  	<Field id="PAYLOADNAME">PAYLOADNAME</Field>
  	<Field id="LOCALFILENAME">LOCALFILENAME</Field>
  	<Field id="SERVICE">SERVICE</Field>
  	<Field id="ACTION">ACTION</Field>
  	<Field id="ARGUMENTS">ARGUMENTS</Field>
  	<Field id="FROMPARTYID">FROMPARTYID</Field>
  	<Field id="MESSAGERECIPIENT">MESSAGERECIPIENT</Field>
  	<Field id="ERRORCODE">ERRORCODE</Field>
  	<Field id="ERRORMESSAGE">ERRORMESSAGE</Field>
  	<Field id="PROCESSINGSTATUS">PROCESSINGSTATUS</Field>
  	<Field id="APPLICATIONSTATUS">APPLICATIONSTATUS</Field>
	  <Field id="ENCRYPTION">ENCRYPTION</Field>
  	<Field id="RECEIVEDTIME">RECEIVEDTIME</Field>
  	<Field id="LASTUPDATETIME">LASTUPDATETIME</Field>
  	<Field id="PROCESSED">PROCESSED</Field>						   
  </Type>
  </TypeInfo>
  <ConnectionInfo>
  <Connection>
	  <Name>HsqlInProcess</Name>
	  <Class>tdunnick.jphineas.queue.PhineasJDBCQ</Class>
	  <Id>sa</Id>
	  <Password></Password>
	  <Unc>jdbc:hsqldb:file:hsql/testdb</Unc>
	  <Driver>org.hsqldb.jdbcDriver</Driver>
	  <Flush>checkpoint</Flush>
	</Connection>
   <Connection>
    <Name>MemQ</Name>
	  <Class>tdunnick.jphineas.queue.PhineasMemQ</Class>
    <Id></Id>
    <Password></Password>
    <Unc>memq</Unc>
    <Driver></Driver>
    <Flush></Flush>
  </Connection>
  </ConnectionInfo>
  <QueueInfo> 
	<Queue>
	  <Name>MemSendQ</Name>
	  <Type>EbXmlSndQ</Type>
	  <Connection>MemQ</Connection>
	  <Table>TransportQ</Table>
	</Queue>
	<Queue>
	  <Name>MemReceiveQ</Name>
	  <Type>EbXmlRcvQ</Type>
	  <Connection>MemQ</Connection>
	  <Table>ReceiveQ</Table>
	</Queue>  
	<Queue>
	  <Name>HsqlSendQ</Name>
	  <Type>EbXmlSndQ</Type>
	  <Connection>HsqlInProcess</Connection>
	  <Table>SendQ</Table>
	</Queue>
	<Queue>
	  <Name>HsqlRcvQ</Name>
	  <Type>EbXmlRcvQ</Type>
	  <Connection>HsqlInProcess</Connection>
	  <Table>ReceiveQ</Table>
	</Queue>
	</QueueInfo>
</Queues>
"""
  
	protected void setUp() throws Exception
	{
		delPhineasdb()
		LogContext dflt = Log.getLogConfig();
		dflt.setLogLevel(LogContext.DEBUG)
		dflt.setLogLocal(true)
		dflt.setLogStream (null)
  	File f = new File ("hsql/testdb.xml")
  	def os = new FileOutputStream (f);
  	os.write (qinfo.toString().getBytes())
  	os.close()
		manager = PhineasQManager.getInstance ();
		manager.configure (f.getAbsolutePath());
	}

	protected void tearDown() throws Exception
	{
		manager.close();
	}
	
	public final void testManager ()
	{
		assert manager != null : "Manager not loaded"
	}
	
	public final void testMemQ ()
	{
		File f = new File("memq/TransportQ")
		if (f.exists())
			f.delete()
		PhineasQ q = manager.getQueue ("MemSendQ")
		assert q != null : "MemSendQ not created"
		PhineasQRow r = new PhineasQRow (q)
		for (int i = 1; i < q.numFields(); i++)
			r.setValue (i, "value_" + i)
		assert r.append () == 1 : "first record should be one"
		r.setValue(1, "second_value")
		assert r.append () == 2 : "next record should be two"
		r.setValue(1, "third_value")
		assert r.append () == 3 : "next record should be three"
		r = q.retrieve (2);
		assert r != null : "failed to retrieve row 2"
		assert r.getRowId() == 2 : "Wrong row ID (2)"
		assert r.remove() : "failed to remove row 2"
		manager.close()
	  f = new File ("hsql/testdb.xml")
		manager = PhineasQManager.getInstance();
		manager.configure (f.getAbsolutePath());
		q = manager.getQueue ("MemSendQ")
		assert q != null : "MemSendQ not created"
	  r = q.retrieve(1)
	  assert r != null : "Failed to reload TransportQ file"
	  assert r.getRowId() == 1 : "Wrong row ID (1)"
	  r = q.retrieve(3)
	  assert r != null : "Failed to retrieve record 3"
		assert r.getRowId() == 3 : "Wrong row ID (3)"
		assert r.getValue(1).equals("third_value") : "Wrong value for field 1 record 3"
	  r = q.retrieve(2)
	  assert r == null : "Record 2 still exists"
		manager.close()
		f.delete()		
	}
	
	private void delPhineasdb()
	{
		File f = new File ("hsql");
		if (!f.isDirectory())
			return;
		File[] l = f.listFiles();
		if (l == null)
			return;
		for (int i = 0; i < l.length; i++)
		{
			f = l[i];
			if (f.getName().startsWith("testdb"))
			{
				f.delete();
			}
		}
	}
	
	public final void testJDBCQ ()
	{
		PhineasQ q = manager.getQueue ("HsqlRcvQ")
		assert q != null : "HsqlRcvQ not created"
		PhineasQRow r = new PhineasQRow (q)
		for (int i = 1; i < q.numFields(); i++)
			r.setValue (i, "value_" + i)
		r.setProcessingStatus ("inprogress")
		int id = r.append ();
		assert id == 1 : "first record should be 1 but got " + id 
		PhineasQRow[] l = q.findProcessingStatus ("done")
		assert l != null : "can't find any done records"
		assert l.length == 0 : "expected 0 found " + l.length + " records"
		l = q.findProcessingStatus ("inprogress")
		assert l != null : "can't find any inprogress records"
		assert l.length == 1 : "expected 1 found " + l.length + " records"
		r.setValue(1, "second_value")
		r.setProcessingStatus ("inprogress")
		assert r.append () == 2 : "next record should be two"
		r.setValue(1, "third_value")
		assert r.append () == 3 : "next record should be three"
		r = q.retrieve (2);
		assert r != null : "failed to retrieve row 2"
		assert r.remove() : "failed to remove row 2"
	  r = q.retrieve(2)
	  assert r == null : "Record 2 still exists"
  	l = q.findProcessingStatus ("inprogress")
		assert l != null : "can't find any inprogress records"
		assert l.length == 2 : "expected 2 found " + l.length + " records"
	}
}
