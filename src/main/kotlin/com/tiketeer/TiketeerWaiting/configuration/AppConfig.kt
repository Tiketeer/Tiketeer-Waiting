package com.tiketeer.TiketeerWaiting.configuration

import io.prometheus.client.exemplars.tracer.otel_agent.OpenTelemetryAgentSpanContextSupplier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Configuration
class AppConfig {
	@Bean
	@Profile("prod")
	fun openTelemetryAgentSpanContextSupplier(): OpenTelemetryAgentSpanContextSupplier {
		return OpenTelemetryAgentSpanContextSupplier()
	}
}