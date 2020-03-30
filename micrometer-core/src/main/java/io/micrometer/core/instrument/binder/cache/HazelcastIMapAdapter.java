package io.micrometer.core.instrument.binder.cache;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

import static java.lang.invoke.MethodType.methodType;

/**
 * Adapter for Hazelcast {@code IMap} class created to provide support for both Hazelcast 3 and Hazelcast 4 at the
 * same time.
 * <p>
 * It dynamically checks which Hazelcast version is on the classpath and resolves the right one. Note that
 * {@code MethodHandle} is used, so the performance does not suffer.
 */
class HazelcastIMapAdapter {
    private static final MethodHandle GET_NAME;
    private static final MethodHandle GET_LOCAL_MAP_STATS;

    static {
        GET_NAME = resolveIMapMethod("getName", methodType(String.class));
        GET_LOCAL_MAP_STATS = resolveIMapMethod("getLocalMapStats", methodType(resolveLocalMapStatsImplementation()));
    }

    private Object cache;

    HazelcastIMapAdapter(Object cache) {
        this.cache = cache;
    }

    static String nameOf(Object cache) {
        try {
            return (String) GET_NAME.invoke(cache);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    LocalMapStats getLocalMapStats() {
        return new LocalMapStats(invoke(GET_LOCAL_MAP_STATS, cache));
    }

    static class LocalMapStats {
        private static final MethodHandle GET_NEAR_CACHE_STATS;

        private static final MethodHandle GET_OWNED_ENTRY_COUNT;
        private static final MethodHandle GET_HITS;
        private static final MethodHandle GET_PUT_OPERATION_COUNT;
        private static final MethodHandle GET_BACKUP_ENTRY_COUNT;
        private static final MethodHandle GET_BACKUP_ENTRY_MEMORY_COST;
        private static final MethodHandle GET_OWNED_ENTRY_MEMORY_COST;
        private static final MethodHandle GET_GET_OPERATION_COUNT;
        private static final MethodHandle GET_TOTAL_GET_LATENCY;
        private static final MethodHandle GET_TOTAL_PUT_LATENCY;
        private static final MethodHandle GET_REMOVE_OPERATION_COUNT;
        private static final MethodHandle GET_TOTAL_REMOVE_LATENCY;

        static {
            GET_NEAR_CACHE_STATS = resolveLocalMapStatsMethod("getNearCacheStats", methodType(resolveNearCacheStatsImplementation()));

            GET_OWNED_ENTRY_COUNT = resolveLocalMapStatsMethod("getOwnedEntryCount", methodType(long.class));
            GET_HITS = resolveLocalMapStatsMethod("getHits", methodType(long.class));
            GET_PUT_OPERATION_COUNT = resolveLocalMapStatsMethod("getPutOperationCount", methodType(long.class));
            GET_BACKUP_ENTRY_COUNT = resolveLocalMapStatsMethod("getBackupEntryCount", methodType(long.class));
            GET_BACKUP_ENTRY_MEMORY_COST = resolveLocalMapStatsMethod("getBackupEntryMemoryCost", methodType(long.class));
            GET_OWNED_ENTRY_MEMORY_COST = resolveLocalMapStatsMethod("getOwnedEntryMemoryCost", methodType(long.class));
            GET_GET_OPERATION_COUNT = resolveLocalMapStatsMethod("getGetOperationCount", methodType(long.class));
            GET_TOTAL_GET_LATENCY = resolveLocalMapStatsMethod("getTotalGetLatency", methodType(long.class));
            GET_TOTAL_PUT_LATENCY = resolveLocalMapStatsMethod("getTotalPutLatency", methodType(long.class));
            GET_REMOVE_OPERATION_COUNT = resolveLocalMapStatsMethod("getRemoveOperationCount", methodType(long.class));
            GET_TOTAL_REMOVE_LATENCY = resolveLocalMapStatsMethod("getTotalRemoveLatency", methodType(long.class));
        }

        private Object localMapStats;

        LocalMapStats(Object localMapStats) {
            this.localMapStats = localMapStats;
        }

        long getOwnedEntryCount() {
            return (long) invoke(GET_OWNED_ENTRY_COUNT, localMapStats);
        }

        long getHits() {
            return (long) invoke(GET_HITS, localMapStats);
        }

        long getPutOperationCount() {
            return (long) invoke(GET_PUT_OPERATION_COUNT, localMapStats);
        }

        double getBackupEntryCount() {
            return (long) invoke(GET_BACKUP_ENTRY_COUNT, localMapStats);
        }

        long getBackupEntryMemoryCost() {
            return (long) invoke(GET_BACKUP_ENTRY_MEMORY_COST, localMapStats);
        }

        long getOwnedEntryMemoryCost() {
            return (long) invoke(GET_OWNED_ENTRY_MEMORY_COST, localMapStats);
        }

        long getGetOperationCount() {
            return (long) invoke(GET_GET_OPERATION_COUNT, localMapStats);
        }

        NearCacheStats getNearCacheStats() {
            return new NearCacheStats(invoke(GET_NEAR_CACHE_STATS, localMapStats));
        }

        long getTotalGetLatency() {
            return (long) invoke(GET_TOTAL_GET_LATENCY, localMapStats);
        }

        long getTotalPutLatency() {
            return (long) invoke(GET_TOTAL_PUT_LATENCY, localMapStats);
        }

        long getRemoveOperationCount() {
            return (long) invoke(GET_REMOVE_OPERATION_COUNT, localMapStats);
        }

        long getTotalRemoveLatency() {
            return (long) invoke(GET_TOTAL_REMOVE_LATENCY, localMapStats);
        }

        private static MethodHandle resolveLocalMapStatsMethod(String name, MethodType mt) {
            try {
                return MethodHandles.publicLookup()
                        .findVirtual(resolveLocalMapStatsImplementation(), name, mt);
            } catch (NoSuchMethodException | IllegalAccessException e) {
                throw new IllegalStateException(e);
            }
        }
    }

    static class NearCacheStats {
        private static final MethodHandle GET_HITS;
        private static final MethodHandle GET_MISSES;
        private static final MethodHandle GET_EVICTIONS;
        private static final MethodHandle GET_PERSISTENCE_COUNT;

        static {
            GET_HITS = resolveNearCacheStatsMethod("getHits", methodType(long.class));
            GET_MISSES = resolveNearCacheStatsMethod("getMisses", methodType(long.class));
            GET_EVICTIONS = resolveNearCacheStatsMethod("getEvictions", methodType(long.class));
            GET_PERSISTENCE_COUNT = resolveNearCacheStatsMethod("getPersistenceCount", methodType(long.class));
        }

        private Object nearCacheStats;

        NearCacheStats(Object nearCacheStats) {
            this.nearCacheStats = nearCacheStats;
        }

        long getHits() {
            return (long) invoke(GET_HITS, nearCacheStats);
        }

        long getMisses() {
            return (long) invoke(GET_MISSES, nearCacheStats);
        }

        long getEvictions() {
            return (long) invoke(GET_EVICTIONS, nearCacheStats);
        }

        long getPersistenceCount() {
            return (long) invoke(GET_PERSISTENCE_COUNT, nearCacheStats);
        }

        private static MethodHandle resolveNearCacheStatsMethod(String name, MethodType mt) {
            try {
                return MethodHandles.publicLookup()
                        .findVirtual(resolveNearCacheStatsImplementation(), name, mt);
            } catch (NoSuchMethodException | IllegalAccessException e) {
                throw new IllegalStateException(e);
            }
        }
    }

    private static MethodHandle resolveIMapMethod(String name, MethodType mt) {
        try {
            return MethodHandles.publicLookup()
                    .findVirtual(resolveIMapImplementation(), name, mt);
        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
    }

    private static Class<?> resolveIMapImplementation() {
        return resolveOneOf("com.hazelcast.map.IMap", "com.hazelcast.core.IMap");
    }

    private static Class<?> resolveLocalMapStatsImplementation() {
        return resolveOneOf("com.hazelcast.map.LocalMapStats", "com.hazelcast.monitor.LocalMapStats");
    }

    private static Class<?> resolveNearCacheStatsImplementation() {
        return resolveOneOf("com.hazelcast.nearcache.NearCacheStats", "com.hazelcast.monitor.NearCacheStats");
    }

    private static Class<?> resolveOneOf(String class1, String class2) {
        try {
            return Class.forName(class1);
        } catch (ClassNotFoundException e) {
            try {
                return Class.forName(class2);
            } catch (ClassNotFoundException ex) {
                throw new IllegalStateException(ex);
            }
        }
    }

    private static Object invoke(MethodHandle mh, Object object) {
        try {
            return mh.invoke(object);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }
}
