package com.phegondev.InventoryManagementSystem.stocktransfer;

import com.phegondev.InventoryManagementSystem.stocktransfer.StockTransfer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StockTransferRepository extends JpaRepository<StockTransfer, Long> {}
