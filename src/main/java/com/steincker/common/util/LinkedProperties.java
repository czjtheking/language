package com.steincker.common.util;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * 有序的Properties
 *
 * */
public class LinkedProperties extends Properties {
    private final Map<Object, Object> linkedMap = new LinkedHashMap<>();

    @Override
    public synchronized Object put(Object key, Object value) {
        return linkedMap.put(key, value);
    }

    @Override
    public synchronized Object get(Object key) {
        return linkedMap.get(key);
    }

    @Override
    public Set<Map.Entry<Object, Object>> entrySet() {
        return linkedMap.entrySet();
    }

    @Override
    public synchronized void clear() {
        linkedMap.clear();
    }

    @Override
    public synchronized boolean containsKey(Object key) {
        return linkedMap.containsKey(key);
    }

    @Override
    public synchronized boolean containsValue(Object value) {
        return linkedMap.containsValue(value);
    }

    @Override
    public synchronized boolean isEmpty() {
        return linkedMap.isEmpty();
    }

    @Override
    public synchronized int size() {
        return linkedMap.size();
    }

    // Override other necessary methods to delegate to linkedMap as needed
}
