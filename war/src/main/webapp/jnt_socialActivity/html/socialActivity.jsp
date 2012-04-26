<%@ page import="javax.servlet.jsp.jstl.fmt.LocalizationContext" %>
<%@ page import="org.jahia.services.render.Resource" %>
<%@ page import="org.jahia.utils.i18n.JahiaResourceBundle" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="social" uri="http://www.jahia.org/tags/socialLib" %>
<%@ taglib prefix="user" uri="http://www.jahia.org/tags/user" %>

<c:set var="fromUser" value="${jcr:getParentOfType(currentNode,'jnt:user')}"/>

<c:set var="message" value="${currentNode.properties['j:message'].string}"/>
<c:if test="${currentNode.properties['j:activityType'].string eq 'resourceBundle'}">
    <c:if test="${fn:contains(message,':')}">
        <c:set value="${fn:substringAfter(message,':')}" var="key"/>
        <c:set value="${fn:substringBefore(message,':')}" var="bundleName"/>

        <%
            Resource currentResource = (Resource) pageContext.findAttribute("currentResource");
            LocalizationContext ctx = new LocalizationContext(
                    new JahiaResourceBundle(currentResource.getLocale(), (String) pageContext.findAttribute("bundleName")),
                    currentResource.getLocale());
            pageContext.setAttribute("bundle", ctx);
        %>
        <c:set var="message"><fmt:message bundle="${bundle}" key="${key}"/></c:set>
    </c:if>
    <c:if test="${not fn:contains(fields['j:messageKey'],':')}">
        <c:set var="message"><fmt:message key="${message}"/></c:set>
    </c:if>
</c:if>

    <div class="activityContent">
        <div class='image'>
            <div class='itemImageLeft'>
                <jcr:nodeProperty var="picture" node="${fromUser}" name="j:picture"/>
                <c:if test="${not empty picture}">
                    <a href="<c:url value='${url.base}${fromUser.path}.html'/>"><img
                            src="${picture.node.thumbnailUrls['avatar_120']}"
                            alt="${userNode.properties.title.string} ${userNode.properties.firstname.string} ${userNode.properties.lastname.string}"
                            width="64"
                            height="64"/></a>
                </c:if>
                <c:if test="${empty picture}"><a href="<c:url value='${url.base}${fromUser.path}.html'/>">
                    <img alt="" src="<c:url value='${url.currentModule}/images/userBig.png'/>" alt="user"
                         border="0"/></a></c:if>
            </div>
        </div>

        <div class="activityInnerContent">

            <c:set var="fromUser" value="${jcr:getParentOfType(currentNode,'jnt:user')}"/>
            <p class='author'><a href="<c:url value='${url.base}${fromUser.path}.html'/>">${fn:escapeXml(not empty fromUser ? user:userFullName(fromUser) : fields["jcr:createdBy"])}</a></p>
            <jcr:node var="targetNode" path="${currentNode.properties['j:targetNode'].string}"/>
            <p class="message">${fn:escapeXml(message)}&nbsp;
                <c:if test="${not empty targetNode and jcr:isDisplayable(targetNode, renderContext)}">
                    <a href="<c:url value='${url.base}${targetNode.path}.html'/>">${fn:escapeXml(targetNode.displayableName)}</a>
                </c:if>
            </p>
            <jcr:nodeProperty node="${currentNode}" name="jcr:lastModified" var="lastModified"/>
            <span class="timestamp"><fmt:formatDate value="${lastModified.time}" pattern="yyyy/MM/dd HH:mm:ss"/></span>
        </div>
    </div>
    <div class='clear'></div>
