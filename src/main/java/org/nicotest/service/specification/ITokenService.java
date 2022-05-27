package org.nicotest.service.specification;

import org.nicotest.model.client.UserDto;

import java.util.List;

public interface ITokenService {
    String generateAccessToken(String username, String issuer, List<String> authorities);

    String generateRefreshToken(String username, String issuer, List<String> authorities);

    String generateToken(String username, Long expiration, String issuer, List<String> authorities);

    UserDto decodeJwtToken(String token);

    String getAccessTokenHeader();

    String getRefreshTokenHeader();

    String getTokenBearerPrefix();
}
