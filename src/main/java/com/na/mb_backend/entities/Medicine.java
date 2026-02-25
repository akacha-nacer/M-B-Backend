package com.na.mb_backend.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "medicines", indexes = {
        @Index(name = "idx_medicine_fullname", columnList = "fullName")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Medicine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String fullName;

    @Column
    private Integer boxSize;

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;
}
