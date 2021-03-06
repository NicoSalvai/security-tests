package org.nicotest.controller;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.nicotest.model.client.UserDto;
import org.nicotest.model.client.UserDtoSingIn;
import org.nicotest.service.specification.IUserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    @NonNull
    private final IUserService userService;

    @GetMapping("/users")
    public ResponseEntity<List<UserDto>> getUsers() {
        return ResponseEntity.ok().body(userService.getUsers());
    }


    @PutMapping("/users/{userId}")
    public UserDto addRoleToUser(@PathVariable("userId") Long userId, @RequestParam String roleName) {
        return userService.addRoleToUser(userId, roleName);
    }

    @PostMapping("/users")
    public UserDto saveUser(@RequestBody UserDtoSingIn userSingIn) {
        return userService.saveUser(userSingIn);
    }


}
