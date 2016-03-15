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

import org.jahia.modules.sociallib.SocialService;
import org.jahia.services.SpringContextSingleton;
import org.jahia.services.content.JCRCallback;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.JCRTemplate;
import org.jahia.services.workflow.jbpm.custom.AbstractWorkItemHandler;
import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemHandler;
import org.kie.api.runtime.process.WorkItemManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;

public class CreateActivityWorkItemHandler extends AbstractWorkItemHandler implements WorkItemHandler {
    private static final long serialVersionUID = 1L;
    private static Logger logger = LoggerFactory.getLogger(CreateActivityWorkItemHandler.class);

    @Override
    public void executeWorkItem(final WorkItem workItem, WorkItemManager manager) {
        final SocialService socialService = (SocialService) SpringContextSingleton.getBean("socialService");
        try {
            JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Object>() {
                public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                    final String uuid = (String) workItem.getParameter("nodeId");
                    String currentUser = (String) workItem.getParameter("currentUser");
                    if (currentUser == null) {
                        currentUser = (String) workItem.getParameter("user");
                    }
                    String activityType = (String) workItem.getParameter("activityType");
                    String parameter1 = (String) workItem.getParameter("parameter1");
                    String parameter2 = (String) workItem.getParameter("parameter2");
                    String parameter3 = (String) workItem.getParameter("parameter3");

                    JCRNodeWrapper node = session.getNodeByIdentifier(uuid);
                    socialService.addActivity(currentUser, node, activityType, session, parameter1, parameter2, parameter3);
                    session.save();
                    return null;
                }
            });
        } catch (RepositoryException e) {
            logger.error("Error creating activity", e);
        }

        manager.completeWorkItem(workItem.getId(), null);
    }

    @Override
    public void abortWorkItem(WorkItem workItem, WorkItemManager manager) {
        manager.abortWorkItem(workItem.getId());
    }
}