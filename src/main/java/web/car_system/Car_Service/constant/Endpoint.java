package web.car_system.Car_Service.constant;

public class Endpoint {
    public static final class V1{
        public static final String PREFIX = "/api/v1";
        public static final class AUTH{
            public static final String PREFIX = V1.PREFIX + "/auth";
            public static final String REGISTER = PREFIX + "/register";
            public static final String LOGIN  = PREFIX + "/login";
            public static final String LOGOUT = PREFIX + "/logout";
            public static final String AUTHORIZE = PREFIX + "/oauth2/authorize";
            public static final String OAUTH2CALLBACK = PREFIX + "/oauth2/callback";
            public static final String REFRESH_TOKEN = PREFIX + "/refresh-token";
            public static final String VALIDATE_ADMIN = PREFIX + "/validate-admin";
            public static final String FORGOT_PASSWORD = PREFIX + "/forgot-password";
            public static final String RESET_PASSWORD = PREFIX + "/reset-password";
            public static final String GOOGLE_ONE_TAP = PREFIX + "/google/one-tap";
        }

        public static final class CAR{
            public static final String CAR_NOT_FOUND = "CAR_NOT_FOUND";
            // Car endpoints
            public static final String CAR = PREFIX + "/cars";
            public static final String CAR_ID = CAR + "/{id}";
            public static final String CAR_IMPORT = CAR + "/import";
            public static final String CAR_IMPORT_BATCH = CAR + "/import/batch";
            public static final String CAR_V2 = CAR + "/v2";
            public static final String CAR_V3 = CAR + "/v3";
            public static final String CAR_PAGINATED = CAR + "/paginated";
            public static final String CAR_ID_THUMBNAIL = CAR + "/{carId}/thumbnail";
            public static final String CAR_ID_IMAGES = CAR + "/{carId}/images";
            public static final String CAR_SPECIFICATIONS = CAR + "/specifications";
            public static final String CAR_SPECIFICATION_ID = CAR_SPECIFICATIONS + "/{id}";
            public static final String CAR_SPECIFICATION_ATTRIBUTES = CAR_SPECIFICATIONS + "/{specId}/attributes";
            public static final String CAR_ATTRIBUTE_ID = CAR + "/attributes/{id}";
            public static final String CAR_ID_NOT_FOUND = CAR + "/{carId}/not-found";
            public static final String CAR_ID_NOT_FOUND_MESSAGE = "Car with ID %s not found.";
            public static final String CAR_ID_NOT_FOUND_CODE = "CAR_NOT_FOUND";
            public static final String FIND_RELATED_CARS_BY_NAME = CAR + "/related-cars";
            public static final String FIND_RELATED_MODELS_BY_NAME = CAR + "/related-models";
            public static final String FIND_RELATED_CAR_NAMES_BY_NAME = CAR + "/related-car-names";
            public static final String COMPARE_CARS = CAR + "/compare-cars";
            public static final String CAR_ID_SUGGESTIONS = CAR_ID + "/suggestions";
            // Car Segment endpoints
            public static final String CAR_SEGMENT = PREFIX + "/car-segments";
            public static final String CAR_SEGMENT_ID = CAR_SEGMENT + "/{segmentId}";
            public static final String CAR_SEGMENT_BATCH = CAR_SEGMENT + "/batch";
            public static final String CAR_SEGMENT_BY_GROUP = CAR_SEGMENT + "/by-group/{groupId}";

            // Car Segment Group endpoints
            public static final String CAR_SEGMENT_GROUP = PREFIX + "/car-segment-groups";
            public static final String CAR_SEGMENT_GROUP_ID = CAR_SEGMENT_GROUP + "/{id}";

            // Manufacturer endpoints
            public static final String MANUFACTURER = PREFIX + "/manufacturers";
            public static final String MANUFACTURER_ID = MANUFACTURER + "/{id}";

