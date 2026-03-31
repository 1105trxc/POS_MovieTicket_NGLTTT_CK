package com.cinema.management.service;

public interface IAuditLogService {
    void logAction(String actionType, String tableName, String fieldName, String oldValue, String newValue);
}
