package io.micrometer.core.instrument.binder.cache;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

import static java.lang.invoke.MethodType.methodType;

class HazelcastIMapAdapter {
    private static final MethodHandle GET_LOCAL_MAP_STATS;

    private static final MethodHandle GET_NAME;

    static {
        GET_NAME = resolveIMapMethod("getName", methodType(String.class));
        GET_LOCAL_MAP_STATS = resolveIMapMethod("getLocalMapStats", methodType(resolveLocalMapStatsImplementation()));
    }

    Object cache;

    HazelcastIMapAdapter(Object cache) {
        this.cache = cache;
    }

    LocalMapStats getLocalMapStats() {
        return new LocalMapStats(invoke(GET_LOCAL_MAP_STATS, cache));
    }

    static String nameOf(Object cache) {
        try {
            return (String) GET_NAME.invoke(cache);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    static class LocalMapStats {
        private static final MethodHandle GET_NEAR_CACHE_STATS;

        private static final MethodHandle getOwnedEntryCount;
        private static final MethodHandle getHits;
        private static final MethodHandle getPutOperationCount;
        private static final MethodHandle getBackupEntryCount;
        private static final MethodHandle getBackupEntryMemoryCost;
        private static final MethodHandle getOwnedEntryMemoryCost;
        private static final MethodHandle getGetOperationCount;
        private static final MethodHandle getTotalGetLatency;
        private static final MethodHandle getTotalPutLatency;
        private static final MethodHandle getRemoveOperationCount;
        private static final MethodHandle getTotalRemoveLatency;

        static {
            GET_NEAR_CACHE_STATS = resolveLocalMapStatsMethod("getNearCacheStats", methodType(resolveNearCacheStatsImplementation()));

            getOwnedEntryCount = resolveLocalMapStatsMethod("getOwnedEntryCount", methodType(long.class));
            getHits = resolveLocalMapStatsMethod("getHits", methodType(long.class));
            getPutOperationCount = resolveLocalMapStatsMethod("getPutOperationCount", methodType(long.class));
            getBackupEntryCount = resolveLocalMapStatsMethod("getBackupEntryCount", methodType(long.class));
            getBackupEntryMemoryCost = resolveLocalMapStatsMethod("getBackupEntryMemoryCost", methodType(long.class));
            getOwnedEntryMemoryCost = resolveLocalMapStatsMethod("getOwnedEntryMemoryCost", methodType(long.class));
            getGetOperationCount = resolveLocalMapStatsMethod("getGetOperationCount", methodType(long.class));
            getTotalGetLatency = resolveLocalMapStatsMethod("getTotalGetLatency", methodType(long.class));
            getTotalPutLatency = resolveLocalMapStatsMethod("getTotalPutLatency", methodType(long.class));
            getRemoveOperationCount = resolveLocalMapStatsMethod("getRemoveOperationCount", methodType(long.class));
            getTotalRemoveLatency = resolveLocalMapStatsMethod("getTotalRemoveLatency", methodType(long.class));
        }

        Object localMapStats;

        LocalMapStats(Object localMapStats) {
            this.localMapStats = localMapStats;
        }

        long getOwnedEntryCount() {
            return (long) invoke(getOwnedEntryCount, localMapStats);
        }

        long getHits() {
            return (long) invoke(getHits, localMapStats);
        }

        long getPutOperationCount() {
            return (long) invoke(getPutOperationCount, localMapStats);
        }

        double getBackupEntryCount() {
            return (long) invoke(getBackupEntryCount, localMapStats);
        }

        long getBackupEntryMemoryCost() {
            return (long) invoke(getBackupEntryMemoryCost, localMapStats);
        }

        long getOwnedEntryMemoryCost() {
            return (long) invoke(getOwnedEntryMemoryCost, localMapStats);
        }

        long getGetOperationCount() {
            return (long) invoke(getGetOperationCount, localMapStats);
        }

        NearCacheStats getNearCacheStats() {
            return new NearCacheStats(invoke(GET_NEAR_CACHE_STATS, localMapStats));
        }

        long getTotalGetLatency() {
            return (long) invoke(getTotalGetLatency, localMapStats);
        }

        long getTotalPutLatency() {
            return (long) invoke(getTotalPutLatency, localMapStats);
        }

        long getRemoveOperationCount() {
            return (long) invoke(getRemoveOperationCount, localMapStats);
        }

        long getTotalRemoveLatency() {
            return (long) invoke(getTotalRemoveLatency, localMapStats);
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

        private static final MethodHandle getHits;
        private static final MethodHandle getMisses;
        private static final MethodHandle getEvictions;
        private static final MethodHandle getPersistenceCount;

        static {
            getHits = resolveNearCacheStatsMethod("getHits", methodType(long.class));
            getMisses = resolveNearCacheStatsMethod("getMisses", methodType(long.class));
            getEvictions = resolveNearCacheStatsMethod("getEvictions", methodType(long.class));
            getPersistenceCount = resolveNearCacheStatsMethod("getPersistenceCount", methodType(long.class));
        }

        Object nearCacheStats;

        NearCacheStats(Object nearCacheStats) {
            this.nearCacheStats = nearCacheStats;
        }

        long getHits() {
            return (long) invoke(getHits, nearCacheStats);
        }

        long getMisses() {
            return (long) invoke(getMisses, nearCacheStats);
        }

        long getEvictions() {
            return (long) invoke(getEvictions, nearCacheStats);
        }

        long getPersistenceCount() {
            return (long) invoke(getPersistenceCount, nearCacheStats);
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
