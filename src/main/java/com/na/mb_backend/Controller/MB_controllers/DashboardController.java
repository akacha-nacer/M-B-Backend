package com.na.mb_backend.Controller.MB_controllers;
import com.na.mb_backend.entities.DTOs.DashboardStatsResponse;
import com.na.mb_backend.service.PrescriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {
    private final PrescriptionService prescriptionService;

    @GetMapping("/stats")
    public ResponseEntity<DashboardStatsResponse> getStats() {
        return ResponseEntity.ok(
                DashboardStatsResponse.from(prescriptionService.getDashboardStats())
        );
    }
}
