package net.breezeware.dynamo.usermanagement.service;

import java.util.Optional;

import org.springframework.stereotype.Service;

import net.breezeware.dynamo.generics.crud.service.GenericService;
import net.breezeware.dynamo.usermanagement.dao.IdmInfoRepository;
import net.breezeware.dynamo.usermanagement.entity.IdmInfo;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class IdmInfoService extends GenericService<IdmInfo> {

    private final IdmInfoRepository idmInfoRepository;

    public IdmInfoService(IdmInfoRepository idmInfoRepository) {
        super(idmInfoRepository);
        this.idmInfoRepository = idmInfoRepository;
    }

    public Optional<IdmInfo> retrieveIdmInfo(String idmUniqueId) {
        return idmInfoRepository.findByIdmUniqueId(idmUniqueId);
    }
}
