package org.jahia.modules.social;

import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionWrapper;

import javax.jcr.RepositoryException;
import java.util.Arrays;
import java.util.List;

public class DefaultSocialActivityRecorder extends BaseActivityRecorder {

    public void recordActivity(JCRNodeWrapper activityNode, String activityType, String user, JCRNodeWrapper targetNode, JCRSessionWrapper session, Object[] args) throws RepositoryException {
        if (args.length>0) {
            String arg = (String) args[0];
            activityNode.setProperty("j:message", arg);
        }
    }

}
