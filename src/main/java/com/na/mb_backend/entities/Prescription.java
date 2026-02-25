package com.na.mb_backend.entities;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "prescriptions", indexes = {
        @Index(name = "idx_prescription_number", columnList = "ordonnanceNumber"),
        @Index(name = "idx_prescription_customer", columnList = "customer_id"),
        @Index(name = "idx_prescription_status", columnList = "status"),
        @Index(name = "idx_prescription_type", columnList = "type")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Prescription {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Column(nullable = false)
    private LocalDate issueDate;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private Integer durationInMonths;

    @Column(nullable = false)
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private PrescriptionStatus status = PrescriptionStatus.ACTIVE;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(nullable = false)
    private String createdBy;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // ─── Type + CNAM-specific fields ──────────────────────────────────────────

    /**
     * CNAM = tiers-payant, reimbursed through CNAM.
     * PRIVATE = à payer, patient pays out of pocket.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    @Builder.Default
    private PrescriptionType type = PrescriptionType.PRIVATE;

    @Column
    private String ordonnanceNumber;

    @Column
    private String codeIndividuel;

    @Column
    private String matriculeAdherent;

    @Column
    private String regime;

    @Column
    private String codeApci;

    @OneToMany(mappedBy = "prescription", cascade = CascadeType.ALL,
            orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<PrescriptionItem> items = new ArrayList<>();

    // ─── Lifecycle ────────────────────────────────────────────────────────────

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.endDate == null && this.startDate != null && this.durationInMonths != null) {
            this.endDate = this.startDate.plusMonths(this.durationInMonths);
        }
    }

    // ─── Computed helpers ─────────────────────────────────────────────────────

    public boolean isExpired() {
        return status == PrescriptionStatus.ACTIVE
                && LocalDate.now().isAfter(endDate);
    }

    public int getCurrentMonth() {
        LocalDate today = LocalDate.now();
        if (today.isBefore(startDate)) return 1;
        long months = ChronoUnit.MONTHS.between(startDate, today);
        return (int) Math.min(months + 1, durationInMonths);
    }

    public void addItem(PrescriptionItem item) {
        item.setPrescription(this);
        items.add(item);
    }
}
