package net.breezeware.dynamo.usermanagement.service;

import java.util.Optional;

import org.springframework.stereotype.Service;

import net.breezeware.dynamo.generics.crud.service.GenericService;
import net.breezeware.dynamo.usermanagement.dao.RoleRepository;
import net.breezeware.dynamo.usermanagement.entity.Role;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class RoleService extends GenericService<Role> {

    private final RoleRepository roleRepository;

    public RoleService(RoleRepository roleRepository) {
        super(roleRepository);
        this.roleRepository = roleRepository;
    }

    public Optional<Role> retrieveRole(String roleName) {
        log.info("Entering retrieveRole()");
        Optional<Role> role = roleRepository.findByName(roleName);
        log.info("Leaving retrieveRole()");
        return role;
    }
}
