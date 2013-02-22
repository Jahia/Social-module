package org.jahia.modules.social;

import org.jahia.modules.sociallib.SocialService;
import org.jahia.services.SpringContextSingleton;
import org.jahia.services.content.JCRCallback;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.JCRTemplate;
import org.jbpm.api.activity.ActivityExecution;
import org.jbpm.api.activity.ExternalActivityBehaviour;

import javax.jcr.RepositoryException;
import java.util.Map;

public class CreateActivityWorkflowActivity implements ExternalActivityBehaviour {
    private static final long serialVersionUID = 1L;
    private String activityType;

    private String parameter1;
    private String parameter2;
    private String parameter3;

    public void setActivityType(String activityType) {
        this.activityType = activityType;
    }

    public void setParameter1(String parameter1) {
        this.parameter1 = parameter1;
    }

    public void setParameter2(String parameter2) {
        this.parameter2 = parameter2;
    }

    public void setParameter3(String parameter3) {
        this.parameter3 = parameter3;
    }

    public void execute(final ActivityExecution execution) throws Exception {
        final SocialService socialService = (SocialService) SpringContextSingleton.getBean("socialService");
        JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Object>() {
            public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                final String uuid = (String) execution.getVariable("nodeId");
                String currentUser = (String) execution.getVariable("currentUser");
                if (currentUser == null) {
                    currentUser = (String) execution.getVariable("user");
                }

                JCRNodeWrapper node = session.getNodeByIdentifier(uuid);
                socialService.addActivity(currentUser, node, activityType, session, parameter1, parameter2, parameter3);
                session.save();
                return null;
            }
        });

        execution.takeDefaultTransition();
    }

    public void signal(ActivityExecution execution, String signalName, Map<String, ?> parameters) throws Exception {
    }

}