package net.breezeware.dynamo.usermanagement.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import net.breezeware.dynamo.usermanagement.dao.IdmInfoRepository;
import net.breezeware.dynamo.usermanagement.entity.IdmInfo;
import net.breezeware.dynamo.utils.exception.DynamoException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@DisplayName("IdmInfo Service implementation unit test")
@ExtendWith(MockitoExtension.class)
public class IdmInfoServiceTest {

    @Mock
    IdmInfoRepository idmInfoRepository;

    @InjectMocks
    IdmInfoService idmInfoService;

    @Test
    @DisplayName("Given valid idmInfo to createIdmInfo, then create a new idmInfo")
    void givenValidIdmInfo_when_createIdmInfo_thenCreateIdmInfo() throws DynamoException {
        log.info("Entering givenValidIdmInfo_when_createIdmInfo_thenCreateIdmInfo()");

        // Given
        IdmInfo idmInfo = IdmInfo.builder().name("cognito").idmUniqueId("unique-id").build();

        // Mock
        IdmInfo mockIdmInfo = IdmInfo.builder().id(1L).name("cognito").idmUniqueId("unique-id").createdOn(Instant.now())
                .modifiedOn(Instant.now()).build();
        when(idmInfoRepository.save(eq(idmInfo))).thenReturn(mockIdmInfo);

        // When
        IdmInfo createdidmInfo = idmInfoService.create(idmInfo);

        // Then
        verify(idmInfoRepository).save(eq(idmInfo));
        assertThat(createdidmInfo.getId()).isNotNull();
        assertThat(createdidmInfo.getCreatedOn()).isNotNull();
        assertThat(createdidmInfo.getModifiedOn()).isNotNull();
        assertThat(createdidmInfo.getName()).isEqualTo("cognito");

        log.info("Leaving givenValidIdmInfo_when_createIdmInfo_thenCreateIdmInfo()");
    }

    @Test
    @DisplayName("Given valid idm uniqueId to retrieveIdmInfo, then return the idmInfo")
    void givenValidIdmUniqueId_whenRetrieveIdmInfo_thenReturnIdmInfo() throws DynamoException {
        log.info("Entering givenValidIdmUniqueId_whenRetrieveIdmInfo_thenReturnIdmInfo()");

        // Given
        String idmUniqueId = "unique-id";

        // Mock
        IdmInfo mockIdmInfo = IdmInfo.builder().id(1L).name("cognito").idmUniqueId(idmUniqueId).createdOn(Instant.now())
                .modifiedOn(Instant.now()).build();
        when(idmInfoRepository.findByIdmUniqueId(eq(idmUniqueId))).thenReturn(Optional.of(mockIdmInfo));

        // When
        Optional<IdmInfo> optIdmInfo = idmInfoService.retrieveIdmInfo(idmUniqueId);

        // Then
        verify(idmInfoRepository).findByIdmUniqueId(eq(idmUniqueId));
        assertThat(optIdmInfo).isNotEmpty();

        log.info("Leaving givenValidIdmUniqueId_whenRetrieveIdmInfo_thenReturnIdmInfo()");
    }

}
