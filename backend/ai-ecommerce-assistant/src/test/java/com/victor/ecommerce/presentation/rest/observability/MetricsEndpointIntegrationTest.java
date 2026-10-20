package com.victor.ecommerce.presentation.rest.observability;

import com.victor.ecommerce.presentation.rest.auth.AuthenticationIntegrationTestProfile;
import io.quarkus.test.common.http.TestHTTPResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
@TestProfile(AuthenticationIntegrationTestProfile.class)
@Tag("integration")
class MetricsEndpointIntegrationTest {

    @TestHTTPResource("/q/metrics")
    URI metricsUri;

    @Test
    void exposesPrometheusMetricsWithoutApplicationSecrets() throws Exception {
        HttpResponse<String> response = HttpClient.newHttpClient().send(
                HttpRequest.newBuilder(metricsUri)
                        .header("Accept", "text/plain")
                        .GET()
                        .build(),
                HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("jvm_"));
        assertTrue(response.body().contains("http_server_requests"));
        assertTrue(!response.body().contains("integration-test-placeholder"));
    }
}
