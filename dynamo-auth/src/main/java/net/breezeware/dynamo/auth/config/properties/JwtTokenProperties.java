package net.breezeware.dynamo.auth.config.properties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JwtTokenProperties {
    private String userClaim;
    private String[] authoritiesClaim;
}