            // Car Type endpoints
            public static final String CAR_TYPE = PREFIX + "/car-types";
            public static final String CAR_TYPE_ID = CAR_TYPE + "/{typeId}";

            // Specification endpoints
            public static final String SPECIFICATIONS = PREFIX + "/specifications";
            public static final String SPECIFICATION_ATTRIBUTES = SPECIFICATIONS + "/attributes";
            public static final String SPECIFICATIONS_SCHEMA = SPECIFICATIONS + "/schema";

            public static final String CHANGE_CAR_STATUS = CAR + "/{carId}/status";

            public static final String CALCULATE_ROLLING_COST = CAR_ID + "/calculate-rolling-cost"; // POST /api/v1/cars/{id}/calculate-rolling-cost
        }
        public static final class ATTRIBUTE {
            public static final String ATTRIBUTE_PREFIX = PREFIX + "/attributes";

            public static final String CREATE_ATTRIBUTE = ATTRIBUTE_PREFIX;                 // POST /api/v1/attributes
            public static final String GET_ALL_ATTRIBUTES = ATTRIBUTE_PREFIX;                 // GET /api/v1/attributes
            public static final String GET_ATTRIBUTE_BY_ID = ATTRIBUTE_PREFIX + "/{id}";    // GET /api/v1/attributes/{id}
            public static final String UPDATE_ATTRIBUTE = ATTRIBUTE_PREFIX + "/{id}";    // PUT /api/v1/attributes/{id}
            public static final String DELETE_ATTRIBUTE = ATTRIBUTE_PREFIX + "/{id}";    // DELETE /api/v1/attributes/{id}

            public static final String ATTRIBUTE_OPTIONS_PREFIX = GET_ATTRIBUTE_BY_ID + "/options"; // /api/v1/attributes/{id}/options

            public static final String ADD_OPTION_TO_ATTRIBUTE = ATTRIBUTE_OPTIONS_PREFIX;              // POST .../{id}/options
            public static final String GET_OPTIONS_FOR_ATTRIBUTE = ATTRIBUTE_OPTIONS_PREFIX;              // GET .../{id}/options
            public static final String UPDATE_OPTION_FOR_ATTRIBUTE = ATTRIBUTE_OPTIONS_PREFIX + "/{valueKey}"; // PUT .../{id}/options/{valueKey}
            public static final String DELETE_OPTION_FROM_ATTRIBUTE = ATTRIBUTE_OPTIONS_PREFIX + "/{valueKey}";// DELETE .../{id}/options/{valueKey}
            public static final String SAVE_ALL_OPTIONS = ATTRIBUTE_OPTIONS_PREFIX; // PUT .../{id}/options/batch
        }
        public static final class USER{
            public static final String USER = PREFIX + "/users";
            public static final String ME = USER + "/me";
            public static final String ME_AVATAR = ME + "/avatar";
            public static final String ME_PASSWORD = ME + "/password";
            public static final String USER_ID = USER + "/{userId}";
            public static final String USER_AUTHORITIES = USER + "/{userId}/authorities";
            public static final String USER_ROLES = USER + "/{userId}/roles";
            public static final String USER_PERMISSIONS = USER + "/{userId}/permissions";
            // Admin-only user management endpoints
            public static final String ADMIN_CREATE = USER + "/admin-create";
            public static final String ADMIN_RESET_PASSWORD = USER + "/{userId}/reset-password";
            public static final String ADMIN_TOGGLE_STATUS = USER + "/{userId}/status";
        }
        public static final class CHATBOT{
            public static final String PREFIX = V1.PREFIX + "/chatbot";
            public static final String CHAT = PREFIX + "/query";
        }
        public static final class ROLE {
            public static final String ROLE = PREFIX + "/roles";
            public static final String ROLE_ID = ROLE + "/{roleId}";
            public static final String ROLE_PERMISSIONS = ROLE_ID + "/permissions";
        }
        public static final class PERMISSION {
            public static final String PERMISSION = PREFIX + "/permissions";
            public static final String PERMISSION_ID = PERMISSION + "/{permissionId}";
        }

