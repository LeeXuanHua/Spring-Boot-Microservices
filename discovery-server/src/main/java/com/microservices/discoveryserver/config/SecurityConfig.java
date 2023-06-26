//TODO: Add basic authentication for Eureka server & link to Eureka clients

//package com.microservices.discoveryserver.config;
//
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.security.config.Customizer;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
//import org.springframework.security.core.userdetails.User;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.security.provisioning.InMemoryUserDetailsManager;
//import org.springframework.security.web.SecurityFilterChain;
//
//@Configuration
//public class SecurityConfig {
//    // Spring Boot blog for WebSecurityConfigurerAdapter replacement:
//    // https://spring.io/blog/2022/02/21/spring-security-without-the-websecurityconfigureradapter
//
//    @Value("${eureka.username}")
//    private String username;
//    @Value("${eureka.password}")
//    private String password;
//
//    @Bean
//    public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
//        httpSecurity.csrf(AbstractHttpConfigurer::disable)
//                .authorizeHttpRequests(authorizeHttpRequest -> authorizeHttpRequest
//                        .anyRequest().authenticated()
//                )
//                .httpBasic(Customizer.withDefaults());
//        return httpSecurity.build();
//    }
//
//    @Bean
//    public InMemoryUserDetailsManager userDetailsService() {
//        // Must specify password encoder (can be null, e.g. {noop}), otherwise basic authentication will always fail (even if correct credentials)
//        // https://stackoverflow.com/questions/68178271/cant-sign-in-using-basic-auth-in-spring-boot-security
//        // PasswordEncoder encoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
//        // String encodedPassword = encoder.encode(password);
//
//        UserDetails user = User
//                .withUsername(username)
//                .password("{noop}"+password)
////                .passwordEncoder(encoder::encode)
////                .password(encodedPassword)
////                .roles("USER")
//                .authorities("USER")
//                .build();
//        return new InMemoryUserDetailsManager(user);
//    }
//}