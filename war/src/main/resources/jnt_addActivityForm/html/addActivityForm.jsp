<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib prefix="uiComponents" uri="http://www.jahia.org/tags/uiComponentsLib" %>

<%--@elvariable id="currentNode" type="org.jahia.services.content.JCRNodeWrapper"--%>
<%--@elvariable id="out" type="java.io.PrintWriter"--%>
<%--@elvariable id="script" type="org.jahia.services.render.scripting.Script"--%>
<%--@elvariable id="scriptInfo" type="java.lang.String"--%>
<%--@elvariable id="workspace" type="java.lang.String"--%>
<%--@elvariable id="renderContext" type="org.jahia.services.render.RenderContext"--%>
<%--@elvariable id="currentResource" type="org.jahia.services.render.Resource"--%>
<%--@elvariable id="url" type="org.jahia.services.render.URLGenerator"--%>
<template:addResources type="css" resources="social-activities-display.css"/>

<c:set var="boundComponent"
       value="${uiComponents:getBindedComponent(currentNode, renderContext, 'j:bindedComponent')}"/>

<jcr:node var="fromUser" path="${renderContext.user.localPath}"/>
<div class="new-activity-form-item">
    <div class='image'>
        <div class='itemImageLeft'>
            <jcr:nodeProperty var="picture" node="${fromUser}" name="j:picture"/>
            <c:if test="${not empty picture}">
                <a href="<c:url value='${url.base}${fromUser.path}.html'/>"><img
                        src="${picture.node.thumbnailUrls['avatar_120']}"
                        alt="${fromUser.properties.title.string} ${fromUser.properties.firstname.string} ${fromUser.properties.lastname.string}"
                        width="64"
                        height="64"/></a>
            </c:if>
            <c:if test="${empty picture}"><a href="<c:url value='${url.base}${fromUser.path}.html'/>">
                <img alt="" src="<c:url value='${url.currentModule}/images/userBig.png'/>" alt="user"
                     border="0"/></a></c:if>
        </div>
    </div>
    <div class="new-activity-form-content">
        <span class="new-activity-form-bubble"></span>
        <form class="new-activity-form"
              action="<c:url value='${url.base}${boundComponent.path}.addActivity.do'/>" method="post">
            <input type="hidden" name="jcrRedirectTo"
                   value="<c:url value='${url.base}${renderContext.mainResource.node.path}'/>"/>
            <input type="hidden" name="activityType" value="text"/>
            <textarea class="activity-text" type="text" name="activityParameters"
                        onFocus="if(this.value==this.defaultValue)value=''" onBlur="if(this.value=='')value=this.defaultValue;"
            ><fmt:message key="label.typeYourMessage"/></textarea>

            <div><input class="button activity-submit" type="submit" value="<fmt:message key='statusUpdateSubmit'/>"></div>
        </form>
    </div>
<div class='clear'></div>
</div>
