package com.tiketeer.TiketeerWaiting.auth.jwt

import com.tiketeer.TiketeerWaiting.auth.constant.JwtMetadata
import org.springframework.http.HttpCookie
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

@Component
class JwtServerAuthenticationConverter(
		private val accessTokenService: AccessTokenService
): ServerAuthenticationConverter {
	override fun convert(exchange: ServerWebExchange): Mono<Authentication> {
		return Mono.justOrEmpty(extractAccessToken(exchange))
				.map(HttpCookie::toString)
				.map(accessTokenService::verifyToken)
				.map { payload -> payload.email }
				.map(this::createAuthentication)
	}

	private fun extractAccessToken(exchange: ServerWebExchange): HttpCookie? {
		return exchange.request.cookies.getFirst(JwtMetadata.ACCESS_TOKEN.value())
	}

	private fun createAuthentication(email: String): Authentication {
		return UsernamePasswordAuthenticationToken(email, null)
	}
}