package com.na.mb_backend.repositories;

import com.na.mb_backend.entities.PrescriptionItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PrescriptionItemRepository extends JpaRepository<PrescriptionItem,Long> {

    /** All items for a prescription, ordered by month then medicine name */
    List<PrescriptionItem> findByPrescriptionIdOrderByMonthNumberAscMedicineNameAsc(Long prescriptionId);

    /** Only pending (not yet dispensed) items for a prescription */
    List<PrescriptionItem> findByPrescriptionIdAndDispensedFalse(Long prescriptionId);

    /** Items for a specific month of a prescription */
    List<PrescriptionItem> findByPrescriptionIdAndMonthNumber(Long prescriptionId, Integer monthNumber);

    /**
     * Returns true if ALL items in a prescription are dispensed.
     * Called after every dispense action to auto-complete the prescription.
     */
    @Query("""
        SELECT COUNT(i) = 0 FROM PrescriptionItem i
        WHERE i.prescription.id = :prescriptionId
          AND i.dispensed = false
    """)
    boolean allDispensed(@Param("prescriptionId") Long prescriptionId);


    /** All items dispensed by a specific pharmacist — for audit trail */
    List<PrescriptionItem> findByDispensedByOrderByDispensedAtDesc(String dispensedBy);
}
