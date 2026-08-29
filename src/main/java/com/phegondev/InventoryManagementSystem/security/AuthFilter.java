package com.phegondev.InventoryManagementSystem.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@Slf4j
@RequiredArgsConstructor
public class AuthFilter extends OncePerRequestFilter {
    private final JwtUtils jwtUtils;
    private final UserDetailsCache userDetailsCache;
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String token = getTokenFromRequest(request);

        if (token != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                // Verify the signature and read the subject BEFORE touching the database.
                // The previous order issued a user lookup for every request carrying any
                // bearer header, valid or not.
                String email = jwtUtils.getUsernameFromToken(token);

                if (StringUtils.hasText(email)) {
                    // Served from a 30s TTL cache; UserServiceImpl evicts on
                    // role/password/email changes so admin actions apply immediately.
                    UserDetails userDetails = userDetailsCache.get(email);

                    if (jwtUtils.isTokenValid(token, userDetails)) {
                        UsernamePasswordAuthenticationToken authenticationToken =
                                new UsernamePasswordAuthenticationToken(
                                        userDetails, null, userDetails.getAuthorities());
                        authenticationToken.setDetails(
                                new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                    }
                }
            } catch (Exception e) {
                // A bad or expired token is not a server error — leave the context
                // unauthenticated and let the entry point return 401.
                log.debug("Rejected bearer token: {}", e.getMessage());
                SecurityContextHolder.clearContext();
            }
        }

        // Must NOT be wrapped in a try/catch. Swallowing here committed a truncated
        // response and hid every downstream failure from the client.
        filterChain.doFilter(request, response);
    }

    private String getTokenFromRequest(HttpServletRequest request){
        String tokenWithBearer = request.getHeader("Authorization");
        if (tokenWithBearer != null && tokenWithBearer.startsWith("Bearer ")) {
            return tokenWithBearer.substring(7);
        }
        // Fallback to httpOnly cookie (set by /api/auth/login). Allows middleware/proxy
        // to guard routes even if client omits Authorization header.
        if (request.getCookies() != null) {
            for (var c : request.getCookies()) {
                if ("authToken".equals(c.getName()) && c.getValue() != null && !c.getValue().isBlank()) {
                    return c.getValue();
                }
            }
        }
        return null;
    }
}
