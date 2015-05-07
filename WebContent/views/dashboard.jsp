<!--  
  Copyright (c) 2012-2013 Thomas Dunnick (https://mywebspace.wisc.edu/tdunnick/web)
  
  This file is part of PhinmsX.

  PhinmsX is free software: you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.

  PhinmsX is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with PhinmsX.  If not, see <http://www.gnu.org/licenses/>.
-->
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<c:set var="id" value="${pageContext.session.id}"/>
<c:set var="m" value="${requestScope.dashboard}" />
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">

<html>
<head>
  <meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
  <title>PhinmsX Dashboard</title>
  <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/calendar.css" />
  <script type="text/javascript" src="${pageContext.request.contextPath}/script/calendar.js"></script>
</head>
<body>
<content tag="pagetitle">Dashboard</content>
<content tag="pageinfo"><c:out value="${m.version}"/></content>
<center>
  <span class="dashboard">
    <c:out value="${m.table}"/>
    <c:if test="${not empty m.constraint}">
      - <c:out value="${m.constraint}"/>
    </c:if>
  </span>

</center>
<hr/>
<div class="dashboard">
<table>
  <tr>
    <td>
      <c:out value="${m.piechart[1]}" escapeXml="false" />
      <center>
        <img 
          <c:out value="src=\"images/piechart_${id}.png\"" escapeXml="false" />
           alt="a chart" usemap="#pie" /><br/>
         ending
         <span style="border:solid 1px;background:#ffffff;"
           onClick='pickDate(this, "<c:out value="${m.date}" />", "cal")' >
           <c:out value="${m.date}" />
         </span>
         <span class="calendar" id="cal"></span>
        &nbsp; for 
        <input type="text" value='<c:out value="${m.days}"/>' size='1' 
          onChange='window.location.href="?days=" + this.value' />
        days    
      </center>
      <br/>
      <c:forEach items="${m.tables}" var="t">
         <a href='?table=<c:out value="${t[0]}"/>'><c:out value="${t[0]}"/></a><br/>
      </c:forEach>
    </td>
    <td>
      <center>
        <img 
          <c:out value="src=\"images/linechart_${id}.png\"" escapeXml="false" />
           alt="a chart" />
      </center>
    </td>
      <!--  this side gets the list of queues/routes/etc -->
    <td>
      <b>Message Statistics</b><br/>
      <c:out value="${m.interval}" /> interval
      <p>
      Total: <c:out value="${m.total}"/><br/>
      Min: <c:out value="${m.min}"/><br/>
      Max: <c:out value="${m.max}"/><br/>
      Avg: <c:out value="${m.total / 5}"/><br/>
      </p>
    </td>
  </tr>
</table>
<hr style="clear:both"/>
<div style="width:98%">
  <img 
    <c:out value="src=\"images/barchart_${id}.png\"" escapeXml="false" />
     alt="a chart"/>
</div>
</div>
</body>
</html>