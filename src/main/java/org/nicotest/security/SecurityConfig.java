package org.nicotest.security;

import lombok.RequiredArgsConstructor;
import org.nicotest.security.filter.AuthenticationFilter;
import org.nicotest.security.filter.AuthorizationFilter;
import org.nicotest.service.specification.ITokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Value("${login-path}")
    private String loginPath;
    @Value("${refresh-path}")
    private String refreshPath;

    @Autowired
    private final ITokenService tokenService;

    private final UserDetailsService userDetailsService;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        final CorsConfiguration corsConfiguration = new CorsConfiguration();
        corsConfiguration.setAllowedOrigins(List.of("*"));
        corsConfiguration.setAllowedMethods(List.of("HEAD", "GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        corsConfiguration.setAllowCredentials(true);
        corsConfiguration.setAllowedHeaders(List.of("Authorization", "Cache-Control", "Content-Type"));
        final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfiguration);
        return source;
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService).passwordEncoder(bCryptPasswordEncoder);
    }

    /**
     * Aca vamos a setear para cada enpoint (o matcher de endpoint) que tipo de autenticacion queremos.
     */
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        final AuthenticationFilter authenticationFilter = new AuthenticationFilter(authenticationManager(), tokenService);

        authenticationFilter.setFilterProcessesUrl(loginPath);

        final AuthorizationFilter authorizationFilter = new AuthorizationFilter(loginPath, refreshPath, tokenService);

        http.csrf().disable();
        http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
        http.authorizeRequests().antMatchers("/api/health", refreshPath, loginPath).permitAll();
        http.authorizeRequests().antMatchers(HttpMethod.GET, "/api/users/**").hasAnyAuthority("ADMIN");
        http.authorizeRequests().antMatchers(HttpMethod.POST, "/api/users/**").hasAnyAuthority("ADMIN");
        http.authorizeRequests().antMatchers(HttpMethod.PUT, "/api/users/**").hasAnyAuthority("ADMIN");
        http.authorizeRequests().antMatchers(HttpMethod.DELETE, "/api/users/**").hasAnyAuthority("ADMIN");
        http.addFilter(authenticationFilter);
        http.addFilterBefore(authorizationFilter, UsernamePasswordAuthenticationFilter.class);
    }

    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }
}
