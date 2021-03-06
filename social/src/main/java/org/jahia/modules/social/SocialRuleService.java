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
package org.jahia.modules.social;

import org.drools.core.spi.KnowledgeHelper;
import org.jahia.modules.sociallib.SocialService;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.decorator.JCRUserNode;
import org.jahia.services.content.rules.AbstractNodeFact;
import org.jahia.services.content.rules.AddedNodeFact;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.usermanager.JahiaUserManagerService;

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

    public void addActivityWithParameter(String activityType, String user, AbstractNodeFact nodeFact, Object param, KnowledgeHelper drools) throws RepositoryException {
        addActivityWithParametersArray(activityType, user, null, nodeFact, new Object[]{param}, drools);
    }

    public void addActivityWithParameter(String activityType, String user, String userRealm, AbstractNodeFact nodeFact, Object param, KnowledgeHelper drools) throws RepositoryException {
        addActivityWithParametersArray(activityType, user, userRealm, nodeFact, new Object[]{param}, drools);
    }

    public void addActivityWithParametersArray(String activityType, String user, AbstractNodeFact nodeFact, Object[] params, KnowledgeHelper drools) throws RepositoryException {
        addActivityWithParametersArray(activityType, user, null, nodeFact, params, drools);
    }
    
    public void addActivityWithParametersArray(String activityType, String user, String userRealm, AbstractNodeFact nodeFact, Object[] params, KnowledgeHelper drools) throws RepositoryException {
        if (user == null || "".equals(user.trim()) || user.equals(" system ")) {
            return;
        }
        JCRUserNode userNode = JahiaUserManagerService.getInstance().lookupUser(user, userRealm, nodeFact.getNode().getSession());
        if (userNode == null) {
            return;
        }
        JCRNodeWrapper n;
        if (params != null) {
            n = socialService.addActivity(userNode, nodeFact.getNode(), activityType, nodeFact.getNode().getSession(), params);
        } else {
            n = socialService.addActivity(userNode, nodeFact.getNode(), activityType, nodeFact.getNode().getSession());
        }
        drools.insert(new AddedNodeFact(n));
    }

    public void sendMessage(String fromUser, String toUser, String subject, String message, AbstractNodeFact nodeFact, KnowledgeHelper drools) throws RepositoryException {
        sendMessage(fromUser, null, toUser, null, subject, message, nodeFact, drools);
    }

    public void sendMessage(String fromUser, String fromUserRealm, String toUser, String toUserRealm, String subject, String message, AbstractNodeFact nodeFact, KnowledgeHelper drools) throws RepositoryException {
        if (fromUser == null || "".equals(fromUser.trim()) || fromUser.equals(" system ")) {
            return;
        }
        JCRUserNode jahiaFromUser = JahiaUserManagerService.getInstance().lookupUser(fromUser, fromUserRealm);
        if (jahiaFromUser == null) {
            return;
        }
        if (toUser == null || "".equals(toUser.trim()) || toUser.equals(" system ")) {
            return;
        }
        JCRUserNode jahiaToUser = ServicesRegistry.getInstance().getJahiaUserManagerService().lookupUser(toUser, toUserRealm);
        if (jahiaToUser == null) {
            return;
        }

        socialService.sendMessage(jahiaFromUser, jahiaToUser, subject, message, nodeFact.getNode().getSession());
    }

    /**
     * @param socialService the socialService to set
     */
    public void setSocialService(SocialService socialService) {
        this.socialService = socialService;
    }

}
