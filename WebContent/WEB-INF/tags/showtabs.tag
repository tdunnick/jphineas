<%@ taglib tagdir="/WEB-INF/tags" prefix="config" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ attribute name="tabs" required="true" type="java.util.ArrayList" %>
<%@ attribute name="id" required="true" %>

<c:if test="${!empty tabs}">
  <div id="${id}" class="configlayout">
   <!-- tab navigation ${id}-->
    <ul id="${id}-nav" class="configlayout">
    <c:forEach items="${tabs}" var="t" varStatus="s">
      <li onMouseOver="showHelp(this,'${t.help}')" onMouseOut="hideHelp()">
        <a href="#${id}_${s.index}">${t.name}</a>
      </li>
    </c:forEach>
    </ul>
 
    <!-- tab bodies ${id} -->
    <div class="tabs-container">
    <c:forEach items="${tabs}" var="t" varStatus="s">
      <div id="${id}_${s.index}" class="t_${id}">
        <config:showform inputs="${t.inputs}" />
        <config:showtabs tabs="${t.tabs}" id="${id}:${s.index}"/>
      </div>
    </c:forEach>
    </div>  
   </div>
   <script type="text/javascript">
    addTabber(new Yetii ({id:"${id}",tabclass:"t_${id}",persists:true}));
   </script> 
</c:if>