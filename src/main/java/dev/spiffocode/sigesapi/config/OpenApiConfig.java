package dev.spiffocode.sigesapi.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@OpenAPIDefinition(
        info = @Info(
                title = "SIGES API",
                version = "1.0",
                contact = @Contact(
                        name = "Carlos Emanuel Salgado Trujillo",
                        email = "20243ds158@utez.edu.mx"
                )
        ),
        security = {
                @SecurityRequirement(name = "jwt")
        },
        servers = {
                @Server(
                        description = "Render default and only production server",
                        url = "https://siges-api-8o8u.onrender.com/api"
                ),
                @Server(
                        description = "Localhost test server",
                        url = "localhost:8080/api"
                )
        }
)
@SecurityScheme(
        type = SecuritySchemeType.APIKEY,
        name = "jwt",
        description = "Bearer JWT Token, only auth method",
        scheme = "bearer",
        bearerFormat = "JWT",
        in = SecuritySchemeIn.HEADER,
        paramName = "Authentication"
)
@Configuration
public class OpenApiConfig {
}
