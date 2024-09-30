package net.breezeware.dynamo.usermanagement.dao;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import net.breezeware.dynamo.generics.crud.dao.GenericRepository;
import net.breezeware.dynamo.usermanagement.entity.User;

@Repository
public interface UserRepository extends GenericRepository<User> {
    Optional<User> findByUniqueId(UUID userId);

    Optional<User> findByEmail(String userId);

    List<User> findByUserRoleMapRoleNameIn(List<String> roles);

    List<User> findByUserGroupMapGroupNameIn(List<String> groups);

    Optional<User> findByIdmUserId(String idmUserId);
    // @Query(value = "SELECT * FROM dynamo.\"user\" WHERE idm_user_id =
    // :idmUserId", nativeQuery = true)
    // Optional<User> findByIdmUserId(@Param("idmUserId") String idmUserId);

}
