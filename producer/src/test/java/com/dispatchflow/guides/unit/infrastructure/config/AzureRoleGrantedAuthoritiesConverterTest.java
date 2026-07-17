package com.dispatchflow.guides.unit.infrastructure.config;

import com.dispatchflow.guides.infrastructure.config.AzureRoleGrantedAuthoritiesConverter;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AzureRoleGrantedAuthoritiesConverterTest {

    /*
     * Casos:
     * 1. Claim "roles" (App Roles) → ROLE_ADMIN
     * 2. Claim "extension_Rol" (atributo B2C) → ROLE_ADMIN
     * 3. Ambos claims presentes → union sin duplicar
     * 4. Sin claims de rol → vacío
     */

    private final AzureRoleGrantedAuthoritiesConverter converter = new AzureRoleGrantedAuthoritiesConverter();

    @Test
    void mapsRolesClaimFromAppRoles() {
        Jwt jwt = jwtWithClaims(Map.of("roles", List.of("ADMIN", "DESCARGA")));

        Set<String> authorities = authorityNames(converter.convert(jwt));

        assertEquals(Set.of("ROLE_ADMIN", "ROLE_DESCARGA"), authorities);
    }

    @Test
    void mapsExtensionRolClaimFromB2cAttribute() {
        Jwt jwt = jwtWithClaims(Map.of("extension_Rol", "ADMIN"));

        Set<String> authorities = authorityNames(converter.convert(jwt));

        assertEquals(Set.of("ROLE_ADMIN"), authorities);
    }

    @Test
    void mergesBothClaimsWithoutDuplicates() {
        Jwt jwt = jwtWithClaims(Map.of(
                "roles", List.of("ADMIN"),
                "extension_Rol", List.of("ADMIN", "DESCARGA")));

        Set<String> authorities = authorityNames(converter.convert(jwt));

        assertEquals(Set.of("ROLE_ADMIN", "ROLE_DESCARGA"), authorities);
    }

    @Test
    void returnsEmptyWhenNoRoleClaimsPresent() {
        Jwt jwt = jwtWithClaims(Map.of("sub", "user-1"));

        Collection<GrantedAuthority> authorities = converter.convert(jwt);

        assertTrue(authorities.isEmpty());
    }

    private static Jwt jwtWithClaims(Map<String, Object> claims) {
        return new Jwt(
                "token-value",
                Instant.parse("2026-06-02T10:00:00Z"),
                Instant.parse("2026-06-02T11:00:00Z"),
                Map.of("alg", "none"),
                claims);
    }

    private static Set<String> authorityNames(Collection<GrantedAuthority> authorities) {
        return authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());
    }
}
