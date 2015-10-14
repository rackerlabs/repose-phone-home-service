/*
 * _=_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_=
 * Repose
 * _-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-
 * Copyright (C) 2010 - 2015 Rackspace US, Inc.
 * _-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * =_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_=_
 */
package org.openrepose.phonehome

import org.apache.http.client.methods.CloseableHttpResponse
import org.apache.http.client.methods.HttpPost
import org.apache.http.client.methods.RequestBuilder
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.HttpClients
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.SpringApplicationContextLoader
import org.springframework.boot.test.WebIntegrationTest
import org.springframework.data.mongodb.core.MongoOperations
import org.springframework.test.context.ContextConfiguration
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

@ContextConfiguration(loader = SpringApplicationContextLoader, classes = PhoneHomeService)
@WebIntegrationTest
class PhoneHomeServiceIntegrationTest extends Specification {

    @Value('${local.server.port}')
    int port
    @Autowired
    MongoOperations mongoOperations

    @Shared
    CloseableHttpClient httpClient

    def setupSpec() {
        httpClient = HttpClients.createDefault()
    }

    def cleanupSpec() {
        httpClient.close()
    }

    def "should return 200 for valid Json"() {
        given:
        HttpPost post = new HttpPost("http://localhost:$port")
        post.setEntity(new StringEntity("""{"foo":"bar"}"""))
        post.setHeader("Content-Type", "application/json")

        when:
        CloseableHttpResponse response = httpClient.execute(post)

        then:
        response.getStatusLine().statusCode == 200
        response.close()

        when:
        List<Map> allEntries = mongoOperations.findAll(Map, "phoneHomeReports")

        then:
        allEntries[0]["foo"] == "bar"
    }

    @Unroll("#method's to #path with #payload of type #contentType return #statusCode")
    def "test the various cases"() {
        given:
        def request = RequestBuilder.create(method)
                                    .setUri("http://localhost:$port$path")
                                    .setEntity(new StringEntity(payload))
                                    .setHeader("Content-Type", contentType)
                                    .build()

        when:
        CloseableHttpResponse response = httpClient.execute(request)

        then:
        response.getStatusLine().statusCode == statusCode
        response.close()

        where:
        method | path     | payload              | contentType        | statusCode
        "POST" | "/"      | """{"foo":"bar"}"""  | "application/json" | 200
        "POST" | "/butts" | """{"foo":"bar"}"""  | "application/json" | 405
        "POST" | "/"      | """{"foo":"bar",}""" | "application/json" | 400
        "POST" | "/"      | """<foo>bar</foo>""" | "application/xml"  | 415
        "GET"  | "/"      | ""                   | "application/json" | 405
    }
}