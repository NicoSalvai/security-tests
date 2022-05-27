package org.nicotest.service.specification;

import org.nicotest.model.client.UserDto;
import org.nicotest.model.client.UserDtoSingIn;

import java.util.List;

public interface IUserService {
    List<UserDto> getUsers();
    UserDto saveUser(UserDtoSingIn userSingIn);
    UserDto addRoleToUser(Long userId, String roleName);
}
