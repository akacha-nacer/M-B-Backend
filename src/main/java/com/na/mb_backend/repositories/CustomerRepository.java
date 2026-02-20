package com.na.mb_backend.repositories;

import com.na.mb_backend.entities.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long > {

    Optional<Customer> findByNationalId(String nationalId);

    Optional<Customer> findByCnssNumber(String cnssNumber);

    boolean existsByNationalId(String nationalId);

    /**
     * Case-insensitive search across firstName, lastName, nationalId, and phone.
     * Covers all likely search inputs a pharmacist would type.
     */
    @Query("""
        SELECT c FROM Customer c
        WHERE LOWER(c.firstName)  LIKE LOWER(CONCAT('%', :query, '%'))
           OR LOWER(c.lastName)   LIKE LOWER(CONCAT('%', :query, '%'))
           OR LOWER(c.nationalId) LIKE LOWER(CONCAT('%', :query, '%'))
           OR LOWER(c.phone)      LIKE LOWER(CONCAT('%', :query, '%'))
        ORDER BY c.lastName, c.firstName
    """)
    List<Customer> search(@Param("query") String query);

    /**
     * Check if a customer has any prescriptions before allowing deletion.
     * Used in CustomerService to guard against orphan-safe deletion.
     */
    @Query("""
        SELECT COUNT(p) > 0 FROM Prescription p
        WHERE p.customer.id = :customerId
    """)
    boolean hasPrescriptions(@Param("customerId") Long customerId);
}
