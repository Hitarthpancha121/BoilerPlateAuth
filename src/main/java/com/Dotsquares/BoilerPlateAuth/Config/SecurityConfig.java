package com.Dotsquares.BoilerPlateAuth.Config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;


@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    @Autowired
    private AuthEntryPointJwt authEntryPointJwt;

    @Value("${security.oauth2.enabled:false}") // Fetch value from application.properties
    private boolean isOAuth2Enabled;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        if (isOAuth2Enabled) {
            http
                    // Authorize requests
                    .authorizeHttpRequests(authorizeRequests ->
                            authorizeRequests
                                    .requestMatchers("/", "/login").permitAll()
                                    .anyRequest().authenticated()
                    )
                    .logout(logout -> logout
                            .logoutUrl("/logout") // Ensure the logout endpoint is mapped
                            .logoutSuccessUrl("/login?logout") // Redirect after logout
                            .invalidateHttpSession(true)
                            .deleteCookies("JSESSIONID")
                    )
                    // Configure OAuth2 login
                    .oauth2Login(oauth2Login ->
                            oauth2Login
                                    .loginPage("/login")
                                    .defaultSuccessUrl("/home", true)
                    );
            return http.build();
        } else {
            http
                    .csrf(csrf -> csrf.disable())
                    .exceptionHandling(exception -> exception.authenticationEntryPoint(authEntryPointJwt))
                    .authorizeHttpRequests(auth -> auth
                                    .requestMatchers("/auth/register", "/auth/login").permitAll() // Allow registration & login
                                    .anyRequest().authenticated()
                            // Secure all other endpoints
                    )
                    .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                    .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

            return http.build();
        }
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }
}