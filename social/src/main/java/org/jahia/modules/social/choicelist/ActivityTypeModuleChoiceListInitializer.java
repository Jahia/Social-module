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
