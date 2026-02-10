-- =====================================================
-- MOCK DATA FOR SALES ANALYTICS DASHBOARD
-- Using REAL cars from database
-- =====================================================

-- STEP 1: Create InventoryCars from real Cars
-- Creating 15 inventory cars based on actual car data

INSERT INTO inventory_cars (car_id, price, color, vin, condition_type, sale_status, mileage, year_of_manufacture, notes, created_at, updated_at)
VALUES
-- 1. Hyundai Palisade Prestige 7 chỗ - 1.559 tỷ
(288, 1559000000, 'Trắng Ngọc Trai', 'VIN-HYU-PAL-001', 'NEW', 'AVAILABLE', 0, 2023, 'Xe mới 100%, full options', NOW(), NOW()),

-- 2. Porsche 718 Cayman T - 4.77 tỷ
(404, 4770000000, 'Đỏ Guards', 'VIN-POR-718-002', 'NEW', 'AVAILABLE', 0, 2021, 'Xe thể thao cao cấp', NOW(), NOW()),

-- 3. Nissan Almera VL - 569 triệu
(365, 569000000, 'Bạc Ánh Kim', 'VIN-NIS-ALM-003', 'NEW', 'AVAILABLE', 0, 2026, 'Sedan hạng B tiết kiệm', NOW(), NOW()),

-- 4. Bentley Bentayga V8 - 19.5 tỷ
(4, 19500000000, 'Đen Onyx', 'VIN-BEN-BTY-004', 'NEW', 'AVAILABLE', 0, 2021, 'SUV siêu sang', NOW(), NOW()),

-- 5. Nissan Navara Pro4X - 960 triệu
(369, 960000000, 'Xanh Rêu', 'VIN-NIS-NAV-005', 'NEW', 'AVAILABLE', 0, 2026, 'Bán tải cao cấp', NOW(), NOW()),

-- 6. Kia K3 1.6 Luxury - 579 triệu
(247, 579000000, 'Trắng', 'VIN-KIA-K3-006', 'NEW', 'AVAILABLE', 0, 2022, 'Sedan hạng C phổ thông', NOW(), NOW()),

-- 7. Kia Carnival 1.6 Turbo Hybrid - 1.849 tỷ
(199, 1849000000, 'Xám Titan', 'VIN-KIA-CAR-007', 'NEW', 'AVAILABLE', 0, 2026, 'MPV 7 chỗ hybrid', NOW(), NOW()),

-- 8. Maserati Ghibli Trofeo - 11.904 tỷ
(81, 11904000000, 'Xanh Blu Emozione', 'VIN-MAS-GHI-008', 'NEW', 'AVAILABLE', 0, 2021, 'Sedan thể thao hạng sang', NOW(), NOW()),

-- 9. BMW X5 xDrive40i xLine - 3.909 tỷ
(36, 3909000000, 'Xanh Alpine', 'VIN-BMW-X5-009', 'NEW', 'AVAILABLE', 0, 2026, 'SUV hạng sang', NOW(), NOW()),

-- 10. Kia Seltos 1.5 Turbo Deluxe - 659 triệu
(253, 659000000, 'Đỏ Aurora', 'VIN-KIA-SEL-010', 'NEW', 'AVAILABLE', 0, 2026, 'SUV cỡ B thời thượng', NOW(), NOW()),

-- 11. Skoda Kodiaq Premium - 1.45 tỷ
(437, 1450000000, 'Nâu Velvet', 'VIN-SKO-KOD-011', 'NEW', 'AVAILABLE', 0, 2025, 'SUV 7 chỗ châu Âu', NOW(), NOW()),

-- 12. Volkswagen T-Cross Luxury - 1.299 tỷ
(512, 1299000000, 'Cam Energetic', 'VIN-VW-TCR-012', 'NEW', 'AVAILABLE', 0, 2022, 'SUV cỡ B năng động', NOW(), NOW()),

