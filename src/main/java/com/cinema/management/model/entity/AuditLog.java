package com.cinema.management.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "AuditLog")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "LogID")
    private Integer logId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ChangedBy", nullable = false)
    @ToString.Exclude
    private User changedBy;

    @Column(name = "TableName", nullable = false, length = 50)
    private String tableName;

    @Column(name = "FieldName", nullable = false, length = 50)
    private String fieldName;

    @Column(name = "OldValue", columnDefinition = "TEXT")
    private String oldValue;

    @Column(name = "NewValue", columnDefinition = "TEXT")
    private String newValue;

    @Column(name = "ChangedAt")
    @Builder.Default
    private LocalDateTime changedAt = LocalDateTime.now();
}
