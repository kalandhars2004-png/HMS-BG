package com.phegondev.InventoryManagementSystem.customer;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
    Optional<Customer> findByPhone(String phone);
    Optional<Customer> findByName(String name);
    List<Customer> findByNameContainingIgnoreCaseOrPhoneContaining(String name, String phone);
}