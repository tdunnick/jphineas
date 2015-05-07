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
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="config" %>   
<c:set var="p" value="${requestScope.ping}" />
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
  <meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
  <title>jPhineas Ping</title>
</head>
<body>
  <content tag="pagetitle"><c:out value="${p.title}"/></content>
  <content tag="pageinfo"><c:out value="${p.version}"/></content>
  <c:if test="${empty p.routes}">
    <h3>Sorry, no Routes have been defined</h3>
  </c:if>
  <c:if test="${not empty p.routes}">
    <form method="post" action="#">
    <table><tr style="vertical-align:top">
      <td>
      <p>Select a ROUTE &nbsp;&nbsp;<input type="submit" name="_action_" value="Export"/></p><br>
      <c:forEach items="${p.routes}" var="r">
        <input type="radio" name="Route" value="${r}"/>${r}<br>
      </c:forEach>
      </td>
      
      <c:if test="${not empty p.queues}">
      <td width="40px">&nbsp;</td>
      <td>
      <p>Select a QUEUE &nbsp;&nbsp;<input type="submit" name="_action_" value="Ping"/></p>
       <c:forEach items="${p.queues}" var="r">
        <input type="radio" name="Queue" value="${r}"/>${r}<br>
      </c:forEach>
      </td>
      </c:if>
    </tr></table>
    </form>
  </c:if>
</body>
</html>