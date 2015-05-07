<%@ taglib tagdir="/WEB-INF/tags" prefix="config" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ attribute name="inputs" required="true" type="java.util.ArrayList" %>

<c:if test="${!empty inputs}">
  <form method="post" action="#">
    <table>
    <c:forEach items="${inputs}" var="n">
      <c:choose>
        <c:when test="${n.type eq 'hidden'}">
          <input type="hidden" name="${n.name}" value="${n.value}" />
          <c:set var="prefix" value="${n.value}"/>
        </c:when>
        <c:when test="${n.type eq 'radio'}">
          <tr><td>&nbsp;&nbsp;&nbsp;</td>
          <td><c:out value="${n.prompt}"/></td>
          <td onMouseOver="showHelp(this,'${n.help}')" onMouseOut="hideHelp()" >
            <c:forEach items="${n.options}" var="o">
               <input type="radio" name="${n.name}" value="${o}" 
                 <c:if test="${o eq n.value}">checked</c:if>
               />
               <c:out value="${o}"/>
            </c:forEach>
          </td></tr>
        </c:when>
        <c:when test="${n.type eq 'select'}">
          <tr><td>&nbsp;&nbsp;&nbsp;</td>
          <td><c:out value="${n.prompt}"/></td>
          <td onMouseOver="showHelp(this,'${n.help}')" onMouseOut="hideHelp()">
            <select name="${n.name}" size="1" >
            <c:forEach items="${n.options}" var="o">
              <option value="${o}" 
                <c:if test="${o eq n.value}">selected</c:if> >
                <c:out value="${o}" />
             </option>
          </c:forEach>
          </select></td></tr>
        </c:when>
        <c:when test="${n.type eq 'submit'}">
          <tr><td colspan="3">
          <c:forEach items="${n.options}" var="v">
            <input type="submit" name="${n.name}" value="${v}" />&nbsp;
          </c:forEach>
          </td></tr>
          </c:when>
        <c:otherwise>
          <tr><td>&nbsp;&nbsp;&nbsp;</td>
          <td><c:out value="${n.prompt}"/></td>
          <td onMouseOver="showHelp(this,'${n.help}')" onMouseOut="hideHelp()" >
            <input type="${n.type}" name="${n.name}" value="${n.value}" 
              <c:if test="${n.width gt 0}">size="${n.width}"</c:if>
            />
          </td></tr>
        </c:otherwise>
      </c:choose>
    </c:forEach>
    </table>
  </form>
</c:if>