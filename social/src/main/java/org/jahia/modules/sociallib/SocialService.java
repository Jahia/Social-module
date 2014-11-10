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
package org.jahia.modules.sociallib;

import org.apache.commons.lang.StringUtils;
import org.apache.jackrabbit.util.ISO8601;
import org.jahia.api.Constants;
import org.jahia.services.content.*;
import org.jahia.services.content.decorator.JCRUserNode;
import org.jahia.services.content.nodetypes.ExtendedPropertyType;
import org.jahia.services.templates.JahiaModulesBeanPostProcessor;
import org.jahia.services.usermanager.JahiaUserManagerService;
import org.jahia.services.workflow.WorkflowService;
import org.jahia.services.workflow.WorkflowVariable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.nodetype.NoSuchNodeTypeException;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;
import java.util.*;

/**
 * Social service class for manipulating social activities data.
 *
 * @author Serge Huber
 */
public class SocialService {

    private static Logger logger = LoggerFactory.getLogger(SocialService.class);
    public static final String JNT_BASE_SOCIAL_ACTIVITY = "jnt:baseSocialActivity";
    public static final String JNT_SOCIAL_MESSAGE = "jnt:socialMessage";
    public static final String JNT_SOCIAL_CONNECTION = "jnt:socialConnection";
    private static final Comparator<? super JCRNodeWrapper> ACTIVITIES_COMPARATOR = new Comparator<JCRNodeWrapper>() {

        public int compare(JCRNodeWrapper activityNode1, JCRNodeWrapper activityNode2) {
            try {
                // we invert the order to sort with most recent dates on top.
                return activityNode2.getProperty("jcr:created").getDate().compareTo(activityNode1.getProperty("jcr:created").getDate());
            } catch (RepositoryException e) {
                logger.error("Error while comparing creation date on two activities, returning them as equal", e);
                return 0;
            }
        }

    };

    private static final String JMIX_AUTOPUBLISH = "jmix:autoPublish";

    private String autoSplitSettings;
    private JahiaUserManagerService userManagerService;
    private WorkflowService workflowService;
    private JCRContentUtils jcrContentUtils;

    private ActivityRecorderRegistry activityRecorderRegistry;

    public void setActivityRecorderRegistry(ActivityRecorderRegistry activityRecorderRegistry) {
        this.activityRecorderRegistry = activityRecorderRegistry;
    }

    public void addActivity(final String userKey, final String message, JCRSessionWrapper session) throws RepositoryException {
        addActivity(userKey, null, "text", session, message);
    }

    public JCRNodeWrapper addActivity(final String userKey, final JCRNodeWrapper targetNode, String activityType, JCRSessionWrapper session, Object... args) throws RepositoryException {
        if (userKey == null || "".equals(userKey.trim())) {
            throw new ConstraintViolationException();
        }
        final JCRUserNode userNode = userManagerService.lookupUserByPath(userKey, session);
        if (userNode == null) {
            logger.warn("No user found, not adding activity !");
            throw new ConstraintViolationException();
        }

        return addActivity(userNode, targetNode, activityType, session, args);
    }

    public JCRNodeWrapper addActivity(final JCRUserNode userNode, final JCRNodeWrapper targetNode, String activityType, JCRSessionWrapper session, Object... args) throws RepositoryException {
        JCRNodeWrapper activitiesNode = getActivitiesNode(session, userNode);

        if (activityRecorderRegistry.getActivityRecorderMap().containsKey(activityType)) {
            ActivityRecorder activityRecorder = activityRecorderRegistry.getActivityRecorderMap().get(activityType);
            String nodeType = activityRecorder.getNodeTypeForActivity(activityType);
            String nodeName = jcrContentUtils.generateNodeName(activitiesNode, nodeType)+"_"+((int) Math.rint(Math.random()*100000));
            JCRNodeWrapper activityNode = activitiesNode.addNode(nodeName, nodeType);
            if (targetNode!=null) {
                activityNode.setProperty("j:targetNode", targetNode.getPath());
            }
            activityNode.setProperty("j:activityType", activityType);
            activityRecorder.recordActivity(activityNode, activityType, userNode.getPath(), targetNode, session, args);

            return activityNode;
        }

        throw new NoSuchNodeTypeException();
    }

