package com.shop.saree_manager.repository;

import com.shop.saree_manager.model.Bill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BillRepository extends JpaRepository<Bill, Long> {
    List<Bill> findByManufacturerNameContainingIgnoreCase(String name);
    List<Bill> findAllByOrderByIdDesc();
}