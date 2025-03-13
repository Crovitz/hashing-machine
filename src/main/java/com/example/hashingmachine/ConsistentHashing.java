package com.example.hashingmachine;

import org.apache.commons.codec.digest.MurmurHash3;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;

import static java.util.Map.entry;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.IntStream.range;

class ConsistentHashing {
    private final NavigableMap<Integer, String> ring;

    ConsistentHashing(List<String> instanceIds) {
        if (instanceIds.isEmpty()) {
            throw new IllegalArgumentException("Instance list cannot be empty");
        }
        this.ring = instanceIds.stream()
                .flatMap(instance -> range(0, 1000)
                        .mapToObj(i -> entry(hash(instance + "#" + i), instance)))
                .collect(toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (existing, replacement) -> existing,
                        ConcurrentSkipListMap::new));
    }

    String getInstanceForKey(String key) {
        return ofNullable(ring.ceilingEntry(hash(key)))
                .orElseGet(ring::firstEntry)
                .getValue();
    }

    private int hash(String key) {
        int hash1 = MurmurHash3.hash32x86(key.getBytes(StandardCharsets.UTF_8));
        int hash2 = Integer.rotateLeft(hash1, 13);
        return (hash1 ^ hash2) & 0x7fffffff;
    }
}