    private JCRNodeWrapper getActivitiesNode(JCRSessionWrapper session, JCRNodeWrapper userNode) throws RepositoryException {
        JCRNodeWrapper activitiesNode;
        try {
            activitiesNode = userNode.getNode("activities");
            if (!activitiesNode.isNodeType("jnt:activitiesList")) {
                activitiesNode.remove();
                session.save();
                throw new PathNotFoundException();
            }
            session.checkout(activitiesNode);
        } catch (PathNotFoundException pnfe) {
            session.checkout(userNode);
            activitiesNode = userNode.addNode("activities", "jnt:activitiesList");
            if (autoSplitSettings != null) {
                activitiesNode.addMixin(Constants.JAHIAMIX_AUTOSPLITFOLDERS);
                activitiesNode.setProperty(Constants.SPLIT_CONFIG, autoSplitSettings);
                activitiesNode.setProperty(Constants.SPLIT_NODETYPE, "jnt:activitiesList");
            }
        }
        return activitiesNode;
    }


    public boolean sendMessage(final String fromUserKey, final String toUserKey, final String subject, final String body) throws RepositoryException {
        return execute(new JCRCallback<Boolean>() {
            public Boolean doInJCR(JCRSessionWrapper session) throws RepositoryException {
                return sendMessage(fromUserKey, toUserKey, subject, body, session);
            }
        });
    }

    public boolean sendMessage(String fromUserKey, String toUserKey, final String subject, final String body, JCRSessionWrapper session) throws RepositoryException {
        JCRUserNode fromUser = userManagerService.lookupUserByPath(fromUserKey, session);
        if (fromUser == null) {
            logger.warn("Couldn't find from user " + fromUserKey + " , aborting message sending...");
            return false;
        }
        JCRUserNode toUser = userManagerService.lookupUserByPath(toUserKey, session);
        if (toUser == null) {
            logger.warn("Couldn't find to user " + toUserKey + " , aborting message sending...");
            return false;
        }

        sendMessage(fromUser, toUser, subject, body, session);
        return true;
    }

    public void sendMessage(final JCRUserNode fromUser, final JCRUserNode toUser, final String subject, final String body, JCRSessionWrapper session) throws RepositoryException {
        JCRNodeWrapper destinationInboxNode = JCRContentUtils.getOrAddPath(session, toUser, "messages/inbox",
                Constants.JAHIANT_CONTENTLIST);
        String destinationInboxNodeName = JCRContentUtils.findAvailableNodeName(destinationInboxNode,
                fromUser.getName() + "_to_" + toUser.getName());
        JCRNodeWrapper destinationMessageNode = destinationInboxNode.addNode(destinationInboxNodeName,
                JNT_SOCIAL_MESSAGE);
        destinationMessageNode.setProperty("j:from", fromUser);
        destinationMessageNode.setProperty("j:to", toUser);
        destinationMessageNode.setProperty("j:subject", subject);
        destinationMessageNode.setProperty("j:body", body);
        destinationMessageNode.setProperty("j:read", false);

        JCRNodeWrapper sentMessagesBoxNode = JCRContentUtils.getOrAddPath(session, fromUser, "messages/sent",
                Constants.JAHIANT_CONTENTLIST);
        String sentMessagesBoxNodeName = JCRContentUtils.findAvailableNodeName(sentMessagesBoxNode, fromUser.getName()
                + "_to_" + toUser.getName());
        JCRNodeWrapper sentMessageNode = sentMessagesBoxNode.addNode(sentMessagesBoxNodeName, JNT_SOCIAL_MESSAGE);
        sentMessageNode.setProperty("j:from", fromUser);
        sentMessageNode.setProperty("j:to", toUser);
        sentMessageNode.setProperty("j:subject", subject);
        sentMessageNode.setProperty("j:body", body);
        sentMessageNode.setProperty("j:read", false);

        session.save();
    }

