package net.breezeware.dynamo.notification.push.svc.entity;

import java.util.UUID;

import net.breezeware.dynamo.generics.crud.entity.GenericEntity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;

/**
 * User device map.
 */
@Table(schema = "dynamo", name = "user_device_map")
@Entity
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class UserDeviceMap extends GenericEntity {

    /**
     * Unique id of the registered user-device.
     */
    @Schema(example = "8cbf2fff-4149-4a33-8848-e302116a4e10", description = "Unique id of the registered user-device.")
    @Column(name = "unique_id", unique = true, columnDefinition = "uuid default uuid_generate_v4()")
    private UUID uniqueId;

    /**
     * Unique id of the registered user.
     */
    @Schema(example = "ebbed6e3-c1b1-49a1-ae63-ac4d99577cb2", description = "Unique id of the registered user.")
    @Column(name = "user_id")
    private UUID userId;

    /**
     * Device type.
     */
    @Column(name = "platform_type")
    @Schema(example = "APNS", description = "Cloud messaging platform type.")
    private String platformType;

    /**
     * Device token.
     */
    @Column(name = "device_token")
    @NotBlank(message = "Device token is required.")
    @Schema(example = "1b947aacfaafc3c10729dcac097c90e466d1906c4926ea8546cce02ff3b73c86", description = "Device token.")
    private String deviceToken;

    /**
     * Target endpoint for publishing the push notification.
     * <p>
     * Target endpoint could be an endpoint from Firebase or an ARN from AWS SNS.
     * </p>
     */
    @Schema(example = "arn:aws:sns:us-east-1:305251478828:endpoint/GCM/Revolution-Picture"
            + "-Cars-Notification/e3b9f1b1-366f-3475-aff4-3844d22859e2",
            description = "AWS SNS generated target end point.")
    @Column(name = "target_endpoint")
    @NotBlank(message = "Generated target end point is required.")
    private String targetEndpoint;
}
