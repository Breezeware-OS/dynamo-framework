package net.breezeware.dynamo.usermanagement.dao;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import net.breezeware.dynamo.generics.crud.dao.GenericRepository;
import net.breezeware.dynamo.usermanagement.entity.IdmInfo;

@Repository
public interface IdmInfoRepository extends GenericRepository<IdmInfo> {
    Optional<IdmInfo> findByIdmUniqueId(String idmUniqueId);
}
