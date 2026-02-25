package com.na.mb_backend.repositories;

import com.na.mb_backend.entities.DispenseEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DispenseEventRepository extends JpaRepository<DispenseEvent, Long>{

    List<DispenseEvent> findByPrescriptionItemIdOrderByDispensedAtAsc(Long prescriptionItemId);


    List<DispenseEvent> findByDispensedByOrderByDispensedAtDesc(String dispensedBy);
}
