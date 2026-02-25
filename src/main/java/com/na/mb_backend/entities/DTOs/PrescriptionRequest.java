package com.na.mb_backend.entities.DTOs;

import com.na.mb_backend.entities.PrescriptionType;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class PrescriptionRequest {
    private Long customerId;

    private LocalDate issueDate;
    private LocalDate startDate;
    private Integer durationInMonths;

    private String notes;

    // ─── Type + CNAM fields ───────────────────────────────────────────────────

    private PrescriptionType type;

    private String ordonnanceNumber;
    private String codeIndividuel;
    private String matriculeAdherent;
    private String regime;
    private String codeApci;


    private List<MedicineInput> medicines;
}
