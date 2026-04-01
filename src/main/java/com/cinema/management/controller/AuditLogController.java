package com.cinema.management.controller;

import com.cinema.management.model.entity.AuditLog;
import com.cinema.management.repository.AuditLogRepository;
import com.cinema.management.service.IAuditLogService;
import com.cinema.management.service.impl.AuditLogServiceImpl;

import java.util.List;

public class AuditLogController {
    private final IAuditLogService auditLogService;

    public AuditLogController() {
        AuditLogRepository repo = new AuditLogRepository();
        this.auditLogService = new AuditLogServiceImpl(repo);
    }

    public List<AuditLog> getAllLogs() {
        return auditLogService.getAllLogs();
    }

    public List<AuditLog> searchLogs(String keyword) {
        return auditLogService.searchLogs(keyword);
    }

    public List<AuditLog> getLogsByDate(java.time.LocalDate date) {
        return auditLogService.getLogsByDate(date);
    }
}
