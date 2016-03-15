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
package org.jahia.test.services.social;

import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.modules.sociallib.SocialService;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.SpringContextSingleton;
import org.jahia.services.content.JCRCallback;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.JCRTemplate;
import org.jahia.services.content.decorator.JCRUserNode;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.usermanager.JahiaUserManagerService;
import org.jahia.services.workflow.WorkflowService;
import org.jahia.services.workflow.WorkflowTask;
import org.jahia.test.JahiaTestCase;
import org.junit.*;

import javax.jcr.RepositoryException;
import java.util.*;

import static org.junit.Assert.*;

/**
 * Unit test for the {@link SocialService}.
 *
 * @author Sergiy Shyrkov
 */
public class SocialServiceTest extends JahiaTestCase {

    private static final int ACTIVITY_COUNT = 100;
    
    private static JCRUserNode iseult;

    private static JCRUserNode juliet;

    private static JCRUserNode romeo;

    private static SocialService service;

    private static JCRUserNode tristan;

    private static JahiaUserManagerService userManager;

    private static WorkflowService workflowService;

    @BeforeClass
    public static void oneTimeSetUp() throws Exception {
        final JahiaTemplatesPackage packageById = ServicesRegistry.getInstance().getJahiaTemplateManagerService().getTemplatePackageById("jahia-social-test-module");
        service = (SocialService) packageById.getContext().getBean("socialService");

        assertNotNull("SocialService cannot be retrieved", service);

        userManager = ServicesRegistry.getInstance().getJahiaUserManagerService();
        assertNotNull("JahiaUserManagerService cannot be retrieved", userManager);

        workflowService = (WorkflowService) SpringContextSingleton.getBean("workflowService");
        assertNotNull("WorkflowService cannot be retrieved", workflowService);

        JCRSessionWrapper session = JCRTemplate.getInstance().getSessionFactory().getCurrentUserSession();
        romeo = userManager.createUser("social-test-user-romeo", "password", new Properties(), session);
        juliet = userManager.createUser("social-test-user-juliet", "password", new Properties(), session);
        tristan = userManager.createUser("social-test-user-tristan", "password", new Properties(), session);
        iseult =  userManager.createUser("social-test-user-iseult", "password", new Properties(), session);
        session.save();
    }

