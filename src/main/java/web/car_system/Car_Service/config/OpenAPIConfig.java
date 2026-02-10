package web.car_system.Car_Service.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenAPIConfig {
    
    @Value("${api.url:http://localhost:8080/api/v1}")
    private String apiUrl;
    
    @Bean
    public OpenAPI carServiceOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("Car Service Management API")
                .description("""
                    ## Complete RESTful API for Car Sales Management System
                    
                    ### 🚗 **Features:**
                    - **Car Management** - Inventory, models, and specifications
                    - **Sales Orders** - Complete order lifecycle management
                    - **Payment Processing** - Deposits, final payments, and refunds
                    - **Lead Management** - Customer relationship tracking
                    - **Test Drive Appointments** - Scheduling and management
                    - **Analytics & Reports** - Sales performance and statistics
                    
                    ### 🔐 **Authentication:**
                    Most endpoints require JWT Bearer token authentication.
                    Use the **Authorize** button above to add your token.
                    
                    ### 📊 **Response Format:**
                    All responses follow a standard format with `meta` and `data` fields.
                    """)
                .version("v1.0.0")
                .contact(new Contact()
                    .name("Car Service Development Team")
                    .email("support@carservice.com")
                    .url("https://github.com/yourteam/car-service"))
                .license(new License()
                    .name("Apache 2.0")
                    .url("https://www.apache.org/licenses/LICENSE-2.0.html")))
            .servers(List.of(
                new Server()
                    .url(apiUrl)
                    .description("API Server"),
                new Server()
                    .url("http://localhost:8080/api/v1")
                    .description("Local Development")
            ))
            // Security can use EITHER Cookie OR Bearer token
            .addSecurityItem(new SecurityRequirement()
                .addList("cookieAuth")
                .addList("bearerAuth"))
            .components(new Components()
                // Cookie-based auth (default for web app)
                .addSecuritySchemes("cookieAuth", 
                    new SecurityScheme()
                        .name("accessToken")
                        .type(SecurityScheme.Type.APIKEY)
                        .in(SecurityScheme.In.COOKIE)
                        .description("JWT token stored in HttpOnly cookie. " +
                            "Login via /api/v1/auth/sign-in to get cookie automatically."))
                // Bearer token auth (for Swagger testing)
                .addSecuritySchemes("bearerAuth", 
                    new SecurityScheme()
                        .name("Authorization")
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .description("JWT Bearer token for testing. " +
                            "Format: Bearer <token>. " +
                            "Get token from login response or browser DevTools → Application → Cookies.")));
    }
}
