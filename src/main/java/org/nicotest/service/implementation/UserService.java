package org.nicotest.service.implementation;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.nicotest.converter.UserDomainToClientConverter;
import org.nicotest.model.client.UserDto;
import org.nicotest.model.client.UserDtoSingIn;
import org.nicotest.model.domain.Role;
import org.nicotest.model.domain.User;
import org.nicotest.repository.specification.UserRepository;
import org.nicotest.service.specification.IRoleService;
import org.nicotest.service.specification.IUserService;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService implements IUserService, UserDetailsService {

    @NonNull
    private final UserRepository userRepository;
    @NonNull
    private final UserDomainToClientConverter userDomainToClientConverter;
    @NonNull
    private final PasswordEncoder passwordEncoder;
    @NonNull
    private final IRoleService roleService;

    /**
     * Implementacion del metodo de UserDetailsService para validar las credenciales de login
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        User user = userRepository.findByUsername(username);

        if(Objects.isNull(user)){
            log.error("User {} not found", username);
            throw new UsernameNotFoundException("User not found");
        }

        Collection<SimpleGrantedAuthority> authorities = user.getRoles().stream()
                .map(Role::getName)
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());

        return new org.springframework.security.core.userdetails.User(user.getUsername(),
                user.getPassword(),
                authorities);
    }

    @Override
    public List<UserDto> getUsers() {
        return userRepository.findAll().stream().map(userDomainToClientConverter::convert).collect(Collectors.toList());
    }

    @Override
    public UserDto saveUser(UserDtoSingIn userSingIn) {
        final User user = new User();
        user.setUsername(userSingIn.getUsername());
        user.setPassword(passwordEncoder.encode(userSingIn.getPassword()));

        return userDomainToClientConverter.convert(userRepository.save(user));
    }

    @Override
    public UserDto addRoleToUser(Long userId, String roleName) {
        final User user = userRepository.findById(userId).get();
        final Role role = roleService.getRoles().stream().filter(r -> r.getName().equals(roleName)).findFirst().get();
        user.getRoles().add(role);

        return userDomainToClientConverter.convert(userRepository.save(user));
    }
}
