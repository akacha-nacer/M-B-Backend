package com.na.mb_backend.Controller.MB_controllers;
import com.na.mb_backend.entities.Medicine;
import com.na.mb_backend.exceptions.BusinessException;
import com.na.mb_backend.repositories.MedicineRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/medicines")
@RequiredArgsConstructor
public class MedicineController {

    private final MedicineRepository medicineRepository;
    @GetMapping
    public ResponseEntity<List<Medicine>> search(
            @RequestParam(required = false) String search) {

        if (search == null || search.isBlank()) {
            return ResponseEntity.ok(medicineRepository.findByActiveTrueOrderByFullNameAsc());
        }
        return ResponseEntity.ok(medicineRepository.searchActive(search.trim()));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Medicine> create(@RequestBody Medicine medicine) {
        medicine.setActive(true);
        return ResponseEntity.ok(medicineRepository.save(medicine));
    }
    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deactivate(@PathVariable Long id) {
        Medicine medicine = medicineRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Médicament introuvable: " + id));
        medicine.setActive(false);
        medicineRepository.save(medicine);
        return ResponseEntity.noContent().build();
    }
}
