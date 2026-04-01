package com.cinema.management.service;

import com.cinema.management.model.entity.AuditLog;

public interface IAuditLogService {
    void logAction(String actionType, String tableName, String fieldName, String oldValue, String newValue);

    java.util.List<AuditLog> getAllLogs();

    java.util.List<AuditLog> searchLogs(String keyword);

    java.util.List<AuditLog> getLogsByDate(java.time.LocalDate date);
}
