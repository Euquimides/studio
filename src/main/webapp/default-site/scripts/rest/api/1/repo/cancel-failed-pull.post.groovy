/*
 * Copyright (C) 2007-2019 Crafter Software Corporation. All Rights Reserved.
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
 */


import groovy.json.JsonException
import groovy.json.JsonSlurper
import org.apache.commons.lang3.StringUtils
import org.craftercms.studio.api.v1.exception.SiteNotFoundException
import scripts.api.SiteServices

def result = [:]
try {
    def requestBody = request.reader.text

    def slurper = new JsonSlurper()
    def parsedReq = slurper.parseText(requestBody)

    def siteId = parsedReq.siteId

/** Validate Parameters */
    def invalidParams = false

// site_id
    try {
        if (StringUtils.isEmpty(siteId)) {
            invalidParams = true
        }
    } catch (Exception exc) {
        invalidParams = true
    }

    if (invalidParams) {
        response.setStatus(400)
        result.message = "Invalid parameter: siteId"
    } else {
        def context = SiteServices.createContext(applicationContext, request)

        try {
            result.repostoryStatus = SiteServices.cancelFailedPull(context, siteId)
            response.setStatus(200)
            result.message = "OK"
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