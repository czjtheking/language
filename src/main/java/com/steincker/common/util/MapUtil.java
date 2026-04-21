package com.steincker.common.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Map 工具类
 * @author ST000050
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MapUtil {
    // ---------- as ----------

    @SuppressWarnings("unchecked")
    public static <K, V, M extends Map<K, V>> M asMap(Object... values) {
        return (M) putMap(new HashMap<>(), values);
    }

    @SuppressWarnings("unchecked")
    public static <K, V, M extends Map<K, V>> M putMap(M map, Object... values) {
        for (int i = 0; i < values.length / 2; i++) {
            map.put((K) values[2 * i], (V) values[2 * i + 1]);
        }
        return map;
    }

    // ---------- put ----------

    /** computeIfAbsent 封装 */
    public static <K, V, M extends Map<K, V>> V computeIfAbsent(M map, K key, Supplier<? extends V> supplier) {
        return computeIfAbsent(map, key, k -> supplier.get());
    }

    /** computeIfAbsent 封装 【ConcurrentHashMap JDK 8 存在死锁和性能问题，且无法避免 mapping 重复执行】 */
    public static <K, V, M extends Map<K, V>> V computeIfAbsent(M map, K key, Function<? super K, ? extends V> mapping) {
        // JDK 11 ConcurrentHashMap 死循环 bug、性能问题 等已修复，可以不用额外处理
        return map.computeIfAbsent(key, mapping);
    }

    // ---------- split -----------

    /** 将 Map 按指定数量分组 */
    public static <K, V, M extends Map<K, V>> List<M> split(M m, int size, Supplier<M> supplier) {
        if (m == null || m.isEmpty()) {
            return Collections.emptyList();
        }

        List<M> list = new ArrayList<>();
        M cc = supplier.get();

        // 指定数据
        for (Map.Entry<K, V> entry : m.entrySet()) {
            K k = entry.getKey();
            V v = entry.getValue();
            cc.put(k, v);
            if (cc.size() == size) {
                list.add(cc);
                cc = supplier.get();
            }
        }

        // 剩余数据
        if (!cc.isEmpty()) {
            list.add(cc);
        }

        return list;
    }
}
