package com.phegondev.InventoryManagementSystem.security;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * Short-TTL cache in front of CustomUserDetailsService. Every authenticated API
 * call used to issue a users-table SELECT just to re-resolve roles; with this the
 * database is hit at most once per user per TTL window.
 *
 * Consistency: role/email/password changes made through UserService evict
 * explicitly, so admin actions apply immediately. Anything else (direct DB edits)
 * converges within USER_DETAILS_TTL.
 */
@Component
public class UserDetailsCache {

    static final Duration USER_DETAILS_TTL = Duration.ofSeconds(30);
    private static final long MAX_ENTRIES = 1_000;

    private final CustomUserDetailsService delegate;
    private final LoadingCache<String, UserDetails> cache;

    public UserDetailsCache(CustomUserDetailsService delegate) {
        this.delegate = delegate;
        this.cache = Caffeine.newBuilder()
                .expireAfterWrite(USER_DETAILS_TTL)
                .maximumSize(MAX_ENTRIES)
                .build(delegate::loadUserByUsername);
    }

    /** Loads through to the DB on miss; throws whatever the loader throws. */
    public UserDetails get(String email) {
        return cache.get(email);
    }

    public void evict(String email) {
        if (email != null) {
            cache.invalidate(email);
        }
    }
}