    public Set<String> getUserConnections(final String userNodePath, final boolean includeSelf)
            throws RepositoryException {
        final Set<String> userPaths = new HashSet<String>();

        execute(new JCRCallback<Boolean>() {
            public Boolean doInJCR(JCRSessionWrapper session) throws RepositoryException {
                QueryManager queryManager = session.getWorkspace().getQueryManager();

                if (includeSelf) {
                    userPaths.add(userNodePath);
                }

                Query myConnectionsQuery = queryManager.createQuery("select * from [" + JNT_SOCIAL_CONNECTION
                        + "] as uC where isdescendantnode(uC,['" + userNodePath + "'])", Query.JCR_SQL2);
                QueryResult myConnectionsResult = myConnectionsQuery.execute();

                NodeIterator myConnectionsIterator = myConnectionsResult.getNodes();
                while (myConnectionsIterator.hasNext()) {
                    JCRNodeWrapper myConnectionNode = (JCRNodeWrapper) myConnectionsIterator.nextNode();
                    JCRNodeWrapper connectedToNode = (JCRNodeWrapper) myConnectionNode.getProperty("j:connectedTo")
                            .getNode();
                    userPaths.add(connectedToNode.getPath());
                }
                return true;
            }
        });

        return userPaths;
    }

    public SortedSet<JCRNodeWrapper> getActivities(JCRSessionWrapper jcrSessionWrapper, Set<String> usersPaths,
                                                   long limit, long offset,
                                                   String targetTreeRootPath) throws RepositoryException {
        return getActivities(jcrSessionWrapper, usersPaths, limit, offset, targetTreeRootPath, null);
    }

    public SortedSet<JCRNodeWrapper> getActivities(JCRSessionWrapper jcrSessionWrapper, Set<String> usersPaths,
                                                   long limit, long offset, String targetTreeRootPath,
                                                   List<String> activityTypes) throws RepositoryException {
        return getActivities(jcrSessionWrapper, usersPaths, limit, offset, targetTreeRootPath, activityTypes, 0);
    }

    public SortedSet<JCRNodeWrapper> getActivities(JCRSessionWrapper jcrSessionWrapper, Set<String> usersPaths,
                                                   long limit, long offset, String targetTreeRootPath,
                                                   List<String> activityTypes, long startDate) throws RepositoryException {
        long timer = System.currentTimeMillis();
        SortedSet<JCRNodeWrapper> activitiesSet = new TreeSet<JCRNodeWrapper>(ACTIVITIES_COMPARATOR);
        StringBuilder statementBuilder = new StringBuilder().append("select * from [").append(
                JNT_BASE_SOCIAL_ACTIVITY).append("] as uA where ");
        boolean addAnd = false;
        if (usersPaths != null && !usersPaths.isEmpty()) {
            int size = usersPaths.size();
            if (size > 1) {
                statementBuilder.append("(");
            }
            Iterator<String> iterator = usersPaths.iterator();
            while (iterator.hasNext()) {
                statementBuilder.append("isdescendantnode(['").append(JCRContentUtils.sqlEncode(iterator.next())).append("'])");
                if (iterator.hasNext()) {
                    statementBuilder.append(" or ");
                }
            }
            if (size > 1) {
                statementBuilder.append(")");
            }
            addAnd = true;
        }
        if (targetTreeRootPath != null) {
            String escapedPath = JCRContentUtils.sqlEncode(targetTreeRootPath);
            if (addAnd) {
                statementBuilder.append(" and ");
            }
            statementBuilder.append("(['j:targetNode'] ").append(escapedPath.indexOf('%') != -1 ? "like" : "=").append(" '");
            statementBuilder.append(escapedPath)
                    .append("' or ['j:targetNode'] like '").append(escapedPath).append("/%')");
            addAnd = true;
        }
        if (activityTypes != null && !activityTypes.isEmpty()) {
            if (addAnd) {
                statementBuilder.append(" and ");
            }
            int size = activityTypes.size();
            if (size > 1) {
                statementBuilder.append("(");
            }
            Iterator<String> iterator = activityTypes.iterator();
            while (iterator.hasNext()) {
                statementBuilder.append("['j:activityType'] = '").append(iterator.next()).append("'");
                if (iterator.hasNext()) {
                    statementBuilder.append(" or ");
                }
            }
            if (size > 1) {
                statementBuilder.append(")");
            }
        }
        if (startDate > 0) {
            // do filtering by date
            Calendar c = Calendar.getInstance();
            c.setTimeInMillis(startDate);
            statementBuilder.append(" AND [jcr:created] >= '").append(ISO8601.format(c))
                    .append("'");
        }
        statementBuilder.append(" order by [jcr:created] desc");
        String statement = statementBuilder.toString();
        QueryManager queryManager = jcrSessionWrapper.getWorkspace().getQueryManager();
        Query activitiesQuery = queryManager.createQuery(statement, Query.JCR_SQL2);
        activitiesQuery.setLimit(limit);
        activitiesQuery.setOffset(offset);
        QueryResult activitiesResult = activitiesQuery.execute();

        NodeIterator activitiesIterator = activitiesResult.getNodes();
        while (activitiesIterator.hasNext()) {
            JCRNodeWrapper activitiesNode = (JCRNodeWrapper) activitiesIterator.nextNode();
            activitiesSet.add(activitiesNode);
        }

        if (logger.isDebugEnabled()) {
            logger.debug("{} activities retrieved in {} ms with query:\n{}", new Object[] {
                    activitiesSet.size(), System.currentTimeMillis() - timer, statement });
        }

        return activitiesSet;
    }

