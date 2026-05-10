package com.shop.saree_manager.model;

import jakarta.persistence.*;
import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "bills")
@Data
public class Bill {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String billNo;
    private String billDate;
    private String manufacturerName;
    private Double amount;
    private String transportName;
    private String lrNumber;
    private String deliveryDate;

    @Column(columnDefinition = "TEXT")
    private String notes;

    private String status = "PENDING";
    private String driveLink;

    @OneToMany(mappedBy = "bill", cascade = CascadeType.ALL)
    private List<Payment> payments = new ArrayList<>();

    private Double paidAmount = 0.0;
}