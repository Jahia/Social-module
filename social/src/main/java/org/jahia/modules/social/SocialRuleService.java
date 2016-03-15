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
