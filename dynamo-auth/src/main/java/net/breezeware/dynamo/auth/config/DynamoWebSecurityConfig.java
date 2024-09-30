package net.breezeware.dynamo.auth.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import net.breezeware.dynamo.auth.config.properties.DynamoAuthCorsProperties;
import net.breezeware.dynamo.auth.config.properties.DynamoAuthHttpProperties;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class DynamoWebSecurityConfig {

    private final JwtFilter jwtFilter;
    private final AccessDeniedHandler accessDeniedHandler;
    private final AuthenticationEntryPoint authenticationEntryPoint;
    private final DynamoAuthHttpProperties dynamoAuthHttpProperties;
    private final DynamoHttpConfiguration corsConfigurationSource;
    private final DynamoAuthCorsProperties dynamoAuthCorsProperties;

    @Value("${spring.security.oauth2.resourceserver.jwt.jwk-set-uri}")
    private String jwtSetUri;

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        log.debug("Entering configure HttpSecurity");
        http.authorizeHttpRequests(auth -> auth.requestMatchers(dynamoAuthHttpProperties.getAllowedEndpoints())
                .permitAll().anyRequest().authenticated()).oauth2ResourceServer(oauth2 -> {
                    oauth2.authenticationEntryPoint(authenticationEntryPoint).jwt(jwtConfigurer -> {
                        jwtConfigurer.jwkSetUri(jwtSetUri);
                    });
                }).addFilterBefore(jwtFilter, BasicAuthenticationFilter.class)
                .exceptionHandling(httpSecurityExceptionHandlingConfigurer -> {
                    httpSecurityExceptionHandlingConfigurer.accessDeniedHandler(accessDeniedHandler)
                            .authenticationEntryPoint(authenticationEntryPoint);
                }).sessionManagement(httpSecuritySessionManagementConfigurer -> {
                    httpSecuritySessionManagementConfigurer.sessionCreationPolicy(SessionCreationPolicy.STATELESS);
                }).csrf(AbstractHttpConfigurer::disable).headers(httpSecurityHeadersConfigurer -> {
                    httpSecurityHeadersConfigurer.frameOptions(HeadersConfigurer.FrameOptionsConfig::disable);
                }).cors((cors) -> cors.configurationSource(
                        corsConfigurationSource.corsConfigurationSource(dynamoAuthCorsProperties)));
        log.debug("Leaving configure HttpSecurity");
        return http.build();
    }
}
