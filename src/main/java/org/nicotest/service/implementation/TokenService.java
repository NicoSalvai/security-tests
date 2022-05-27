package org.nicotest.service.implementation;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.nicotest.model.client.UserDto;
import org.nicotest.service.specification.ITokenService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class TokenService implements ITokenService {

    private static final String BEARER = "Bearer ";
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

    @Override
    public String generateAccessToken(String username, String issuer, List<String> authorities) {
        return generateToken(username, accessTokenExpiration, issuer, authorities);
    }

    @Override
    public String generateRefreshToken(String username, String issuer, List<String> authorities) {
        return generateToken(username, refreshTokenExpiration, issuer, authorities);
    }

    @Override
    public String generateToken(String username, Long expiration, String issuer, List<String> authorities){
        return JWT.create()
                .withSubject(username)
                .withExpiresAt(new Date(System.currentTimeMillis() + expiration))
                .withIssuer(issuer)
                .withClaim(claimName, authorities)
                .sign(Algorithm.HMAC256(secretKey.getBytes()));
    }

    @Override
    public UserDto decodeJwtToken(String token){
        final UserDto userDto = new UserDto();
        JWTVerifier jwtVerifier = JWT.require(Algorithm.HMAC256(secretKey.getBytes())).build();
        DecodedJWT decodedJWT = jwtVerifier.verify(token);

        String username = decodedJWT.getSubject();
        String[] roles = decodedJWT.getClaim(claimName).asArray(String.class);

        userDto.setUsername(username);
        userDto.setRoles(List.of(roles));

        return userDto;
    }

    @Override
    public String getAccessTokenHeader() {
        return accessTokenHeader;
    }

    @Override
    public String getRefreshTokenHeader() {
        return refreshTokenHeader;
    }

    @Override
    public String getTokenBearerPrefix() {
        return BEARER;
    }
}
