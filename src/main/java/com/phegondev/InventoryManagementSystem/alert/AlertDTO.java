package com.phegondev.InventoryManagementSystem.alert;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AlertDTO {
    private Long id;
    private String type;
    private String severity;
    private String title;
    private String message;
    private Long relatedEntityId;
    private String relatedEntityType;
    private Boolean read;
    private LocalDateTime createdAt;
}