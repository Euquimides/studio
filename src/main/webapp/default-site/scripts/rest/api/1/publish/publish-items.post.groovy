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


import groovy.json.JsonException
import groovy.json.JsonSlurper
import org.apache.commons.collections.CollectionUtils
import org.apache.commons.lang3.StringUtils
import org.craftercms.studio.api.v1.exception.SiteNotFoundException
import scripts.api.DeploymentServices

import java.time.ZonedDateTime

import static org.craftercms.studio.api.v1.constant.StudioConstants.API_REQUEST_PARAM_ENVIRONMENT
import static org.craftercms.studio.api.v1.constant.StudioConstants.API_REQUEST_PARAM_SITE
import static org.craftercms.studio.api.v1.constant.StudioConstants.API_REQUEST_PARAM_ENTITIES

def result = [:]
try {
    def site = request.getParameter(API_REQUEST_PARAM_SITE)
    def requestBody = request.reader.text

    def slurper = new JsonSlurper()
    def parsedReq = slurper.parseText(requestBody)

    def environment = parsedReq.environment

    def entities = parsedReq.entities

    def paths = entities.collect { it.item }

    def schedule = null
    if (StringUtils.isNotEmpty(parsedReq.schedule)) {
        schedule = ZonedDateTime.parse(parsedReq.schedule)
    }

    def submissionComment = parsedReq.submission_comment

    /** Validate Parameters */
    def invalidParams = false;
    def paramsList = []



// site
    try {
        if (StringUtils.isEmpty(site)) {
            invalidParams = true
            paramsList.add(API_REQUEST_PARAM_SITE)
        }
    } catch (Exception exc) {
        invalidParams = true
        paramsList.add(API_REQUEST_PARAM_SITE)
    }

// paths
    try {
        if (CollectionUtils.isEmpty(paths)) {
            invalidParams = true
            paramsList.add(API_REQUEST_PARAM_ENTITIES)
        }
    } catch (Exception exc) {
        invalidParams = true
        paramsList.add(API_REQUEST_PARAM_ENTITIES)
    }

    if (invalidParams) {
        response.setStatus(400)
        result.message = "Invalid parameter(s): " + paramsList
    } else {

        def context = DeploymentServices.createContext(applicationContext, request)
        try {
            result = DeploymentServices.publishItems(context, site, environment, schedule, paths, submissionComment)
        } catch (SiteNotFoundException e) {
            response.setStatus(404)
            result.message = "Site not found"
        } catch (Exception e) {
            response.setStatus(500)
            result.message = "Internal server error: \n" + e
        }
    }
} catch (JsonException e) {
    response.setStatus(400)
    result.message = "Bad Request"
}
return result
