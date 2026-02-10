# Analytics Mock Data Setup

## Mục đích

File này chứa dữ liệu mẫu để test Dashboard Analytics, đặc biệt là API Monthly Revenue.

## Cách sử dụng

### Cách 1: Import qua MySQL Workbench/phpMyAdmin

1. Mở MySQL Workbench hoặc phpMyAdmin
2. Chọn database của project
3. Chạy nội dung file `mock_analytics_data.sql`

### Cách 2: Command Line

```bash
mysql -u username -p database_name < mock_analytics_data.sql
```

### Cách 3: Spring Boot (Auto-execute)

Nếu muốn auto-run khi start app, đổi tên file thành:

```
src/main/resources/db/migration/V999__mock_analytics_data.sql
```

(Chỉ làm trong môi trường DEV!)

## Dữ liệu được tạo

### Đơn hàng theo tháng (2024):

- **Tháng 1**: 2 đơn - 1,800,000,000 VND
- **Tháng 2**: 1 đơn - 1,200,000,000 VND
- **Tháng 3**: 3 đơn - 2,800,000,000 VND
- **Tháng 4**: 2 đơn - 1,930,000,000 VND
- **Tháng 5**: 1 đơn - 1,250,000,000 VND
- **Tháng 6**: 2 đơn - 2,020,000,000 VND

**Tổng**: 13 đơn hoàn thành - 11,000,000,000 VND

## API Test

Sau khi import, test API:

```
GET /api/v1/admin/analytics/revenue/monthly?year=2024
```

Expected Response:

```json
{
  "data": [
    { "month": 1, "year": 2024, "amount": 1800000000 },
    { "month": 2, "year": 2024, "amount": 1200000000 },
    { "month": 3, "year": 2024, "amount": 2800000000 },
    { "month": 4, "year": 2024, "amount": 1930000000 },
    { "month": 5, "year": 2024, "amount": 1250000000 },
    { "month": 6, "year": 2024, "amount": 2020000000 }
  ]
}
```

## Lưu ý

- Data này chỉ dùng cho **testing/development**
- Nếu production, **XÓA FILE NÀY** trước khi deploy
- Đảm bảo `sales_orders` table đã tồn tại
- Có thể cần adjust `order_number` nếu conflict với data hiện có