-- 13. Toyota Veloz Cross CVT - 638 triệu
(468, 638000000, 'Trắng Ngọc', 'VIN-TOY-VEL-013', 'NEW', 'AVAILABLE', 0, 2022, 'MPV 7 chỗ gia đình', NOW(), NOW()),

-- 14. Porsche Macan Base - 3.35 tỷ
(396, 3350000000, 'Xám Agate', 'VIN-POR-MAC-014', 'NEW', 'AVAILABLE', 0, 2022, 'SUV thể thao hạng sang', NOW(), NOW()),

-- 15. Hongqi H9 2.0 Elegance - 1.508 tỷ
(326, 1508000000, 'Đen Bóng', 'VIN-HQ-H9-015', 'NEW', 'AVAILABLE', 0, 2022, 'Sedan hạng sang Trung Quốc', NOW(), NOW());


-- STEP 2: Create Sales Orders across 6 months in 2026
-- Mix of different price ranges for realistic revenue distribution

-- ===== JANUARY 2026 - 2 orders =====
INSERT INTO sales_orders (
    order_number, customer_name, customer_phone, customer_email, customer_address,
    inventory_car_id, 
    base_price, additional_fees, discount_amount, total_price, 
    deposit_amount, paid_amount, remaining_amount, sales_staff_id,
    order_date, order_status, payment_status,
    created_at, updated_at
)
VALUES 
-- Jan 1: Kia Seltos
('ORD-2026-01-001', 'Nguyen Van Anh', '0901234567', 'nguyenvananh@email.com', 'Ha Noi',
 (SELECT id FROM inventory_cars WHERE vin = 'VIN-KIA-SEL-010' LIMIT 1),
 659000000, 50000000, 10000000, 699000000,
 200000000, 699000000, 0,
 (SELECT user_id FROM users LIMIT 1),
 '2026-01-15', 'COMPLETED', 'FULLY_PAID',
 '2026-01-15 10:00:00', '2026-01-20 15:30:00'),

-- Jan 2: Hyundai Palisade
('ORD-2026-01-002', 'Tran Thi Binh', '0987654321', 'tranthibinh@email.com', 'TP HCM',
 (SELECT id FROM inventory_cars WHERE vin = 'VIN-HYU-PAL-001' LIMIT 1),
 1559000000, 80000000, 20000000, 1619000000,
 400000000, 1619000000, 0,
 (SELECT user_id FROM users LIMIT 1),
 '2026-01-22', 'COMPLETED', 'FULLY_PAID',
 '2026-01-22 14:00:00', '2026-01-28 16:00:00');


-- ===== FEBRUARY 2026 - 2 orders =====
INSERT INTO sales_orders (
    order_number, customer_name, customer_phone, customer_email, customer_address,
    inventory_car_id,
    base_price, additional_fees, discount_amount, total_price,
    deposit_amount, paid_amount, remaining_amount, sales_staff_id,
    order_date, order_status, payment_status,
    created_at, updated_at
)
VALUES 
-- Feb 1: BMW X5
('ORD-2026-02-001', 'Le Van Cuong', '0912345678', 'levancuong@email.com', 'Da Nang',
 (SELECT id FROM inventory_cars WHERE vin = 'VIN-BMW-X5-009' LIMIT 1),
 3909000000, 150000000, 50000000, 4009000000,
 1000000000, 4009000000, 0,
 (SELECT user_id FROM users ORDER BY user_id DESC LIMIT 1),
 '2026-02-10', 'COMPLETED', 'FULLY_PAID',
 '2026-02-10 09:00:00', '2026-02-18 10:00:00'),

