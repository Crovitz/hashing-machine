package com.example.hashingmachine;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static java.util.Arrays.stream;
import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;

@RestController
@RequestMapping("/samples")
class SampleController {
    private final List<Integer> samples = List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);

    @GetMapping
    List<Integer> getAll() {
        return samples;
    }

    @GetMapping("/limited")
    List<Integer> getSamples() {
        var instances = getInstances();
        var currentInstanceId = System.getenv("FLY_MACHINE_ID");
        return samples.stream()
                .filter(sample -> new ConsistentHashing(instances).getInstanceForKey(String.valueOf(sample)).equals(currentInstanceId))
                .toList();
    }

    record Machine(String id) {
    }

    @GetMapping("/instances")
    List<String> getInstances() {
        try {
            var entity = getStringHttpEntity();
            var response = new RestTemplate().exchange(
                    "http://_api.internal:4280/v1/apps/hashing-machine/machines?state=started",
                    HttpMethod.GET,
                    entity,
                    Machine[].class);

            return ofNullable(response.getBody())
                    .map(it -> stream(it).map(Machine::id).toList())
                    .orElse(emptyList());
        } catch (Exception e) {
            throw new IllegalStateException("Failed to fetch instances", e);
        }
    }

    private static HttpEntity<String> getStringHttpEntity() {
        var headers = new HttpHeaders();
        headers.set("Authorization", "FlyV1 fm2_lJPECAAAAAAAAAaPxBBsAOhFqtoxrV6eiw16DZurwrVodHRwczovL2FwaS5mbHkuaW8vdjGWAJLOAAGFYB8Lk7lodHRwczovL2FwaS5mbHkuaW8vYWFhL3YxxDwqGzPk4u1MW1iyyxrrDqSZYX6xbxZvKrZCgINUZjerHyFQMNOhqV/5mIDxVO7VHNkWRbHn7r4rFs8EpqHETri/wcyxrbJu5PhZw7dBg1lml4+FQVtefm4ZYEyINbkhww26QIGUTDNKPG9ugc8V3QhBMAPBqtN4DC8vHX5HW6AgvqJksI2+Gp4eenAUSA2SlAORgc4AaQfgHwWRgqdidWlsZGVyH6J3Zx8BxCAe0pnTIoJOxqHhBrc+vmpbnkDTRWcZe4t1/Nh4MlcoEg==,fm2_lJPETri/wcyxrbJu5PhZw7dBg1lml4+FQVtefm4ZYEyINbkhww26QIGUTDNKPG9ugc8V3QhBMAPBqtN4DC8vHX5HW6AgvqJksI2+Gp4eenAUSMQQZL/ejYvbhO7VxiEycGWtecO5aHR0cHM6Ly9hcGkuZmx5LmlvL2FhYS92MZgEks5ny3ibzmfOG7kXzgABZUEKkc4AAWVBDMQQv5MyO1GyNdeQM8c06kmVB8Qgs5fAKMK4v1/cQ5lTdfv9NGRPxUzp3//qAeZkuBZt5HY=");

        return new HttpEntity<>(headers);
    }
}
