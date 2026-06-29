package com.erp.platform.core.security;

import lombok.Builder;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Getter
@Builder
public class JwtUserPrincipal implements UserDetails {

    private final String email;
    private final String role;
    private final String userId;
    private final String partnerId;       // UUID en String, peut être null (PLATFORM_ADMIN)
    private final String partnerCode;
    private final String defaultLocalId;  // UUID en String, peut être null

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        String authorityName = role.startsWith("ROLE_") ? role : "ROLE_" + role;
        return List.of(new SimpleGrantedAuthority(authorityName));
    }

    @Override public String getPassword()  { return null; }
    @Override public String getUsername()  { return email; }
    @Override public boolean isAccountNonExpired()   { return true; }
    @Override public boolean isAccountNonLocked()    { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled()   { return true; }
}