-- Feb 2: Toyota Veloz Cross
('ORD-2026-02-002', 'Pham Thi Dung', '0923456789', 'phamthidung@email.com', 'Hai Phong',
 (SELECT id FROM inventory_cars WHERE vin = 'VIN-TOY-VEL-013' LIMIT 1),
 638000000, 45000000, 8000000, 675000000,
 200000000, 675000000, 0,
 (SELECT user_id FROM users ORDER BY user_id DESC LIMIT 1),
 '2026-02-25', 'COMPLETED', 'FULLY_PAID',
 '2026-02-25 11:00:00', '2026-02-28 14:00:00');


-- ===== MARCH 2026 - 2 orders =====
INSERT INTO sales_orders (
    order_number, customer_name, customer_phone, customer_email, customer_address,
    inventory_car_id,
    base_price, additional_fees, discount_amount, total_price,
    deposit_amount, paid_amount, remaining_amount, sales_staff_id,
    order_date, order_status, payment_status,
    created_at, updated_at
)
VALUES 
-- Mar 1: Kia Carnival Hybrid
('ORD-2026-03-001', 'Hoang Van Em', '0934567890', 'hoangvanem@email.com', 'Can Tho',
 (SELECT id FROM inventory_cars WHERE vin = 'VIN-KIA-CAR-007' LIMIT 1),
 1849000000, 90000000, 30000000, 1909000000,
 500000000, 1909000000, 0,
 (SELECT user_id FROM users LIMIT 1),
 '2026-03-05', 'COMPLETED', 'FULLY_PAID',
 '2026-03-05 13:00:00', '2026-03-12 15:00:00'),

-- Mar 2: Nissan Almera
('ORD-2026-03-002', 'Vo Thi Phuong', '0945678901', 'vothiphuong@email.com', 'Nha Trang',
 (SELECT id FROM inventory_cars WHERE vin = 'VIN-NIS-ALM-003' LIMIT 1),
 569000000, 40000000, 5000000, 604000000,
 150000000, 604000000, 0,
 (SELECT user_id FROM users ORDER BY user_id DESC LIMIT 1),
 '2026-03-20', 'COMPLETED', 'FULLY_PAID',
 '2026-03-20 10:30:00', '2026-03-25 16:30:00');


-- ===== APRIL 2026 - 3 orders (peak month) =====
INSERT INTO sales_orders (
    order_number, customer_name, customer_phone, customer_email, customer_address,
    inventory_car_id,
    base_price, additional_fees, discount_amount, total_price,
    deposit_amount, paid_amount, remaining_amount, sales_staff_id,
    order_date, order_status, payment_status,
    created_at, updated_at
)
VALUES 
-- Apr 1: Porsche Macan
('ORD-2026-04-001', 'Dang Van Gia', '0956789012', 'dangvangia@email.com', 'Hue',
 (SELECT id FROM inventory_cars WHERE vin = 'VIN-POR-MAC-014' LIMIT 1),
 3350000000, 120000000, 40000000, 3430000000,
 1000000000, 3430000000, 0,
 (SELECT user_id FROM users LIMIT 1),
 '2026-04-08', 'COMPLETED', 'FULLY_PAID',
 '2026-04-08 09:30:00', '2026-04-15 11:00:00'),

-- Apr 2: Skoda Kodiaq
('ORD-2026-04-002', 'Bui Thi Hang', '0967890123', 'buithihang@email.com', 'Vung Tau',
 (SELECT id FROM inventory_cars WHERE vin = 'VIN-SKO-KOD-011' LIMIT 1),
 1450000000, 70000000, 15000000, 1505000000,
 400000000, 1505000000, 0,
 (SELECT user_id FROM users ORDER BY user_id DESC LIMIT 1),
 '2026-04-18', 'COMPLETED', 'FULLY_PAID',
 '2026-04-18 14:30:00', '2026-04-23 16:00:00'),

