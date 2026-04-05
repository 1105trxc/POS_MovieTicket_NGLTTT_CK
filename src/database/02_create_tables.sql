
USE `CinemaDB`;

-- ==========================================
-- I. NHÓM HỆ THỐNG & NGƯỜI DÙNG
-- ==========================================

CREATE TABLE `Role` (
    `RoleID` VARCHAR(50) PRIMARY KEY,
    `RoleName` VARCHAR(100) NOT NULL
) ENGINE=InnoDB;

CREATE TABLE `User` (
    `UserID` VARCHAR(50) PRIMARY KEY,
    `RoleID` VARCHAR(50) NOT NULL,
    `Username` VARCHAR(100) UNIQUE NOT NULL,
    `Password` VARCHAR(255) NOT NULL,
    `FullName` VARCHAR(255),
    FOREIGN KEY (`RoleID`) REFERENCES `Role`(`RoleID`)
) ENGINE=InnoDB;

-- ==========================================
-- II. NHÓM NGHIỆP VỤ RẠP PHIM
-- ==========================================

CREATE TABLE `Movie` (
    `MovieID` VARCHAR(50) PRIMARY KEY,
    `Title` VARCHAR(255) NOT NULL,
    `Duration` INT NOT NULL,
    `ReleaseDate` DATE,
    `Description` TEXT
) ENGINE=InnoDB;

CREATE TABLE `Genre` (
    `GenreID` VARCHAR(50) PRIMARY KEY,
    `GenreName` VARCHAR(100) NOT NULL
) ENGINE=InnoDB;

CREATE TABLE `MovieGenre` (
    `MovieID` VARCHAR(50),
    `GenreID` VARCHAR(50),
    PRIMARY KEY (`MovieID`, `GenreID`),
    FOREIGN KEY (`MovieID`) REFERENCES `Movie`(`MovieID`),
    FOREIGN KEY (`GenreID`) REFERENCES `Genre`(`GenreID`)
) ENGINE=InnoDB;

CREATE TABLE `Room` (
    `RoomID` VARCHAR(50) PRIMARY KEY,
    `RoomName` VARCHAR(100) NOT NULL,
    `Capacity` INT NOT NULL
) ENGINE=InnoDB;

CREATE TABLE `SeatType` (
    `SeatTypeID` VARCHAR(50) PRIMARY KEY,
    `TypeName` VARCHAR(100) NOT NULL,
    `BasePrice` DECIMAL(15, 2) NOT NULL DEFAULT 0
) ENGINE=InnoDB;

CREATE TABLE `Seat` (
    `SeatID` VARCHAR(50) PRIMARY KEY,
    `RoomID` VARCHAR(50) NOT NULL,
    `SeatTypeID` VARCHAR(50) NOT NULL,
    `RowChar` VARCHAR(10) NOT NULL,
    `SeatNumber` INT NOT NULL,
    FOREIGN KEY (`RoomID`) REFERENCES `Room`(`RoomID`),
    FOREIGN KEY (`SeatTypeID`) REFERENCES `SeatType`(`SeatTypeID`)
) ENGINE=InnoDB;

CREATE TABLE `ShowTime` (
    `ShowTimeID` VARCHAR(50) PRIMARY KEY,
    `MovieID` VARCHAR(50) NOT NULL,
    `RoomID` VARCHAR(50) NOT NULL,
    `StartTime` DATETIME NOT NULL,
    `EndTime` DATETIME NOT NULL,
    FOREIGN KEY (`MovieID`) REFERENCES `Movie`(`MovieID`),
    FOREIGN KEY (`RoomID`) REFERENCES `Room`(`RoomID`)
) ENGINE=InnoDB;

CREATE TABLE `Product` (
    `ProductID` VARCHAR(50) PRIMARY KEY,
    `ProductName` VARCHAR(255) NOT NULL,
    `CurrentPrice` DECIMAL(15, 2) NOT NULL
) ENGINE=InnoDB;

-- ==========================================
-- III. NHÓM KHÁCH HÀNG & TÍCH ĐIỂM
-- ==========================================

