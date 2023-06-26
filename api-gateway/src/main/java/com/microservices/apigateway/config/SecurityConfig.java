package com.microservices.apigateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.web.server.SecurityWebFilterChain;

import java.util.List;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
    private String issuerUri;
    @Value("${spring.security.oauth2.resourceserver.jwt.aud}")
    private String resourceServerAudience;
    @Value("${spring.security.oauth2.resourceserver.jwt.type}")
    private String jwtType;

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity serverHttpSecurity) {
//        serverHttpSecurity.cors(
////                corsSpec -> corsSpec.configurationSource(
////                        serverWebExchange -> {
////                            var corsConfiguration = new org.springframework.web.cors.CorsConfiguration();
////                            corsConfiguration.addAllowedOrigin("*");
////                            corsConfiguration.addAllowedMethod("*");
////                            corsConfiguration.addAllowedHeader("*");
////                            return corsConfiguration;
////                        }
//                        Customizer.withDefaults()
//                )
        serverHttpSecurity
                .csrf(ServerHttpSecurity.CsrfSpec::disable)                // disable CSRF for Postman testing
                .authorizeExchange(exchange -> exchange
                        .pathMatchers("/eureka/**").permitAll() // permit all requests to /eureka/**
                        .anyExchange().authenticated()                    // authenticate all other requests
                )
                /*
                    Either of the following works too:
                    .oauth2ResourceServer(ServerHttpSecurity.OAuth2ResourceServerSpec::jwt);  // use JWT for OAuth2 resource server
                    .oauth2ResourceServer(oauth2 -> oauth2
                            .jwt(withDefaults()));
                 */
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtDecoder(jwtDecoder())));

        return serverHttpSecurity.build();
    }

    @Bean
    public NimbusReactiveJwtDecoder jwtDecoder() {
        NimbusReactiveJwtDecoder jwtDecoder = (NimbusReactiveJwtDecoder) ReactiveJwtDecoders.fromIssuerLocation(issuerUri);
        OAuth2TokenValidator<Jwt> oAuth2TokenValidator = new DelegatingOAuth2TokenValidator<>(
                new JwtTimestampValidator(),
                new JwtIssuerValidator(issuerUri),
                new JwtClaimValidator<List<String>>("aud",
                        aud -> aud.stream().allMatch(s -> s.equals(resourceServerAudience))),
                new JwtClaimValidator<String>("typ",
                        typ -> typ.equals(jwtType)),
                new JwtClaimValidator<String>("preferred_username",
                        preferred_username -> !preferred_username.isEmpty())
        );
        jwtDecoder.setJwtValidator(oAuth2TokenValidator);

        return jwtDecoder;
    }
}