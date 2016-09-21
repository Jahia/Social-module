/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2016 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.modules.sociallib;

import org.jahia.services.content.*;
import org.jahia.services.content.decorator.JCRUserNode;

import javax.jcr.RepositoryException;
import java.util.*;

/**
 * Social service interface for manipulating social activities data.
 *
 * @author Serge Huber
 */
public interface SocialService {
    void addActivity(final String userKey, final String message, JCRSessionWrapper session) throws RepositoryException;

    JCRNodeWrapper addActivity(final String userKey, final JCRNodeWrapper targetNode, String activityType, JCRSessionWrapper session, Object... args) throws RepositoryException;

    JCRNodeWrapper addActivity(final JCRUserNode userNode, final JCRNodeWrapper targetNode, String activityType, JCRSessionWrapper session, Object... args) throws RepositoryException;

    boolean sendMessage(final String fromUserKey, final String toUserKey, final String subject, final String body) throws RepositoryException;

    boolean sendMessage(String fromUserKey, String toUserKey, final String subject, final String body, JCRSessionWrapper session) throws RepositoryException;

    void sendMessage(final JCRUserNode fromUser, final JCRUserNode toUser, final String subject, final String body, JCRSessionWrapper session) throws RepositoryException;

    Set<String> getUserConnections(final String userNodePath, final boolean includeSelf) throws RepositoryException;

    SortedSet<JCRNodeWrapper> getActivities(JCRSessionWrapper jcrSessionWrapper, Set<String> usersPaths,
                                                   long limit, long offset,
                                                   String targetTreeRootPath) throws RepositoryException;

    SortedSet<JCRNodeWrapper> getActivities(JCRSessionWrapper jcrSessionWrapper, Set<String> usersPaths,
                                                   long limit, long offset, String targetTreeRootPath,
                                                   List<String> activityTypes) throws RepositoryException;

    SortedSet<JCRNodeWrapper> getActivities(JCRSessionWrapper jcrSessionWrapper, Set<String> usersPaths,
                                                   long limit, long offset, String targetTreeRootPath,
                                                   List<String> activityTypes, long startDate) throws RepositoryException;

    void removeSocialConnection(final String fromUuid, final String toUuid, final String connectionType) throws RepositoryException;

    /**
     * Creates the social connection between two users.
     *
     * @param fromUserKey    the source user key
     * @param toUserKey      the target user key
     * @param connectionType the connection type
     * @throws RepositoryException in case of an error
     */
    void createSocialConnection(final String fromUserKey, final String toUserKey, final String connectionType)
            throws RepositoryException;

    /**
     * Starts the workflow process for accepting the social connection between two users.
     *
     * @param fromUserKey    the source user key
     * @param toUserKey      the target user key
     * @param connectionType the connection type
     * @throws RepositoryException in case of an error
     */
    void requestSocialConnection(String fromUserKey, String toUserKey, String connectionType) throws RepositoryException;

    Map<String, ActivityRecorder> getActivityRecorderMap();

    /**
     * @param jcrContentUtils the jcrContentUtils to set
     */
    void setJCRContentUtils(JCRContentUtils jcrContentUtils);

}
