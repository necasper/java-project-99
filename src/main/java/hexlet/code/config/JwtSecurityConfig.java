package hexlet.code.config;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.OctetSequenceKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import hexlet.code.service.CustomUserDetailsService;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

@Configuration
public class JwtSecurityConfig {

    private static final int GENERATED_SECRET_BYTES = 32;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    @Autowired
    private Environment environment;

    private String cachedJwtSecret;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public JwtEncoder jwtEncoder() {
        OctetSequenceKey jwk = new OctetSequenceKey.Builder(secretKeyBytes())
                .algorithm(JWSAlgorithm.HS256)
                .build();
        JWKSource<SecurityContext> jwkSource = new ImmutableJWKSet<>(new JWKSet(jwk));
        return new NimbusJwtEncoder(jwkSource);
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        return NimbusJwtDecoder.withSecretKey(secretKey()).build();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            CustomUserDetailsService userDetailsService,
            PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return new ProviderManager(provider);
    }

    private byte[] secretKeyBytes() {
        return getJwtSecret().getBytes(StandardCharsets.UTF_8);
    }

    private SecretKey secretKey() {
        return new SecretKeySpec(secretKeyBytes(), "HmacSHA256");
    }

    private String getJwtSecret() {
        if (cachedJwtSecret != null && !cachedJwtSecret.isBlank()) {
            return cachedJwtSecret;
        }

        var fromProperties = environment.getProperty("jwt.secret");
        if (fromProperties != null && !fromProperties.isBlank()) {
            cachedJwtSecret = fromProperties;
            return cachedJwtSecret;
        }

        var fromEnv = environment.getProperty("JWT_SECRET");
        if (fromEnv != null && !fromEnv.isBlank()) {
            cachedJwtSecret = fromEnv;
            return cachedJwtSecret;
        }

        byte[] randomBytes = new byte[GENERATED_SECRET_BYTES];
        SECURE_RANDOM.nextBytes(randomBytes);
        cachedJwtSecret = Base64.getEncoder().encodeToString(randomBytes);
        return cachedJwtSecret;
    }
}
