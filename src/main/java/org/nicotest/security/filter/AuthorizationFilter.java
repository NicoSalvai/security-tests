package org.nicotest.security.filter;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
public class AuthorizationFilter extends OncePerRequestFilter {

    private static final String BEARER = "Bearer ";
    private final String secretKey;
    private final String claimName;
    private final String loginPath;

    public AuthorizationFilter(String secretKey, String claimName, String loginPath) {
        this.loginPath = loginPath;
        this.claimName = claimName;
        this.secretKey = secretKey;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // No valido authorizacion en requests que vayan hacia el login
        if (request.getServletPath().equals(loginPath)) {
            filterChain.doFilter(request, response);
        } else {
            String authorizationHeader = request.getHeader(AUTHORIZATION);

            if(Objects.nonNull(authorizationHeader) && authorizationHeader.startsWith(BEARER)){
                try {
                    String token = authorizationHeader.substring(BEARER.length());
                    JWTVerifier jwtVerifier = JWT.require(Algorithm.HMAC256(secretKey.getBytes())).build();
                    DecodedJWT decodedJWT = jwtVerifier.verify(token);

                    String username = decodedJWT.getSubject();

                    String[] roles = decodedJWT.getClaim(claimName).asArray(String.class);
                    Collection<SimpleGrantedAuthority> authorities = Stream.of(roles)
                            .map(SimpleGrantedAuthority::new)
                            .collect(Collectors.toList());

                    UsernamePasswordAuthenticationToken authenticationToken =
                            new UsernamePasswordAuthenticationToken(username, null, authorities);
                    SecurityContextHolder.getContext().setAuthentication(authenticationToken);

                    filterChain.doFilter(request, response);
                } catch (Exception ex){
                    log.error("Error durante el login: ", ex);

                    Map<String, String> errors = new HashMap<>();
                    errors.put("errors", ex.getMessage());
                    response.setStatus(HttpStatus.FORBIDDEN.value());
                    response.setContentType(APPLICATION_JSON_VALUE);
                    new ObjectMapper().writeValue(response.getOutputStream(), errors);
                }
            } else {
                filterChain.doFilter(request, response);
            }
        }
    }
}
