package com.example.hashingmachine;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

import static java.util.stream.StreamSupport.stream;

@Service
class InstanceService {
    private static final RestTemplate REST_TEMPLATE = new RestTemplate();
    private static final String INSTANCES_URL = "http://_api.internal:4280/v1/apps/hashing-machine/machines?state=started";
    private static final HttpEntity<String> HEADERS = new HttpEntity<>(MultiValueMap.fromSingleValue(Map.of("Authorization", "")));

    String getCurrentInstanceId() {
        return System.getenv("FLY_MACHINE_ID");
    }

    List<String> getInstanceIds() {
        try {
            var response = REST_TEMPLATE.exchange(
                    INSTANCES_URL,
                    HttpMethod.GET,
                    HEADERS,
                    JsonNode.class).getBody();

            if (response == null || !response.isArray()) {
                return List.of();
            }
            return stream(response.spliterator(), false)
                    .map(node -> node.get("id").asText())
                    .sorted()
                    .toList();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to fetch instances", e);
        }
    }
}
