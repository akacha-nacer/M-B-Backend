package com.na.mb_backend.entities.DTOs;

import lombok.Data;

@Data
public class DispenseRequest {

    private Long itemId;

    private Integer quantity;

    private boolean overrideExpired;

    private boolean overrideEarly;

    private String note;
}
