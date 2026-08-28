package com.phegondev.InventoryManagementSystem.security;

import com.phegondev.InventoryManagementSystem.user.User;
import lombok.Builder;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Data
@Builder
public class AuthUser implements UserDetails {

    private User user;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        String role = user.getRole() != null ? user.getRole().name() : "UNKNOWN";
        var perms = RolePermissionMapping.forRole(user.getRole());
        var authorities = new java.util.ArrayList<GrantedAuthority>();
        authorities.add(new SimpleGrantedAuthority(role));
        authorities.add(new SimpleGrantedAuthority("ROLE_" + role));
        for (var p : perms) {
            authorities.add(new SimpleGrantedAuthority(p.name()));
            authorities.add(new SimpleGrantedAuthority("PERM_" + p.name()));
        }
        return authorities;
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return UserDetails.super.isAccountNonExpired();
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
