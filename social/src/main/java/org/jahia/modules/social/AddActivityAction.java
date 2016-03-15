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
package org.jahia.modules.social;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.jahia.bin.ActionResult;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.jahia.services.render.URLResolver;

/**
 * Action handler for adding a user activity.
 * 
 * @author Sergiy Shyrkov
 */
public class AddActivityAction extends BaseSocialAction {

    public ActionResult doExecute(HttpServletRequest req, RenderContext renderContext,
            Resource resource, JCRSessionWrapper session, Map<String, List<String>> parameters,
            URLResolver urlResolver) throws Exception {

        if (parameters.containsKey("activityType")) {
            if (parameters.containsKey("activityParameters")) {
                socialService.addActivity(session.getUser().getUserKey(), resource.getNode(),
                        parameters.get("activityType").get(0), session, parameters.get("activityParameters").toArray());
            } else {
                socialService.addActivity(session.getUser().getUserKey(), resource.getNode(),
                        parameters.get("activityType").get(0), session);
            }
        } else {
            final String text = req.getParameter("text");

            if (text != null) {
                socialService.addActivity(session.getUser().getUsername(), text, session);
            } else {
                return ActionResult.BAD_REQUEST;
            }
        }
        session.save();
        return ActionResult.OK_JSON;
    }

}
