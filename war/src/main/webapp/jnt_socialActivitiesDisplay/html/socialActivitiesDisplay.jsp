<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="social" uri="http://www.jahia.org/tags/socialLib" %>
<%@ taglib prefix="uiComponents" uri="http://www.jahia.org/tags/uiComponentsLib" %>
<template:addResources type="javascript" resources="jquery.min.js"/>
<template:addResources type="javascript" resources="jquery.cuteTime.js"/>
<script type="text/javascript">
    $(document).ready(function() {
        $('.timestamp').cuteTime({ refresh: 60000 });
    });
</script>
<template:include view="hidden.header"/>
<div class="boxsocial">
    <div class="boxsocialpadding10 boxsocialmarginbottom16">
        <div class="boxsocial-inner">
            <div class="boxsocial-inner-border"><!--start boxsocial -->
                <ul class="activitiesList">
                    <c:forEach items="${moduleMap.currentList}" var="activity" begin="${moduleMap.begin}" end="${moduleMap.end}">
                        <template:module path="${activity.path}"/>
                    </c:forEach>
                </ul>
            </div>
        </div>
    </div>
</div>
<template:include view="hidden.footer"/>
