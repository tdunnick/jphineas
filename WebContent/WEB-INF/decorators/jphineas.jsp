<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<%@ taglib uri="http://www.opensymphony.com/sitemesh/decorator" prefix="dec" %>
<%@ page import="tdunnick.jphineas.common.JPhineas" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<!--  
  Copyright (c) 2015 Thomas Dunnick (https://mywebspace.wisc.edu/tdunnick/web)
  
  This file is part of jPhineas.

  jPhineas is free software: you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.

  jPhineas is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with jPhineas.  If not, see <http://www.gnu.org/licenses/>.
-->
<html>
<head>
  <meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
  <title><dec:title default="jPhineas" /></title>
  <link rel="stylesheet" type="text/css" 
    href="${pageContext.request.contextPath}/css/jphineas.css" />
  <dec:head />
  <script type="text/javascript">
  function askRestart ()
  {
    if (confirm ("Restart jPHINEAS?"))
    {
      deleteCookie (null);
      window.location = "${pageContext.request.contextPath}/console/restart.html";
      window.location.reload (true);
    }
  }
  function deleteCookie(cname) // delete a cookie
  {
    var cookies = document.cookie.split(";");

    for (var i = 0; i < cookies.length; i++) 
    {
      var cookie = cookies[i].replace (/^\s+/, "");
      var eqPos = cookie.indexOf("=");
      var name = eqPos > -1 ? cookie.substr(0, eqPos) : cookie;
      if ((cname == null) || (cname == name))
      {
        document.cookie = name + "=deleted;expires=" + 
          new Date(0).toUTCString() + ";path=/";
      }
    }
  }
  </script>
</head>
<body>
  <div class="navigation">
  <%-- The top of the page holds basic info and navigation icons --%>
  <table summary="Heading">
    <tr>
      <%-- the logo goes on the left side of the heading --%>
      <td>
        <img src="${pageContext.request.contextPath}/images/phineas.gif" height="96" alt="jPhineas logo">
      </td>
      <%-- the title and navigation go on the right side --%>
      <td>
        <table>
          <%-- identification and title on top --%>
          <tr>
            <td>
              <big><big><b>
                <%= JPhineas.name %> <%= JPhineas.revision %>
              </b></big></big> 
              <%= JPhineas.updated %>
            </td>
            <td id="title">
              <dec:getProperty property="page.pagetitle" 
                default="PHIN-MS Compatible Messaging"/>
            </td>
          </tr>
        </table>
        <%-- navigation icons underneath --%>
        <table summary="Navigation">
         <tr>
           <td>
              <a href="${pageContext.request.contextPath}/console/queues.html">
              <img alt="show queues" src="${pageContext.request.contextPath}/images/queue.gif"></a>
            </td>
           <td>
              <a href="${pageContext.request.contextPath}/console/dashboard.html">
              <img src="${pageContext.request.contextPath}/images/dashboard.gif" alt="dashboard"></a>
            </td>
            <td>
              <a href="${pageContext.request.contextPath}/console/ping.html">
              <img alt="ping route" src="${pageContext.request.contextPath}/images/ping.gif"></a>
            </td>
            <td>
              <a href="${pageContext.request.contextPath}/console/logs.html">
              <img alt="show configuration" src="${pageContext.request.contextPath}/images/loop.gif"></a>
            </td>
            <td>
              <a href="${pageContext.request.contextPath}/console/config.html">
              <img alt="configure" src="${pageContext.request.contextPath}/images/build.gif"></a>
            </td>
            <td>
              <a href="javascript:window.print();">
              <img alt="print" src="${pageContext.request.contextPath}/images/print.gif"></a>
            </td>
            <td>
              <a href="javascript:window.location.reload(true);">
              <img alt="refresh" src="${pageContext.request.contextPath}/images/refresh.gif"></a>
            </td>
            <!--
            <td>
              <a href="${pageContext.request.contextPath}/console/delete">
              <img alt="delete" src=
              "${pageContext.request.contextPath}/images/delete.gif"></a>
            </td>
            -->
            <td>
              <a href="javascript:askRestart();">
              <img alt="restart" src="${pageContext.request.contextPath}/images/start.gif" id="restart"></a>
            </td>
             <td>
              <a href="${pageContext.request.contextPath}/index.html">
              <img alt="help" src="${pageContext.request.contextPath}/images/help.gif"></a>
            </td>
          </tr>
        </table>
      </td>
    </tr>
  </table>
  </div>
  <hr style="clear:both"/>
  <%-- the body of decorated page gets filled in here --%>
  <dec:body />
  <%-- finally, a common footer with copyright, etc --%>
  <hr style="clear:both"/>
  <div class="footer">
     Copyright <%= JPhineas.copyright %><p>
     For additional information please see
     <a href="<%= JPhineas.projectUrl %>" target="_blank">
      <%= JPhineas.projectUrl %></a>.
  </div> 
</body>
</html>