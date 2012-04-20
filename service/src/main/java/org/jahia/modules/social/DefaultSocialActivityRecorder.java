package org.jahia.modules.social;

import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionWrapper;

import javax.jcr.RepositoryException;
import java.util.Arrays;
import java.util.List;

public class DefaultSocialActivityRecorder implements ActivityRecorder {
    private List<String> types = Arrays.asList("jnt:simpleSocialActivity","jnt:resourceBundleSocialActivity");

    public void recordActivity(String activityType, String user, JCRNodeWrapper activityNode, JCRNodeWrapper targetNode, JCRSessionWrapper session, Object[] args) throws RepositoryException {
        if (args.length>0) {
            String arg = (String) args[0];
            if (activityNode.isNodeType("jnt:resourceBundleSocialActivity")) {
                activityNode.setProperty("j:messageKey", arg);
            } else if (activityNode.isNodeType("jnt:simpleSocialActivity")) {
                activityNode.setProperty("j:message", arg);
            }
        }
    }

    public List<String> getActivityTypes() {
        return types;
    }


    public void setActivityTypes(List<String> types) {
        this.types = types;
    }
}
