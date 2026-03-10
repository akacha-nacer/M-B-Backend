package com.na.mb_backend.Controller.MB_controllers;
import com.na.mb_backend.User.User;
import com.na.mb_backend.entities.DTOs.*;
import com.na.mb_backend.entities.Prescription;
import com.na.mb_backend.entities.PrescriptionItem;
import com.na.mb_backend.repositories.DispenseEventRepository;
import com.na.mb_backend.repositories.PrescriptionItemRepository;
import com.na.mb_backend.service.PrescriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/prescriptions")
@RequiredArgsConstructor
public class PrescriptionController {
    private final PrescriptionService prescriptionService;
    private final PrescriptionItemRepository prescriptionItemRepository;
    private final DispenseEventRepository dispenseEventRepository;

    @PostMapping
    public ResponseEntity<PrescriptionResponse> create(
            @RequestBody PrescriptionRequest request,
            @AuthenticationPrincipal User currentUser) {

        Prescription prescription = prescriptionService.create(request, currentUser.getEmail());
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(prescription));
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<PrescriptionResponse>> getByCustomer(
            @PathVariable Long customerId) {

        List<PrescriptionResponse> responses = prescriptionService
                .getByCustomer(customerId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PrescriptionResponse> getById(@PathVariable Long id) {
        Prescription prescription = prescriptionService.getById(id);
        return ResponseEntity.ok(toResponse(prescription));
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<PrescriptionResponse> cancel(@PathVariable Long id) {
        Prescription prescription = prescriptionService.cancel(id);
        return ResponseEntity.ok(toResponse(prescription));
    }

    @PostMapping("/dispense")
    public ResponseEntity<DispenseResult> dispense(
            @RequestBody DispenseRequest request,
            @AuthenticationPrincipal User currentUser) {

        DispenseResult result = prescriptionService.dispense(request, currentUser.getEmail());

        if (result.isSuccess()) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.accepted().body(result);
        }
    }


    private PrescriptionResponse toResponse(Prescription prescription) {

        List<PrescriptionItem> items = prescriptionItemRepository
                .findByPrescriptionIdOrderByMonthNumberAsc(prescription.getId());

        Map<Integer, List<PrescriptionItemResponse>> itemsByMonth = items.stream()
                .collect(Collectors.groupingBy(
                        PrescriptionItem::getMonthNumber,
                        java.util.TreeMap::new,
                        Collectors.mapping(item -> {
                            List<DispenseEventResponse> events = dispenseEventRepository
                                    .findByPrescriptionItemIdOrderByDispensedAtAsc(item.getId())
                                    .stream()
                                    .map(DispenseEventResponse::from)
                                    .collect(Collectors.toList());
                            return PrescriptionItemResponse.from(item, events);
                        }, Collectors.toList())
                ));

        return PrescriptionResponse.from(prescription, itemsByMonth);
    }
}