CREATE TABLE `Customer` (
    `CustomerID` VARCHAR(50) PRIMARY KEY,
    `FullName` VARCHAR(255),
    `Phone` VARCHAR(20) UNIQUE,
    `Email` VARCHAR(100),
    `RewardPoints` INT DEFAULT 0,
    `TotalSpent` DECIMAL(19, 2) DEFAULT 0,
    `MemberTier` VARCHAR(50) DEFAULT 'Member'
) ENGINE=InnoDB;

-- ==========================================
-- IV. NHÓM GIAO DỊCH & THƯƠNG MẠI
-- ==========================================

CREATE TABLE `Promotion` (
    `PromotionID` VARCHAR(50) PRIMARY KEY,
    `Code` VARCHAR(50) UNIQUE NOT NULL,
    `DiscountPercent` DECIMAL(5, 2),
    `MaxDiscountAmount` DECIMAL(15, 2),
    `ExpiryDate` DATE
) ENGINE=InnoDB;

CREATE TABLE `Invoice` (
    `InvoiceID` VARCHAR(50) PRIMARY KEY,
    `UserID` VARCHAR(50) NOT NULL,
    `CustomerID` VARCHAR(50),
    `PromotionID` VARCHAR(50),
    `TotalAmount` DECIMAL(19, 2) NOT NULL DEFAULT 0,
    `UsedPoints` INT DEFAULT 0,
    `DiscountFromPoints` DECIMAL(15, 2) DEFAULT 0,
    `EarnedPoints` INT DEFAULT 0,
    `CreatedAt` DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (`UserID`) REFERENCES `User`(`UserID`),
    FOREIGN KEY (`CustomerID`) REFERENCES `Customer`(`CustomerID`),
    FOREIGN KEY (`PromotionID`) REFERENCES `Promotion`(`PromotionID`)
) ENGINE=InnoDB;

CREATE TABLE `BookingSeat` (
    `ShowTimeID` VARCHAR(50) NOT NULL,
    `SeatID` VARCHAR(50) NOT NULL,
    `InvoiceID` VARCHAR(50) NULL,
    `Price` DECIMAL(15, 2) NOT NULL,
    PRIMARY KEY (`ShowTimeID`, `SeatID`),
    FOREIGN KEY (`ShowTimeID`) REFERENCES `ShowTime`(`ShowTimeID`),
    FOREIGN KEY (`SeatID`) REFERENCES `Seat`(`SeatID`),
    FOREIGN KEY (`InvoiceID`) REFERENCES `Invoice`(`InvoiceID`)
) ENGINE=InnoDB;

CREATE TABLE `OrderDetail` (
    `InvoiceID` VARCHAR(50) NOT NULL,
    `ProductID` VARCHAR(50) NOT NULL,
    `Quantity` INT NOT NULL,
    `Price` DECIMAL(15, 2) NOT NULL,
    PRIMARY KEY (`InvoiceID`, `ProductID`),
    FOREIGN KEY (`InvoiceID`) REFERENCES `Invoice`(`InvoiceID`),
    FOREIGN KEY (`ProductID`) REFERENCES `Product`(`ProductID`)
) ENGINE=InnoDB;

CREATE TABLE `Payment` (
    `PaymentID` VARCHAR(50) PRIMARY KEY,
    `InvoiceID` VARCHAR(50) NOT NULL,
    `Amount` DECIMAL(19, 2) NOT NULL,
    `PaymentMethod` VARCHAR(50),
    `TransactionCode` VARCHAR(100),
    `Status` VARCHAR(50),
    `CreatedAt` DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (`InvoiceID`) REFERENCES `Invoice`(`InvoiceID`)
) ENGINE=InnoDB;

CREATE TABLE `PointHistory` (
    `HistoryID` VARCHAR(50) PRIMARY KEY,
    `CustomerID` VARCHAR(50) NOT NULL,
    `InvoiceID` VARCHAR(50),
    `PointAmount` INT NOT NULL,
    `TransactionType` VARCHAR(50),
    `Description` TEXT,
    `CreatedAt` DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (`CustomerID`) REFERENCES `Customer`(`CustomerID`),
    FOREIGN KEY (`InvoiceID`) REFERENCES `Invoice`(`InvoiceID`)
) ENGINE=InnoDB;

