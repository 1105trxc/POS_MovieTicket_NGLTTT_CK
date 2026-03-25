-- ==========================================
-- DỮ LIỆU MẪU CHO CinemaDB
-- ==========================================
USE `CinemaDB`;

-- ==========================================
-- I. NHÓM HỆ THỐNG, NGƯỜI DÙNG & AUDIT
-- ==========================================

INSERT INTO `Role` (`RoleID`, `RoleName`) VALUES
('ROLE_ADMIN',    'Quản trị viên'),
('ROLE_STAFF',    'Nhân viên bán vé'),
('ROLE_MANAGER',  'Quản lý rạp'),
('ROLE_CASHIER',  'Thu ngân'),
('ROLE_SECURITY', 'Bảo vệ / Soát vé');

INSERT INTO `User` (`UserID`, `RoleID`, `Username`, `Password`, `FullName`, `IsActive`) VALUES
('U001', 'ROLE_ADMIN',   'admin',        '$2b$12$hashed_pw_admin',   'Nguyễn Văn An',    TRUE),
('U002', 'ROLE_MANAGER', 'manager01',    '$2b$12$hashed_pw_mgr01',   'Trần Thị Bình',    TRUE),
('U003', 'ROLE_STAFF',   'staff01',      '$2b$12$hashed_pw_stf01',   'Lê Minh Châu',     TRUE),
('U004', 'ROLE_CASHIER', 'cashier01',    '$2b$12$hashed_pw_csh01',   'Phạm Thị Dung',    TRUE),
('U005', 'ROLE_STAFF',   'staff02',      '$2b$12$hashed_pw_stf02',   'Hoàng Văn Em',     FALSE);

INSERT INTO `AuditLog` (`LogID`, `ChangedBy`, `TableName`, `FieldName`, `OldValue`, `NewValue`, `ChangedAt`) VALUES
(1, 'U001', 'SeatType', 'BasePrice', '80000',  '90000',  '2025-01-10 09:15:00'),
(2, 'U001', 'SeatType', 'BasePrice', '120000', '140000', '2025-01-10 09:16:00'),
(3, 'U002', 'Product',  'CurrentPrice', '45000', '50000', '2025-02-14 10:00:00'),
(4, 'U001', 'SeatType', 'BasePrice', '90000',  '100000', '2025-03-01 08:30:00'),
(5, 'U002', 'Product',  'CurrentPrice', '30000', '35000', '2025-03-15 11:45:00');

-- ==========================================
-- II. NHÓM NGHIỆP VỤ RẠP PHIM
-- ==========================================

INSERT INTO `Movie` (`MovieID`, `Title`, `Duration`, `ReleaseDate`, `Description`) VALUES
('MOV001', 'Avengers: Doomsday',         150, '2025-05-01', 'Các siêu anh hùng Marvel hợp sức đối mặt với mối đe dọa toàn cầu mới.'),
('MOV002', 'Lật Mặt 8',                  120, '2025-04-20', 'Phim hành động Việt Nam với nhiều pha hành động mãn nhãn.'),
('MOV003', 'Ne Zha 2',                   110, '2025-02-05', 'Tiếp nối hành trình của Na Tra trong thế giới thần thoại Trung Hoa.'),
('MOV004', 'Mission: Impossible 9',      140, '2025-06-15', 'Ethan Hunt trở lại với nhiệm vụ bất khả thi mới đầy kịch tính.'),
('MOV005', 'Kẻ Trộm Mặt Trăng',         95,  '2025-03-10', 'Bộ phim hoạt hình gia đình vui nhộn dành cho mọi lứa tuổi.');

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
('MOV003', 'GEN001'),
('MOV004', 'GEN001'),
('MOV005', 'GEN003'),
('MOV005', 'GEN004');

INSERT INTO `Room` (`RoomID`, `RoomName`, `Capacity`) VALUES
('ROOM01', 'Phòng 1 - Standard',  120),
('ROOM02', 'Phòng 2 - Premium',    80),
('ROOM03', 'Phòng 3 - IMAX',       60),
('ROOM04', 'Phòng 4 - 4DX',        50),
('ROOM05', 'Phòng 5 - Standard',  100);

INSERT INTO `SeatType` (`SeatTypeID`, `TypeName`, `BasePrice`) VALUES
('ST001', 'Standard',  100000.00),
('ST002', 'VIP',       150000.00),
('ST003', 'Couple',    200000.00),
('ST004', 'IMAX',      180000.00),
('ST005', '4DX',       220000.00);

