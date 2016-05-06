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

package tdunnick.jphineas.xml;


/**
 * This manages the response part of an ebXML package.  The response is returned
 * by the receiver with Mime ID "statusResponse@cdc.gov"
 * 
 * @author Thomas Dunnick
 *
 */
public class ResponseXml extends XmlContent
{
  private final static String template =
  	"<response><msh_response><status/><error/><appdata/></msh_response></response>";
  private final static String status = "response.msh_response.status";
  private final static String error = "response.msh_response.error";
  private final static String appdata = "response.msh_response.appdata";

  /**
   * Create a new empty response
   */
  public ResponseXml ()
  {
  	load (template);
  }
  
  /**
   * Parse a response
   * @param xml
   */
  public ResponseXml (String xml)
  {
  	load (xml);
  }
  
  /**
   * check if this is a valid response
   * @return true if valid
   */
  public boolean ok ()
  {
		return getElement ("response") != null;
  }
  
  private String get (String tag)
  {
  	String s = getValue (tag);
  	if (s == null) return "";
  	return s;
  }
  
  /**
   * Get the status of the response
   * @return the status, or empty string if null
   */
  public String getStatus ()
  {
  	return get (status);
  }
  
  /**
   * Set the status value
   * @param value of status
   * @return true if successful
   */
  public boolean setStatus (String value)
  {
  	return setValue (status, value);
  }
  
  /**
   * Get the error of the response
   * @return the error, or empty string if null
   */
  public String getError()
  {
  	return get (error);
  }
  
  /**
   * Set the error value
   * @param value of error
   * @return true if successful
   */
  public boolean setError (String value)
  {
  	return setValue (error, value);
  }
  
  /**
   * Get the appdata of the response
   * @return the appdata, or empty string if null
   */
  public String getAppData ()
  {
  	return get (appdata);
  }
  
  /**
   * Set the appdata value
   * @param value of status
   * @return true if successful
   */
  public boolean setAppData (String value)
  {
  	return setValue (appdata, value);
  }
}
