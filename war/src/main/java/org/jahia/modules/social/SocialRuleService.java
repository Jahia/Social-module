/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2012 Jahia Solutions Group SA. All rights reserved.
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

package org.jahia.modules.social;

import org.drools.spi.KnowledgeHelper;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.rules.AbstractNodeFact;
import org.jahia.services.content.rules.AddedNodeFact;
import org.jahia.services.usermanager.JahiaUser;

import javax.jcr.RepositoryException;

/**
 * Social service class for manipulating social activities from the
 * right-hand-side (consequences) of rules.
 *
 * @author Serge Huber
 */
public class SocialRuleService {
    
    private SocialService socialService;

    /* Rules Consequence implementations */

    public void addActivityWithParameter(final String activityType, final String user, final AbstractNodeFact nodeFact, Object param, KnowledgeHelper drools) throws RepositoryException {
        addActivityWithParametersArray(activityType, user, nodeFact, new Object[] {param}, drools);
    }

    public void addActivityWithParametersArray(final String activityType, final String user, final AbstractNodeFact nodeFact, Object[] params, KnowledgeHelper drools) throws RepositoryException {
        if (user == null || "".equals(user.trim()) || user.equals(" system ")) {
            return;
        }
        final JahiaUser jahiaUser = ServicesRegistry.getInstance().getJahiaUserManagerService().lookupUser(user);
        JCRNodeWrapper n;
        if (params != null) {
            n = socialService.addActivity(jahiaUser.getUserKey(), nodeFact.getNode(), activityType, nodeFact.getNode().getSession(), params);
        } else {
            n = socialService.addActivity(jahiaUser.getUserKey(), nodeFact.getNode(), activityType, nodeFact.getNode().getSession());
        }
        drools.insert(new AddedNodeFact(n));
    }

    public void sendMessage(final String fromUser, final String toUser, final String subject, final String message, AbstractNodeFact nodeFact, KnowledgeHelper drools) throws RepositoryException {
        if (fromUser == null || "".equals(fromUser.trim()) || fromUser.equals(" system ")) {
            return;
        }
        final JahiaUser jahiaFromUser = ServicesRegistry.getInstance().getJahiaUserManagerService().lookupUser(fromUser);
        if (toUser == null || "".equals(toUser.trim()) || toUser.equals(" system ")) {
            return;
        }
        final JahiaUser jahiaToUser = ServicesRegistry.getInstance().getJahiaUserManagerService().lookupUser(toUser);

        socialService.sendMessage(jahiaFromUser.getUserKey(), jahiaToUser.getUserKey(), subject, message, nodeFact.getNode().getSession());
    }

    /**
     * @param socialService the socialService to set
     */
    public void setSocialService(SocialService socialService) {
        this.socialService = socialService;
    }

}