package com.erp.platform.core.security;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Getter
public class UserPrincipal implements UserDetails {

    private final UUID id;
    private final String email;
    private final String password;
    private final String role;
    private final UUID partnerId;
    private final String partnerCode;
    private final UUID defaultLocalId;
    private final List<UUID> localAccess;
    private final boolean active;

    public UserPrincipal(UUID id, String email, String password, String role,
                         UUID partnerId, String partnerCode, UUID defaultLocalId,
                         List<UUID> localAccess, boolean active) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.role = role;
        this.partnerId = partnerId;
        this.partnerCode = partnerCode;
        this.defaultLocalId = defaultLocalId;
        this.localAccess = localAccess;
        this.active = active;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role));
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return active;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return active;
    }
}
