package net.breezeware.dynamo.dynamoformbuilder.dynamoformsvc.enumeration;

import java.util.Arrays;
import java.util.Optional;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Getter
@AllArgsConstructor
@Slf4j
public enum FormInvitationStatus {

    INVITED("invited"), SUBMITTED("submitted");

    private final String value;

    public static Optional<FormInvitationStatus> retrieveFormInvitationStatus(String value) {
        log.info("Entering retrieveFormInvitationStatus(), value: {}", value);
        Optional<FormInvitationStatus> formInvitationStatus = Arrays.stream(values())
                .filter(invitationStatus -> invitationStatus.value.equalsIgnoreCase(value.toLowerCase())).findFirst();
        log.info("Leaving retrieveFormInvitationStatus(), is formInvitationStatus available for value: {} = {}", value,
                formInvitationStatus.isPresent());
        return formInvitationStatus;
    }
}
