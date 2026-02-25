package com.na.mb_backend.service;
import com.na.mb_backend.entities.Customer;
import com.na.mb_backend.exceptions.BusinessException;
import com.na.mb_backend.repositories.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CustomerService {
    private final CustomerRepository customerRepository;

    // ─── Create ───────────────────────────────────────────────────────────────

    @Transactional
    public Customer create(Customer customer) {
        if (customerRepository.existsByNationalId(customer.getNationalId())) {
            throw new BusinessException("A customer with this CIN already exists: " + customer.getNationalId());
        }
        return customerRepository.save(customer);
    }

    // ─── Read ─────────────────────────────────────────────────────────────────

    public Customer getById(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Customer not found: " + id));
    }

    public Customer getByNationalId(String nationalId) {
        return customerRepository.findByNationalId(nationalId)
                .orElseThrow(() -> new BusinessException("Customer not found with CIN: " + nationalId));
    }

    public List<Customer> search(String query) {
        if (query == null || query.isBlank()) {
            return customerRepository.findAll();
        }
        return customerRepository.search(query.trim());
    }

    // ─── Update ───────────────────────────────────────────────────────────────

    @Transactional
    public Customer update(Long id, Customer updated) {
        Customer existing = getById(id);

        if (!existing.getNationalId().equals(updated.getNationalId())) {
            if (customerRepository.existsByNationalId(updated.getNationalId())) {
                throw new BusinessException("CIN already in use: " + updated.getNationalId());
            }
            existing.setNationalId(updated.getNationalId());
        }

        existing.setFirstName(updated.getFirstName());
        existing.setLastName(updated.getLastName());
        existing.setCnssNumber(updated.getCnssNumber());
        existing.setPhone(updated.getPhone());
        existing.setAddress(updated.getAddress());

        return customerRepository.save(existing);
    }

    // ─── Delete ───────────────────────────────────────────────────────────────

    @Transactional
    public void delete(Long id) {
        if (!customerRepository.existsById(id)) {
            throw new BusinessException("Customer not found: " + id);
        }
        if (customerRepository.hasPrescriptions(id)) {
            throw new BusinessException(
                    "Cannot delete customer: they have existing prescriptions. " +
                            "Cancel or archive prescriptions first."
            );
        }
        customerRepository.deleteById(id);
    }
}
