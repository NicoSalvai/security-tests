package org.nicotest.security;

import lombok.RequiredArgsConstructor;
import org.nicotest.security.filter.AuthenticationFilter;
import org.nicotest.security.filter.AuthorizationFilter;
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

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Value("${secret.key}")
    private String secretKey;
    @Value("${access-token.header}")
    private String accessTokenHeader;
    @Value("${access-token.expiration}")
    private Long accessTokenExpiration;
    @Value("${refresh-token.header}")
    private String refreshTokenHeader;
    @Value("${refresh-token.expiration}")
    private Long refreshTokenExpiration;
    @Value("${claim-name}")
    private String claimName;
    @Value("${login-path}")
    private String loginPath;

    private final UserDetailsService userDetailsService;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService).passwordEncoder(bCryptPasswordEncoder);
    }

    /**
     * Aca vamos a setear para cada enpoint (o matcher de endpoint) que tipo de autenticacion queremos.
     */
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        final AuthenticationFilter authenticationFilter = new AuthenticationFilter(authenticationManager(),
                secretKey,
                accessTokenHeader,
                accessTokenExpiration,
                refreshTokenHeader,
                refreshTokenExpiration,
                claimName);

        authenticationFilter.setFilterProcessesUrl(loginPath);

        final AuthorizationFilter authorizationFilter = new AuthorizationFilter(secretKey, claimName, loginPath);

        http.csrf().disable();
        http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
        http.authorizeRequests().antMatchers("/api/health").permitAll();
        http.authorizeRequests().antMatchers(HttpMethod.GET, "/api/users").hasAnyAuthority("ADMIN");
        http.authorizeRequests().antMatchers(HttpMethod.GET, "/api/users/**").hasAnyAuthority("ADMIN");
        http.addFilter(authenticationFilter);
        http.addFilterBefore(authorizationFilter, UsernamePasswordAuthenticationFilter.class);
    }

    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }
}
