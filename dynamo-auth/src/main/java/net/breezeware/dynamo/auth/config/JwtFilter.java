package net.breezeware.dynamo.auth.config;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import net.breezeware.dynamo.auth.config.properties.DynamoAuthHttpProperties;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@AllArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private DynamoAuthHttpProperties dynamoAuthHttpProperties;

    @Override
    protected void doFilterInternal(HttpServletRequest req, @NonNull HttpServletResponse res,
            @NonNull FilterChain chain) throws IOException, ServletException {
        log.debug("Entering JwtFilter");
        Optional<String> optAuthorizationHeader = Optional.ofNullable(req.getHeader("Authorization"));

        if (optAuthorizationHeader.isPresent() && optAuthorizationHeader.get().startsWith("Bearer ")) {
            Optional<UsernamePasswordAuthenticationToken> optAuthenticationToken =
                    buildAuthenticationToken(optAuthorizationHeader.get());
            SecurityContext securityContext = SecurityContextHolder.getContext();
            if (Objects.nonNull(securityContext) && optAuthenticationToken.isPresent()) {
                securityContext.setAuthentication(optAuthenticationToken.get());
            } else {
                log.info("Security context not found or UsernamePasswordAuthenticationToken is 'null'");
            }

        }

        chain.doFilter(req, res);
        log.debug("Leaving JwtFilter");

    }

    /**
     * Builds an {@link Optional} {@link UsernamePasswordAuthenticationToken} from
     * the Authorization header.
     * @param  authorizationHeader header containing Access-token.
     * @return                     empty {@link Optional} if authorizationHeader is
     *                             empty or null, else the {@link Optional}
     *                             {@link UsernamePasswordAuthenticationToken}
     */
    private Optional<UsernamePasswordAuthenticationToken> buildAuthenticationToken(String authorizationHeader) {
        log.debug("Entering buildAuthenticationToken()");
        Optional<UsernamePasswordAuthenticationToken> optToken = Optional.empty();
        if (Optional.ofNullable(authorizationHeader).isPresent()) {
            String authToken = authorizationHeader.replace("Bearer ", "");
            // parse claims from the token.
            SignedJWT signedJwt;
            try {
                signedJwt = SignedJWT.parse(authToken);
                JWTClaimsSet jwtClaimsSet = signedJwt.getJWTClaimsSet();
                if (Objects.nonNull(jwtClaimsSet)) {
                    Map<String, Object> claims = jwtClaimsSet.getClaims();
                    if (Objects.nonNull(claims) && !claims.isEmpty()) {
                        List<GrantedAuthority> authorities = new ArrayList<>();
                        for (String claimAttr : dynamoAuthHttpProperties.getJwtToken().getAuthoritiesClaim()) {
                            ArrayList<String> claimObject = (ArrayList<String>) claims.get(claimAttr);
                            if (Objects.nonNull(claimObject)) {
                                for (String o : claimObject) {
                                    authorities.add(new SimpleGrantedAuthority("ROLE_" + o.toUpperCase()));
                                }

                            }

                        }

                        // Retrieve the JWT claims according to the app requirements
                        // String optUserClaim = signedJwt.getJWTClaimsSet().getSubject();
                        Optional<String> optUserClaim = Optional.ofNullable(signedJwt.getJWTClaimsSet()
                                .getStringClaim(dynamoAuthHttpProperties.getJwtToken().getUserClaim()));
                        if (optUserClaim.isPresent()) {
                            optToken = Optional
                                    .of(new UsernamePasswordAuthenticationToken(optUserClaim.get(), null, authorities));
                            log.debug("""
                                    Leaving buildAuthenticationToken() \
                                    with UsernamePasswordAuthenticationToken for {} \
                                    """, optUserClaim.get());
                            return optToken;
                        }

                    }

                }

            } catch (ParseException e) {
                log.error("Error while parsing JWT Token {} ", e.getMessage());
            }

        }

        log.debug("Leaving getAuthentication() without UsernamePasswordAuthenticationToken");
        return optToken;
    }
}
