package com.example.hashingmachine;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/samples")
@RequiredArgsConstructor
class SampleController {
    private final InstanceService instanceService;
    private final List<Integer> samples = List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);

    @GetMapping
    List<Integer> getAll() {
        return samples;
    }

    record LimitedSample(String instanceId, List<Integer> samples) {
    }

    @GetMapping("/limited")
    LimitedSample getLimited() {
        var currentInstanceId = instanceService.getCurrentInstanceId();
        return getLimitedSamples(currentInstanceId);
    }

    @GetMapping("/limited/{instanceId}")
    LimitedSample getLimitedBy(@PathVariable String instanceId) {
        return getLimitedSamples(instanceId);
    }

    private LimitedSample getLimitedSamples(String instanceId) {
        var instances = instanceService.getInstanceIds();
        var filteredSamples = samples.stream()
                .filter(sample -> new ConsistentHashing(instances).getInstanceForKey(String.valueOf(sample)).equals(instanceId))
                .toList();
        return new LimitedSample(instanceId, filteredSamples);
    }

    @GetMapping("/instances")
    List<String> getInstances() {
        return instanceService.getInstanceIds();
    }
}
