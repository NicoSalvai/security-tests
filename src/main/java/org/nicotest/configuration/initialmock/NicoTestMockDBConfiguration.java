package org.nicotest.configuration.initialmock;

import org.nicotest.model.client.UserDtoSingIn;
import org.nicotest.model.domain.Role;
import org.nicotest.repository.specification.RoleRepository;
import org.nicotest.service.implementation.UserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class NicoTestMockDBConfiguration {

    @Bean
    CommandLineRunner run(UserService userService, RoleRepository roleRepository) {
        return args -> {
            Role roleAdmin = new Role();
            roleAdmin.setId(1L);
            roleAdmin.setName("ADMIN");
            Role roleClient = new Role();
            roleClient.setId(2L);
            roleClient.setName("CLIENT");
            roleRepository.save(roleAdmin);
            roleRepository.save(roleClient);

            UserDtoSingIn user = new UserDtoSingIn();
            user.setUsername("admin");
            user.setPassword("123321");

            UserDtoSingIn userClient = new UserDtoSingIn();
            userClient.setUsername("client");
            userClient.setPassword("111111");

            Long userId = userService.saveUser(user).getId();
            Long userClientId = userService.saveUser(userClient).getId();

            userService.addRoleToUser(userId, "ADMIN");
            userService.addRoleToUser(userClientId, "CLIENT");
        };
    }
}
