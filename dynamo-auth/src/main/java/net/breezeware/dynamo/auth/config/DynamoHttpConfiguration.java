package net.breezeware.dynamo.auth.config;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.fasterxml.jackson.databind.ObjectMapper;

import net.breezeware.dynamo.auth.config.properties.DynamoAuthCorsProperties;
import net.breezeware.dynamo.utils.exception.ErrorResponse;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
public class DynamoHttpConfiguration {

    @Bean
    public CorsConfigurationSource corsConfigurationSource(DynamoAuthCorsProperties dynamoAuthCorsProperties) {
        log.debug("Entering corsConfigurationSource");
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(dynamoAuthCorsProperties.getAllowedOrigins());
        configuration.setAllowedMethods(dynamoAuthCorsProperties.getAllowedMethods());
        configuration.setAllowedHeaders(dynamoAuthCorsProperties.getAllowedHeaders());
        configuration.setAllowCredentials(true);
        configuration.setExposedHeaders(dynamoAuthCorsProperties.getExposedHeaders());

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        log.debug("Leaving corsConfigurationSource() with Allowed Origins{}", configuration.getAllowedOrigins());
        return source;
    }

    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        return (request, response, accessDeniedException) -> {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            ErrorResponse errorResponse = buildErrorResponse(response, accessDeniedException);
            buildServletOutputStream(response, errorResponse);
        };
    }

    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint() {
        return (request, response, authException) -> {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            ErrorResponse errorResponse = buildErrorResponse(response, authException);
            buildServletOutputStream(response, errorResponse);
        };
    }

    private void buildServletOutputStream(HttpServletResponse response, ErrorResponse errorResponse)
            throws IOException {
        ServletOutputStream out = response.getOutputStream();
        new ObjectMapper().writeValue(out, errorResponse);
        out.flush();
    }

    private ErrorResponse buildErrorResponse(HttpServletResponse httpServletResponse, Exception ex) {
        List<String> details = new ArrayList<>();
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setStatusCode(httpServletResponse.getStatus());
        errorResponse.setMessage(HttpStatus.valueOf(httpServletResponse.getStatus()).name());
        details.add(ex.getLocalizedMessage().split(":")[0]);
        errorResponse.setDetails(details);
        return errorResponse;
    }
}
