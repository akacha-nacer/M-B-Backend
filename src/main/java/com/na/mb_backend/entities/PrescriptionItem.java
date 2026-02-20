package com.na.mb_backend.entities;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "prescription_items", indexes = {
        @Index(name = "idx_item_prescription", columnList = "prescription_id"),
        @Index(name = "idx_item_dispensed", columnList = "dispensed")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PrescriptionItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "prescription_id", nullable = false)
    private Prescription prescription;

    @Column(nullable = false)
    private String medicineName;

    @Column
    private String dosage;

    @Column(columnDefinition = "TEXT")
    private String instructions;

    @Column(nullable = false)
    private Integer monthNumber;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false)
    @Builder.Default
    private Boolean dispensed = false;

    @Column
    private LocalDateTime dispensedAt;

    @Column
    private String dispensedBy;


    public boolean isDispensable() {
        if (Boolean.TRUE.equals(dispensed)) return false;
        if (prescription.getStatus() == PrescriptionStatus.CANCELLED) return false;
        if (prescription.getStatus() == PrescriptionStatus.COMPLETED) return false;
        return monthNumber <= prescription.getCurrentMonth();
    }


    public void markDispensed(String dispensedByUsername) {
        this.dispensed = true;
        this.dispensedAt = LocalDateTime.now();
        this.dispensedBy = dispensedByUsername;
    }
}
