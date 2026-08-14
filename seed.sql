DECLARE @i INT = 1;
DECLARE @status VARCHAR(20);
DECLARE @randomDays INT;
DECLARE @total DECIMAL(18,2);
DECLARE @pk BIGINT;
DECLARE @productPk BIGINT;
DECLARE @productPrice DECIMAL(18,2);
DECLARE @productNameVn NVARCHAR(255);
DECLARE @productNameEng NVARCHAR(255);
DECLARE @qty INT;
DECLARE @subtotal DECIMAL(18,2);
DECLARE @orderDate DATETIME2;

WHILE @i <= 200
BEGIN
    SET @randomDays = ABS(CHECKSUM(NEWID())) % 365;
    SET @orderDate = DATEADD(DAY, -@randomDays, GETDATE());
    
    DECLARE @randStatus INT = ABS(CHECKSUM(NEWID())) % 7;
    SET @status = CASE @randStatus
        WHEN 0 THEN 'PENDING'
        WHEN 1 THEN 'WAITING_PAYMENT'
        WHEN 2 THEN 'PAID'
        WHEN 3 THEN 'PROCESSING'
        WHEN 4 THEN 'SHIPPED'
        WHEN 5 THEN 'DELIVERED'
        WHEN 6 THEN 'CANCELLED'
    END;

    INSERT INTO orders (id, fullname, phone_number, address, payment_method, status, created_date, expired_date, expired, del_if, shipping_fee, total, account_pk)
    VALUES (
        'O-FAKE-' + CAST(@i AS VARCHAR(10)),
        N'Khách ảo ' + CAST(@i AS VARCHAR(10)),
        '090' + CAST(ABS(CHECKSUM(NEWID())) % 8999999 + 1000000 AS VARCHAR(10)),
        N'Địa chỉ ảo ' + CAST(@i AS VARCHAR(10)),
        CASE WHEN ABS(CHECKSUM(NEWID())) % 2 = 0 THEN 'COD' ELSE 'STORE' END,
        @status,
        @orderDate,
        DATEADD(DAY, 14, @orderDate),
        0, 0, 30000, 30000, 1
    );
    
    SET @pk = SCOPE_IDENTITY();
    SET @total = 30000;
    
    DECLARE @j INT = 1;
    DECLARE @numProducts INT = ABS(CHECKSUM(NEWID())) % 3 + 1;
    
    WHILE @j <= @numProducts
    BEGIN
        SELECT TOP 1 @productPk = pk, @productPrice = price, @productNameVn = name_vn, @productNameEng = name_eng 
        FROM products WHERE del_if = 0 AND available = 1 ORDER BY NEWID();
        
        IF @productPk IS NOT NULL
        BEGIN
            SET @qty = ABS(CHECKSUM(NEWID())) % 3 + 1;
            SET @subtotal = @productPrice * @qty;
            
            INSERT INTO orders_details (order_pk, product_pk, product_name_vn, product_name_eng, product_price, quantity, subtotal)
            VALUES (@pk, @productPk, @productNameVn, @productNameEng, @productPrice, @qty, @subtotal);
            
            SET @total = @total + @subtotal;
        END
        SET @j = @j + 1;
    END

    UPDATE orders SET total = @total WHERE pk = @pk;
    
    SET @i = @i + 1;
END
