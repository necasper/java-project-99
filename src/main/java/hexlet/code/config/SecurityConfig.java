package hexlet.code.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtDecoder jwtDecoder;

    public SecurityConfig(JwtDecoder jwtDecoder) {
        this.jwtDecoder = jwtDecoder;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .cors(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.GET, "/welcome").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/login").permitAll()
                        .requestMatchers("/h2-console/**").permitAll()
                        .requestMatchers("/docs/**", "/v3/api-docs/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/", "/index.html", "/assets/**").permitAll()
                        .requestMatchers(HttpMethod.GET,
                                "/{path:^(?!api|h2-console|welcome|assets).*}/**").permitAll()
                        .anyRequest().authenticated())
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.decoder(jwtDecoder)))
                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) ->
                                response.sendError(401))
                        .accessDeniedHandler((request, response, accessDeniedException) ->
                                response.sendError(403)))
                .build();
    }
}
