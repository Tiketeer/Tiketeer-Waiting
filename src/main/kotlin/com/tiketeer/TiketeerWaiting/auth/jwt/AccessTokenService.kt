package com.tiketeer.TiketeerWaiting.auth.jwt

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.io.Decoders
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import javax.crypto.SecretKey

@Service
class AccessTokenService(@Value("\${jwt.secret-key}") secretKey: String) {
    private val secretKey: SecretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKey))

    fun verifyToken(accessToken: String): AccessTokenPayload {
        val payload = Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(accessToken).payload
        val role = payload.get("role", String::class.java)
        return AccessTokenPayload(payload.subject, role)
    }

    data class AccessTokenPayload(val email: String, val role: String)
}
