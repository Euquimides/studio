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
package org.craftercms.studio.model;

import org.craftercms.studio.api.v2.dal.UserTO;

/**
 Represents a {@link UserTO} that has been authenticated.
 *
 * @author avasquez
 */
public class AuthenticatedUser extends UserTO {

    private static final long serialVersionUID = -4678834461080865934L;
    private AuthenticationType authenticationType;

    public AuthenticatedUser(UserTO user) {
        setId(user.getId());
        setUsername(user.getUsername());
        setPassword(user.getPassword());
        setEmail(user.getEmail());
        setFirstName(user.getFirstName());
        setLastName(user.getLastName());
        setActive(user.getActive());
        setExternallyManaged(user.getExternallyManaged());

        setTimezone(user.getTimezone());
        setLocale(user.getLocale());
        setRecordLastUpdated(getRecordLastUpdated());
    }

    public AuthenticationType getAuthenticationType() {
        return authenticationType;
    }

    public void setAuthenticationType(AuthenticationType authenticationType) {
        this.authenticationType = authenticationType;
    }

}
