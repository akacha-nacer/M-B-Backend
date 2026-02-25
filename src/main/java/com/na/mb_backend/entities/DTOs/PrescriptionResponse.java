package com.na.mb_backend.entities.DTOs;
import com.na.mb_backend.entities.Prescription;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class PrescriptionResponse {

    private Long id;

    private Long customerId;
    private String customerFullName;
    private String customerNationalId;


    private LocalDate issueDate;
    private LocalDate startDate;
    private Integer durationInMonths;
    private LocalDate endDate;

    // ─── Status  ────────────────────────────────
    private String status;
    private boolean expired;
    private int currentMonth;

    // ─── Type + CNAM ─────────────────────────────────────────────────────────
    private String type;
    private String ordonnanceNumber;
    private String codeIndividuel;
    private String matriculeAdherent;
    private String regime;
    private String codeApci;

    // ── ─────────────────────────────────────────────────────────────────
    private String notes;
    private String createdBy;
    private LocalDateTime createdAt;


    private Map<Integer, List<PrescriptionItemResponse>> itemsByMonth;

    public static PrescriptionResponse from(Prescription prescription, Map<Integer, List<PrescriptionItemResponse>> itemsByMonth) {
        return PrescriptionResponse.builder()
                .id(prescription.getId())
                .customerId(prescription.getCustomer().getId())
                .customerFullName(prescription.getCustomer().getFullName())
                .customerNationalId(prescription.getCustomer().getNationalId())
                .issueDate(prescription.getIssueDate())
                .startDate(prescription.getStartDate())
                .durationInMonths(prescription.getDurationInMonths())
                .endDate(prescription.getEndDate())
                .status(prescription.getStatus().name())
                .expired(prescription.isExpired())
                .currentMonth(prescription.getCurrentMonth())
                .type(prescription.getType().name())
                .ordonnanceNumber(prescription.getOrdonnanceNumber())
                .codeIndividuel(prescription.getCodeIndividuel())
                .matriculeAdherent(prescription.getMatriculeAdherent())
                .regime(prescription.getRegime())
                .codeApci(prescription.getCodeApci())
                .notes(prescription.getNotes())
                .createdBy(prescription.getCreatedBy())
                .createdAt(prescription.getCreatedAt())
                .itemsByMonth(itemsByMonth)
                .build();
    }
}
