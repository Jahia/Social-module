<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="social" uri="http://www.jahia.org/tags/socialLib" %>
<%@ taglib prefix="uiComponents" uri="http://www.jahia.org/tags/uiComponentsLib" %>

<jcr:nodeProperty node="${currentNode}" name="j:connectionSource" var="connectionSource"/>
<jcr:nodeProperty node="${currentNode}" name="j:activitiesLimit" var="activitiesLimit"/>
<jcr:nodeProperty node="${currentNode}" name="j:activityTypes" var="activityTypes"/>
<c:forEach items="${activityTypes}" var="activityType" varStatus="status">
    <c:choose>
        <c:when test="${status.first}">
            <c:set var="activityTypesStr" value="${activityType.string}" />
        </c:when>
        <c:otherwise>
            <c:set var="activityTypesStr" value="${activityTypesStr},${activityType.string}" />
        </c:otherwise>
    </c:choose>
</c:forEach>
<c:set var="bindedComponent"
       value="${uiComponents:getBindedComponent(currentNode, renderContext, 'j:bindedComponent')}"/>
<c:if test="${not empty bindedComponent}">
    <c:set var="limit" value="${activitiesLimit.long}"/>
    <c:if test="${not empty pagerLimits[currentNode.identifier] and (empty limit or pagerLimits[currentNode.identifier] < limit)}">
        <c:set var="limit" value="${pagerLimits[currentNode.identifier]}"/>
    </c:if>

    <c:choose>
        <c:when test="${jcr:isNodeType(bindedComponent, 'jnt:user')}">
            <social:get-connections var="connections" path="${bindedComponent.path}"/>
            <social:get-activities var="currentList" sourcePaths="${connections}" limit="${limit}"
                                   activityTypes="${activityTypesStr}" />
        </c:when>
        <c:otherwise>
            <c:set var="creationDate" value="${bindedComponent.properties['jcr:created']}"/>
            <social:get-activities var="currentList" pathFilter="${bindedComponent.path}" limit="${limit}"
                                   activityTypes="${activityTypesStr}"
                                   startDate="${not empty creationDate ? creationDate.time.time : '0'}"/>
        </c:otherwise>
    </c:choose>

    <jsp:useBean id="pagerLimits" class="java.util.HashMap" scope="request"/>
    <c:set target="${pagerLimits}" property="${currentNode.identifier}_loaded" value="${fn:length(currentList)}"/>

    <c:set target="${moduleMap}" property="editable" value="false" />
    <c:set target="${moduleMap}" property="currentList" value="${currentList}" />
    <c:set target="${moduleMap}" property="end" value="${fn:length(moduleMap.currentList)}" />
    <c:set target="${moduleMap}" property="listTotalSize" value="${moduleMap.end}" />

</c:if>
