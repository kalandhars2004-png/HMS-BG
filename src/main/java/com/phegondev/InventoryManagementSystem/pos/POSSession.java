package com.phegondev.InventoryManagementSystem.pos;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "pos_sessions")
public class POSSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "branch_id")
    private Long branchId;

    @Column(name = "organization_id")
    private Long organizationId;

    @Version
    private Long version;

    private String sessionNumber;

    private Long openedBy;

    private Long closedBy;

    private String status;

    private BigDecimal openingBalance;

    private BigDecimal closingBalance;

    private BigDecimal totalSales;

    private BigDecimal totalRefunds;

    private BigDecimal netAmount;

    private LocalDateTime openedAt;

    private LocalDateTime closedAt;

    private String notes;
}
