package com.coogpath.coogpath.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.function.Supplier;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

import com.coogpath.coogpath.dto.PlanResult;

/**
 * Generated plans, keyed by everything that determines them: the catalog version
 * plus the planner inputs. A new transcript, preference, or migration produces a
 * new key, so entries never need explicit eviction and simply expire. Backed by
 * Redis when CACHE_TYPE=redis, otherwise by an in-process Caffeine cache.
 *
 * Cache failures (e.g. Redis unavailable) are logged and the plan is computed
 * directly, so the cache can never take plan generation down.
 */
@Component
public class PlanCache {

    public static final String CACHE_NAME = "plans";

    private static final Logger log = LoggerFactory.getLogger(PlanCache.class);
    private static final String KEY_FORMAT_VERSION = "v1";

    private final Cache cache;
    private final ObjectProvider<Flyway> flyway;
    private volatile String catalogVersion;

    public PlanCache(CacheManager cacheManager, ObjectProvider<Flyway> flyway) {
        this.cache = cacheManager.getCache(CACHE_NAME);
        this.flyway = flyway;
    }

    /** Builds a compact key from the planner inputs; {@code parts} must fully determine the plan. */
    public String key(Object... parts) {
        StringBuilder raw = new StringBuilder();
        for (Object part : parts) {
            raw.append(part).append('|');
        }
        return KEY_FORMAT_VERSION + ":" + catalogVersion() + ":" + sha256(raw.toString());
    }

    public PlanResult get(String key, Supplier<PlanResult> generate) {
        if (cache == null) return generate.get();

        try {
            PlanResult cached = cache.get(key, PlanResult.class);
            if (cached != null) return cached;
        } catch (RuntimeException ex) {
            log.warn("Plan cache read failed; generating without cache: {}", ex.getMessage());
            return generate.get();
        }

        PlanResult plan = generate.get();
        try {
            cache.put(key, plan);
        } catch (RuntimeException ex) {
            log.warn("Plan cache write failed: {}", ex.getMessage());
        }
        return plan;
    }

    /** The latest applied Flyway migration, so cached plans from an older catalog are never served. */
    private String catalogVersion() {
        String version = catalogVersion;
        if (version == null) {
            Flyway migrations = flyway.getIfAvailable();
            MigrationInfo current = migrations != null ? migrations.info().current() : null;
            version = current != null && current.getVersion() != null ? current.getVersion().getVersion() : "0";
            catalogVersion = version;
        }
        return version;
    }

    private static String sha256(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest, 0, 16);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException(ex);
        }
    }
}