        public static final class AUDIT {
            public static final String AUDIT = PREFIX + "/audits";
            public static final String CAR_AUDIT_HISTORY = AUDIT + "/cars/{id}";
            public static final String CAR_AT_REVISION = AUDIT + "/cars/{id}/revision/{revisionNumber}";

        }

        public static final class OPTIONS {
            // Options endpoints
            public static final String OPTIONS_PREFIX = PREFIX + "/options";
            public static final String OPTIONS_BY_SOURCE_NAME = OPTIONS_PREFIX + "/{sourceName}";
        }
        public static final class COMPARISON_RULE {
            private static final String RULE_PREFIX = PREFIX + "/comparison-rules";
            public static final String GET_ALL = RULE_PREFIX; // GET /api/v1/comparison-rules
        }
        public static final class UTIL {
            private static final String UTIL_PREFIX = PREFIX + "/utils";
            public static final String GET_CONTROL_TYPES =  UTIL_PREFIX + "/control-types";
        }
        public static final class INVENTORY_CAR {
            // === PUBLIC ENDPOINTS (Dành cho Khách hàng) ===
            public static final String PREFIX = V1.PREFIX + "/inventory-cars";
            public static final String GET_ALL_AVAILABLE = PREFIX;              // GET /api/v1/inventory-cars
            public static final String GET_DETAILS_BY_ID = PREFIX + "/{id}";    // GET /api/v1/inventory-cars/{id}

            // === ADMIN ENDPOINTS (Dành cho Quản trị viên) ===
            public static final String ADMIN_PREFIX = V1.PREFIX + "/admin/inventory-cars";
            public static final String ADD_TO_INVENTORY = ADMIN_PREFIX;         // POST /api/v1/admin/inventory-cars
            public static final String GET_ALL_FOR_ADMIN = ADMIN_PREFIX;        // GET /api/v1/admin/inventory-cars
            public static final String UPDATE = ADMIN_PREFIX + "/{id}";         // PUT /api/v1/admin/inventory-cars/{id}
            public static final String DELETE = ADMIN_PREFIX + "/{id}";         // DELETE /api/v1/admin/inventory-cars/{id}
            public static final String UPDATE_STATUS = ADMIN_PREFIX + "/{id}/status"; // PATCH /api/v1/admin/inventory-cars/{id}/status

        }
        public static final class TEST_DRIVE {
            // === PUBLIC ENDPOINT (Dành cho Khách hàng) ===
            public static final String SUBMIT_APPOINTMENT = V1.PREFIX + "/test-drive-appointments"; // POST

            // === ADMIN ENDPOINTS (Dành cho Quản trị viên) ===
            public static final String ADMIN_PREFIX = V1.PREFIX + "/admin/test-drive-appointments";
            public static final String GET_ALL = ADMIN_PREFIX;                 // GET (có phân trang)
            public static final String SEARCH = ADMIN_PREFIX + "/search";      // GET (tìm kiếm với filter)
            public static final String GET_BY_ID = ADMIN_PREFIX + "/{id}";    // GET
            public static final String UPDATE = ADMIN_PREFIX + "/{id}";       // PUT (để xác nhận, gán việc, ghi chú)
            public static final String DELETE = ADMIN_PREFIX + "/{id}";       // DELETE
        }

        public static final class LEAD {
            // === PUBLIC ENDPOINT (Dành cho Khách hàng) ===
            public static final String SUBMIT_LEAD = V1.PREFIX + "/leads"; // POST

