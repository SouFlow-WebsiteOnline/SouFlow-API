use master
go

CREATE DATABASE flower_shop;
GO

USE flower_shop;
GO

CREATE TABLE categories (
    pk BIGINT IDENTITY(1,1) PRIMARY KEY,
    id VARCHAR(50) UNIQUE NOT NULL,
    name_vn NVARCHAR(100) NOT NULL,
    name_eng VARCHAR(100) NOT NULL,
    description_vn NVARCHAR(255),
    description_eng VARCHAR(255),
    del_if BIT NOT NULL DEFAULT 0
);
GO

CREATE TABLE roles (
	code VARCHAR(10) UNIQUE NOT NULL,
	name_vn NVARCHAR(100) NOT NULL UNIQUE,
    name_eng VARCHAR(100) NOT NULL UNIQUE,
    del_if BIT NOT NULL DEFAULT 0
);
GO

CREATE TABLE accounts (
    pk BIGINT IDENTITY(1,1) PRIMARY KEY,
    username VARCHAR(50) UNIQUE,
    password VARCHAR(255), 
    fullname NVARCHAR(50) NOT NULL,
    email NVARCHAR(50) UNIQUE,
    photo NVARCHAR(255),
	address NVARCHAR(100), 
	phone_number VARCHAR(12) , 
	created_date DATETIME2,
    disabled BIT NOT NULL DEFAULT 0,
    credential_expired_date DATETIME2 NOT NULL,
    credential_expired BIT NOT NULL DEFAULT 0,
    del_if BIT NOT NULL DEFAULT 0,

    role_code varchar(10) NOT NULL
    CONSTRAINT fk_accounts_roles 
        FOREIGN KEY (role_code)
        REFERENCES roles(code)
);
GO

CREATE TABLE products (
    pk BIGINT IDENTITY(1,1) PRIMARY KEY,
    id VARCHAR(50) UNIQUE NOT NULL,
    name_vn NVARCHAR(100) NOT NULL,
    name_eng VARCHAR(100) NOT NULL,
    description_vn NVARCHAR(255) NOT NULL,
    description_eng VARCHAR(255) NOT NULL,
	price DECIMAL(18,2) NOT NULL,
    created_date DATETIME2,
    available BIT NOT NULL DEFAULT 1,
	quantity INT NOT NULL DEFAULT 5,
    customised BIT NOT NULL,
	sales INT NOT NULL DEFAULT 0,
    del_if BIT NOT NULL DEFAULT 0,

    category_pk BIGINT NOT NULL,
    CONSTRAINT fk_products_categories
        FOREIGN KEY (category_pk)
        REFERENCES categories(pk)
);
GO

CREATE TABLE product_images (
    pk BIGINT IDENTITY(1,1) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
	created_date DATETIME2 NOT NULL,
    del_if BIT NOT NULL DEFAULT 0,

    product_pk BIGINT
    CONSTRAINT fk_images_products
        FOREIGN KEY (product_pk)
        REFERENCES products(pk)
);
GO

CREATE TABLE orders (
    pk BIGINT IDENTITY(1,1) PRIMARY KEY,
    id VARCHAR(50) UNIQUE NOT NULL,
    fullname NVARCHAR(100) NOT NULL,
    phone_number VARCHAR(12) NOT NULL,
    address NVARCHAR(100) NOT NULL,
    total DECIMAL(18,2) NOT NULL,
    created_date DATETIME2,
    expired_date DATETIME2 NOT NULL,
    expired BIT DEFAULT 0 NOT NULL,
    status VARCHAR(50) DEFAULT 'PENDING',
    shipping_fee DECIMAL(18,2) DEFAULT 0,
    payment_method VARCHAR(50) DEFAULT 'COD',
    discount_code VARCHAR(255),
    discount_amount FLOAT DEFAULT 0,
    del_if BIT NOT NULL DEFAULT 0,

    account_pk BIGINT NOT NULL,
    CONSTRAINT fk_orders_accounts
        FOREIGN KEY (account_pk)
        REFERENCES accounts(pk)
);
GO

CREATE TABLE orders_details (
    pk BIGINT IDENTITY(1,1) PRIMARY KEY,
    product_name_vn NVARCHAR(100) NOT NULL,
    product_name_eng VARCHAR(100) NOT NULL,
	product_price DECIMAL(18,2) NOT NULL,
    quantity INT NOT NULL,
	subtotal DECIMAL(18,2) NOT NULL,

    order_pk BIGINT NOT NULL,
    product_pk BIGINT NOT NULL,
    CONSTRAINT fk_orderdetails_orders
        FOREIGN KEY (order_pk)
        REFERENCES orders(pk),

    CONSTRAINT fk_orderdetails_products
        FOREIGN KEY (product_pk)
        REFERENCES products(pk)
);
GO

