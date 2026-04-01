package com.cinema.management.controller;

import com.cinema.management.model.dto.PaymentDashboardDto;
import com.cinema.management.model.dto.PaymentManagementRowDto;
import com.cinema.management.model.entity.ShiftReport;
import com.cinema.management.model.entity.User;
import com.cinema.management.service.IPaymentService;
import com.cinema.management.service.impl.PaymentServiceImpl;

import java.time.LocalDate;
import java.util.List;

public class PaymentController {

    private final IPaymentService paymentService;

    public PaymentController() {
        this.paymentService = new PaymentServiceImpl();
    }

    public List<PaymentManagementRowDto> searchPayments(LocalDate fromDate,
                                                        LocalDate toDate,
                                                        String shiftReportId,
                                                        String staffUserId,
                                                        String paymentMethod) {
        return paymentService.searchPayments(fromDate, toDate, shiftReportId, staffUserId, paymentMethod);
    }

    public PaymentDashboardDto summarize(List<PaymentManagementRowDto> rows) {
        return paymentService.summarize(rows);
    }

    public List<ShiftReport> getAllShifts() {
        return paymentService.getAllShifts();
    }

    public List<User> getAllCashiers() {
        return paymentService.getAllCashiers();
    }
}
