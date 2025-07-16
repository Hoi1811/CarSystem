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
        }
        public static final class USER{
            public static final String USER = PREFIX + "/users";
            public static final String ME = USER + "/me";
            public static final String USER_ID = USER + "/{userId}";
            public static final String USER_AUTHORITIES = USER + "/{userId}/authorities";
            public static final String USER_ROLES = USER + "/{userId}/roles";
            public static final String USER_PERMISSIONS = USER + "/{userId}/permissions";
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
    }
}
