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

<c:set var="m" value="${requestScope.queues}" />

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
  <meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
  <title>jPhineas Queue Monitor</title>
</head>
<body>
  <content tag="pagetitle">Queue Monitor</content>
  <content tag="pageinfo"><c:out value="${m.version}"/></content>
  <h2><c:out value="${m.table}"/>
  <c:if test="${not empty m.constraint}">
    <c:out value="${m.constraint}"/>
  </c:if>
  </h2>
<%-- right side is the list of queues and routes/parties to select for display --%>
<div class='queuetables'>
  <c:forEach items="${m.tables}" var="t">
     <a href='?table=<c:out value="${t[0]}"/>'><c:out value="${t[0]}"/></a><br/>
     <c:if test="${m.table == t[0]}">
      <c:forEach items="${t[1]}" var="c">
         <span>
          <a href='?constraint=<c:out value="${c}"/>'><c:out value="${c}"/></a>
         </span><br/>
     </c:forEach>
     </c:if>
  </c:forEach>
</div>
<%--  left side gets the row table, followed by the row details --%>
<div class="queuerows">
  <c:if test="${not empty m.rows}">
  <div class="records">
    <table>
      <tr>
  	  <c:forEach items="${m.rowfields}" var="n">
  	    <th><c:out value="${n}"/></th>
  	  </c:forEach>
      </tr>
      <c:forEach items="${m.rows}" var="r" varStatus="rs">
        <tr class="${m.rowClass[rs.index]}"
          <c:if test="${m.recordId == r[0]}">
            style="font-weight:bold;"
          </c:if>
        >
        <c:forEach items="${r}" var="n" varStatus="s">
          <td>
             <c:if test="${empty n}">
               &nbsp;
             </c:if>
             <c:if test="${not empty n}">
               <c:if test="${s.first}">
                 <a href='?recordId=<c:out value="${n}"/>'>
               </c:if>
               <c:out value="${n}"/>
               <c:if test="${s.first}">
                 </a>
               </c:if>
             </c:if>
          </td>
        </c:forEach>
        </tr>
      </c:forEach>
    </table>
  </div>
  <%-- navigation for rows shown above --%>
  <div class="button">
    <c:if test="${not empty m.next}">
      <a href="?top=<c:out value="${m.next}"/>">Next</a>
    </c:if>
    <c:if test="${not empty m.prev}">
      <a href="?top=<c:out value="${m.prev}"/>">Previous</a>
    </c:if>
  </div>
  <%-- then details of the selected row --%>
  <hr style="clear:both; margin-top:15px;" />
  <c:set value="${m.record[0]}" var="rec" />

  <table class="details">
  <c:forEach items="${m.record}" var="f" varStatus="s" >
    <tr>
      <td><b><c:out value="${m.fields[s.index]}"/>:</b>&nbsp;</td>
      <td><c:out value="${f}"/>
      <c:if test="${s.index eq 0}">
        <span>
          <a href='?delete=<c:out value="${rec}"/>'>
          <img src="${pageContext.request.contextPath}/images/delete.gif" />
          Delete</a>
          <c:if test="${m.resend}">
             <a href='?resend=<c:out value="${rec}"/>'>
            <img src="${pageContext.request.contextPath}/images/resend.gif" style="width:30px; height:30px"/>
            Resend</a>
          </c:if>
        </span>      
      </c:if>
      </td>
    </tr>   
	  </c:forEach>
  </table>
  </c:if>
  <c:if test="${empty m.rows}">
    No records found!
  </c:if>
</div>
</body>
</html>