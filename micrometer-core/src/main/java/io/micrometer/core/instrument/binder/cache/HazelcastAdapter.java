package io.micrometer.core.instrument.binder.cache;

import com.hazelcast.monitor.NearCacheStats;

class HazelcastAdapter {
    static String nameOf(Object cache) {
        if (cache instanceof com.hazelcast.core.IMap) {
            return ((com.hazelcast.core.IMap) cache).getName();
        }
        return null;
    }

    static class IMap<K, V> {
        com.hazelcast.core.IMap cache;

        public IMap(Object cache) {
            if (cache instanceof com.hazelcast.core.IMap) {
                this.cache = (com.hazelcast.core.IMap) cache;
            }
        }

        public LocalMapStats getLocalMapStats() {
            return new LocalMapStats(cache.getLocalMapStats());
        }
    }

    static class LocalMapStats {
        com.hazelcast.monitor.LocalMapStats localMapStats;

        public LocalMapStats(Object localMapStats) {
            if (localMapStats instanceof com.hazelcast.monitor.LocalMapStats) {
                this.localMapStats = (com.hazelcast.monitor.LocalMapStats) localMapStats;
            }
        }

        public Long getOwnedEntryCount() {
            return localMapStats.getOwnedEntryCount();
        }

        public long getHits() {
            return localMapStats.getHits();
        }

        public long getPutOperationCount() {
            return localMapStats.getPutOperationCount();
        }

        public double getBackupEntryCount() {
            return localMapStats.getBackupEntryCount();
        }

        public double getBackupEntryMemoryCost() {
            return localMapStats.getBackupEntryMemoryCost();
        }

        public double getOwnedEntryMemoryCost() {
            return localMapStats.getOwnedEntryMemoryCost();
        }

        public long getGetOperationCount() {
            return localMapStats.getGetOperationCount();
        }

        public NearCacheStats getNearCacheStats() {
            return localMapStats.getNearCacheStats();
        }

        public double getTotalGetLatency() {
            return localMapStats.getTotalGetLatency();
        }

        public double getTotalPutLatency() {
            return localMapStats.getTotalPutLatency();
        }

        public long getRemoveOperationCount() {
            return localMapStats.getRemoveOperationCount();
        }

        public double getTotalRemoveLatency() {
            return localMapStats.getTotalRemoveLatency();
        }
    }

}
