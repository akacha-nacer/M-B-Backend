package com.na.mb_backend.User;


import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.na.mb_backend.User.Permission.*;

@RequiredArgsConstructor
public enum Role {
    USER(Collections.emptySet()),
    ADMIN(Set.of(
            ADMIN_CREATE,
            ADMIN_DELETE,
            ADMIN_UPDATE,
            ADMIN_READ,
            MANAGER_CREATE,
            MANAGER_DELETE,
            MANAGER_UPDATE,
            MANAGER_READ
    )),
    MANAGER(Set.of(
            MANAGER_CREATE,
            MANAGER_DELETE,
            MANAGER_UPDATE,
            MANAGER_READ
    ));
    ;
    @Getter
    private final Set<Permission> permissions ;

    public List<SimpleGrantedAuthority> getAuthorities(){
        var authorities = getPermissions()
                .stream()
                .map(permission -> new SimpleGrantedAuthority(permission.getPermission()))
                .collect(Collectors.toList());
        authorities.add(new SimpleGrantedAuthority("ROLE_"+this.name()));
        return authorities;
    }

}
