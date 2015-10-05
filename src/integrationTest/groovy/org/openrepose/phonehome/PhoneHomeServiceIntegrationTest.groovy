package org.openrepose.phonehome

import org.apache.http.client.methods.CloseableHttpResponse
import org.apache.http.client.methods.HttpGet
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.HttpClients
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.SpringApplicationContextLoader
import org.springframework.boot.test.WebIntegrationTest
import org.springframework.data.mongodb.core.MongoOperations
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory
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
    @Shared
    HttpComponentsClientHttpRequestFactory requestFactory

    def setupSpec() {
        httpClient = HttpClients.createDefault()
        requestFactory = new HttpComponentsClientHttpRequestFactory(httpClient)
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
        def request = buildRequest("http://localhost:$port$path", method)
        if (payload) {
            request.setEntity(new StringEntity(payload))
        }
        request.setHeader("Content-Type", contentType)

        when:
        CloseableHttpResponse response = httpClient.execute(request)

        then:
        response.getStatusLine().statusCode == statusCode
        response.close()

        where:
        method | path     | payload              | contentType        | statusCode
        "post" | "/"      | """{"foo":"bar"}"""  | "application/json" | 200
        "post" | "/butts" | """{"foo":"bar"}"""  | "application/json" | 405
        "post" | "/"      | """{"foo":"bar",}""" | "application/json" | 400
        "post" | "/"      | """<foo>bar</foo>""" | "application/xml"  | 415
        "get"  | "/"      | ""                   | "application/json" | 405
    }

    def buildRequest(String uri, String method) {
        def request
        switch (method) {
            case "post":
                request = new HttpPost(uri)
                break
            case "get":
                request = new HttpGet(uri)
                break
        }
        request
    }
}