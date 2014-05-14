/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2014 Jahia Solutions Group SA. All rights reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 * As a special exception to the terms and conditions of version 2.0 of
 * the GPL (or any later version), you may redistribute this Program in connection
 * with Free/Libre and Open Source Software ("FLOSS") applications as described
 * in Jahia's FLOSS exception. You should have received a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license
 *
 * Commercial and Supported Versions of the program (dual licensing):
 * alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms and conditions contained in a separate
 * written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
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
