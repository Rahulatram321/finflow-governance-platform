package com.company.finflow.security.auth;

import com.company.finflow.user.domain.User;
import java.util.Collection;
import java.util.List;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Getter
public class FinflowUserPrincipal implements UserDetails {

    private final Long userId;
    private final Long tenantId;
    private final String username;
    private final String password;
    private final String status;
    private final ApplicationRole role;
    private final List<GrantedAuthority> authorities;

    public FinflowUserPrincipal(User user) {
        this.userId = user.getId();
        this.tenantId = user.getTenant().getId();
        this.username = user.getEmail();
        this.password = user.getPasswordHash();
        this.status = user.getStatus();
        this.role = resolveRole(user.getRole());
        this.authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    public boolean hasRole(ApplicationRole expectedRole) {
        return role == expectedRole;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
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
        return "ACTIVE".equalsIgnoreCase(status);
    }

    private ApplicationRole resolveRole(String roleValue) {
        try {
            return ApplicationRole.valueOf(roleValue.toUpperCase());
        } catch (IllegalArgumentException exception) {
            throw new IllegalStateException("Unsupported role configured for user: " + roleValue);
        }
    }
}
