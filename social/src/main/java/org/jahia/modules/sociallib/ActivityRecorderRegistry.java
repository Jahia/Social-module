/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2017 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.modules.sociallib;

import org.jahia.services.templates.JahiaModulesBeanPostProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;

import java.util.*;

/**
 * Registry for ActivityRecorder objects
 */
public class ActivityRecorderRegistry implements JahiaModulesBeanPostProcessor {
    private static Logger logger = LoggerFactory.getLogger(ActivityRecorderRegistry.class);

    private Map<String, ActivityRecorder> postProcessorActivityRecorders = new HashMap<>();
    private List<OsgiActivityRecorderService> osgiListActivityRecorders = new ArrayList<>();

    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof ActivityRecorder && !(bean instanceof OsgiActivityRecorderService)) {
            logger.warn("An ActivityRecorder have been detected and registered using JahiaModulesBeanPostProcessor. " +
                    "Since DX 7.2.0.0, it's not recommended to use this mechanism of inter module beans lookup. Module Spring contexts are now " +
                    "started independently and beans could be registered before the JahiaModulesBeanPostProcessor causing unintended side-effects. " +
                    "To avoid these potential side-effects, we recommend you to replace your ActivityRecorder by an OsgiActivityRecorderService and " +
                    "expose it as a service in OSGi. You can find more information about these mechanisms in the documentation.");
            postProcessorActivityRecorders.put(beanName, (ActivityRecorder) bean);
        }
        return bean;
    }

    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    @Override
    public void postProcessBeforeDestruction(Object bean, String beanName) throws BeansException {
        if (bean instanceof ActivityRecorder && !(bean instanceof OsgiActivityRecorderService)) {
            postProcessorActivityRecorders.remove(beanName);
        }
    }

    public Map<String, ActivityRecorder> getActivityRecorderMap() {
        Map<String, ActivityRecorder> activityRecorderMap = new LinkedHashMap<>();
        populateRecorderMap(postProcessorActivityRecorders.values(), activityRecorderMap);
        populateRecorderMap(osgiListActivityRecorders, activityRecorderMap);
        return activityRecorderMap;
    }

    private void populateRecorderMap(Collection<? extends ActivityRecorder> activityRecorders, Map<String, ActivityRecorder> activityRecorderMap) {
        if (activityRecorders != null && activityRecorders.size() > 0) {
            for (ActivityRecorder activityRecorder : activityRecorders) {
                for (String activityType : activityRecorder.getActivityTypes().keySet()) {
                    activityRecorderMap.put(activityType, activityRecorder);
                }
            }
        }
    }

    public void setOsgiListActivityRecorders(List<OsgiActivityRecorderService> osgiListActivityRecorders) {
        this.osgiListActivityRecorders = osgiListActivityRecorders;
    }
}