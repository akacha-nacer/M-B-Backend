package com.na.mb_backend.repositories;

import com.na.mb_backend.entities.PrescriptionItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PrescriptionItemRepository extends JpaRepository<PrescriptionItem,Long> {

    List<PrescriptionItem> findByPrescriptionIdOrderByMonthNumberAsc(Long prescriptionId);

    @Query("""
        SELECT i FROM PrescriptionItem i
        WHERE i.prescription.id = :prescriptionId
          AND i.quantityDispensed < i.quantityPrescribed
    """)
    List<PrescriptionItem> findPendingByPrescriptionId(@Param("prescriptionId") Long prescriptionId);

    List<PrescriptionItem> findByPrescriptionIdAndMonthNumber(Long prescriptionId, Integer monthNumber);

    @Query("""
        SELECT COUNT(i) = 0 FROM PrescriptionItem i
        WHERE i.prescription.id = :prescriptionId
          AND i.quantityDispensed < i.quantityPrescribed
    """)
    boolean allDispensed(@Param("prescriptionId") Long prescriptionId);
}
