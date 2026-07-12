package com.dispatchflow.guides.infrastructure.config;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class AzureRoleGrantedAuthoritiesConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

    private static final String AUTHORITY_PREFIX = "ROLE_";
    private static final String APP_ROLES_CLAIM = "roles";
    private static final String B2C_EXTENSION_ROLE_CLAIM = "extension_Rol";

    @Override
    public Collection<GrantedAuthority> convert(Jwt jwt) {
        Set<String> roleNames = new LinkedHashSet<>();
        roleNames.addAll(claimValues(jwt, APP_ROLES_CLAIM));
        roleNames.addAll(claimValues(jwt, B2C_EXTENSION_ROLE_CLAIM));

        List<GrantedAuthority> authorities = new ArrayList<>();
        for (String roleName : roleNames) {
            authorities.add(new SimpleGrantedAuthority(AUTHORITY_PREFIX + roleName));
        }
        return authorities;
    }

    private static Collection<String> claimValues(Jwt jwt, String claimName) {
        Object claim = jwt.getClaim(claimName);
        if (claim == null) {
            return List.of();
        }
        if (claim instanceof String roleName) {
            return roleName.isBlank() ? List.of() : List.of(roleName.trim());
        }
        if (claim instanceof Collection<?> values) {
            List<String> roleNames = new ArrayList<>();
            for (Object value : values) {
                if (value != null) {
                    String roleName = value.toString().trim();
                    if (!roleName.isEmpty()) {
                        roleNames.add(roleName);
                    }
                }
            }
            return roleNames;
        }
        return List.of();
    }
}
