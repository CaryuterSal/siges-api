package dev.spiffocode.sigesapi.common.infrastructure.config;

import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.responses.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ProblemDetail;

import java.util.Map;

@Slf4j
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

    @Bean
    public OpenApiCustomizer openApiCustomizer() {
        return openApi -> {
            var sharedErrorSchema = ModelConverters.getInstance()
                    .read(ProblemDetail.class).get(ProblemDetail.class.getSimpleName());

            var validationErrorSchema = ModelConverters.getInstance()
                    .read(ValidationProblem.class).get(ValidationProblem.class.getSimpleName());
            if (sharedErrorSchema == null) {
                throw new IllegalStateException("Cannot generate body for 4xx and 5xx responses");
            }

            for (PathItem pathItem : openApi.getPaths().values()) {
                for (Operation operation : pathItem.readOperations()) {
                    for (Map.Entry<String, ApiResponse> entry : operation.getResponses().entrySet()) {
                        var status = entry.getKey();
                        var response = entry.getValue();
                        if (status.startsWith("4") || status.startsWith("5")) {
                            log.debug("Adding default schema for content {}", response.getContent());
                            if(response.getContent() == null) continue;
                            response.getContent().forEach((code, mediaType) -> {
                                if (status.equals("400")) {
                                    mediaType.setSchema(validationErrorSchema);
                                } else {
                                    mediaType.setSchema(sharedErrorSchema);
                                }
                            });
                        }
                    }
                }
            }
        };
    }
}
