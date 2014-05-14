/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2014 Jahia Solutions Group SA. All rights reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 * As a special exception to the terms and conditions of version 2.0 of
 * the GPL (or any later version), you may redistribute this Program in connection
 * with Free/Libre and Open Source Software ("FLOSS") applications as described
 * in Jahia's FLOSS exception. You should have received a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license
 *
 * Commercial and Supported Versions of the program (dual licensing):
 * alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms and conditions contained in a separate
 * written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
 */

package org.jahia.modules.social.choicelist;

import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.modules.sociallib.ActivityRecorder;
import org.jahia.modules.sociallib.SocialService;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.JCRPropertyWrapper;
import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;
import org.jahia.services.content.nodetypes.initializers.ChoiceListValue;
import org.jahia.services.content.nodetypes.initializers.ModuleChoiceListInitializer;
import org.jahia.services.content.nodetypes.renderer.ModuleChoiceListRenderer;
import org.jahia.services.render.RenderContext;
import org.jahia.utils.i18n.Messages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import java.util.*;

/**
 * @author rincevent
 * Created : 25/04/12
 */
public class ActivityTypeModuleChoiceListInitializer implements ModuleChoiceListInitializer,ModuleChoiceListRenderer {
    private static Logger logger = LoggerFactory.getLogger(ActivityTypeModuleChoiceListInitializer.class);

    private String key;
    private SocialService socialService;

    public void setSocialService(SocialService socialService) {
        this.socialService = socialService;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }

    public List<ChoiceListValue> getChoiceListValues(ExtendedPropertyDefinition epd, String param,
                                                     List<ChoiceListValue> values, Locale locale,
                                                     Map<String, Object> context) {
        List<ChoiceListValue> choiceListValues = new ArrayList<ChoiceListValue>();
        final Map<String,ActivityRecorder> recorderMap = socialService.getActivityRecorderMap();
        for (String activityType : recorderMap.keySet()) {
            final ActivityRecorder recorder = recorderMap.get(activityType);

            final String activityTypeKey = getResourceBundleKey(activityType);
            JahiaTemplatesPackage pack = ServicesRegistry.getInstance().getJahiaTemplateManagerService().getTemplatePackage(recorder.getTemplatePackageName());
            if (pack == null) {
                logger.warn("Unable to to find " + recorder.getTemplatePackageName() + " bundle in registry when trying to display social activity");
            }
            choiceListValues.add(new ChoiceListValue(activityTypeKey!=null && pack != null? Messages.get(pack, activityTypeKey, locale):activityType, activityType));
        }
        return choiceListValues;
    }

    public Map<String, Object> getObjectRendering(RenderContext context, JCRPropertyWrapper propertyWrapper)
            throws RepositoryException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String getStringRendering(RenderContext context, JCRPropertyWrapper propertyWrapper)
            throws RepositoryException {
        final String activityType = propertyWrapper.getString();
        final ActivityRecorder recorder = socialService.getActivityRecorderMap().get(activityType);
        JahiaTemplatesPackage pack = ServicesRegistry.getInstance().getJahiaTemplateManagerService().getTemplatePackage(recorder.getTemplatePackageName());
        return Messages.get(pack, activityType, context.getUILocale());
    }

    public Map<String, Object> getObjectRendering(RenderContext context, ExtendedPropertyDefinition propDef,
                                                  Object propertyValue) throws RepositoryException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String getStringRendering(RenderContext context, ExtendedPropertyDefinition propDef, Object activityType)
            throws RepositoryException {
        final ActivityRecorder recorder = socialService.getActivityRecorderMap().get(activityType.toString());
        JahiaTemplatesPackage pack = ServicesRegistry.getInstance().getJahiaTemplateManagerService().getTemplatePackage(recorder.getTemplatePackageName());
        return Messages.get(pack, activityType.toString(), context.getUILocale());
    }

    public Map<String, Object> getObjectRendering(Locale locale, ExtendedPropertyDefinition propDef,
                                                  Object activityType) throws RepositoryException {
        final ActivityRecorder recorder = socialService.getActivityRecorderMap().get(activityType.toString());
        JahiaTemplatesPackage pack = ServicesRegistry.getInstance().getJahiaTemplateManagerService().getTemplatePackage(recorder.getTemplatePackageName());
        Map<String,Object> results = new LinkedHashMap<String, Object>();
        results.put("displayName",Messages.get(pack, activityType.toString(), locale));
        results.put("value",activityType.toString());
        return results;
    }

    private String getResourceBundleKey(String activityType) {
        return "label.activityTypes." + activityType;
    }

    public String getStringRendering(Locale locale, ExtendedPropertyDefinition propDef, Object propertyValue)
            throws RepositoryException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
