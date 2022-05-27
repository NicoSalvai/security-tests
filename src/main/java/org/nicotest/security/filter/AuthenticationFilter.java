package org.nicotest.security.filter;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
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
import java.util.Date;
import java.util.stream.Collectors;

public class AuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private final AuthenticationManager authenticationManager;
    private final String secretKey;
    private final String accessTokenHeader;
    private final Long accessTokenExpiration;
    private final String refreshTokenHeader;
    private final Long refreshTokenExpiration;
    private final String claimName;

    public AuthenticationFilter(AuthenticationManager authenticationManager, String secretKey, String accessTokenHeader,
                                Long accessTokenExpiration, String refreshTokenHeader, Long refreshTokenExpiration,
                                String claimName){
        this.authenticationManager = authenticationManager;
        this.secretKey = secretKey;
        this.accessTokenHeader = accessTokenHeader;
        this.accessTokenExpiration = accessTokenExpiration;
        this.refreshTokenHeader = refreshTokenHeader;
        this.refreshTokenExpiration = refreshTokenExpiration;
        this.claimName = claimName;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        final String username = request.getParameter("username");
        final String password = request.getParameter("password");

        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(username, password);
        return authenticationManager.authenticate(authenticationToken);
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException, ServletException {
        final User user = (User) authResult.getPrincipal();

        final String accessToken = generateToken(user, accessTokenExpiration, request.getRequestURL().toString(),
                claimName,  secretKey);
        final String refreshToken = generateToken(user, refreshTokenExpiration, request.getRequestURL().toString(),
                claimName,  secretKey);
        response.setHeader(accessTokenHeader, accessToken);
        response.setHeader(refreshTokenHeader, refreshToken);

    }

    private String generateToken(User user, Long expiration, String issuer, String claimName, String key){
        return JWT.create()
                .withSubject(user.getUsername())
                .withExpiresAt(new Date(System.currentTimeMillis() + expiration))
                .withIssuer(issuer)
                .withClaim(claimName, user.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList()))
                .sign(Algorithm.HMAC256(key.getBytes()));
    }
}
