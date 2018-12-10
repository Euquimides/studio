/*
 * Copyright (C) 2007-2018 Crafter Software Corporation. All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.craftercms.studio.controller.rest.v2;

import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.exception.security.AuthenticationException;
import org.craftercms.studio.api.v2.service.ui.UiService;
import org.craftercms.studio.model.rest.ApiResponse;
import org.craftercms.studio.model.rest.ResponseBody;
import org.craftercms.studio.model.rest.ResultList;
import org.craftercms.studio.model.ui.MenuItem;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.craftercms.studio.controller.rest.v2.ResultConstants.RESULT_KEY_MENU_ITEMS;

/**
 * Controller that provides the UI elements the current user has access to.
 *
 * @author avasquez
 */
@RestController
@RequestMapping("/api/2/ui")
public class UiController {

    private UiService uiService;

    @GetMapping("/views/global_menu")
    public ResponseBody getGlobalMenu() throws AuthenticationException, ServiceLayerException {
        ResultList<MenuItem> result = new ResultList<>();
        result.setResponse(ApiResponse.OK);
        result.setEntities(RESULT_KEY_MENU_ITEMS, uiService.getGlobalMenu());

        ResponseBody responseBody = new ResponseBody();
        responseBody.setResult(result);

        return responseBody;
    }

    @GetMapping("/system/active_environment")
    public ResponseBody getActiveEnvironment() {
        ResponseBody responseBody = new ResponseBody();

        return responseBody;
    }

    @Required
    public void setUiService(UiService uiService) {
        this.uiService = uiService;
    }

}
