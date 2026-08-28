package com.phegondev.InventoryManagementSystem.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * Reference-data caching (categories, brands, units, ...). These tables change
 * rarely but are read on almost every page load; a short TTL keeps them off the
 * database without any explicit invalidation contract beyond the @CacheEvict on
 * their write paths. Products are deliberately NOT cached — stock mutates constantly.
 */
@Configuration
@EnableCaching
public class CacheConfig {

    static final long TTL_MINUTES = 5;
    static final long MAX_ENTRIES = 500;

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager manager = new CaffeineCacheManager(
                "categories", "brands", "units", "variants", "warehouses", "equipments", "branches");
        manager.setCaffeine(Caffeine.newBuilder()
                .expireAfterWrite(TTL_MINUTES, TimeUnit.MINUTES)
                .maximumSize(MAX_ENTRIES));
        return manager;
    }
}
