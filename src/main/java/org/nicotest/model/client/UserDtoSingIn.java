package org.nicotest.model.client;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserDtoSingIn {
    private String username;
    private String password;
}