/*
 * Copyright (C) 2007-2022 Crafter Software Corporation. All Rights Reserved.
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

package org.craftercms.studio.model.rest;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.craftercms.commons.validation.annotations.param.EsapiValidatedParam;

import jakarta.validation.constraints.NotBlank;

import static org.craftercms.commons.validation.annotations.param.EsapiValidationType.USERNAME;

public class ResetPasswordRequest {

    @NotBlank
    @EsapiValidatedParam(type = USERNAME)
    private String username;
    @NotBlank
    private String newPassword;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @JsonProperty("new")
    public String getNewPassword() {
        return newPassword;
    }

    @JsonProperty("new")
    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}
