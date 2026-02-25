package com.na.mb_backend.repositories;

import com.na.mb_backend.entities.Medicine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MedicineRepository extends JpaRepository<Medicine, Long> {

    List<Medicine> findByActiveTrueOrderByFullNameAsc();

    @Query("""
        SELECT m FROM Medicine m
        WHERE m.active = true
          AND LOWER(m.fullName) LIKE LOWER(CONCAT('%', :query, '%'))
        ORDER BY m.fullName ASC
    """)
    List<Medicine> searchActive(@Param("query") String query);
}
