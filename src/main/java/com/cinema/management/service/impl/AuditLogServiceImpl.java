package com.cinema.management.service.impl;

import java.time.LocalDateTime;

import com.cinema.management.model.entity.AuditLog;
import com.cinema.management.model.entity.User;
import com.cinema.management.repository.AuditLogRepository;
import com.cinema.management.service.IAuditLogService;
import com.cinema.management.util.UserSessionContext;

// TODO: implement service

public class AuditLogServiceImpl implements IAuditLogService {
    private final AuditLogRepository auditLogRepo;

    public AuditLogServiceImpl(AuditLogRepository auditLogRepo) {
        this.auditLogRepo = auditLogRepo;
    }

    @Override
    public void logAction(String actionType, String tableName, String fieldName, String oldValue, String newValue) {
        // Gọi User đang đăng nhập bằng Session tiện ích
        User currentUser = UserSessionContext.getCurrentUser();

        if (currentUser == null) {
            System.out.println("Lỗi: User chưa đăng nhập để ghi log");
            return;
        }
        AuditLog log = AuditLog.builder()
                .changedBy(currentUser)
                .tableName(tableName)
                .fieldName(fieldName)
                .oldValue(oldValue)
                .newValue(newValue)
                .changedAt(LocalDateTime.now())
                .build();

        auditLogRepo.save(log);
    }
}