            // === ADMIN ENDPOINTS (Dành cho Quản trị viên) ===
            public static final String ADMIN_PREFIX = V1.PREFIX + "/admin/leads";
            public static final String GET_ALL = ADMIN_PREFIX;                 // GET (có phân trang)
            public static final String SEARCH = ADMIN_PREFIX + "/search";      // GET (tìm kiếm với filter)
            public static final String GET_BY_ID = ADMIN_PREFIX + "/{id}";    // GET
            public static final String UPDATE = ADMIN_PREFIX + "/{id}";       // PUT (để gán việc, cập nhật trạng thái)
            public static final String DELETE = ADMIN_PREFIX + "/{id}";       // DELETE
        }
        public static final class RECOMMENDATION {
            // === PUBLIC ENDPOINT (Dành cho Khách hàng) ===
            // Khách hàng sẽ gửi câu trả lời của họ đến endpoint này
            public static final String GET_SUGGESTIONS = V1.PREFIX + "/recommendations/suggest"; // POST

            // === ADMIN ENDPOINTS (Dành cho Quản trị viên) ===
            public static final String ADMIN_PREFIX = V1.PREFIX + "/admin/recommendation-rules";
            public static final String GET_ALL_RULES = ADMIN_PREFIX;          // GET
            public static final String GET_RULE_BY_ID = ADMIN_PREFIX + "/{id}"; // GET
            public static final String CREATE_RULE = ADMIN_PREFIX;            // POST
            public static final String UPDATE_RULE = ADMIN_PREFIX + "/{id}";    // PUT
            public static final String DELETE_RULE = ADMIN_PREFIX + "/{id}";    // DELETE
        }
        // 2. Endpoint CRUD cho Admin quản lý phí
        public static final class REGIONAL_FEE {
            public static final String ADMIN_PREFIX = V1.PREFIX + "/admin/regional-fees";
            public static final String PUBLIC_PREFIX = V1.PREFIX + "/regional-fees";
            public static final String GET_ALL_PUBLIC = PUBLIC_PREFIX;          // GET
            public static final String GET_ALL = ADMIN_PREFIX;          // GET
            public static final String CREATE = ADMIN_PREFIX;             // POST
            public static final String UPDATE = ADMIN_PREFIX + "/{id}";     // PUT
            public static final String DELETE = ADMIN_PREFIX + "/{id}";     // DELETE
        }
        
        // Sales Order Management
        public static final class SALES_ORDER {
            public static final String ADMIN_PREFIX = V1.PREFIX + "/admin/sales-orders";
            public static final String PUBLIC_PREFIX = V1.PREFIX + "/orders";
            
            // Admin endpoints
            public static final String CREATE_ORDER = ADMIN_PREFIX;                 // POST
            public static final String GET_ALL_ORDERS = ADMIN_PREFIX;               // GET
            public static final String GET_ORDER_BY_ID = ADMIN_PREFIX + "/{id}";    // GET
            public static final String UPDATE_ORDER = ADMIN_PREFIX + "/{id}";       // PUT
            public static final String DELETE_ORDER = ADMIN_PREFIX + "/{id}";       // DELETE
            public static final String CANCEL_ORDER = ADMIN_PREFIX + "/{id}/cancel"; // POST
            public static final String UPDATE_STATUS = ADMIN_PREFIX + "/{id}/status"; // PATCH
            
            // Public endpoint
            public static final String TRACK_ORDER = PUBLIC_PREFIX + "/track";      // GET
        }
        
        // Analytics & Business Intelligence
        public static final class ANALYTICS {
            public static final String ADMIN_PREFIX = V1.PREFIX + "/admin/analytics";
            
            // Dashboard endpoints
            public static final String DASHBOARD = ADMIN_PREFIX + "/dashboard";                     // GET
            public static final String MONTHLY_REVENUE = ADMIN_PREFIX + "/revenue/monthly";         // GET
            public static final String STATUS_DISTRIBUTION = ADMIN_PREFIX + "/orders/status-distribution"; // GET
            public static final String SYSTEM_HEALTH = ADMIN_PREFIX + "/system/health";             // GET
        }
    }
}

