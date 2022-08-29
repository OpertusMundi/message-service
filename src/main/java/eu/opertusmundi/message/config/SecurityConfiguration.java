package eu.opertusmundi.message.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import eu.opertusmundi.message.security.JwtAuthorizationFilter;

@Configuration
@EnableGlobalMethodSecurity(securedEnabled = true)
@EnableWebSecurity
public class SecurityConfiguration {

    @Value("${springdoc.api-docs.path}")
    private String openApiSpec;

    @Value("${opertus-mundi.security.jwt.secret}")
    private String jwtSecret;

    @Bean
    public AuthenticationManager authenticationManager() {
        return (Authentication authentication) -> {
            throw new BadCredentialsException("Bad credentials");
        };
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        // Configure request authentication
        http.authorizeRequests()
            // Public
            .antMatchers(
                // Public resources
                "/",
                "/error/**",
                "/assets/**",
                // Swagger UI
                "/swagger-ui/**",
                // ReDoc Open API documentation viewer
                "/docs",
                // Open API specification
                this.openApiSpec,
                this.openApiSpec + "/**"
             ).permitAll()
            // Permit access to actuator (consider restricting accessible details)
            .requestMatchers(EndpointRequest.toAnyEndpoint()).permitAll()
            // Secure any other path
            .anyRequest().authenticated();

        http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);

        http.csrf().disable();

        // Disable basic authentication
        http.httpBasic().disable();

        // Support JWT authentication
        http.addFilterAfter(new JwtAuthorizationFilter(this.authenticationManager(), this.jwtSecret), BasicAuthenticationFilter.class);

        return http.build();
    }
}
