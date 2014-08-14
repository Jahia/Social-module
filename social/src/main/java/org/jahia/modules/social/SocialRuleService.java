/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *     Copyright (C) 2002-2014 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ======================================================================================
 *
 *     IF YOU DECIDE TO CHOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     "This program is free software; you can redistribute it and/or
 *     modify it under the terms of the GNU General Public License
 *     as published by the Free Software Foundation; either version 2
 *     of the License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program; if not, write to the Free Software
 *     Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 *     As a special exception to the terms and conditions of version 2.0 of
 *     the GPL (or any later version), you may redistribute this Program in connection
 *     with Free/Libre and Open Source Software ("FLOSS") applications as described
 *     in Jahia's FLOSS exception. You should have received a copy of the text
 *     describing the FLOSS exception, also available here:
 *     http://www.jahia.com/license"
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ======================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 *
 *
 * ==========================================================================================
 * =                                   ABOUT JAHIA                                          =
 * ==========================================================================================
 *
 *     Rooted in Open Source CMS, Jahia’s Digital Industrialization paradigm is about
 *     streamlining Enterprise digital projects across channels to truly control
 *     time-to-market and TCO, project after project.
 *     Putting an end to “the Tunnel effect”, the Jahia Studio enables IT and
 *     marketing teams to collaboratively and iteratively build cutting-edge
 *     online business solutions.
 *     These, in turn, are securely and easily deployed as modules and apps,
 *     reusable across any digital projects, thanks to the Jahia Private App Store Software.
 *     Each solution provided by Jahia stems from this overarching vision:
 *     Digital Factory, Workspace Factory, Portal Factory and eCommerce Factory.
 *     Founded in 2002 and headquartered in Geneva, Switzerland,
 *     Jahia Solutions Group has its North American headquarters in Washington DC,
 *     with offices in Chicago, Toronto and throughout Europe.
 *     Jahia counts hundreds of global brands and governmental organizations
 *     among its loyal customers, in more than 20 countries across the globe.
 *
 *     For more information, please visit http://www.jahia.com
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

    public void addActivityWithParameter(final String activityType, final String user, final AbstractNodeFact nodeFact, Object param, KnowledgeHelper drools) throws RepositoryException {
        addActivityWithParametersArray(activityType, user, nodeFact, new Object[]{param}, drools);
    }

    public void addActivityWithParametersArray(final String activityType, final String user, final AbstractNodeFact nodeFact, Object[] params, KnowledgeHelper drools) throws RepositoryException {
        if (user == null || "".equals(user.trim()) || user.equals(" system ")) {
            return;
        }
        final JCRUserNode userNode = JahiaUserManagerService.getInstance().lookupUser(user);
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

    public void sendMessage(final String fromUser, final String toUser, final String subject, final String message, AbstractNodeFact nodeFact, KnowledgeHelper drools) throws RepositoryException {
        if (fromUser == null || "".equals(fromUser.trim()) || fromUser.equals(" system ")) {
            return;
        }
        final JCRUserNode jahiaFromUser = JahiaUserManagerService.getInstance().lookupUser(fromUser);
        if (jahiaFromUser == null) {
            return;
        }
        if (toUser == null || "".equals(toUser.trim()) || toUser.equals(" system ")) {
            return;
        }
        final JCRUserNode jahiaToUser = ServicesRegistry.getInstance().getJahiaUserManagerService().lookupUser(toUser);
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
