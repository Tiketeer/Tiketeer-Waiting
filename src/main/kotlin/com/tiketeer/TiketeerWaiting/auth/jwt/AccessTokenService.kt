package com.tiketeer.TiketeerWaiting.auth.jwt

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.io.Decoders
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import javax.crypto.SecretKey

@Service
class AccessTokenService(@Value("\${jwt.secret-key}") secretKey: String) {
	private val secretKey: SecretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKey))

	fun verifyToken(accessToken: String): Mono<AccessTokenPayload> {
		return Mono.just(accessToken)
				.map {token -> Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).payload}
				.map {payload -> AccessTokenPayload(payload.subject, payload.get("role", String::class.java))}
	}

	data class AccessTokenPayload(val email: String, val role: String)
}
