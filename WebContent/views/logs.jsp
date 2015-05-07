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
<c:set var="l" value="${requestScope.logs}" />
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
  <meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
  <title>jPhineas Configuration</title>
  <script type="text/javascript" src="${pageContext.request.contextPath}/script/yetii.js"></script>
  <script type="text/javascript" src="${pageContext.request.contextPath}/script/config.js"></script>
</head>
<body>
  <content tag="pagetitle"><c:out value="${l.title}"/></content>
  <content tag="pageinfo"><c:out value="${l.version}"/></content>
   <c:if test="${!empty logs}">
    <div id="log" class="loglayout">
     <%-- tab navigation log --%>
      <ul id="log-nav" class="configlayout">
      <c:forEach items="${l.logs}" var="t" varStatus="s">
        <li>
          <a href="#$log_${s.index}">${t.logId}</a>
        </li>
      </c:forEach>
      </ul>
   
      <%-- tab bodies log --%>
      <div class="tabs-container">
      <c:forEach items="${l.logs}" var="t" varStatus="s">
        <div id="log_${s.index}" class="t_log">
          <c:forEach items="${t.log}" var="line" >
            <c:out value="${line}" /><br>
          </c:forEach>
        </div>
      </c:forEach>
      </div>  
     </div>
     <script type="text/javascript">
      addTabber(new Yetii ({id:"log",tabclass:"t_log",persists:true}));
     </script> 
  </c:if>  
</body>
</html>