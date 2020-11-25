/*
 * Copyright (C) 2007-2020 Crafter Software Corporation. All Rights Reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3 as published by
 * the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.craftercms.studio.impl.v2.service.ui.internal;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.lang3.StringUtils;
import org.craftercms.commons.lang.UrlUtils;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.service.content.ContentService;
import org.craftercms.studio.api.v2.annotation.IsActionAllowed;
import org.craftercms.studio.api.v2.exception.ConfigurationException;
import org.craftercms.studio.api.v2.utils.StudioConfiguration;
import org.craftercms.studio.impl.v1.util.ConfigUtils;
import org.craftercms.studio.model.ui.MenuItem;
import org.springframework.beans.factory.annotation.Required;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.craftercms.studio.api.v2.security.AvailableActions.READ_CONST_LONG;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.CONFIGURATION_GLOBAL_CONFIG_BASE_PATH;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.CONFIGURATION_GLOBAL_MENU_FILE_NAME;

/**
 * Default implementation of {@link UiServiceInternal}.
 *
 * @author avasquez
 */
public class UiServiceInternalImpl implements UiServiceInternal {

    private static final String MENU_ITEMS_CONFIG_KEY = "items.item";
    private static final String PERMISSION_CONFIG_KEY = "permission";
    private static final String ID_CONFIG_KEY = "id";
    private static final String LABEL_CONFIG_KEY = "label";
    private static final String ICON_CONFIG_KEY = "icon";

    private static final String ANY_PERMISSION_WILDCARD = "*";

    private StudioConfiguration studioConfiguration;
    private ContentService contentService;

    @Required
    public void setStudioConfiguration(StudioConfiguration studioConfiguration) {
        this.studioConfiguration = studioConfiguration;
    }

    @Required
    public void setContentService(ContentService contentService) {
        this.contentService = contentService;
    }

    @Override
    @SuppressWarnings("unchecked")
    @IsActionAllowed(allowedActionsMask = READ_CONST_LONG)
    public List<MenuItem> getGlobalMenu(Set<String> permissions) throws ServiceLayerException {
        if (CollectionUtils.isNotEmpty(permissions)) {
            HierarchicalConfiguration menuConfig = getGlobalMenuConfig();
            List<MenuItem> menuItems = new ArrayList<>();

            // TODO: Move this config to ConfigurationService
            List<HierarchicalConfiguration> itemsConfig = menuConfig.configurationsAt(MENU_ITEMS_CONFIG_KEY);
            if (CollectionUtils.isNotEmpty(itemsConfig)) {
                for (HierarchicalConfiguration itemConfig : itemsConfig) {
                    String requiredPermission = getRequiredStringProperty(itemConfig, PERMISSION_CONFIG_KEY);
                    if (requiredPermission.equals(ANY_PERMISSION_WILDCARD) ||
                        permissions.contains(requiredPermission)) {
                        MenuItem item = new MenuItem();
                        item.setId(getRequiredStringProperty(itemConfig, ID_CONFIG_KEY));
                        item.setLabel(getRequiredStringProperty(itemConfig, LABEL_CONFIG_KEY));
                        item.setIcon(getRequiredStringProperty(itemConfig, ICON_CONFIG_KEY));

                        menuItems.add(item);
                    }
                }
            } else {
                throw new ConfigurationException("No menu items found in global menu config");
            }

            return menuItems;
        } else {
            return null;
        }
    }

    protected HierarchicalConfiguration getGlobalMenuConfig() throws ConfigurationException {
        String configPath = getGlobalMenuConfigPath();

        try (InputStream is = contentService.getContent(StringUtils.EMPTY, configPath)) {
            return ConfigUtils.readXmlConfiguration(is);
        } catch (Exception e) {
            throw new ConfigurationException("Unable to read global menu config @ " + configPath, e);
        }
    }

    protected String getRequiredStringProperty(Configuration config, String key) throws ConfigurationException {
        String property = config.getString(key);
        if (StringUtils.isEmpty(property)) {
            throw new ConfigurationException("Missing required property '" + key + "'");
        } else {
            return property;
        }
    }

    protected String getGlobalMenuConfigPath() {
        return UrlUtils.concat(getGlobalConfigPath(), getGlobalMenuFileName());
    }

    protected String getGlobalConfigPath() {
        return studioConfiguration.getProperty(CONFIGURATION_GLOBAL_CONFIG_BASE_PATH);
    }

    protected String getGlobalMenuFileName() {
        return studioConfiguration.getProperty(CONFIGURATION_GLOBAL_MENU_FILE_NAME);
    }

}
