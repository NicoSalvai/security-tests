package org.nicotest.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.nicotest.model.client.UserDto;
import org.nicotest.service.specification.ITokenService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class TokenRefreshController {

    @NonNull
    private ITokenService tokenService;

    @GetMapping("/auth/refresh")
    public void refreshToken(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String authorizationHeader = request.getHeader(AUTHORIZATION);
        if(Objects.nonNull(authorizationHeader) && authorizationHeader.startsWith(tokenService.getTokenBearerPrefix())){
            try {
                String refreshToken = authorizationHeader.substring(tokenService.getTokenBearerPrefix().length());
                final UserDto userDto = tokenService.decodeJwtToken(refreshToken);

                String accessToken = tokenService.generateAccessToken(userDto.getUsername(),
                        request.getRequestURL().toString(), userDto.getRoles());
                response.setHeader(tokenService.getAccessTokenHeader(), accessToken);
                response.setHeader(tokenService.getRefreshTokenHeader(), refreshToken);
            } catch (Exception ex){
                log.error("Error durante el refresco del token: ", ex);

                Map<String, String> errors = new HashMap<>();
                errors.put("errors", ex.getMessage());
                response.setStatus(HttpStatus.FORBIDDEN.value());
                response.setContentType(APPLICATION_JSON_VALUE);
                new ObjectMapper().writeValue(response.getOutputStream(), errors);
            }
        } else {
            throw new RuntimeException("Refresh token is missing from headers ");
        }
    }
}