    @AfterClass
    public static void oneTimeTearDown() throws Exception {
        JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Boolean>() {
            @Override
            public Boolean doInJCR(JCRSessionWrapper session) throws RepositoryException {
                if (romeo != null) {
                    userManager.deleteUser(romeo.getPath(), session);
                }
                if (juliet != null) {
                    userManager.deleteUser(juliet.getPath(), session);
                }
                if (tristan != null) {
                    userManager.deleteUser(tristan.getPath(), session);
                }
                if (iseult != null) {
                    userManager.deleteUser(iseult.getPath(), session);
                }
                session.save();
                return true;
            }
        });
        service = null;
        userManager = null;
    }

    private void cleanUpUser(JCRUserNode userNode, JCRSessionWrapper session) throws RepositoryException {
        session.checkout(userNode);
        JahiaUser user = userNode.getJahiaUser();
        List<WorkflowTask> tasks = workflowService.getTasksForUser(user, Locale.ENGLISH);
        for (WorkflowTask task : tasks) {
            try {
                workflowService.completeTask(task.getId(), user, task.getProvider(),task.getOutcomes().iterator().next(),
                        new HashMap<String, Object>());
            } catch (Exception e) {
                try {
                    workflowService.abortProcess(task.getProcessId(), task.getProvider());
                } catch (Exception e1) {
                    workflowService.deleteProcess(task.getProcessId(), task.getProvider());
                }
            }
        }
        tasks = workflowService.getTasksForUser(user, Locale.ENGLISH);
        assertTrue("user should have no tasks remaining",tasks.isEmpty());
        if (userNode.hasNode("activities")) {
            session.getNode(userNode.getPath() + "/activities").remove();
        }
        if (userNode.hasNode("connections")) {
            session.getNode(userNode.getPath() + "/connections").remove();
        }
        if (userNode.hasNode("messages")) {
            session.getNode(userNode.getPath() + "/messages").remove();
        }
        session.save();
    }
    
    @Before
    public void setUp() throws RepositoryException {
        JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Boolean>() {
            public Boolean doInJCR(JCRSessionWrapper session) throws RepositoryException {
                cleanUpUser(romeo, session);
                cleanUpUser(juliet, session);
                cleanUpUser(tristan, session);
                cleanUpUser(iseult, session);

                session.save();

                return true;
            }
        });
    }

    @After
    public void tearDown() throws RepositoryException {
        JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Boolean>() {
            public Boolean doInJCR(JCRSessionWrapper session) throws RepositoryException {
                cleanUpUser(romeo, session);
                cleanUpUser(juliet, session);
                cleanUpUser(tristan, session);
                cleanUpUser(iseult, session);

                session.save();

                return true;
            }
        });
    }

    @Test
    public void testAddTypedActivity() throws Exception {
        JCRTemplate.getInstance().doExecuteWithSystemSessionAsUser(romeo.getJahiaUser(), null, null, new JCRCallback<Boolean>() {
            public Boolean doInJCR(JCRSessionWrapper session) throws RepositoryException {
                service.addActivity(romeo.getUserKey(),romeo,"resourceBundle",session, "test.fake.rb.key");
                session.save();
                return Boolean.TRUE;
            }
        });

        JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Boolean>() {
            public Boolean doInJCR(JCRSessionWrapper session) throws RepositoryException {

                final String path = romeo.getPath();
                final SortedSet<JCRNodeWrapper> activities = (SortedSet<JCRNodeWrapper>) service.getActivities(session,
                        null, -1, 0, path);
                int count = activities.size();
                assertEquals("User should have only one activity", 1, count);
                final JCRNodeWrapper activity = activities.first();
                assertEquals("resourceBundle",activity.getProperty("j:activityType").getString());
                assertEquals("test.fake.rb.key",activity.getProperty("j:message").getString());
                assertEquals(path,activity.getProperty("j:targetNode").getString());
                return Boolean.TRUE;
            }
        });
    }

    @Test
    public void testAddStatusMessage() throws Exception {
        JCRTemplate.getInstance().doExecuteWithSystemSessionAsUser(romeo.getJahiaUser(), null, null, new JCRCallback<Boolean>() {
            public Boolean doInJCR(JCRSessionWrapper session) throws RepositoryException {
                service.addActivity(romeo.getUserKey(), "To be, or not to be: that is the question."
                                                        + " Regards. Romeo.", session);
                session.save();
                return Boolean.TRUE;
            }
        });

        JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Boolean>() {
            public Boolean doInJCR(JCRSessionWrapper session) throws RepositoryException {

                final SortedSet<JCRNodeWrapper> activities = (SortedSet<JCRNodeWrapper>) service.getActivities(session,
                        new HashSet<String>(Arrays.asList(romeo.getPath())), -1, 0, null);
                int count = activities.size();
                assertEquals("User should have only one activity", 1, count);
                final JCRNodeWrapper activity = activities.first();
                assertEquals("text",activity.getProperty("j:activityType").getString());
                assertEquals("To be, or not to be: that is the question."
                             + " Regards. Romeo.",activity.getProperty("j:message").getString());
                return Boolean.TRUE;
            }
        });
    }

    @Test
    public void testAddActivityPerformance() throws Exception {
        for (int i = 0; i < ACTIVITY_COUNT; i++) {
            final int counter = i;
            JCRTemplate.getInstance().doExecuteWithSystemSessionAsUser(romeo.getJahiaUser(), null, null, new JCRCallback<Boolean>() {
                public Boolean doInJCR(JCRSessionWrapper session) throws RepositoryException {
                    service.addActivity(romeo.getUserKey(), "[" + counter
                            + "] To be, or not to be: that is the question." + " Regards. Romeo.", session);
                    session.save();
                    return Boolean.TRUE;
                }
            });
        }

        JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Boolean>() {
            public Boolean doInJCR(JCRSessionWrapper session) throws RepositoryException {

                int count = service.getActivities(session,
                        new HashSet<String>(Arrays.asList(romeo.getPath())), -1, 0, null).size();
                assertEquals("User should have " + ACTIVITY_COUNT + " one activity", ACTIVITY_COUNT, count);
                return Boolean.TRUE;
            }
        });
    }




}
