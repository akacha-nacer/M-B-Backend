package com.na.mb_backend.entities.DTOs;
import com.na.mb_backend.entities.PrescriptionItem;
import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class PrescriptionItemResponse {
    private Long id;
    private String medicineName;
    private Long medicineId;
    private String instructions;
    private Integer monthNumber;
    private Integer quantityPrescribed;
    private Integer quantityDispensed;
    private Integer quantityRemaining;
    private boolean fullyDispensed;

    // L/R history
    private List<DispenseEventResponse> dispenseEvents;

    public static PrescriptionItemResponse from(PrescriptionItem item,
                                                List<DispenseEventResponse> events) {
        return PrescriptionItemResponse.builder()
                .id(item.getId())
                .medicineName(item.getDisplayName())
                .medicineId(item.getMedicine() != null ? item.getMedicine().getId() : null)
                .instructions(item.getInstructions())
                .monthNumber(item.getMonthNumber())
                .quantityPrescribed(item.getQuantityPrescribed())
                .quantityDispensed(item.getQuantityDispensed())
                .quantityRemaining(item.getQuantityRemaining())
                .fullyDispensed(item.isFullyDispensed())
                .dispenseEvents(events)
                .build();
    }
}
