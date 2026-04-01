-- 1. Khởi tạo Database
DROP DATABASE IF EXISTS `CinemaDB`;
CREATE DATABASE `CinemaDB` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `CinemaDB`;

-- ==========================================
-- I. NHÓM HỆ THỐNG, NGƯỜI DÙNG & AUDIT
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
    `IsActive` BOOLEAN DEFAULT TRUE, -- Admin quản lý trạng thái Staff
    FOREIGN KEY (`RoleID`) REFERENCES `Role`(`RoleID`)
) ENGINE=InnoDB;

-- Bảng lưu nhật ký thay đổi giá (FR-AD-04)
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
-- III. NHÓM KHÁCH HÀNG & KHUYẾN MÃI
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

CREATE TABLE `Promotion` (
    `PromotionID` VARCHAR(50) PRIMARY KEY,
    `Code` VARCHAR(50) UNIQUE NOT NULL,
    `DiscountPercent` DECIMAL(5, 2),
    `MaxDiscountAmount` DECIMAL(15, 2),
    `ExpiryDate` DATE,
    `ApplyToMovie` VARCHAR(50) NULL,      -- Điều kiện theo phim (FR-AD-05)
    `ValidDays` VARCHAR(100) NULL,        -- Điều kiện theo thứ (FR-AD-05)
    `IsExclusive` BOOLEAN DEFAULT FALSE,  -- Không cộng dồn (BR-01)
    FOREIGN KEY (`ApplyToMovie`) REFERENCES `Movie`(`MovieID`)
) ENGINE=InnoDB;

-- ==========================================
-- IV. NHÓM GIAO DỊCH & QUẢN LÝ GHẾ
-- ==========================================

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
    `Status` VARCHAR(20) DEFAULT 'COMPLETED',
    `CancellationReason` TEXT,
    `ApprovedBy` VARCHAR(50),
    FOREIGN KEY (`UserID`) REFERENCES `User`(`UserID`),
    FOREIGN KEY (`CustomerID`) REFERENCES `Customer`(`CustomerID`),
    FOREIGN KEY (`PromotionID`) REFERENCES `Promotion`(`PromotionID`),
    FOREIGN KEY (`ApprovedBy`) REFERENCES `User`(`UserID`)
) ENGINE=InnoDB;

-- Bảng vé chính thức (Sau khi thanh toán)
CREATE TABLE `BookingSeat` (
    `ShowTimeID` VARCHAR(50) NOT NULL,
    `SeatID` VARCHAR(50) NOT NULL,
    `InvoiceID` VARCHAR(50) NOT NULL,
    `Price` DECIMAL(15, 2) NOT NULL, -- Giá snapshot (FR-NF-01)
    PRIMARY KEY (`ShowTimeID`, `SeatID`),
    FOREIGN KEY (`ShowTimeID`) REFERENCES `ShowTime`(`ShowTimeID`),
    FOREIGN KEY (`SeatID`) REFERENCES `Seat`(`SeatID`),
    FOREIGN KEY (`InvoiceID`) REFERENCES `Invoice`(`InvoiceID`)
) ENGINE=InnoDB;

-- Bảng quản lý khóa ghế 15 phút (FR-ST-02, BR-04)
CREATE TABLE `SeatLock` (
    `ShowTimeID` VARCHAR(50) NOT NULL,
    `SeatID` VARCHAR(50) NOT NULL,
    `LockedBy` VARCHAR(50) NOT NULL,
    `LockedAt` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `ExpiresAt` DATETIME NOT NULL, -- CreatedAt + 15 mins (BR-04)
    PRIMARY KEY (`ShowTimeID`, `SeatID`),
    FOREIGN KEY (`ShowTimeID`) REFERENCES `ShowTime`(`ShowTimeID`),
    FOREIGN KEY (`SeatID`) REFERENCES `Seat`(`SeatID`),
    FOREIGN KEY (`LockedBy`) REFERENCES `User`(`UserID`)
) ENGINE=InnoDB;

CREATE TABLE `OrderDetail` (
    `InvoiceID` VARCHAR(50) NOT NULL,
    `ProductID` VARCHAR(50) NOT NULL,
    `Quantity` INT NOT NULL,
    `Price` DECIMAL(15, 2) NOT NULL, -- Giá snapshot
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