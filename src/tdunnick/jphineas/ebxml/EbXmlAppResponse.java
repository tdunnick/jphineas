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

package tdunnick.jphineas.ebxml;

import tdunnick.jphineas.mime.MimeContent;
import tdunnick.jphineas.xml.ResponseXml;

/**
 * Manage the application response part of an ebXML message
 * 
 * @author Thomas Dunnick
 * 
 */
public class EbXmlAppResponse
{
	String status = "success",
	       error = "none",
	       appData = "none";
	
	public void reset ()
	{
		status = "success";
    error = "none";
    appData = "none";
	}
	
	public void set (MimeContent r)
	{
		ResponseXml resp = new ResponseXml (r.getBody());
		status = resp.getStatus();
		error = resp.getError();
		appData = resp.getAppData();
	}
	
	public void set (String status, String error, String appdata)
	{
		this.status = status;
		this.error = error;
		this.appData = appdata;
	}
	
	public MimeContent get ()
	{
		MimeContent r = new MimeContent ();
		r.setContentId ("<statusResponse@cdc.gov>");
		r.setContentType(MimeContent.XML);
		ResponseXml resp = new ResponseXml ();
		resp.setStatus(status);
		resp.setError(error);
		resp.setAppData(appData);
		r.setBody (resp.toString());
		return r;
	}
	
	public MimeContent get (String status, String error, String appdata)
	{
		set (status, error, appdata);
		return get ();
	}
	
	public String toString ()
	{
		return get().toString ();
	}

	public void setStatus (String status)
	{
		this.status = status;
	}
	
	public String getStatus ()
	{
		return status;
	}
	
	public void setError (String error)
	{
		this.error =  error;
	}
	
	public String getError ()
	{
		return error;
	}

	public void setAppData (String appdata)
	{
		this.appData = appdata;
	}
	
  public String getAppData ()
	{
		return appData;
	}
}