CREATE TABLE payments (
    pk BIGINT IDENTITY(1,1) PRIMARY KEY,
    paid BIT NOT NULL,
    amount DECIMAL(18,2) NOT NULL,
    payment_date DATETIME2 NOT NULL,

    order_pk BIGINT NOT NULL,
    CONSTRAINT fk_payments_orders 
        FOREIGN KEY (order_pk)
        REFERENCES orders(pk)

);
GO

CREATE TABLE carts (
    pk BIGINT IDENTITY(1,1) PRIMARY KEY,
    id VARCHAR(50) UNIQUE NOT NULL,
	created_date DATETIME2,
    expired_date DATETIME2 NOT NULL,
    expired BIT DEFAULT 0 NOT NULL,
    total DECIMAL(18,2) NOT NULL,
    del_if BIT NOT NULL DEFAULT 0,

    account_pk BIGINT NOT NULL,
    CONSTRAINT fk_carts_accounts
        FOREIGN KEY (account_pk)
        REFERENCES accounts(pk)
);
GO

CREATE TABLE items (
    pk BIGINT IDENTITY(1,1) PRIMARY KEY,
    quantity INT NOT NULL,
    subtotal DECIMAL(18,2) NOT NULL,

    product_pk BIGINT NOT NULL,
    cart_pk BIGINT NOT NULL,
    CONSTRAINT fk_items_carts
        FOREIGN KEY (cart_pk)
        REFERENCES Carts(pk), 

    CONSTRAINT fk_items_products
        FOREIGN KEY (product_pk)
        REFERENCES Products(pk)
);
GO

CREATE TABLE discounts (
    pk BIGINT IDENTITY(1,1) PRIMARY KEY,
    id VARCHAR(50) UNIQUE NOT NULL,
    code VARCHAR(255) UNIQUE,
    percentage FLOAT,
    min_order_amount FLOAT,
    usage_limit INT,
    current_usage INT DEFAULT 0,
    description_vn NVARCHAR(255),
    description_eng VARCHAR(255),
    created_date DATETIME2 NOT NULL,
    expired_date DATETIME2 NOT NULL,
    expired BIT DEFAULT 0,
    del_if BIT NOT NULL DEFAULT 0
);
GO

CREATE TABLE products_discounts (
    product_pk BIGINT NOT NULL,
    discount_pk BIGINT NOT NULL,

    CONSTRAINT pk_products_discounts PRIMARY KEY (product_pk, discount_pk),
    CONSTRAINT fk_products_discounts_products
        FOREIGN KEY (product_pk) REFERENCES products(pk),
    CONSTRAINT fk_products_discounts_discounts
        FOREIGN KEY (discount_pk) REFERENCES discounts(pk)
);
GO

CREATE TABLE comments ( 
    pk BIGINT IDENTITY(1,1) PRIMARY KEY,
    content NVARCHAR(500) NOT NULL,
    created_date DATETIME2,
    del_if BIT NOT NULL DEFAULT 0,

    product_pk BIGINT NOT NULL,
    account_pk BIGINT NOT NULL,
    CONSTRAINT fk_comments_products
        FOREIGN KEY (product_pk)
        REFERENCES products(pk),
    CONSTRAINT fk_comments_accounts
        FOREIGN KEY (account_pk)
        REFERENCES accounts(pk)
);
GO

CREATE TABLE replies (
    pk BIGINT IDENTITY(1,1) PRIMARY KEY,      
    content NVARCHAR(500) NOT NULL,       
	created_date DATETIME2,
    del_if BIT NOT NULL DEFAULT 0,

    comment_pk BIGINT NOT NULL,                
    account_pk BIGINT NOT NULL,            
    CONSTRAINT fk_replies_comments
        FOREIGN KEY (comment_pk)
        REFERENCES comments(pk),                  
    CONSTRAINT fk_replies_account
        FOREIGN KEY (account_pk)
        REFERENCES accounts(pk)
);
GO

CREATE TABLE chat_messages (
    pk BIGINT IDENTITY(1,1) PRIMARY KEY,
    content NVARCHAR(500) NOT NULL,
    created_date DATETIME2,

    account_pk BIGINT NOT NULL,
    CONSTRAINT fk_chat_messages_accounts
        FOREIGN KEY (account_pk)
        REFERENCES accounts(pk)
);
GO


INSERT INTO roles (
    code,
    name_vn,
    name_eng
)
VALUES
(
    'ADMIN',
    N'Quản trị viên',
    'Administrator'
),
(
    'USER',
    N'Người dùng',
    'User'
);

