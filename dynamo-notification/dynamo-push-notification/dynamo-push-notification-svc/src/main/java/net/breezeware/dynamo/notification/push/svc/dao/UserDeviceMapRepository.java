package net.breezeware.dynamo.notification.push.svc.dao;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import net.breezeware.dynamo.generics.crud.dao.GenericRepository;
import net.breezeware.dynamo.notification.push.svc.entity.UserDeviceMap;

@Repository
public interface UserDeviceMapRepository extends GenericRepository<UserDeviceMap> {
    Optional<UserDeviceMap> findByDeviceToken(String deviceToken);
}
