package org.nicotest.security.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.nicotest.service.specification.ITokenService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private final AuthenticationManager authenticationManager;
    private final ITokenService tokenService;

    public AuthenticationFilter(AuthenticationManager authenticationManager, ITokenService tokenService){
        this.authenticationManager = authenticationManager;
        this.tokenService = tokenService;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        final String username = request.getParameter("username");
        final String password = request.getParameter("password");

        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(username, password);
        response.addHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "POST");
        response.setHeader("Access-Control-Allow-Credentials", "true");
        response.setHeader("Access-Control-Allow-Headers",
                "content-type, x-gwt-module-base, x-gwt-permutation, clientid, longpush");

        return authenticationManager.authenticate(authenticationToken);
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException, ServletException {
        final User user = (User) authResult.getPrincipal();
        final List<String> userAuthorities = user.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList());

        final String accessToken = tokenService.generateAccessToken(user.getUsername(), request.getRequestURL().toString(), userAuthorities);
        final String refreshToken = tokenService.generateRefreshToken(user.getUsername(), request.getRequestURL().toString(), userAuthorities);
        response.setContentType("");
        Map<String, Object> userBody = new HashMap<>();
        userBody.put(tokenService.getAccessTokenHeader(), accessToken);
        userBody.put(tokenService.getRefreshTokenHeader(), refreshToken);
        userBody.put("username", user.getUsername());
        userBody.put("roles", user.getAuthorities().stream().map(GrantedAuthority::getAuthority).toArray());
        response.setContentType("");
        new ObjectMapper().writeValue(response.getOutputStream(), userBody);

    }
}
