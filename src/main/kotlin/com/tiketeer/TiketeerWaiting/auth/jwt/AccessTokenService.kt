package com.tiketeer.TiketeerWaiting.auth.jwt

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.io.Decoders
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import javax.crypto.SecretKey

@Service
class AccessTokenService(@Value("\${jwt.secret - key}") secretKey: String) {
    private val secretKey: SecretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKey))

    fun verifyToken(accessToken: String): AccessTokenPayload {
        val claims = Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(accessToken).payload
        return AccessTokenPayload(claims.subject)
    }

    data class AccessTokenPayload(val email: String)
}
