package com.na.mb_backend.entities.DTOs;
import com.na.mb_backend.entities.DispenseEvent;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class DispenseEventResponse {

    private Long id;
    private Integer quantityDispensed;
    private LocalDateTime dispensedAt;
    private String dispensedBy;
    private String note;
    private boolean dispensedEarly;
    private boolean dispensedOnExpired;

    public static DispenseEventResponse from(DispenseEvent event) {
        return DispenseEventResponse.builder()
                .id(event.getId())
                .quantityDispensed(event.getQuantityDispensed())
                .dispensedAt(event.getDispensedAt())
                .dispensedBy(event.getDispensedBy())
                .note(event.getNote())
                .dispensedEarly(event.isDispensedEarly())
                .dispensedOnExpired(event.isDispensedOnExpired())
                .build();
    }
}
