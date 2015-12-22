/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2016 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ===================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
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
