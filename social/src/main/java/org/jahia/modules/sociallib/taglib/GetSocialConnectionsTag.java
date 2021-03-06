/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2017 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ===================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 */
package org.jahia.modules.sociallib.taglib;

import org.apache.taglibs.standard.tag.common.core.Util;
import org.jahia.modules.sociallib.SocialService;
import org.jahia.services.SpringContextSingleton;
import org.jahia.taglibs.jcr.AbstractJCRTag;

import javax.jcr.RepositoryException;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import java.util.Set;

/**
 * A tag that retrieves the list of paths corresponding to the user's connections.
 *
 * @author loom
 *         Date: Jul 1, 2010
 *         Time: 1:56:19 PM
 */
public class GetSocialConnectionsTag extends AbstractJCRTag {

    private static final long serialVersionUID = -2967779565265433297L;
    
    private int scope = PageContext.PAGE_SCOPE;
    private String var;
    private String path;
    private boolean includeSelf = true;
    private long limit;
    private long offset;
    private SocialService socialService;

    public int doEndTag() throws JspException {
        try {
            pageContext.setAttribute(var, getConnections(), scope);
        } catch (RepositoryException e) {
            throw new JspException("Error while retrieving the "+path+" connections!", e);
        }
        resetState();
        return EVAL_PAGE;
    }

    @Override
    protected void resetState() {
        scope = PageContext.PAGE_SCOPE;
        var = null;
        path = null;
        includeSelf = true;
        limit = 0;
        offset = 0;
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

    public void setPath(String path) {
        this.path = path;
    }

    public void setIncludeSelf(boolean includeSelf) {
        this.includeSelf = includeSelf;
    }

    private SocialService getSocialService() {
        if (socialService == null) {
            socialService = (SocialService) SpringContextSingleton.getBeanInModulesContext("socialService");
        }
        return socialService;
    }

    private Set<String> getConnections() throws RepositoryException {
        return getSocialService().getUserConnections(path, includeSelf);
    }
}
