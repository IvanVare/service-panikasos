package com.PanikaSos.PS_springB.utils.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class JwtUtils {
    @Value("${security.jwt.secret.key}")
    private String Secretkey;
    @Value("${security.jwt.user.generator}")
    private String JwtUserGenerator;

    public String createToken(Authentication authentication){
        Algorithm algorithm = Algorithm.HMAC512(this.Secretkey);
        String email = authentication.getPrincipal().toString();
        String authorities = authentication.getAuthorities()
                .stream().map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        String jwtToken = JWT.create()
                .withIssuer(this.JwtUserGenerator)
                .withSubject(email)
                .withClaim("roles", authorities)
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis()+86400000))
                .withJWTId(UUID.randomUUID().toString())
                .withNotBefore(new Date(System.currentTimeMillis()))
                .sign(algorithm);
        return jwtToken;
    }

    public DecodedJWT validateToken(String token){
        try {
            Algorithm algorithm = Algorithm.HMAC512(this.Secretkey);
            JWTVerifier verifier = JWT.require(algorithm)
                    .withIssuer(this.JwtUserGenerator)
                    .build();

            DecodedJWT decodedJWT = verifier.verify(token);
            return decodedJWT;
        } catch (JWTVerificationException exception){
            throw new JWTVerificationException("Token invalid, not authorized");
        }
    }

    public String extractEmail(DecodedJWT decodedJWT){
        return decodedJWT.getSubject().toString();
    }

    public Claim getSpecificClaim(DecodedJWT decodedJWT, String claimName){
        return decodedJWT.getClaim(claimName);
    }

    public Map<String,Claim> getAllClaims(DecodedJWT decodedJWT){
        return decodedJWT.getClaims();
    }

}