package com.phegondev.InventoryManagementSystem.tenant;

import com.phegondev.InventoryManagementSystem.enums.UserRole;
import com.phegondev.InventoryManagementSystem.security.AuthUser;
import com.phegondev.InventoryManagementSystem.user.User;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Resolves branch tenancy from the authenticated user and the optional
 * X-Branch-Id header (used by SUPER_ADMIN to "view as branch").
 *
 * Rules (§7-8):
 * - Never trust branchId from frontend for non-super-admins: we ignore the header.
 * - SUPER_ADMIN may send X-Branch-Id to scope the request to that branch.
 * - For non-super-admin, branchId is forced to user.branchId.
 */
@Component
@Slf4j
public class TenantFilter extends OncePerRequestFilter {

    private static final String BRANCH_HEADER = "X-Branch-Id";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof AuthUser au) {
                User user = au.getUser();
                UserRole role = user.getRole();
                boolean isSuper = role == UserRole.SUPER_ADMIN || role == UserRole.ADMIN;
                Long branchId = user.getBranchId();
                Long orgId = user.getOrganizationId();

                // Super admin may override branch via header for "view as branch"
                String headerBranch = request.getHeader(BRANCH_HEADER);
                if (isSuper && headerBranch != null && !headerBranch.isBlank()) {
                    try {
                        branchId = Long.valueOf(headerBranch.trim());
                    } catch (NumberFormatException e) {
                        log.warn("Invalid {} header: {}", BRANCH_HEADER, headerBranch);
                    }
                }
                // Non-super-admins: ignore header, force own branch
                TenantContext.set(new TenantContext.Tenant(
                        user.getId(), user.getEmail(), role, branchId, orgId != null ? orgId : 1L, isSuper
                ));
            }
            filterChain.doFilter(request, response);
        } finally {
            TenantContext.clear();
        }
    }
}
