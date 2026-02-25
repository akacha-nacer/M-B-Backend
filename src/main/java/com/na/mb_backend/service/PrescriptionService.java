package com.na.mb_backend.service;

import com.na.mb_backend.entities.*;
import com.na.mb_backend.entities.DTOs.DispenseRequest;
import com.na.mb_backend.entities.DTOs.DispenseResult;
import com.na.mb_backend.entities.DTOs.MedicineInput;
import com.na.mb_backend.entities.DTOs.PrescriptionRequest;
import com.na.mb_backend.exceptions.BusinessException;
import com.na.mb_backend.repositories.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PrescriptionService {
    private final PrescriptionRepository      prescriptionRepository;
    private final PrescriptionItemRepository  prescriptionItemRepository;
    private final DispenseEventRepository dispenseEventRepository;
    private final CustomerRepository customerRepository;
    private final MedicineRepository medicineRepository;

    @Transactional
    public Prescription create(PrescriptionRequest request, String createdBy) {

        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new BusinessException("Patient introuvable: " + request.getCustomerId()));

        PrescriptionType type = request.getType() != null ? request.getType() : PrescriptionType.CNAM;
        if (type == PrescriptionType.CNAM && request.getOrdonnanceNumber() == null) {
            throw new BusinessException("Le numéro d'ordonnance est obligatoire pour une ordonnance CNAM.");
        }

        LocalDate startDate = request.getStartDate();
        Integer duration = request.getDurationInMonths();

        Prescription prescription = Prescription.builder()
                .customer(customer)
                .issueDate(request.getIssueDate())
                .startDate(startDate)
                .durationInMonths(duration)
                .endDate(startDate.plusMonths(duration))
                .status(PrescriptionStatus.ACTIVE)
                .type(type)
                .ordonnanceNumber(request.getOrdonnanceNumber())
                .codeIndividuel(request.getCodeIndividuel())
                .matriculeAdherent(request.getMatriculeAdherent())
                .regime(request.getRegime())
                .codeApci(request.getCodeApci())
                .notes(request.getNotes())
                .createdBy(createdBy)
                .build();

        if (request.getMedicines() != null) {
            for (MedicineInput input : request.getMedicines()) {
                validateMedicineInput(input, duration);

                int from = input.getFromMonth() != null ? input.getFromMonth() : 1;
                int to   = input.getToMonth()   != null ? input.getToMonth()   : duration;

                Medicine medicine = null;
                if (input.getMedicineId() != null) {
                    medicine = medicineRepository.findById(input.getMedicineId())
                            .orElseThrow(() -> new BusinessException(
                                    "Médicament introuvable: " + input.getMedicineId()));
                }

                for (int month = from; month <= to; month++) {
                    Medicine finalMedicine = medicine;
                    PrescriptionItem item = PrescriptionItem.builder()
                            .medicine(finalMedicine)
                            .customMedicineName(finalMedicine == null ? input.getCustomMedicineName() : null)
                            .instructions(input.getInstructions())
                            .monthNumber(month)
                            .quantityPrescribed(input.getQuantityPerMonth())
                            .quantityDispensed(0)
                            .build();
                    prescription.addItem(item);
                }
            }
        }

        return prescriptionRepository.save(prescription);
    }

    private void validateMedicineInput(MedicineInput input, int duration) {
        if (input.getMedicineId() == null && (input.getCustomMedicineName() == null
                || input.getCustomMedicineName().isBlank())) {
            throw new BusinessException(
                    "Chaque médicament doit avoir un nom ou être sélectionné depuis le catalogue.");
        }
        if (input.getQuantityPerMonth() == null || input.getQuantityPerMonth() <= 0) {
            throw new BusinessException("La quantité par mois doit être supérieure à 0.");
        }
        int from = input.getFromMonth() != null ? input.getFromMonth() : 1;
        int to   = input.getToMonth()   != null ? input.getToMonth()   : duration;
        if (from < 1 || to > duration || from > to) {
            throw new BusinessException(
                    "Plage de mois invalide: " + from + " → " + to +
                            " pour une ordonnance de " + duration + " mois.");
        }
    }
    public Prescription getById(Long id) {
        return prescriptionRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Ordonnance introuvable: " + id));
    }

    public List<Prescription> getByCustomer(Long customerId) {
        return prescriptionRepository.findByCustomerIdOrderByCreatedAtDesc(customerId);
    }

    public List<Prescription> getActiveByCustomer(Long customerId) {
        return prescriptionRepository.findByCustomerIdAndStatus(customerId, PrescriptionStatus.ACTIVE);
    }

    public List<Prescription> getExpiredActive() {
        return prescriptionRepository.findExpiredActive(LocalDate.now());
    }

    @Transactional
    public DispenseResult dispense(DispenseRequest request, String dispensedBy) {

        PrescriptionItem item = prescriptionItemRepository.findById(request.getItemId())
                .orElseThrow(() -> new BusinessException("Article introuvable: " + request.getItemId()));

        Prescription prescription = item.getPrescription();

        if (prescription.getStatus() == PrescriptionStatus.CANCELLED) {
            throw new BusinessException("Impossible de livrer: l'ordonnance est ANNULÉE.");
        }
        if (prescription.getStatus() == PrescriptionStatus.COMPLETED) {
            throw new BusinessException("Impossible de livrer: l'ordonnance est déjà COMPLÈTE.");
        }
        if (item.isFullyDispensed()) {
            throw new BusinessException(
                    "Ce médicament a déjà été entièrement livré pour ce mois.");
        }
        if (request.getQuantity() == null || request.getQuantity() <= 0) {
            throw new BusinessException("La quantité à livrer doit être supérieure à 0.");
        }
        if (request.getQuantity() > item.getQuantityRemaining()) {
            throw new BusinessException(
                    "Quantité demandée (" + request.getQuantity() +
                            ") supérieure au reste à livrer (" + item.getQuantityRemaining() + ").");
        }

        boolean isEarly = item.getMonthNumber() > prescription.getCurrentMonth();
        if (isEarly && !request.isOverrideEarly()) {
            return DispenseResult.earlyDispenseWarning(item);
        }

        boolean isExpired = prescription.isExpired();
        if (isExpired && !request.isOverrideExpired()) {
            return DispenseResult.expiredWarning(prescription);
        }

        DispenseEvent event = DispenseEvent.builder()
                .prescriptionItem(item)
                .quantityDispensed(request.getQuantity())
                .dispensedBy(dispensedBy)
                .dispensedEarly(isEarly)
                .dispensedOnExpired(isExpired)
                .note(request.getNote())
                .build();
        dispenseEventRepository.save(event);

        item.setQuantityDispensed(item.getQuantityDispensed() + request.getQuantity());
        prescriptionItemRepository.save(item);

        boolean completed = false;
        if (prescriptionItemRepository.allDispensed(prescription.getId())) {
            prescription.setStatus(PrescriptionStatus.COMPLETED);
            prescriptionRepository.save(prescription);
            completed = true;
        }

        return DispenseResult.success(item, completed, isExpired, isEarly);
    }


    @Transactional
    public Prescription cancel(Long prescriptionId) {
        Prescription prescription = getById(prescriptionId);
        if (prescription.getStatus() == PrescriptionStatus.COMPLETED) {
            throw new BusinessException("Impossible d'annuler une ordonnance COMPLÈTE.");
        }
        if (prescription.getStatus() == PrescriptionStatus.CANCELLED) {
            throw new BusinessException("L'ordonnance est déjà ANNULÉE.");
        }
        prescription.setStatus(PrescriptionStatus.CANCELLED);
        return prescriptionRepository.save(prescription);
    }


    public DashboardStats getDashboardStats() {
        long active    = prescriptionRepository.countByStatus(PrescriptionStatus.ACTIVE);
        long completed = prescriptionRepository.countByStatus(PrescriptionStatus.COMPLETED);
        long cancelled = prescriptionRepository.countByStatus(PrescriptionStatus.CANCELLED);
        long expired   = prescriptionRepository.findExpiredActive(LocalDate.now()).size();
        return new DashboardStats(active, completed, cancelled, expired);
    }
}
