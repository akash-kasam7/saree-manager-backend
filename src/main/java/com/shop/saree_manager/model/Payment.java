package com.shop.saree_manager.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Double amountPaid;
    private String transactionId;
    private String paymentDate;
    private String paymentMode; // Cash, UPI, NEFT
    private String type; // FULL, PARTIAL, RETURN

    @ManyToOne @JoinColumn(name = "bill_id") @JsonIgnore
    private Bill bill;
}