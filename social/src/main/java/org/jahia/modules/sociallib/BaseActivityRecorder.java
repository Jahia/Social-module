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
package org.jahia.modules.sociallib;

import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionWrapper;

import javax.jcr.RepositoryException;
import java.util.Map;

public class BaseActivityRecorder implements ActivityRecorder {
    private Map<String,String> activityTypes;
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

    public String getTemplatePackageName() {
        return templatePackageName;
    }

    public void setTemplatePackageName(String templatePackageName) {
        this.templatePackageName = templatePackageName;
    }
}