    public void removeSocialConnection(final String fromUuid, final String toUuid, final String connectionType)
            throws RepositoryException {

        execute(new JCRCallback<Boolean>() {
            public Boolean doInJCR(JCRSessionWrapper session) throws RepositoryException {

                QueryManager queryManager = session.getWorkspace().getQueryManager();

                // first we look for the first connection.
                StringBuilder q = new StringBuilder(64);
                q.append("select * from [" + JNT_SOCIAL_CONNECTION + "] where [j:connectedFrom]='").append(fromUuid)
                        .append("' and [j:connectedTo]='").append(toUuid).append("'");
                if (StringUtils.isNotEmpty(connectionType)) {
                    q.append(" and [j:type]='").append(connectionType).append("'");
                }
                Query connectionQuery = queryManager.createQuery(q.toString(), Query.JCR_SQL2);
                QueryResult connectionResult = connectionQuery.execute();
                NodeIterator connectionIterator = connectionResult.getNodes();
                while (connectionIterator.hasNext()) {
                    Node connectionNode = connectionIterator.nextNode();
                    session.checkout(connectionNode.getParent());
                    connectionNode.remove();
                }

                // now let's remove the reverse connection.
                q.delete(0, q.length());
                q.append("select * from [" + JNT_SOCIAL_CONNECTION + "] where [j:connectedFrom]='").append(toUuid)
                        .append("' and [j:connectedTo]='").append(fromUuid).append("'");
                if (StringUtils.isNotEmpty(connectionType)) {
                    q.append(" and [j:type]='").append(connectionType).append("'");
                }
                Query reverseConnectionQuery = queryManager.createQuery(q.toString(), Query.JCR_SQL2);
                QueryResult reverseConnectionResult = reverseConnectionQuery.execute();
                NodeIterator reverseConnectionIterator = reverseConnectionResult.getNodes();
                while (reverseConnectionIterator.hasNext()) {
                    Node connectionNode = reverseConnectionIterator.nextNode();
                    session.checkout(connectionNode.getParent());
                    connectionNode.remove();
                }

                session.save();
                return true;
            }
        });
    }

    /**
     * Creates the social connection between two users.
     *
     * @param fromUserKey    the source user key
     * @param toUserKey      the target user key
     * @param connectionType the connection type
     * @throws RepositoryException in case of an error
     */
    public void createSocialConnection(final String fromUserKey, final String toUserKey, final String connectionType)
            throws RepositoryException {

        execute(new JCRCallback<Boolean>() {
            public Boolean doInJCR(JCRSessionWrapper session) throws RepositoryException {

                JCRUserNode leftUser = userManagerService.lookupUserByPath(fromUserKey, session);
                if (leftUser == null) {
                    throw new IllegalArgumentException("Cannot find user with key " + fromUserKey);
                }

                JCRUserNode rightUser = userManagerService.lookupUserByPath(toUserKey, session);
                if (rightUser == null) {
                    throw new IllegalArgumentException("Cannot find user with key " + toUserKey);
                }

                // now let's connect this user's node to the target node.

                JCRNodeWrapper leftConnectionsNode = null;
                try {
                    leftConnectionsNode = leftUser.getNode("connections");
                    session.checkout(leftConnectionsNode);
                } catch (PathNotFoundException pnfe) {
                    session.checkout(leftUser);
                    leftConnectionsNode = leftUser.addNode("connections", "jnt:contentList");
                    leftConnectionsNode.addMixin(JMIX_AUTOPUBLISH);
                }
                JCRNodeWrapper leftUserConnection = leftConnectionsNode.addNode(leftUser.getName() + "-" + rightUser.getName(), SocialService.JNT_SOCIAL_CONNECTION);
                leftUserConnection.setProperty("j:connectedFrom", leftUser);
                leftUserConnection.setProperty("j:connectedTo", rightUser);
                if (connectionType != null) {
                    leftUserConnection.setProperty("j:type", connectionType);
                }

                // now let's do the connection in the other direction.
                JCRNodeWrapper rightConnectionsNode = null;
                try {
                    rightConnectionsNode = rightUser.getNode("connections");
                    session.checkout(rightConnectionsNode);
                } catch (PathNotFoundException pnfe) {
                    session.checkout(rightUser);
                    rightConnectionsNode = rightUser.addNode("connections", "jnt:contentList");
                    rightConnectionsNode.addMixin(JMIX_AUTOPUBLISH);
                }
                JCRNodeWrapper rightUserConnection = rightConnectionsNode.addNode(rightUser.getName() + "-" + leftUser.getName(), SocialService.JNT_SOCIAL_CONNECTION);
                rightUserConnection.setProperty("j:connectedFrom", rightUser);
                rightUserConnection.setProperty("j:connectedTo", leftUser);
                if (connectionType != null) {
                    rightUserConnection.setProperty("j:type", connectionType);
                }

                session.save();
                return true;
            }
        });
    }

