package com.na.mb_backend.repositories;


import com.na.mb_backend.entities.Prescription;
import com.na.mb_backend.entities.PrescriptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PrescriptionRepository extends JpaRepository<Prescription, Long> {

    Optional<Prescription> findByPrescriptionNumber(String prescriptionNumber);

    boolean existsByPrescriptionNumber(String prescriptionNumber);

    List<Prescription> findByCustomerIdOrderByCreatedAtDesc(Long customerId);

    List<Prescription> findByCustomerIdAndStatus(Long customerId, PrescriptionStatus status);



    // ─── Dashboard / reporting queries ───────────────────────────────────────

    /** Count by status — used on the dashboard */
    long countByStatus(PrescriptionStatus status);

    /**
     * Prescriptions that are ACTIVE but have passed their endDate.
     * Used for the dashboard warning panel (expired but not yet completed/cancelled).
     */
    @Query("""
        SELECT p FROM Prescription p
        WHERE p.status = 'ACTIVE'
          AND p.endDate < :today
        ORDER BY p.endDate ASC
    """)
    List<Prescription> findExpiredActive(@Param("today") LocalDate today);

    /**
     * Prescriptions created within a date range — useful for monthly reports.
     */
    @Query("""
        SELECT p FROM Prescription p
        WHERE p.startDate >= :from
          AND p.startDate <= :to
        ORDER BY p.startDate DESC
    """)
    List<Prescription> findByDateRange(
            @Param("from") LocalDate from,
            @Param("to") LocalDate to
    );
}
