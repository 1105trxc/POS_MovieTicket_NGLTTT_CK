package com.cinema.management.controller;

import com.cinema.management.model.entity.Seat;
import com.cinema.management.model.entity.SeatType;
import com.cinema.management.service.ISeatService;
import com.cinema.management.service.ISeatTypeService;
import com.cinema.management.service.impl.SeatServiceImpl;
import com.cinema.management.service.impl.SeatTypeServiceImpl;

import java.math.BigDecimal;
import java.util.List;

/**
 * Controller cho Quản lý Ghế và Loại Ghế.
 * Gom chung vì hai domain này liên kết chặt với nhau trên cùng 1 Panel.
 */
public class SeatController {

    private final ISeatService seatService;
    private final ISeatTypeService seatTypeService;

    public SeatController() {
        this.seatService = new SeatServiceImpl();
        this.seatTypeService = new SeatTypeServiceImpl();
    }

    public SeatController(ISeatService seatService, ISeatTypeService seatTypeService) {
        this.seatService = seatService;
        this.seatTypeService = seatTypeService;
    }

    // ── Seat operations ──────────────────────────────────────────────────────

    public List<Seat> getSeatsByRoom(String roomId) {
        return seatService.getSeatsByRoom(roomId);
    }

    public Seat addSeat(String roomId, String seatTypeId, String rowChar, int seatNumber) {
        return seatService.addSeat(roomId, seatTypeId, rowChar, seatNumber);
    }

    public Seat updateSeatType(String seatId, String seatTypeId) {
        return seatService.updateSeatType(seatId, seatTypeId);
    }

    public void deleteSeat(String seatId) {
        seatService.deleteSeat(seatId);
    }

    // ── SeatType operations ──────────────────────────────────────────────────

    public List<SeatType> getAllSeatTypes() {
        return seatTypeService.getAllSeatTypes();
    }

    public SeatType addSeatType(String typeName, BigDecimal basePrice) {
        return seatTypeService.addSeatType(typeName, basePrice);
    }

    public SeatType updateSeatType(String seatTypeId, String typeName,
                                    BigDecimal basePrice, String changedByUserId) {
        return seatTypeService.updateSeatType(seatTypeId, typeName, basePrice, changedByUserId);
    }

    public void deleteSeatType(String seatTypeId) {
        seatTypeService.deleteSeatType(seatTypeId);
    }
}
