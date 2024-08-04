package com.vapps.auth.config;

import java.security.*;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.time.Duration;
import java.util.Base64;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.oauth2.server.resource.OAuth2ResourceServerConfigurer;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.OAuth2TokenFormat;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.AuthorizationFilter;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import com.vapps.auth.dto.UserDTO;
import com.vapps.auth.exception.AppException;
import com.vapps.auth.service.AppUserService;
import com.vapps.auth.service.CustomOAuth2UserService;

import jakarta.servlet.Filter;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.web.util.matcher.RequestMatcher;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
public class OAuthConfig {

    @Autowired
    private CustomOAuth2UserService userService;

    @Autowired
    private AppUserService appUserService;

    @Value("${spring.security.oauth2.authorizationserver.issuer}")
    private String issuer;

    @Value("${app.security.privateKey}")
    private String rsaPrivateKey;

    @Value("${app.security.publicKey}")
    private String rsaPublicKey;

    @Value("${app.security.keyId}")
    private String rsaKeyId;

    private final static Logger LOGGER = LoggerFactory.getLogger(OAuthConfig.class);

    @Bean
    @Order(1)
    SecurityFilterChain asSecurityFilterChain(HttpSecurity http) throws Exception {

        OAuth2AuthorizationServerConfigurer authorizationServerConfigurer = new OAuth2AuthorizationServerConfigurer();
        authorizationServerConfigurer.authorizationEndpoint(
                authorizationEndpoint -> authorizationEndpoint.consentPage("/oauth/consent"));
        RequestMatcher endpointsMatcher = authorizationServerConfigurer.getEndpointsMatcher();

        http.securityMatcher(endpointsMatcher)
                .authorizeHttpRequests(authorize -> authorize.anyRequest().authenticated())
                .csrf(csrf -> csrf.ignoringRequestMatchers(endpointsMatcher)).apply(authorizationServerConfigurer);

        return http.getConfigurer(OAuth2AuthorizationServerConfigurer.class).oidc(withDefaults()).and()
                .exceptionHandling(e -> e.authenticationEntryPoint(new LoginUrlAuthenticationEntryPoint("/oauth")))
                .oauth2ResourceServer(OAuth2ResourceServerConfigurer::jwt).build();

    }

    @Bean
    @Order(2)
    SecurityFilterChain appSecurityFilterChain(HttpSecurity http) throws Exception {
        return http.authorizeHttpRequests(authorize -> {
                    try {
                        authorize.requestMatchers("/index.html", "/welcome", "/static/**", "/*.ico", "/*.json",
                                        "/*" + ".png",
                                        "/*.jpg", "/*jpeg", "/*.html", "/authenticate", "/*.js.map").permitAll()
                                .requestMatchers("/test").permitAll().requestMatchers(HttpMethod.POST, "/api/user").permitAll()
                                .requestMatchers(HttpMethod.OPTIONS, "/api/user").permitAll()
                                .requestMatchers(HttpMethod.GET, "/api/user/{userId}/profile-image").permitAll().anyRequest()
                                .authenticated().and().formLogin().loginPage("/oauth").loginProcessingUrl(
                                        "/authenticate")
                                .defaultSuccessUrl("/welcome").usernameParameter("name").passwordParameter("password")
                                .permitAll().and().csrf().disable()
                                .logout(l -> l.clearAuthentication(true).deleteCookies().invalidateHttpSession(true)
                                        .deleteCookies().logoutSuccessUrl(issuer + "/welcome"));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }).oauth2Login().loginPage("/oauth").userInfoEndpoint().userService(userService).and().and().csrf().disable()
                .addFilterAfter(staticResourceFilter(), AuthorizationFilter.class).build();

    }

    /**
     * For client side react routing.
     */
    @Bean
    Filter staticResourceFilter() {
        return (request, response, chain) -> {
            String path = ((HttpServletRequest) request).getRequestURI();

            boolean isApi = path.startsWith("/api");
            boolean isStaticResource = path.matches(".*\\.(js|css|ico|png|jpg|jpeg|html)");

            if (isApi || isStaticResource) {
                chain.doFilter(request, response);
            } else {
                request.getRequestDispatcher("/index.html").forward(request, response);
            }
        };
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public TokenSettings tokenSettings() {
        return TokenSettings.builder().accessTokenFormat(OAuth2TokenFormat.SELF_CONTAINED)
                .accessTokenTimeToLive(Duration.ofDays(1)).build();
    }

    @Bean
    public ClientSettings clientSettings() {
        return ClientSettings.builder().requireProofKey(false).requireAuthorizationConsent(true).build();
    }

    @Bean
    public AuthorizationServerSettings authorizationServerSettings() {
        LOGGER.info("Issuer {}", issuer);
        return AuthorizationServerSettings.builder().issuer(issuer).build();
    }

    @Bean
    JwtDecoder jwtDecoder(JWKSource<SecurityContext> jwkSource) {
        return OAuth2AuthorizationServerConfiguration.jwtDecoder(jwkSource);
    }

    @Bean
    JWKSource<SecurityContext> jwkSource() {
        RSAKey rsaKey = generateRsa();
        JWKSet jwkSet = new JWKSet(rsaKey);
        return (jwkSelector, securityContext) -> jwkSelector.select(jwkSet);
    }

    public RSAKey generateRsa() {
        try {
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");

            byte[] publicKeyBytes = Base64.getDecoder().decode(rsaPublicKey);
            X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(publicKeyBytes);
            PublicKey publicKey = keyFactory.generatePublic(publicKeySpec);

            byte[] privateKeyBytes = Base64.getDecoder().decode(rsaPrivateKey);
            PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
            PrivateKey privateKey = keyFactory.generatePrivate(privateKeySpec);

            return new RSAKey.Builder((RSAPublicKey) publicKey).privateKey((RSAPrivateKey) privateKey).keyID(rsaKeyId)
                    .build();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate RSA Key", e);
        }
    }

    @Bean
    OAuth2TokenCustomizer<JwtEncodingContext> tokenCustomizer() {
        return context -> {
            Authentication principal = context.getPrincipal();
            try {
                UserDTO userDTO = appUserService.findByUserId(principal.getName());
                context.getClaims().claim("name", userDTO.getUserName());
                context.getClaims().claim("email", userDTO.getEmail());
                context.getClaims().claim("id", userDTO.getId());
                context.getClaims().claim("image", userDTO.getProfileImage());
            } catch (AppException e) {
                LOGGER.error(e.getMessage(), e);
            }
            if (context.getTokenType().getValue().equals("access_token")) {
                Set<String> authorities = principal.getAuthorities().stream().map(GrantedAuthority::getAuthority)
                        .collect(Collectors.toSet());
                context.getClaims().claim("authorities", authorities).claim("user", principal.getName());
            }
        };
    }
}