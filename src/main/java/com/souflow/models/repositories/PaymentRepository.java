package com.souflow.models.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.souflow.models.entities.Payment;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    
}
