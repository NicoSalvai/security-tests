package org.nicotest.converter;

import org.nicotest.model.client.UserDto;
import org.nicotest.model.domain.Role;
import org.nicotest.model.domain.User;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class UserDomainToClientConverter implements Converter<User, UserDto> {

    @Override
    public UserDto convert(User source) {
        final UserDto userDto = new UserDto();
        userDto.setId(source.getId());
        userDto.setUsername(source.getUsername());
        userDto.setRoles(Objects.nonNull(source.getRoles()) ?
                source.getRoles().stream().map(Role::getName).collect(Collectors.toList()) :
                Collections.emptyList());

        return userDto;
    }
}
