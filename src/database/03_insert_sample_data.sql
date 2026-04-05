-- ==========================================
-- DỮ LIỆU MẪU CHO CinemaDB (FULL CỘT THEO SCHEMA MỚI)
-- Khớp với: create_database_with_constraints.sql
-- ==========================================
USE `CinemaDB`;

-- ==========================================
-- I. NHÓM HỆ THỐNG, NGƯỜI DÙNG & AUDIT
-- ==========================================

INSERT INTO `Role` (`RoleID`, `RoleName`) VALUES
('ROLE_ADMIN',    'Quản trị viên'),
('ROLE_STAFF',    'Nhân viên bán vé');


INSERT INTO `User` (
    `UserID`, `RoleID`, `Username`, `Password`, `FullName`,
    `Phone`, `Email`, `BirthDate`, `CCCD`, `Gender`, `IsActive`
) VALUES
('U001', 'ROLE_ADMIN',   'admin',     '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92',   'Nguyễn Văn An',   '0901000001', 'admin@cinema.local',   '1990-01-15', '079090000001', 'Nam', TRUE),
('U002', 'ROLE_ADMIN',   'manager01', '$2b$12$hashed_pw_mgr01',   'Trần Thị Bình',   '0901000002', 'manager@cinema.local', '1992-03-20', '079090000002', 'Nữ',  TRUE),
('U003', 'ROLE_STAFF',   'staff01',   '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92',   'Lê Minh Châu',    '0901000003', 'staff01@cinema.local', '1998-07-11', '079090000003', 'Nam', TRUE),
('U004', 'ROLE_STAFF',   'cashier01', '$2b$12$hashed_pw_csh01',   'Phạm Thị Dung',   '0901000004', 'cashier@cinema.local', '1996-09-05', '079090000004', 'Nữ',  TRUE),
('U005', 'ROLE_STAFF',   'staff02',   '$2b$12$hashed_pw_stf02',   'Hoàng Văn Em',    '0901000005', 'staff02@cinema.local', '2000-12-01', '079090000005', 'Nam', FALSE);

-- ==========================================
-- II. NHÓM NGHIỆP VỤ RẠP PHIM
-- ==========================================

INSERT INTO `Movie` (`MovieID`, `Title`, `Duration`, `ReleaseDate`, `Description`, `AgeRestriction`) VALUES
('MOV001', 'Avengers: Doomsday',    150, '2026-03-01', 'Các siêu anh hùng Marvel hợp sức đối mặt với mối đe dọa toàn cầu mới.', 'T13'),
('MOV002', 'Lật Mặt 8',             120, '2026-02-15', 'Phim hành động Việt Nam với nhiều pha hành động mãn nhãn.',            'T16'),
('MOV003', 'Ne Zha 2',              110, '2026-01-20', 'Tiếp nối hành trình của Na Tra trong thế giới thần thoại.',             'P'),
('MOV004', 'Mission: Impossible 9', 140, '2026-03-10', 'Ethan Hunt trở lại với nhiệm vụ bất khả thi mới đầy kịch tính.',        'T16'),
('MOV005', 'Kẻ Trộm Mặt Trăng',      95, '2026-02-01', 'Bộ phim hoạt hình gia đình vui nhộn dành cho mọi lứa tuổi.',            'P');

INSERT INTO `Genre` (`GenreID`, `GenreName`) VALUES
('GEN001', 'Hành Động'),
('GEN002', 'Khoa Học Viễn Tưởng'),
('GEN003', 'Hoạt Hình'),
('GEN004', 'Hài Hước'),
('GEN005', 'Kinh Dị');

INSERT INTO `MovieGenre` (`MovieID`, `GenreID`) VALUES
('MOV001', 'GEN001'),
('MOV001', 'GEN002'),
('MOV002', 'GEN001'),
('MOV002', 'GEN004'),
('MOV003', 'GEN003'),
('MOV004', 'GEN001'),
('MOV005', 'GEN003'),
('MOV005', 'GEN004');