INSERT INTO accounts (
    username,
    password,
    fullname,
    email,
    created_date,
    credential_expired_date,
    role_code
)
VALUES (
    'admin',
    '$2a$10$OQV2lk31K/eTmbHEP0ljiue92qx/2WG.wWjwfDeyazOtNCapbOYPq',
    N'Administrator',
    'admin@example.com',
    GETDATE(),
    DATEADD(YEAR, 10, GETDATE()),
    'ADMIN'
);


CREATE PROCEDURE sp_GetRevenueReport
    @StartDate DATETIME2,
    @EndDate   DATETIME2,
    @Type      VARCHAR(20)   -- 'MONTH', 'QUARTER', 'YEAR'
AS
BEGIN
    SET NOCOUNT ON;

    DECLARE @RealStart DATETIME2;
    DECLARE @RealEnd   DATETIME2;

    -- ===== Làm tròn khoảng ngày theo đơn vị được chọn =====
    IF @Type = 'YEAR'
    BEGIN
        -- Lấy trọn năm bắt đầu -> trọn năm kết thúc
        SET @RealStart = DATEFROMPARTS(YEAR(@StartDate), 1, 1);
        SET @RealEnd   = DATEFROMPARTS(YEAR(@EndDate), 12, 31);
    END
    ELSE IF @Type = 'QUARTER'
    BEGIN
        -- Lấy trọn quý bắt đầu -> trọn quý kết thúc
        DECLARE @StartQuarterMonth INT = (DATEPART(QUARTER, @StartDate) - 1) * 3 + 1;
        DECLARE @EndQuarterMonth   INT = (DATEPART(QUARTER, @EndDate) - 1) * 3 + 1;

        SET @RealStart = DATEFROMPARTS(YEAR(@StartDate), @StartQuarterMonth, 1);
        SET @RealEnd   = EOMONTH(DATEFROMPARTS(YEAR(@EndDate), @EndQuarterMonth + 2, 1));
    END
    ELSE IF @Type = 'MONTH'
    BEGIN
        -- Lấy trọn tháng bắt đầu -> trọn tháng kết thúc
        SET @RealStart = DATEFROMPARTS(YEAR(@StartDate), MONTH(@StartDate), 1);
        SET @RealEnd   = EOMONTH(DATEFROMPARTS(YEAR(@EndDate), MONTH(@EndDate), 1));
    END
    ELSE
    BEGIN
        RAISERROR('Invalid @Type. Must be MONTH, QUARTER, or YEAR.', 16, 1);
        RETURN;
    END

    -- ===== Group theo đơn vị đã chọn, dùng khoảng ngày đã làm tròn =====
    IF @Type = 'MONTH'
    BEGIN
        SELECT 
            YEAR(created_date)  AS year_value,
            MONTH(created_date) AS month_value,
            CAST(YEAR(created_date) AS VARCHAR(4)) + '-' 
                + RIGHT('0' + CAST(MONTH(created_date) AS VARCHAR(2)), 2) AS label,
            COUNT(*)     AS order_count,
            SUM(total)   AS revenue
        FROM orders
        WHERE del_if = 0
          AND status = 'DELIVERED'
          AND created_date >= @RealStart
          AND created_date < DATEADD(DAY, 1, @RealEnd)
        GROUP BY YEAR(created_date), MONTH(created_date)
        ORDER BY YEAR(created_date), MONTH(created_date);
    END
    ELSE IF @Type = 'QUARTER'
    BEGIN
        SELECT
            YEAR(created_date) AS year_value,
            DATEPART(QUARTER, created_date) AS quarter_value,
            CAST(YEAR(created_date) AS VARCHAR(4)) + '-Q' 
                + CAST(DATEPART(QUARTER, created_date) AS VARCHAR(1)) AS label,
            COUNT(*)     AS order_count,
            SUM(total)   AS revenue
        FROM orders
        WHERE del_if = 0
          AND status = 'DELIVERED'
          AND created_date >= @RealStart
          AND created_date < DATEADD(DAY, 1, @RealEnd)
        GROUP BY YEAR(created_date), DATEPART(QUARTER, created_date)
        ORDER BY YEAR(created_date), DATEPART(QUARTER, created_date);
    END
    ELSE IF @Type = 'YEAR'
    BEGIN
        SELECT
            YEAR(created_date) AS year_value,
            CAST(YEAR(created_date) AS VARCHAR(4)) AS label,
            COUNT(*)     AS order_count,
            SUM(total)   AS revenue
        FROM orders
        WHERE del_if = 0
          AND status = 'DELIVERED'
          AND created_date >= @RealStart
          AND created_date < DATEADD(DAY, 1, @RealEnd)
        GROUP BY YEAR(created_date)
        ORDER BY YEAR(created_date);
    END
END
GO
