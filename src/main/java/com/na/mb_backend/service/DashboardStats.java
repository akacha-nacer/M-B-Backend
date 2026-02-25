package com.na.mb_backend.service;

public record DashboardStats(
        long active,
        long completed,
        long cancelled,
        long expiredActive
) {}