    /**
     * Starts the workflow process for accepting the social connection between two users.
     *
     * @param fromUserKey    the source user key
     * @param toUserKey      the target user key
     * @param connectionType the connection type
     * @throws RepositoryException in case of an error
     */
    public void requestSocialConnection(String fromUserKey, String toUserKey, String connectionType)
            throws RepositoryException {

        final JCRUserNode from = userManagerService.lookupUserByPath(fromUserKey);
        if (from == null) {
            throw new IllegalArgumentException("Cannot find user with key " + fromUserKey);
        }

        final JCRUserNode to = userManagerService.lookupUserByPath(toUserKey);
        if (to == null) {
            throw new IllegalArgumentException("Cannot find user with key " + toUserKey);
        }

        final Map<String, Object> args = new HashMap<String, Object>();
        args.put("fromUser", from.getName());
        args.put("from", fromUserKey);
        args.put("to", toUserKey);
        args.put("connectionType", connectionType);

        args.put("jcr:title", new WorkflowVariable(from.getName(), ExtendedPropertyType.STRING));

        JCRTemplate.getInstance().doExecuteWithSystemSession(from.getName(), Constants.LIVE_WORKSPACE, Locale.ENGLISH,
                new JCRCallback<Boolean>() {
                    public Boolean doInJCR(JCRSessionWrapper session) throws RepositoryException {
                        workflowService.startProcess(Arrays.asList(from.getIdentifier()), session,
                                "user-connection", "jBPM", args, null);
                        return true;
                    }
                });
    }

    public Map<String, ActivityRecorder> getActivityRecorderMap() {
        return activityRecorderRegistry.getActivityRecorderMap();
    }

    /**
     * @param autoSplitSettings the autoSplitSettings to set
     */
    public void setAutoSplitSettings(String autoSplitSettings) {
        this.autoSplitSettings = autoSplitSettings;
    }

    /**
     * @param userManagerService the userManagerService to set
     */
    public void setUserManagerService(JahiaUserManagerService userManagerService) {
        this.userManagerService = userManagerService;
    }

    /**
     * @param workflowService the workflowService to set
     */
    public void setWorkflowService(WorkflowService workflowService) {
        this.workflowService = workflowService;
    }

    /**
     * @param jcrContentUtils the jcrContentUtils to set
     */
    public void setJCRContentUtils(JCRContentUtils jcrContentUtils) {
        this.jcrContentUtils = jcrContentUtils;
        if (jcrContentUtils.getNameGenerationHelper() != null &&
                jcrContentUtils.getNameGenerationHelper() instanceof DefaultNameGenerationHelperImpl) {
            ((DefaultNameGenerationHelperImpl) jcrContentUtils.getNameGenerationHelper()).getRandomizedNames().add(JNT_BASE_SOCIAL_ACTIVITY);
        }
    }

    private boolean execute(JCRCallback<Boolean> jcrCallback) throws RepositoryException {
        return JCRTemplate.getInstance().doExecuteWithSystemSession(jcrCallback);
    }

}
