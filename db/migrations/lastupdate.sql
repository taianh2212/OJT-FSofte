create database TourBookingDB
use TourBookingDB

CREATE TABLE Categories (
    CategoryID BIGINT IDENTITY(1,1) PRIMARY KEY,
    CategoryName NVARCHAR(200) NOT NULL UNIQUE,
    Description NVARCHAR(MAX),
    CreatedAt DATETIME2 NOT NULL DEFAULT GETDATE(),
    UpdatedAt DATETIME2 NOT NULL DEFAULT GETDATE()
);
CREATE TABLE Cities (
    CityID BIGINT IDENTITY(1,1) PRIMARY KEY,
    CityName NVARCHAR(100) NOT NULL UNIQUE,
    Description NVARCHAR(MAX),
    CenterLatitude DECIMAL(9,6),
    CenterLongitude DECIMAL(9,6),
    ZipCode NVARCHAR(20),
    CreatedAt DATETIME2 NOT NULL DEFAULT GETDATE(),
    UpdatedAt DATETIME2 NOT NULL DEFAULT GETDATE()
);
-- 2. NGƯỜI DÙNG & BẢO MẬT (Identity & Security)
CREATE TABLE Users (
    UserID BIGINT IDENTITY(1,1) PRIMARY KEY,
    FullName NVARCHAR(150),
    Email NVARCHAR(150) UNIQUE NOT NULL,
    PasswordHash NVARCHAR(255),
    Role NVARCHAR(50) DEFAULT 'CUSTOMER', -- ADMIN, CUSTOMER, GUIDE, STAFF
    PhoneNumber NVARCHAR(20),
    Address NVARCHAR(255),
    AvatarURL NVARCHAR(500),
    IsActive BIT DEFAULT 1,
    CurrentSessionID NVARCHAR(64),
    CreatedAt DATETIME2 NOT NULL DEFAULT GETDATE(),
    UpdatedAt DATETIME2 NOT NULL DEFAULT GETDATE()
);
CREATE TABLE Tokens (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    token NVARCHAR(500) NOT NULL,
    email NVARCHAR(150),
    expiryDate DATETIME2,
    used BIT DEFAULT 0,
    type NVARCHAR(50), -- VERIFY, RESET
    CreatedAt DATETIME2 NOT NULL DEFAULT GETDATE(),
    UpdatedAt DATETIME2 NOT NULL DEFAULT GETDATE()
);
-- 3. HỆ THỐNG TOUR (Tour Subsystem)
CREATE TABLE Tours (
    TourID BIGINT IDENTITY(1,1) PRIMARY KEY,
    TourName NVARCHAR(200) NOT NULL,
    Description NVARCHAR(MAX),
    Price DECIMAL(10, 2),
    OriginalPrice DECIMAL(10, 2),
    
    -- Premium Content (HTML)
    Inclusions NVARCHAR(MAX),
    Exclusions NVARCHAR(MAX),
    Tips NVARCHAR(MAX),
    Itinerary NVARCHAR(MAX),
    
    -- Policies
    PaymentPolicy NVARCHAR(MAX),
    CancellationPolicy NVARCHAR(MAX),
    ChildPolicy NVARCHAR(MAX),
    
    -- Filters & Flags
    HasPickup BIT DEFAULT 0,
    HasLunch BIT DEFAULT 0,
    IsInstantConfirmation BIT DEFAULT 0,
    isDaily BIT DEFAULT 0,
    MinDepositRate DECIMAL(5, 2),
    RefundGracePeriod INT,
    
    -- Logistics & SEO
    Duration INT,
    StartLocation NVARCHAR(100),
    EndLocation NVARCHAR(100),
    Latitude DECIMAL(9, 6),
    Longitude DECIMAL(9, 6),
    TransportType NVARCHAR(50),
    MetaTitle NVARCHAR(200),
    MetaDescription NVARCHAR(500),
    
    -- Relationships
    CategoryID BIGINT,
    CityID BIGINT, -- Linked to Cities Table
    Rating FLOAT DEFAULT 0.0,
    Source NVARCHAR(50) DEFAULT 'LOCAL',
    ExternalId NVARCHAR(100),
    
    CreatedAt DATETIME2 NOT NULL DEFAULT GETDATE(),
    UpdatedAt DATETIME2 NOT NULL DEFAULT GETDATE(),
    
    CONSTRAINT FK_Tours_Categories FOREIGN KEY (CategoryID) REFERENCES Categories(CategoryID),
    CONSTRAINT FK_Tours_Cities FOREIGN KEY (CityID) REFERENCES Cities(CityID)
);
CREATE TABLE TourImages (
    ImageID BIGINT IDENTITY(1,1) PRIMARY KEY,
    TourID BIGINT,
    ImageURL NVARCHAR(500) NOT NULL,
    Caption NVARCHAR(255),
    IsMain BIT DEFAULT 0,
    CreatedAt DATETIME2 NOT NULL DEFAULT GETDATE(),
    UpdatedAt DATETIME2 NOT NULL DEFAULT GETDATE(),
    CONSTRAINT FK_TourImages_Tours FOREIGN KEY (TourID) REFERENCES Tours(TourID)
);
CREATE TABLE TourHighlights (
    HighlightID BIGINT IDENTITY(1,1) PRIMARY KEY,
    TourID BIGINT,
    Highlight NVARCHAR(500) NOT NULL,
    CreatedAt DATETIME2 NOT NULL DEFAULT GETDATE(),
    UpdatedAt DATETIME2 NOT NULL DEFAULT GETDATE(),
    CONSTRAINT FK_Highlights_Tours FOREIGN KEY (TourID) REFERENCES Tours(TourID)
);
CREATE TABLE TourFaqs (
    FaqID BIGINT IDENTITY(1,1) PRIMARY KEY,
    TourID BIGINT,
    Question NVARCHAR(MAX) NOT NULL,
    Answer NVARCHAR(MAX) NOT NULL,
    CreatedAt DATETIME2 NOT NULL DEFAULT GETDATE(),
    UpdatedAt DATETIME2 NOT NULL DEFAULT GETDATE(),
    CONSTRAINT FK_Faqs_Tours FOREIGN KEY (TourID) REFERENCES Tours(TourID)
);
-- 4. VẬN HÀNH & ĐẶT TOUR (Operations & Booking)
CREATE TABLE TourSchedules (
    ScheduleID BIGINT IDENTITY(1,1) PRIMARY KEY,
    TourID BIGINT,
    GuideID BIGINT,
    StartDate DATE NOT NULL,
    EndDate DATE NOT NULL,
    AvailableSlots INT,
    MaxSlots INT,
    Status NVARCHAR(50) DEFAULT 'OPEN', -- OPEN, FULL, IN_PROGRESS, COMPLETED, CANCELLED
    
    -- Guide Progress Report (UC28-30)
    CurrentProgress NVARCHAR(MAX),
    ReportContent NVARCHAR(MAX),
    ReportSubmissionDate DATETIME2,
    
    CreatedAt DATETIME2 NOT NULL DEFAULT GETDATE(),
    UpdatedAt DATETIME2 NOT NULL DEFAULT GETDATE(),
    CONSTRAINT FK_Schedules_Tours FOREIGN KEY (TourID) REFERENCES Tours(TourID),
    CONSTRAINT FK_Schedules_Guides FOREIGN KEY (GuideID) REFERENCES Users(UserID)
);
CREATE TABLE TourActivityImages (
    ActivityImageID BIGINT IDENTITY(1,1) PRIMARY KEY,
    ScheduleID BIGINT,
    ImageURL NVARCHAR(500) NOT NULL,
    Caption NVARCHAR(255),
    CreatedAt DATETIME2 NOT NULL DEFAULT GETDATE(),
    UpdatedAt DATETIME2 NOT NULL DEFAULT GETDATE(),
    CONSTRAINT FK_ActImages_Schedules FOREIGN KEY (ScheduleID) REFERENCES TourSchedules(ScheduleID)
);
CREATE TABLE Bookings (
    BookingID BIGINT IDENTITY(1,1) PRIMARY KEY,
    UserID BIGINT,
    ScheduleID BIGINT,
    BookingDate DATETIME2 NOT NULL DEFAULT GETDATE(),
    NumberOfPeople INT,
    TotalPrice DECIMAL(12, 2),
    Status NVARCHAR(50) DEFAULT 'PENDING',
    CreatedAt DATETIME2 NOT NULL DEFAULT GETDATE(),
    UpdatedAt DATETIME2 NOT NULL DEFAULT GETDATE(),
    CONSTRAINT FK_Bookings_Users FOREIGN KEY (UserID) REFERENCES Users(UserID),
    CONSTRAINT FK_Bookings_Schedules FOREIGN KEY (ScheduleID) REFERENCES TourSchedules(ScheduleID)
);
CREATE TABLE Payments (
    PaymentID BIGINT IDENTITY(1,1) PRIMARY KEY,
    BookingID BIGINT,
    Amount DECIMAL(12, 2),
    PaymentMethod NVARCHAR(50),
    TransactionCode NVARCHAR(100),
    PaymentDate DATETIME2,
    Status NVARCHAR(50), -- SUCCESS, FAILED
    CreatedAt DATETIME2 NOT NULL DEFAULT GETDATE(),
    UpdatedAt DATETIME2 NOT NULL DEFAULT GETDATE(),
    CONSTRAINT FK_Payments_Bookings FOREIGN KEY (BookingID) REFERENCES Bookings(BookingID)
);
CREATE TABLE PaymentLogs (
    LogID BIGINT IDENTITY(1,1) PRIMARY KEY,
    PaymentID BIGINT,
    LogMessage NVARCHAR(MAX),
    CreatedAt DATETIME2 NOT NULL DEFAULT GETDATE(),
    UpdatedAt DATETIME2 NOT NULL DEFAULT GETDATE(),
    CONSTRAINT FK_PaymentLogs_Payments FOREIGN KEY (PaymentID) REFERENCES Payments(PaymentID)
);
-- 5. TƯƠNG TÁC KHÁCH HÀNG (Customer Interaction)
CREATE TABLE Reviews (
    ReviewID BIGINT IDENTITY(1,1) PRIMARY KEY,
    UserID BIGINT,
    TourID BIGINT,
    Rating INT CHECK (Rating >= 1 AND Rating <= 5),
    Comment NVARCHAR(MAX),
    ReviewDate DATETIME2 DEFAULT GETDATE(),
    CreatedAt DATETIME2 NOT NULL DEFAULT GETDATE(),
    UpdatedAt DATETIME2 NOT NULL DEFAULT GETDATE(),
    CONSTRAINT FK_Reviews_Users FOREIGN KEY (UserID) REFERENCES Users(UserID),
    CONSTRAINT FK_Reviews_Tours FOREIGN KEY (TourID) REFERENCES Tours(TourID)
);
CREATE TABLE Wishlists (
    WishlistID BIGINT IDENTITY(1,1) PRIMARY KEY,
    UserID BIGINT,
    TourID BIGINT,
    CreatedAt DATETIME2 NOT NULL DEFAULT GETDATE(),
    UpdatedAt DATETIME2 NOT NULL DEFAULT GETDATE(),
    UNIQUE(UserID, TourID),
    CONSTRAINT FK_Wishlists_Users FOREIGN KEY (UserID) REFERENCES Users(UserID),
    CONSTRAINT FK_Wishlists_Tours FOREIGN KEY (TourID) REFERENCES Tours(TourID)
);
CREATE TABLE Newsletters (
    SubscriberID BIGINT IDENTITY(1,1) PRIMARY KEY,
    Email NVARCHAR(150) UNIQUE NOT NULL,
    SubscribedAt DATETIME2 NOT NULL DEFAULT GETDATE(),
    CreatedAt DATETIME2 NOT NULL DEFAULT GETDATE(),
    UpdatedAt DATETIME2 NOT NULL DEFAULT GETDATE()
);
-- 6. HỆ THỐNG CHAT & HỖ TRỢ (Support Subsystem)
CREATE TABLE ChatSessions (
    SessionID BIGINT IDENTITY(1,1) PRIMARY KEY,
    GuestId NVARCHAR(100),
    UserID BIGINT,
    Status NVARCHAR(50), -- ACTIVE, CLOSED
    LastMessageAt DATETIME2,
    CreatedAt DATETIME2 NOT NULL DEFAULT GETDATE(),
    UpdatedAt DATETIME2 NOT NULL DEFAULT GETDATE(),
    CONSTRAINT FK_ChatSessions_Users FOREIGN KEY (UserID) REFERENCES Users(UserID)
);
CREATE TABLE ChatMessages (
    MessageID BIGINT IDENTITY(1,1) PRIMARY KEY,
    SessionID BIGINT,
    SenderType NVARCHAR(50), -- USER, GUEST, AI, STAFF
    Content NVARCHAR(MAX),
    CreatedAt DATETIME2 NOT NULL DEFAULT GETDATE(),
    UpdatedAt DATETIME2 NOT NULL DEFAULT GETDATE(),
    CONSTRAINT FK_ChatMessages_Sessions FOREIGN KEY (SessionID) REFERENCES ChatSessions(SessionID)
);
CREATE TABLE ChatEscalations (
    EscalationID BIGINT IDENTITY(1,1) PRIMARY KEY,
    CustomerID BIGINT,
    GuestID NVARCHAR(100),
    RequestNote NVARCHAR(MAX),
    MeetingPreference NVARCHAR(255),
    Status NVARCHAR(20), -- PENDING, IN_PROGRESS, RESOLVED
    AssignedStaffID BIGINT,
    CreatedAt DATETIME2 NOT NULL DEFAULT GETDATE(),
    UpdatedAt DATETIME2 NOT NULL DEFAULT GETDATE(),
    CONSTRAINT FK_Escalations_Customers FOREIGN KEY (CustomerID) REFERENCES Users(UserID),
    CONSTRAINT FK_Escalations_Staff FOREIGN KEY (AssignedStaffID) REFERENCES Users(UserID)
);
-- ==========================================================================
-- DỮ LIỆU MẪU CƠ BẢN (Initial Seed Data)
-- ==========================================================================
-- Cities
INSERT INTO Cities (CityName, CenterLatitude, CenterLongitude) VALUES 
(N'Đà Nẵng', 16.0544, 108.2022),
(N'Huế', 16.4637, 107.5909),
(N'Hội An', 15.8801, 108.3380);
-- Categories
INSERT INTO Categories (CategoryName, Description) VALUES 
(N'Tour Trong Ngày', N'Khám phá ngắn trong 24h'),
(N'Tour Văn Hóa', N'Hành trình di sản'),
(N'Tour Thiên Nhiên', N'Đảo và Núi rừng');
-- Admin & Staff
INSERT INTO Users (FullName, Email, PasswordHash, Role) VALUES 
(N'Admin', 'admin@tourbooking.com', 'hashed_pass', 'ADMIN'),
(N'Nguyễn', 'staff@tourbooking.com', 'hashed_pass', 'STAFF');
GO