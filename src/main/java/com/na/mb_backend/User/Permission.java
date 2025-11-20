package com.na.mb_backend.User;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum Permission {

    ADMIN_READ("admin:read"),
    ADMIN_UPDATE("admin:update"),
    ADMIN_CREATE("admin:create"),
    ADMIN_DELETE("admin:delete"),

    MANAGER_READ("manager:read"),
    MANAGER_UPDATE("manager:update"),
    MANAGER_CREATE("manager:create"),
    MANAGER_DELETE("manager:delete"),

    NURSE_READ("nurse:read"),
    NURSE_UPDATE("nurse:update"),
    NURSE_CREATE("nurse:create"),
    NURSE_DELETE("nurse:delete"),

    PATIENT_READ("patient:read"),
    PATIENT_UPDATE("patient:update"),
    PATIENT_CREATE("patient:create"),
    PATIENT_DELETE("patient:delete"),
    ;
    @Getter
    private final String permission;

}