INSERT INTO `Room` (`RoomID`, `RoomName`, `Capacity`) VALUES
('ROOM01', 'Phòng 1 - Standard', 120),
('ROOM02', 'Phòng 2 - Premium',   80),
('ROOM03', 'Phòng 3 - IMAX',      60),
('ROOM04', 'Phòng 4 - 4DX',       50);

INSERT INTO `SeatType` (`SeatTypeID`, `TypeName`, `BasePrice`) VALUES
('ST001', 'Standard', 100000.00),
('ST002', 'VIP',      150000.00),
('ST003', 'Couple',   200000.00),
('ST004', 'IMAX',     180000.00),
('ST005', '4DX',      220000.00);

INSERT INTO `Seat` (`SeatID`, `RoomID`, `SeatTypeID`, `RowChar`, `SeatNumber`) VALUES
('SEAT_R1_A1', 'ROOM01', 'ST001', 'A', 1),
('SEAT_R1_A2', 'ROOM01', 'ST001', 'A', 2),
('SEAT_R1_B1', 'ROOM01', 'ST002', 'B', 1),
('SEAT_R1_C1', 'ROOM01', 'ST003', 'C', 1),
('SEAT_R3_A1', 'ROOM03', 'ST004', 'A', 1),
('SEAT_R3_A2', 'ROOM03', 'ST004', 'A', 2),
('SEAT_R4_A1', 'ROOM04', 'ST005', 'A', 1),
('SEAT_R4_A2', 'ROOM04', 'ST005', 'A', 2);

INSERT INTO `ShowTime` (`ShowTimeID`, `MovieID`, `RoomID`, `StartTime`, `EndTime`) VALUES
('ST_001', 'MOV001', 'ROOM01', '2026-04-02 09:00:00', '2026-04-02 11:30:00'),
('ST_002', 'MOV001', 'ROOM03', '2026-04-02 14:00:00', '2026-04-02 16:30:00'),
('ST_003', 'MOV002', 'ROOM01', '2026-04-02 19:00:00', '2026-04-02 21:00:00'),
('ST_004', 'MOV003', 'ROOM04', '2026-04-03 10:00:00', '2026-04-03 11:50:00'),
('ST_005', 'MOV004', 'ROOM02', '2026-04-03 15:30:00', '2026-04-03 17:50:00');

INSERT INTO `Product` (`ProductID`, `ProductName`, `CurrentPrice`) VALUES
('PRD001', 'Bắp Rang Lớn',     55000.00),
('PRD002', 'Bắp Rang Nhỏ',     40000.00),
('PRD003', 'Nước Ngọt Lớn',    35000.00),
('PRD004', 'Nước Ngọt Nhỏ',    25000.00),
('PRD005', 'Combo Bắp + Nước', 80000.00);

-- ==========================================
-- III. NHÓM KHÁCH HÀNG & KHUYẾN MÃI
-- ==========================================

INSERT INTO `Customer` (
    `CustomerID`, `FullName`, `Phone`, `Email`, `RewardPoints`, `TotalSpent`, `MemberTier`
) VALUES
('CUS001', 'Nguyễn Thị Hoa', '0901234567', 'hoa.nguyen@email.com', 1500, 3500000.00, 'Gold'),
('CUS002', 'Trần Văn Khoa',  '0912345678', 'khoa.tran@email.com',   300,  800000.00, 'Member'),
('CUS003', 'Lê Thị Lan',     '0923456789', 'lan.le@email.com',      800, 2000000.00, 'Silver'),
('CUS004', 'Phạm Đình Mạnh', '0934567890', 'manh.pham@email.com',  2500, 6000000.00, 'Platinum'),
('CUS005', 'Hoàng Thị Ngọc', '0945678901', 'ngoc.hoang@email.com',  100,  250000.00, 'Member');

