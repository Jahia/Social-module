/**
 * ==========================================================================================
 * =                            JAHIA'S ENTERPRISE DISTRIBUTION                             =
 * ==========================================================================================
 *
 *                                  http://www.jahia.com
 *
 * JAHIA'S ENTERPRISE DISTRIBUTIONS LICENSING - IMPORTANT INFORMATION
 * ==========================================================================================
 *
 *     Copyright (C) 2002-2016 Jahia Solutions Group. All rights reserved.
 *
 *     This file is part of a Jahia's Enterprise Distribution.
 *
 *     Jahia's Enterprise Distributions must be used in accordance with the terms
 *     contained in the Jahia Solutions Group Terms & Conditions as well as
 *     the Jahia Sustainable Enterprise License (JSEL).
 *
 *     For questions regarding licensing, support, production usage...
 *     please contact our team at sales@jahia.com or go to http://www.jahia.com/license.
 *
 * ==========================================================================================
 */
package org.jahia.modules.sociallib.taglib;

import org.apache.commons.lang.StringUtils;
import org.apache.taglibs.standard.tag.common.core.Util;
import org.jahia.modules.sociallib.SocialService;
import org.jahia.services.SpringContextSingleton;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.taglibs.jcr.AbstractJCRTag;
import org.jahia.utils.Patterns;

import javax.jcr.RepositoryException;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;

/**
 * TODO Comment me
 *
 * @author loom
 *         Date: Jul 1, 2010
 *         Time: 1:56:41 PM
 */
public class GetSocialActivitiesTag extends AbstractJCRTag {

    private static final long serialVersionUID = 815042079517998908L;
    
    private int scope = PageContext.PAGE_SCOPE;
    private String var;
    private long limit = 100;
    private long offset = 0;
    private String pathFilter = null;
    private Set<String> sourcePaths;
    private SocialService socialService;
    private String activityTypes;
    private long startDate;

    public int doEndTag() throws JspException {
        try {
            pageContext.setAttribute(var, getActivities(), scope);
        } catch (RepositoryException e) {
            throw new JspException("Error while retrieving the activities!", e);
        }
        resetState();
        return EVAL_PAGE;
    }

    @Override
    protected void resetState() {
        activityTypes = null;
        limit = 100;
        offset = 0;
        pathFilter = null;
        scope = PageContext.PAGE_SCOPE;
        sourcePaths = null;
        startDate = 0;
        var = null;
        super.resetState();
    }

    public void setScope(String scope) {
        this.scope = Util.getScope(scope);
    }

    public void setVar(String var) {
        this.var = var;
    }

    public void setLimit(long limit) {
        this.limit = limit;
    }

    public void setOffset(long offset) {
        this.offset = offset;
    }

    public void setSourcePaths(Set<String> sourcePaths) {
        this.sourcePaths = sourcePaths;
    }

    public void setPathFilter(String pathFilter) {
        this.pathFilter = pathFilter;
    }

    public void setActivityTypes(String activityTypes) {
        this.activityTypes = activityTypes;
    }

    private SocialService getSocialService() {
        if (socialService == null) {
            socialService = (SocialService) SpringContextSingleton.getBeanInModulesContext("socialService");
        }
        return socialService;
    }

    private SortedSet<JCRNodeWrapper> getActivities() throws RepositoryException {
        JCRSessionWrapper session = getJCRSession();
        List<String> activityTypesList = null;
        if (activityTypes != null && !StringUtils.isEmpty(activityTypes)) {
            activityTypesList = Arrays.asList(Patterns.COMMA.split(activityTypes));
        }
        return getSocialService().getActivities(session, sourcePaths, limit, offset, pathFilter, activityTypesList, startDate);
    }

    public void setStartDate(long startDate) {
        this.startDate = startDate;
    }

}
