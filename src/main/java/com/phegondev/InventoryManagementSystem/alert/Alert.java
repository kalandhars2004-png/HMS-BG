package com.phegondev.InventoryManagementSystem.alert;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "alerts")
public class Alert {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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