package com.na.mb_backend.entities.DTOs;
import com.na.mb_backend.service.DashboardStats;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DashboardStatsResponse {
    private long active;
    private long completed;
    private long cancelled;
    private long expiredActive;

    public static DashboardStatsResponse from(DashboardStats stats) {
        return DashboardStatsResponse.builder()
                .active(stats.active())
                .completed(stats.completed())
                .cancelled(stats.cancelled())
                .expiredActive(stats.expiredActive())
                .build();
    }
}
