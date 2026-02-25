package com.na.mb_backend.entities;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "prescription_items", indexes = {
        @Index(name = "idx_item_prescription", columnList = "prescription_id"),
        @Index(name = "idx_item_medicine", columnList = "medicine_id")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PrescriptionItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "prescription_id", nullable = false)
    private Prescription prescription;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medicine_id")
    private Medicine medicine;

    @Column
    private String customMedicineName;

    @Column(columnDefinition = "TEXT")
    private String instructions;

    @Column(nullable = false)
    private Integer monthNumber;

    @Column(nullable = false)
    private Integer quantityPrescribed;

    @Column(nullable = false)
    @Builder.Default
    private Integer quantityDispensed = 0;

    @OneToMany(mappedBy = "prescriptionItem", cascade = CascadeType.ALL,
            orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<DispenseEvent> dispenseEvents = new ArrayList<>();



    public boolean isFullyDispensed() {
        return quantityDispensed >= quantityPrescribed;
    }


    public int getQuantityRemaining() {
        return Math.max(0, quantityPrescribed - quantityDispensed);
    }

    public String getDisplayName() {
        return medicine != null ? medicine.getFullName() : customMedicineName;
    }

    public boolean isDispensable() {
        if (isFullyDispensed()) return false;
        if (prescription.getStatus() == PrescriptionStatus.CANCELLED) return false;
        if (prescription.getStatus() == PrescriptionStatus.COMPLETED) return false;
        return monthNumber <= prescription.getCurrentMonth();
    }
}
