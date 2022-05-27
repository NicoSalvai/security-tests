package org.nicotest.service.implementation;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.nicotest.model.domain.Role;
import org.nicotest.repository.specification.RoleRepository;
import org.nicotest.service.specification.IRoleService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoleService implements IRoleService {

    @NonNull
    private final RoleRepository roleRepository;

    @Override
    public List<Role> getRoles(){
        return roleRepository.findAll();
    }
}
