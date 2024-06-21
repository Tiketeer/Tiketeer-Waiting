package com.tiketeer.TiketeerWaiting.configuration;

import com.tiketeer.TiketeerWaiting.auth.jwt.JwtAuthenticationManager
import com.tiketeer.TiketeerWaiting.auth.jwt.JwtServerAuthenticationConverter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.SecurityWebFiltersOrder
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity.*
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.security.web.server.authentication.AuthenticationWebFilter
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository

@Configuration
@EnableWebFluxSecurity
class SecurityConfig {
	@Bean
	fun securityWebFilterChain(
			http: ServerHttpSecurity,
			authenticationManager: JwtAuthenticationManager,
			serverAuthenticationConverter: JwtServerAuthenticationConverter
	): SecurityWebFilterChain {
		val authenticationWebFilter = AuthenticationWebFilter(authenticationManager)
		authenticationWebFilter.setServerAuthenticationConverter(serverAuthenticationConverter)

		return http
				.csrf(CsrfSpec::disable)
				.formLogin(FormLoginSpec::disable)
				.httpBasic(HttpBasicSpec::disable)
				.securityContextRepository(NoOpServerSecurityContextRepository.getInstance())
				.addFilterAt(authenticationWebFilter, SecurityWebFiltersOrder.AUTHENTICATION)
				.authorizeExchange {
					it
						.pathMatchers("/actuator/**").permitAll()
						.anyExchange().authenticated()
				}
				.build()
	}
}