INSERT INTO `Promotion` (
    `PromotionID`, `Code`, `DiscountPercent`, `MaxDiscountAmount`, `ExpiryDate`,
    `Description`, `StartDate`, `ApplyToMovie`, `ValidDays`, `IsExclusive`
) VALUES
('PROMO001', 'SUMMER25',   25.00, 100000.00, '2026-08-31', 'Khuyến mãi mùa hè cho toàn hệ thống.',             '2026-04-01', NULL,     NULL,                                   0),
('PROMO002', 'AVENGERS15', 15.00,  75000.00, '2026-06-30', 'Ưu đãi riêng cho suất chiếu Avengers.',            '2026-04-01', 'MOV001', NULL,                                   1),
('PROMO003', 'WEEKDAY10',  10.00,  50000.00, '2026-12-31', 'Giảm giá ngày thường từ thứ 2 đến thứ 5.',         '2026-01-01', NULL,     'Monday,Tuesday,Wednesday,Thursday',      0),
('PROMO004', 'NEWMEMBER20',20.00,  80000.00, '2026-07-31', 'Ưu đãi cho khách mới đăng ký thành viên.',         '2026-03-01', NULL,     NULL,                                   0),
('PROMO005', 'FLASHSALE30',30.00, 120000.00, '2026-06-15', 'Flash sale thứ 6 hàng tuần số lượng có hạn.',      '2026-04-05', NULL,     'Friday',                               1);

-- ==========================================
-- IV. NHÓM GIAO DỊCH, GHẾ, THANH TOÁN, ĐIỂM
-- ==========================================

INSERT INTO `Invoice` (
    `InvoiceID`, `UserID`, `CustomerID`, `PromotionID`, `TotalAmount`,
    `UsedPoints`, `DiscountFromPoints`, `EarnedPoints`, `CreatedAt`,
    `DiscountFromTier`, `DiscountFromPromotion`, `FinalAmount`
) VALUES
('INV001', 'U003', 'CUS001', 'PROMO001', 450000.00,   0,     0.00, 45, '2026-04-02 10:30:00', 25000.00, 50000.00, 375000.00),
('INV002', 'U003', 'CUS002', NULL,       300000.00,   0,     0.00, 30, '2026-04-02 14:15:00',     0.00,     0.00, 300000.00),
('INV003', 'U004', 'CUS003', 'PROMO003', 320000.00, 100, 20000.00, 32, '2026-04-02 16:00:00', 10000.00, 25000.00, 265000.00),
('INV004', 'U003', 'CUS004', 'PROMO002', 540000.00, 500, 50000.00, 54, '2026-04-03 09:20:00', 20000.00, 70000.00, 400000.00),
('INV005', 'U004', 'CUS005', NULL,       180000.00,   0,     0.00, 18, '2026-04-03 15:45:00',     0.00,     0.00, 180000.00);

INSERT INTO `BookingSeat` (`ShowTimeID`, `SeatID`, `InvoiceID`, `Price`) VALUES
('ST_001', 'SEAT_R1_A1', 'INV001', 100000.00),
('ST_001', 'SEAT_R1_B1', 'INV001', 150000.00),
('ST_001', 'SEAT_R1_A2', 'INV002', 100000.00),
('ST_002', 'SEAT_R3_A1', 'INV003', 180000.00),
('ST_002', 'SEAT_R3_A2', 'INV004', 180000.00);

INSERT INTO `SeatLock` (`ShowTimeID`, `SeatID`, `LockedBy`, `LockedAt`, `ExpiresAt`) VALUES
('ST_003', 'SEAT_R1_C1', 'U003', '2026-04-02 18:45:00', '2026-04-02 19:00:00'),
('ST_003', 'SEAT_R1_A1', 'U003', '2026-04-02 18:46:00', '2026-04-02 19:01:00'),
('ST_004', 'SEAT_R4_A1', 'U004', '2026-04-03 09:47:00', '2026-04-03 10:02:00'),
('ST_005', 'SEAT_R3_A1', 'U003', '2026-04-03 15:15:00', '2026-04-03 15:30:00');

INSERT INTO `OrderDetail` (`InvoiceID`, `ProductID`, `Quantity`, `Price`) VALUES
('INV001', 'PRD005', 2, 80000.00),
('INV001', 'PRD004', 2, 25000.00),
('INV002', 'PRD001', 1, 55000.00),
('INV003', 'PRD003', 2, 35000.00),
('INV004', 'PRD005', 3, 80000.00);

