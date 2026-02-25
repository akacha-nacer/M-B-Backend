package com.na.mb_backend.entities;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "dispense_events", indexes = {
        @Index(name = "idx_event_item", columnList = "prescription_item_id"),
        @Index(name = "idx_event_dispensed_by", columnList = "dispensedBy")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DispenseEvent  {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "prescription_item_id", nullable = false)
    private PrescriptionItem prescriptionItem;

    @Column(nullable = false)
    private Integer quantityDispensed;

    @Column(nullable = false)
    private LocalDateTime dispensedAt;

    @Column(nullable = false)
    private String dispensedBy;

    @Column
    private String note;

    @Column(nullable = false)
    @Builder.Default
    private boolean dispensedEarly = false;

    @Column(nullable = false)
    @Builder.Default
    private boolean dispensedOnExpired = false;

    @PrePersist
    protected void onCreate() {
        if (this.dispensedAt == null) {
            this.dispensedAt = LocalDateTime.now();
        }
    }
}
