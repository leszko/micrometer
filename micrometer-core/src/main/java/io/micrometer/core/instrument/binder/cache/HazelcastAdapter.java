package io.micrometer.core.instrument.binder.cache;

class HazelcastAdapter {
    static String nameOf(Object cache) {
        try {
            return (String) iMapClass().getMethod("getName").invoke(cache);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static Class iMapClass() throws ClassNotFoundException {
        try {
            return Class.forName("com.hazelcast.map.IMap");
        } catch (ClassNotFoundException e) {
            return Class.forName("com.hazelcast.core.IMap");
        }
    }

    private static Class localMapStatsClass() throws ClassNotFoundException {
        try {
            return Class.forName("com.hazelcast.map.LocalMapStats");
        } catch (ClassNotFoundException e) {
            return Class.forName("com.hazelcast.monitor.LocalMapStats");
        }
    }

    private static Class nearCacheStatsClass() throws ClassNotFoundException {
        try {
            return Class.forName("com.hazelcast.nearcache.NearCacheStats");
        } catch (ClassNotFoundException e) {
            return Class.forName("com.hazelcast.monitor.NearCacheStats");
        }
    }

    static class IMap<K, V> {
        Object cache;

        IMap(Object cache) {
            this.cache = cache;
        }

        LocalMapStats getLocalMapStats() {
            try {
                return new LocalMapStats(iMapClass().getMethod("getLocalMapStats").invoke(cache));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    static class LocalMapStats {
        Object localMapStats;

        LocalMapStats(Object localMapStats) {
            this.localMapStats = localMapStats;
        }

        long getOwnedEntryCount() {
            try {
                return (long) localMapStatsClass().getMethod("getOwnedEntryCount").invoke(localMapStats);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        long getHits() {
            try {
                return (long) localMapStatsClass().getMethod("getHits").invoke(localMapStats);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        long getPutOperationCount() {
            try {
                return (long) localMapStatsClass().getMethod("getPutOperationCount").invoke(localMapStats);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        double getBackupEntryCount() {
            try {
                return (long) localMapStatsClass().getMethod("getBackupEntryCount").invoke(localMapStats);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        long getBackupEntryMemoryCost() {
            try {
                return (long) localMapStatsClass().getMethod("getBackupEntryMemoryCost").invoke(localMapStats);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        long getOwnedEntryMemoryCost() {
            try {
                return (long) localMapStatsClass().getMethod("getOwnedEntryMemoryCost").invoke(localMapStats);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        long getGetOperationCount() {
            try {
                return (Long) localMapStatsClass().getMethod("getGetOperationCount").invoke(localMapStats);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        NearCacheStats getNearCacheStats() {
            try {
                return new NearCacheStats(localMapStatsClass().getMethod("getNearCacheStats").invoke(localMapStats));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        long getTotalGetLatency() {
            try {
                return (long) localMapStatsClass().getMethod("getTotalGetLatency").invoke(localMapStats);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        long getTotalPutLatency() {
            try {
                return (long) localMapStatsClass().getMethod("getTotalPutLatency").invoke(localMapStats);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        long getRemoveOperationCount() {
            try {
                return (Long) localMapStatsClass().getMethod("getRemoveOperationCount").invoke(localMapStats);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        long getTotalRemoveLatency() {
            try {
                return (long) localMapStatsClass().getMethod("getTotalRemoveLatency").invoke(localMapStats);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    static class NearCacheStats {
        Object nearCacheStats;

        NearCacheStats(Object nearCacheStats) {
            this.nearCacheStats = nearCacheStats;
        }

        long getHits() {
            try {
                return (long) nearCacheStatsClass().getMethod("getHits").invoke(nearCacheStats);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        long getMisses() {
            try {
                return (long) nearCacheStatsClass().getMethod("getMisses").invoke(nearCacheStats);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        long getEvictions() {
            try {
                return (long) nearCacheStatsClass().getMethod("getEvictions").invoke(nearCacheStats);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        long getPersistenceCount() {
            try {
                return (long) nearCacheStatsClass().getMethod("getPersistenceCount").invoke(nearCacheStats);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

}