INSERT INTO `Payment` (
    `PaymentID`, `InvoiceID`, `Amount`, `PaymentMethod`, `TransactionCode`, `Status`, `CreatedAt`
) VALUES
('PAY001', 'INV001', 375000.00, 'VNPay',         'VNP20260402103045',  'SUCCESS', '2026-04-02 10:30:45'),
('PAY002', 'INV002', 300000.00, 'Tiền Mặt',      NULL,                  'SUCCESS', '2026-04-02 14:16:00'),
('PAY003', 'INV003', 265000.00, 'MoMo',          'MM20260402160120',    'SUCCESS', '2026-04-02 16:01:20'),
('PAY004', 'INV004', 400000.00, 'Thẻ Ngân Hàng', 'CARD20260403092045',  'SUCCESS', '2026-04-03 09:20:45'),
('PAY005', 'INV005', 180000.00, 'ZaloPay',       'ZLP20260403154512',   'SUCCESS', '2026-04-03 15:45:12');

INSERT INTO `PointHistory` (
    `HistoryID`, `CustomerID`, `InvoiceID`, `PointAmount`, `TransactionType`, `Description`, `CreatedAt`
) VALUES
('PH001', 'CUS001', 'INV001',   45, 'EARN',   'Tích điểm từ hóa đơn INV001',           '2026-04-02 10:31:00'),
('PH002', 'CUS002', 'INV002',   30, 'EARN',   'Tích điểm từ hóa đơn INV002',           '2026-04-02 14:16:30'),
('PH003', 'CUS003', 'INV003', -100, 'REDEEM', 'Dùng điểm giảm giá hóa đơn INV003',     '2026-04-02 16:00:30'),
('PH004', 'CUS003', 'INV003',   32, 'EARN',   'Tích điểm từ hóa đơn INV003',           '2026-04-02 16:01:30'),
('PH005', 'CUS004', 'INV004', -500, 'REDEEM', 'Dùng điểm giảm giá hóa đơn INV004',     '2026-04-03 09:20:00');

-- ==========================================
-- V. NHÓM BÁO CÁO CA & NHẬT KÝ
-- ==========================================

INSERT INTO `ShiftReport` (
    `ShiftReportID`, `UserID`, `ShiftStart`, `ShiftEnd`,
    `OpeningCash`, `CashRevenue`, `TransferRevenue`, `CardRevenue`, `TotalRevenue`,
    `ExpectedCash`, `ActualCash`, `Discrepancy`, `RemittedCash`, `CarryOverCash`,
    `Status`, `ApprovedBy`, `ApprovedAt`, `Notes`, `CreatedAt`
) VALUES
('SR001', 'U003', '2026-04-02 08:00:00', '2026-04-02 16:00:00',
  2000000.00, 300000.00, 100000.00, 200000.00, 600000.00,
  2300000.00, 2300000.00, 0.00, 2000000.00, 300000.00,
  'APPROVED', 'U002', '2026-04-02 16:30:00', 'Ca sáng ổn định, không chênh lệch.', '2026-04-02 16:05:00'),
('SR002', 'U004', '2026-04-03 08:00:00', '2026-04-03 16:00:00',
  1500000.00, 180000.00, 120000.00, 100000.00, 400000.00,
  1680000.00, 1675000.00, -5000.00, 1500000.00, 175000.00,
  'PENDING', NULL, NULL, 'Thiếu 5.000 VND đang chờ đối soát cuối ngày.', '2026-04-03 16:10:00');

INSERT INTO `AuditLog` (
    `LogID`, `ChangedBy`, `TableName`, `FieldName`, `OldValue`, `NewValue`, `ChangedAt`
) VALUES
(1, 'U001', 'SeatType', 'BasePrice',    '80000',  '100000', '2026-04-01 09:15:00'),
(2, 'U001', 'SeatType', 'BasePrice',    '120000', '150000', '2026-04-01 09:16:00'),
(3, 'U002', 'Product',  'CurrentPrice', '45000',  '55000',  '2026-04-01 10:00:00'),
(4, 'U001', 'Promotion','DiscountPercent', '20',  '25',     '2026-04-01 11:30:00'),
(5, 'U002', 'ShiftReport', 'Status',    'PENDING','APPROVED','2026-04-02 16:30:00');
