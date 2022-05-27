package org.nicotest.security.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.nicotest.model.client.UserDto;
import org.nicotest.service.specification.ITokenService;
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
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
public class AuthorizationFilter extends OncePerRequestFilter {

    private static final String BEARER = "Bearer ";
    private final String loginPath;
    private final String refreshPath;
    private final ITokenService tokenService;

    public AuthorizationFilter(String loginPath, String refreshPath, ITokenService tokenService) {
        this.loginPath = loginPath;
        this.refreshPath = refreshPath;
        this.tokenService = tokenService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // No valido authorizacion en requests que vayan hacia el login
        if (request.getServletPath().equals(loginPath) || request.getServletPath().equals(refreshPath)) {
            filterChain.doFilter(request, response);
        } else {
            String authorizationHeader = request.getHeader(AUTHORIZATION);
            if(Objects.nonNull(authorizationHeader) && authorizationHeader.startsWith(BEARER)){
                try {
                    String token = authorizationHeader.substring(BEARER.length());
                    final UserDto userDto = tokenService.decodeJwtToken(token);

                    UsernamePasswordAuthenticationToken authenticationToken =
                            new UsernamePasswordAuthenticationToken(userDto.getUsername(),
                                    null,
                                    userDto.getRoles().stream()
                                            .map(SimpleGrantedAuthority::new)
                                            .collect(Collectors.toList()));
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
