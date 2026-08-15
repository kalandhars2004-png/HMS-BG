package com.phegondev.InventoryManagementSystem.category;

import lombok.Data;
import java.util.List;

@Data
public class BulkStatusRequest {
    private List<Long> ids;
    private boolean active;
}
