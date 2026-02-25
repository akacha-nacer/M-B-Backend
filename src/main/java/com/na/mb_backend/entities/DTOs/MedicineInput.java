package com.na.mb_backend.entities.DTOs;

import lombok.Data;

@Data
public class MedicineInput {

    private Long medicineId;

    private String customMedicineName;

    private String instructions;

    private Integer fromMonth;

    private Integer toMonth;

    private Integer quantityPerMonth;
}
