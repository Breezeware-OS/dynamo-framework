package net.breezeware.dynamo.dynamoformbuilder.dynamoformbuilderwebbff.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import io.swagger.v3.oas.annotations.media.Schema;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FormInvitationDto {

    /**
     * The share link key of the form.
     */
    @Schema(example = "6b47e3a3", description = "The share link key of the form.")
    private String formUniqueId;

    /**
     * The email ID of the invited user.
     */
    @Schema(example = "user@example.com", description = "The email ID of the invited user")
    private List<String> emailList;

}