-- Ghế cho Phòng 1 (Standard)
INSERT INTO `Seat` (`SeatID`, `RoomID`, `SeatTypeID`, `RowChar`, `SeatNumber`) VALUES
('SEAT_R1_A1',  'ROOM01', 'ST001', 'A', 1),
('SEAT_R1_A2',  'ROOM01', 'ST001', 'A', 2),
('SEAT_R1_B1',  'ROOM01', 'ST002', 'B', 1),
('SEAT_R1_C1',  'ROOM01', 'ST003', 'C', 1),
('SEAT_R1_C2',  'ROOM01', 'ST003', 'C', 2),
-- Ghế cho Phòng 3 (IMAX)
('SEAT_R3_A1',  'ROOM03', 'ST004', 'A', 1),
('SEAT_R3_A2',  'ROOM03', 'ST004', 'A', 2),
('SEAT_R3_B1',  'ROOM03', 'ST004', 'B', 1),
('SEAT_R3_B2',  'ROOM03', 'ST004', 'B', 2),
-- Ghế cho Phòng 4 (4DX)
('SEAT_R4_A1',  'ROOM04', 'ST005', 'A', 1),
('SEAT_R4_A2',  'ROOM04', 'ST005', 'A', 2),
('SEAT_R4_B1',  'ROOM04', 'ST005', 'B', 1),
('SEAT_R4_B2',  'ROOM04', 'ST005', 'B', 2),
('SEAT_R4_C1',  'ROOM04', 'ST005', 'C', 1);

INSERT INTO `ShowTime` (`ShowTimeID`, `MovieID`, `RoomID`, `StartTime`, `EndTime`) VALUES
('ST_001', 'MOV001', 'ROOM01', '2025-06-01 09:00:00', '2025-06-01 11:30:00'),
('ST_002', 'MOV001', 'ROOM03', '2025-06-01 14:00:00', '2025-06-01 16:30:00'),
('ST_003', 'MOV002', 'ROOM01', '2025-06-01 19:00:00', '2025-06-01 21:00:00'),
('ST_004', 'MOV003', 'ROOM04', '2025-06-02 10:00:00', '2025-06-02 11:50:00'),
('ST_005', 'MOV004', 'ROOM02', '2025-06-02 15:30:00', '2025-06-02 17:50:00');

INSERT INTO `Product` (`ProductID`, `ProductName`, `CurrentPrice`) VALUES
('PRD001', 'Bắp Rang Lớn',     55000.00),
('PRD002', 'Bắp Rang Nhỏ',     40000.00),
('PRD003', 'Nước Ngọt Lớn',    35000.00),
('PRD004', 'Nước Ngọt Nhỏ',    25000.00),
('PRD005', 'Combo Bắp + Nước', 80000.00);

-- ==========================================
-- III. NHÓM KHÁCH HÀNG & KHUYẾN MÃI
-- ==========================================

INSERT INTO `Customer` (`CustomerID`, `FullName`, `Phone`, `Email`, `RewardPoints`, `TotalSpent`, `MemberTier`) VALUES
('CUS001', 'Nguyễn Thị Hoa',   '0901234567', 'hoa.nguyen@email.com',  1500, 3500000.00, 'Gold'),
('CUS002', 'Trần Văn Khoa',    '0912345678', 'khoa.tran@email.com',    300, 800000.00,  'Member'),
('CUS003', 'Lê Thị Lan',       '0923456789', 'lan.le@email.com',       800, 2000000.00, 'Silver'),
('CUS004', 'Phạm Đình Mạnh',   '0934567890', 'manh.pham@email.com',   2500, 6000000.00, 'Platinum'),
('CUS005', 'Hoàng Thị Ngọc',   '0945678901', 'ngoc.hoang@email.com',   100, 250000.00,  'Member');

INSERT INTO `Promotion` (`PromotionID`, `Code`, `DiscountPercent`, `MaxDiscountAmount`, `ExpiryDate`, `ApplyToMovie`, `ValidDays`, `IsExclusive`) VALUES
('PROMO001', 'SUMMER25',    25.00, 100000.00, '2025-08-31', NULL,     NULL,                FALSE),
('PROMO002', 'AVENGERS15',  15.00,  75000.00, '2025-06-30', 'MOV001', NULL,                TRUE),
('PROMO003', 'WEEKDAY10',   10.00,  50000.00, '2025-12-31', NULL,     'Monday,Tuesday,Wednesday,Thursday', FALSE),
('PROMO004', 'NEWMEMBER20', 20.00,  80000.00, '2025-07-31', NULL,     NULL,                FALSE),
('PROMO005', 'FLASHSALE30', 30.00, 120000.00, '2025-06-15', NULL,     'Friday',            TRUE);

-- ==========================================
-- IV. NHÓM GIAO DỊCH & QUẢN LÝ GHẾ
-- ==========================================

