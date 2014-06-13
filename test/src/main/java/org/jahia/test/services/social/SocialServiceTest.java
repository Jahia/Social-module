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
package org.jahia.test.services.social;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.SortedSet;

import javax.jcr.RepositoryException;

import org.jahia.modules.sociallib.SocialService;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.SpringContextSingleton;
import org.jahia.services.content.JCRCallback;
import org.jahia.services.content.JCRContentUtils;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.JCRTemplate;
import org.jahia.services.usermanager.JahiaUserManagerService;
import org.jahia.services.usermanager.jcr.JCRUser;
import org.jahia.services.workflow.WorkflowService;
import org.jahia.services.workflow.WorkflowTask;
import org.jahia.test.JahiaTestCase;
import org.junit.*;

/**
 * Unit test for the {@link SocialService}.
 *
 * @author Sergiy Shyrkov
 */
public class SocialServiceTest extends JahiaTestCase {

    private static final int ACTIVITY_COUNT = 100;

    private static JCRUser iseult;

    private static JCRUser juliet;

    private static final int MESSAGE_COUNT = 100;

    private static JCRUser romeo;

    private static SocialService service;

    private static JCRUser tristan;

    private static JahiaUserManagerService userManager;

    private static WorkflowService workflowService;

    @BeforeClass
    public static void oneTimeSetUp() throws Exception {
        service = (SocialService) SpringContextSingleton.getBean("socialService");
        assertNotNull("SocialService cannot be retrieved", service);

        userManager = ServicesRegistry.getInstance().getJahiaUserManagerService();
        assertNotNull("JahiaUserManagerService cannot be retrieved", userManager);

        workflowService = (WorkflowService) SpringContextSingleton.getBean("workflowService");
        assertNotNull("WorkflowService cannot be retrieved", workflowService);

        romeo = (JCRUser) userManager.createUser("social-test-user-romeo", "password", new Properties());
        juliet = (JCRUser) userManager.createUser("social-test-user-juliet", "password", new Properties());
        tristan = (JCRUser) userManager.createUser("social-test-user-tristan", "password", new Properties());
        iseult = (JCRUser) userManager.createUser("social-test-user-iseult", "password", new Properties());
    }

    @AfterClass
    public static void oneTimeTearDown() throws Exception {
        if (romeo != null) {
            userManager.deleteUser(romeo);
        }
        if (juliet != null) {
            userManager.deleteUser(juliet);
        }
        if (tristan != null) {
            userManager.deleteUser(tristan);
        }
        if (iseult != null) {
            userManager.deleteUser(iseult);
        }
        service = null;
        userManager = null;
    }

    private void cleanUpUser(JCRUser user, JCRSessionWrapper session) throws RepositoryException {
        JCRNodeWrapper userNode = user.getNode(session);
        session.checkout(userNode);
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
            userNode.getNode("activities").remove();
        }
        if (userNode.hasNode("connections")) {
            userNode.getNode("connections").remove();
        }
        if (userNode.hasNode("messages")) {
            userNode.getNode("messages").remove();
        }
    }

    private void connect(final JCRUser from, final JCRUser to, String connectionType, boolean doAccept)
            throws RepositoryException {
        // request a connection
        service.requestSocialConnection(from.getUserKey(), to.getUserKey(), connectionType);

        List<WorkflowTask> tasks = workflowService.getTasksForUser(to, Locale.ENGLISH);
        assertEquals("No task for user '" + to.getName() + "' was created for accepting the social connection", 1,
                tasks.size());

        WorkflowTask task = tasks.get(0);
        // reject the connection
        workflowService.completeTask(task.getId(), to, task.getProvider(), doAccept ? "accept" : "reject",
                new HashMap<String, Object>());

        tasks = workflowService.getTasksForUser(to, Locale.ENGLISH);
        assertEquals("There should be no pending tasks for user '" + to.getName() + "'", 0, tasks.size());
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
        JCRTemplate.getInstance().doExecuteWithSystemSession(romeo.getName(), new JCRCallback<Boolean>() {
            public Boolean doInJCR(JCRSessionWrapper session) throws RepositoryException {
                service.addActivity(romeo.getUserKey(),romeo.getNode(session),"resourceBundle",session, "test.fake.rb.key");
                session.save();
                return Boolean.TRUE;
            }
        });

        JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Boolean>() {
            public Boolean doInJCR(JCRSessionWrapper session) throws RepositoryException {

                final String path = romeo.getNode(session).getPath();
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
        JCRTemplate.getInstance().doExecuteWithSystemSession(romeo.getName(), new JCRCallback<Boolean>() {
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
                        new HashSet<String>(Arrays.asList(romeo.getNode(session).getPath())), -1, 0, null);
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
            JCRTemplate.getInstance().doExecuteWithSystemSession(romeo.getName(), new JCRCallback<Boolean>() {
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
                        new HashSet<String>(Arrays.asList(romeo.getNode(session).getPath())), -1, 0, null).size();
                assertEquals("User should have " + ACTIVITY_COUNT + " one activity", ACTIVITY_COUNT, count);
                return Boolean.TRUE;
            }
        });
    }




}
