package org.jahia.modules.social;

import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionWrapper;

import javax.jcr.RepositoryException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BaseActivityRecorder implements ActivityRecorder {
    private Map<String,String> activityTypes;
    private Map<String,String> activityTypesRB;
    private String templatePackageName;


    public void recordActivity(JCRNodeWrapper activityNode, String activityType, String user, JCRNodeWrapper targetNode, JCRSessionWrapper session, Object[] args) throws RepositoryException {

    }

    public String getNodeTypeForActivity(String activityType) {
        return activityTypes.get(activityType);
    }

    public Map<String, String> getActivityTypes() {
        return activityTypes;
    }

    public void setActivityTypes(Map<String, String> types) {
        this.activityTypes = types;
    }

    public Map<String, String> getActivityTypesRB() {
        return activityTypesRB;
    }

    public void setActivityTypesRB(Map<String, String> activityTypesRB) {
        this.activityTypesRB = activityTypesRB;
    }

    public String getTemplatePackageName() {
        return templatePackageName;
    }

    public void setTemplatePackageName(String templatePackageName) {
        this.templatePackageName = templatePackageName;
    }
}