-- Bảng lưu nhật ký thay đổi
CREATE TABLE `AuditLog` (
    `LogID` INT AUTO_INCREMENT PRIMARY KEY,
    `ChangedBy` VARCHAR(50) NOT NULL,
    `TableName` VARCHAR(50) NOT NULL,
    `FieldName` VARCHAR(50) NOT NULL,
    `OldValue` TEXT,
    `NewValue` TEXT,
    `ChangedAt` DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (`ChangedBy`) REFERENCES `User`(`UserID`)
) ENGINE=InnoDB;

-- Bảng quản lý khóa ghế 15 phút
CREATE TABLE `SeatLock` (
    `ShowTimeID` VARCHAR(50) NOT NULL,
    `SeatID` VARCHAR(50) NOT NULL,
    `LockedBy` VARCHAR(50) NOT NULL,
    `LockedAt` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `ExpiresAt` DATETIME NOT NULL,
    PRIMARY KEY (`ShowTimeID`, `SeatID`),
    FOREIGN KEY (`ShowTimeID`) REFERENCES `ShowTime`(`ShowTimeID`),
    FOREIGN KEY (`SeatID`) REFERENCES `Seat`(`SeatID`),
    FOREIGN KEY (`LockedBy`) REFERENCES `User`(`UserID`)
) ENGINE=InnoDB;

-- ==========================================
-- V. NHÓM QUẢN LÝ CA LÀM VIỆC & KẾT TOÁN
-- ==========================================

CREATE TABLE `ShiftReport` (
    `ShiftReportID` VARCHAR(50) PRIMARY KEY,
    `UserID` VARCHAR(50) NOT NULL,
    `ShiftStart` DATETIME NOT NULL,
    `ShiftEnd` DATETIME NOT NULL,
    `OpeningCash` DECIMAL(19, 2) DEFAULT 0,
    `CashRevenue` DECIMAL(19, 2) DEFAULT 0,
    `TransferRevenue` DECIMAL(19, 2) DEFAULT 0,
    `CardRevenue` DECIMAL(19, 2) DEFAULT 0,
    `TotalRevenue` DECIMAL(19, 2) DEFAULT 0,
    `ExpectedCash` DECIMAL(19, 2) DEFAULT 0,
    `ActualCash` DECIMAL(19, 2) DEFAULT 0,
    `Discrepancy` DECIMAL(19, 2) DEFAULT 0,
    `RemittedCash` DECIMAL(19, 2) DEFAULT 0,
    `CarryOverCash` DECIMAL(19, 2) DEFAULT 0,
    `Status` VARCHAR(20) DEFAULT 'PENDING',
    `ApprovedBy` VARCHAR(50),
    `ApprovedAt` DATETIME,
    `Notes` TEXT,
    `CreatedAt` DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (`UserID`) REFERENCES `User`(`UserID`),
    FOREIGN KEY (`ApprovedBy`) REFERENCES `User`(`UserID`)
) ENGINE=InnoDB;

-- ==========================================
-- VI. CẬP NHẬT SCHEMA PHIÊN BẢN MỚI
-- ==========================================

ALTER TABLE `User`
ADD COLUMN `Phone` VARCHAR(20),
ADD COLUMN `Email` VARCHAR(100),
ADD COLUMN `BirthDate` DATE,
ADD COLUMN `CCCD` VARCHAR(20) UNIQUE,
ADD COLUMN `Gender` VARCHAR(10),
ADD COLUMN `IsActive` BOOLEAN DEFAULT TRUE;

ALTER TABLE `Invoice`
ADD COLUMN `DiscountFromTier` DECIMAL(15, 2) DEFAULT 0,
ADD COLUMN `DiscountFromPromotion` DECIMAL(15, 2) DEFAULT 0,
ADD COLUMN `FinalAmount` DECIMAL(19, 2) NOT NULL DEFAULT 0,
ADD COLUMN `Status` VARCHAR(20) DEFAULT 'COMPLETED',
ADD COLUMN `CancellationReason` VARCHAR(500),
ADD COLUMN `ApprovedBy` VARCHAR(50),
ADD CONSTRAINT `FK_Invoice_ApprovedBy` FOREIGN KEY (`ApprovedBy`) REFERENCES `User`(`UserID`);

