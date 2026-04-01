package com.cinema.management.service;

import com.cinema.management.model.dto.PaymentDashboardDto;
import com.cinema.management.model.dto.PaymentManagementRowDto;
import com.cinema.management.model.entity.ShiftReport;
import com.cinema.management.model.entity.User;

import java.time.LocalDate;
import java.util.List;

public interface IPaymentService {
    List<PaymentManagementRowDto> searchPayments(LocalDate fromDate,
                                                 LocalDate toDate,
                                                 String shiftReportId,
                                                 String staffUserId,
                                                 String paymentMethod);

    PaymentDashboardDto summarize(List<PaymentManagementRowDto> rows);

    List<ShiftReport> getAllShifts();

    List<User> getAllCashiers();
}