INSERT INTO `Invoice` (`InvoiceID`, `UserID`, `CustomerID`, `PromotionID`, `TotalAmount`, `UsedPoints`, `DiscountFromPoints`, `EarnedPoints`, `CreatedAt`) VALUES
('INV001', 'U003', 'CUS001', 'PROMO001', 425000.00,  0,   0.00,     42, '2025-05-20 10:30:00'),
('INV002', 'U003', 'CUS002', NULL,        300000.00,  0,   0.00,     30, '2025-05-21 14:15:00'),
('INV003', 'U004', 'CUS003', 'PROMO003', 270000.00, 100, 20000.00,  27, '2025-05-22 09:00:00'),
('INV004', 'U003', 'CUS004', 'PROMO002', 510000.00, 500, 50000.00,  51, '2025-05-23 19:30:00'),
('INV005', 'U004', 'CUS005', NULL,        180000.00,  0,   0.00,     18, '2025-05-24 16:00:00');

INSERT INTO `BookingSeat` (`ShowTimeID`, `SeatID`, `InvoiceID`, `Price`) VALUES
('ST_001', 'SEAT_R1_A1', 'INV001', 100000.00),
('ST_001', 'SEAT_R1_B1', 'INV001', 150000.00),
('ST_001', 'SEAT_R1_A2', 'INV002', 100000.00),
('ST_002', 'SEAT_R3_A1', 'INV003', 180000.00),
('ST_002', 'SEAT_R3_A2', 'INV004', 180000.00);

INSERT INTO `SeatLock` (`ShowTimeID`, `SeatID`, `LockedBy`, `LockedAt`, `ExpiresAt`) VALUES
('ST_003', 'SEAT_R1_C1', 'U003', '2025-06-01 18:45:00', '2025-06-01 19:00:00'),
('ST_003', 'SEAT_R1_C2', 'U003', '2025-06-01 18:46:00', '2025-06-01 19:01:00'),
('ST_004', 'SEAT_R4_A1', 'U004', '2025-06-02 09:47:00', '2025-06-02 10:02:00'),
('ST_005', 'SEAT_R3_B1', 'U003', '2025-06-02 15:15:00', '2025-06-02 15:30:00'),
('ST_005', 'SEAT_R3_B2', 'U004', '2025-06-02 15:18:00', '2025-06-02 15:33:00');

INSERT INTO `OrderDetail` (`InvoiceID`, `ProductID`, `Quantity`, `Price`) VALUES
('INV001', 'PRD005', 2, 80000.00),
('INV001', 'PRD004', 2, 25000.00),
('INV002', 'PRD001', 1, 55000.00),
('INV003', 'PRD003', 2, 35000.00),
('INV004', 'PRD005', 3, 80000.00);

INSERT INTO `Payment` (`PaymentID`, `InvoiceID`, `Amount`, `PaymentMethod`, `TransactionCode`, `Status`, `CreatedAt`) VALUES
('PAY001', 'INV001', 425000.00, 'VNPay',      'VNP20250520103045',  'SUCCESS', '2025-05-20 10:30:45'),
('PAY002', 'INV002', 300000.00, 'Tiền Mặt',   NULL,                 'SUCCESS', '2025-05-21 14:16:00'),
('PAY003', 'INV003', 270000.00, 'MoMo',       'MM20250522090120',   'SUCCESS', '2025-05-22 09:01:20'),
('PAY004', 'INV004', 510000.00, 'Thẻ Ngân Hàng','CARD20250523193045','SUCCESS','2025-05-23 19:30:45'),
('PAY005', 'INV005', 180000.00, 'ZaloPay',    'ZLP20250524160012',  'SUCCESS', '2025-05-24 16:00:12');

INSERT INTO `PointHistory` (`HistoryID`, `CustomerID`, `InvoiceID`, `PointAmount`, `TransactionType`, `Description`, `CreatedAt`) VALUES
('PH001', 'CUS001', 'INV001',  42, 'EARN',   'Tích điểm từ hóa đơn INV001',         '2025-05-20 10:31:00'),
('PH002', 'CUS002', 'INV002',  30, 'EARN',   'Tích điểm từ hóa đơn INV002',         '2025-05-21 14:16:30'),
('PH003', 'CUS003', 'INV003', -100,'REDEEM', 'Dùng điểm đổi giảm giá hóa đơn INV003','2025-05-22 09:00:30'),
('PH004', 'CUS003', 'INV003',  27, 'EARN',   'Tích điểm từ hóa đơn INV003',         '2025-05-22 09:01:30'),
('PH005', 'CUS004', 'INV004', -500,'REDEEM', 'Dùng điểm đổi giảm giá hóa đơn INV004','2025-05-23 19:30:00');