ALTER TABLE `Promotion`
ADD COLUMN `Description` TEXT,
ADD COLUMN `StartDate` DATE,
ADD COLUMN `ApplyToMovie` VARCHAR(50),
ADD COLUMN `ValidDays` VARCHAR(100),
ADD COLUMN `IsExclusive` TINYINT(1) NOT NULL DEFAULT 0,
ADD CONSTRAINT `FK_Promo_Movie` FOREIGN KEY (`ApplyToMovie`) REFERENCES `Movie`(`MovieID`);

ALTER TABLE `Movie`
ADD COLUMN `AgeRestriction` VARCHAR(10) DEFAULT 'P';

ALTER TABLE `BookingSeat`
MODIFY COLUMN `InvoiceID` VARCHAR(50) NOT NULL;

-- ==========================================
-- VII. RÀNG BUỘC MIỀN GIÁ TRỊ (SỐ LƯỢNG / TIỀN / THỜI GIAN)
-- ==========================================

-- Tiền
ALTER TABLE `Product`
ADD CONSTRAINT `CK_Product_CurrentPrice_NonNeg` CHECK (`CurrentPrice` >= 0);

ALTER TABLE `SeatType`
ADD CONSTRAINT `CK_SeatType_BasePrice_NonNeg` CHECK (`BasePrice` >= 0);

ALTER TABLE `BookingSeat`
ADD CONSTRAINT `CK_BookingSeat_Price_NonNeg` CHECK (`Price` >= 0);

ALTER TABLE `OrderDetail`
ADD CONSTRAINT `CK_OrderDetail_Price_NonNeg` CHECK (`Price` >= 0);

ALTER TABLE `Payment`
ADD CONSTRAINT `CK_Payment_Amount_Positive` CHECK (`Amount` > 0);

ALTER TABLE `Invoice`
ADD CONSTRAINT `CK_Invoice_TotalAmount_NonNeg` CHECK (`TotalAmount` >= 0),
ADD CONSTRAINT `CK_Invoice_DiscountFromPoints_NonNeg` CHECK (`DiscountFromPoints` >= 0),
ADD CONSTRAINT `CK_Invoice_DiscountFromTier_NonNeg` CHECK (`DiscountFromTier` >= 0),
ADD CONSTRAINT `CK_Invoice_DiscountFromPromotion_NonNeg` CHECK (`DiscountFromPromotion` >= 0),
ADD CONSTRAINT `CK_Invoice_FinalAmount_NonNeg` CHECK (`FinalAmount` >= 0);

ALTER TABLE `Customer`
ADD CONSTRAINT `CK_Customer_TotalSpent_NonNeg` CHECK (`TotalSpent` >= 0);

ALTER TABLE `Promotion`
ADD CONSTRAINT `CK_Promotion_MaxDiscountAmount_NonNeg`
CHECK (`MaxDiscountAmount` IS NULL OR `MaxDiscountAmount` >= 0);

-- Số lượng
ALTER TABLE `OrderDetail`
ADD CONSTRAINT `CK_OrderDetail_Quantity_Positive` CHECK (`Quantity` > 0);

ALTER TABLE `Room`
ADD CONSTRAINT `CK_Room_Capacity_Positive` CHECK (`Capacity` > 0);

ALTER TABLE `Seat`
ADD CONSTRAINT `CK_Seat_SeatNumber_Positive` CHECK (`SeatNumber` > 0);

ALTER TABLE `Customer`
ADD CONSTRAINT `CK_Customer_RewardPoints_NonNeg` CHECK (`RewardPoints` >= 0);

ALTER TABLE `Invoice`
ADD CONSTRAINT `CK_Invoice_UsedPoints_NonNeg` CHECK (`UsedPoints` >= 0),
ADD CONSTRAINT `CK_Invoice_EarnedPoints_NonNeg` CHECK (`EarnedPoints` >= 0);

-- Thời gian
ALTER TABLE `ShowTime`
ADD CONSTRAINT `CK_ShowTime_StartBeforeEnd` CHECK (`StartTime` < `EndTime`);

ALTER TABLE `SeatLock`
ADD CONSTRAINT `CK_SeatLock_LockedBeforeExpire` CHECK (`LockedAt` < `ExpiresAt`);

ALTER TABLE `Promotion`
ADD CONSTRAINT `CK_Promotion_StartBeforeExpiry`
CHECK (`StartDate` IS NULL OR `ExpiryDate` IS NULL OR `StartDate` <= `ExpiryDate`);