-- Apr 3: Nissan Navara
('ORD-2026-04-003', 'Nguyen Thi Yen', '0978901234', 'nguyenthiyen@email.com', 'Quy Nhon',
 (SELECT id FROM inventory_cars WHERE vin = 'VIN-NIS-NAV-005' LIMIT 1),
 960000000, 55000000, 10000000, 1005000000,
 300000000, 1005000000, 0,
 (SELECT user_id FROM users LIMIT 1),
 '2026-04-25', 'COMPLETED', 'FULLY_PAID',
 '2026-04-25 10:00:00', '2026-04-29 15:00:00');


-- ===== MAY 2026 - 1 order =====
INSERT INTO sales_orders (
    order_number, customer_name, customer_phone, customer_email, customer_address,
    inventory_car_id,
    base_price, additional_fees, discount_amount, total_price,
    deposit_amount, paid_amount, remaining_amount, sales_staff_id,
    order_date, order_status, payment_status,
    created_at, updated_at
)
VALUES 
-- May 1: Hongqi H9
('ORD-2026-05-001', 'Tran Van Khanh', '0989012345', 'tranvankhanh@email.com', 'Bien Hoa',
 (SELECT id FROM inventory_cars WHERE vin = 'VIN-HQ-H9-015' LIMIT 1),
 1508000000, 75000000, 20000000, 1563000000,
 450000000, 1563000000, 0,
 (SELECT user_id FROM users ORDER BY user_id DESC LIMIT 1),
 '2026-05-12', 'COMPLETED', 'FULLY_PAID',
 '2026-05-12 10:00:00', '2026-05-18 15:00:00');


-- ===== JUNE 2026 - 2 orders =====
INSERT INTO sales_orders (
    order_number, customer_name, customer_phone, customer_email, customer_address,
    inventory_car_id,
    base_price, additional_fees, discount_amount, total_price,
    deposit_amount, paid_amount, remaining_amount, sales_staff_id,
    order_date, order_status, payment_status,
    created_at, updated_at
)
VALUES 
-- Jun 1: VW T-Cross
('ORD-2026-06-001', 'Le Thi Lan', '0990123456', 'lethilan@email.com', 'Long Xuyen',
 (SELECT id FROM inventory_cars WHERE vin = 'VIN-VW-TCR-012' LIMIT 1),
 1299000000, 65000000, 15000000, 1349000000,
 400000000, 1349000000, 0,
 (SELECT user_id FROM users LIMIT 1),
 '2026-06-05', 'COMPLETED', 'FULLY_PAID',
 '2026-06-05 09:00:00', '2026-06-11 14:00:00'),

-- Jun 2: Kia K3
('ORD-2026-06-002', 'Phan Van Minh', '0901234568', 'phanvanminh@email.com', 'Vinh',
 (SELECT id FROM inventory_cars WHERE vin = 'VIN-KIA-K3-006' LIMIT 1),
 579000000, 42000000, 7000000, 614000000,
 180000000, 614000000, 0,
 (SELECT user_id FROM users ORDER BY user_id DESC LIMIT 1),
 '2026-06-22', 'COMPLETED', 'FULLY_PAID',
 '2026-06-22 13:00:00', '2026-06-27 16:00:00');


-- =====================================================
-- SUMMARY
-- =====================================================
-- Total: 12 COMPLETED orders
-- Revenue by month (2026):
-- Jan: 2,318,000,000 VND (2 orders)
-- Feb: 4,684,000,000 VND (2 orders)
-- Mar: 2,513,000,000 VND (2 orders)
-- Apr: 5,940,000,000 VND (3 orders - peak)
-- May: 1,563,000,000 VND (1 order)
-- Jun: 1,963,000,000 VND (2 orders)
-- 
-- TOTAL REVENUE: 18,981,000,000 VND (~19 tỷ)
-- Average order value: ~1.58 tỷ VND
-- 
-- Price range:
-- - Lowest: Nissan Almera 604tr
-- - Highest: BMW X5 4.009 tỷ
-- - Mix of affordable (500-700tr) and premium (1.5-4 tỷ)
-- =====================================================
