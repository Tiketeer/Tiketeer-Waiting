package com.tiketeer.TiketeerWaiting.config;

import com.tiketeer.TiketeerWaiting.auth.jwt.JwtServerAuthenticationConverter
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.ReactiveAuthenticationManager
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.SecurityWebFiltersOrder
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity.CsrfSpec
import org.springframework.security.config.web.server.ServerHttpSecurity.FormLoginSpec
import org.springframework.security.config.web.server.ServerHttpSecurity.HttpBasicSpec
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter

@Configuration
@EnableWebFluxSecurity
class SecurityConfig(
		authenticationManager: ReactiveAuthenticationManager,
		serverAuthenticationConverter: JwtServerAuthenticationConverter
) {
	private val authenticationWebFilter: AuthenticationWebFilter = AuthenticationWebFilter(authenticationManager)
	init {
		authenticationWebFilter.setServerAuthenticationConverter(serverAuthenticationConverter)
	}
	@Bean
	fun securityWebFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain {
		return http
				.csrf(CsrfSpec::disable)
				.formLogin(FormLoginSpec::disable)
				.httpBasic(HttpBasicSpec::disable)
				.addFilterAt(authenticationWebFilter, SecurityWebFiltersOrder.AUTHENTICATION)
				.authorizeExchange {
					e -> e.anyExchange().authenticated()
				}
				.build()
	}
}
