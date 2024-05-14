package com.tiketeer.TiketeerWaiting.configuration

import io.r2dbc.h2.H2ConnectionConfiguration
import io.r2dbc.h2.H2ConnectionFactory
import io.r2dbc.h2.H2ConnectionOption
import io.r2dbc.spi.ConnectionFactory
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.core.io.ClassPathResource
import org.springframework.data.r2dbc.config.AbstractR2dbcConfiguration
import org.springframework.r2dbc.connection.init.ConnectionFactoryInitializer
import org.springframework.r2dbc.connection.init.ResourceDatabasePopulator

@TestConfiguration
class R2dbcConfiguration : AbstractR2dbcConfiguration() {
	@Bean
	override fun connectionFactory() = H2ConnectionFactory(
		H2ConnectionConfiguration.builder()
			.inMemory("test")
			.property(H2ConnectionOption.DB_CLOSE_DELAY, "-1")
			.build()
	)

	@Bean
	fun initializer(connectionFactory: ConnectionFactory): ConnectionFactoryInitializer {
		val initializer = ConnectionFactoryInitializer()
		initializer.setConnectionFactory(connectionFactory)
		val populator = ResourceDatabasePopulator(ClassPathResource("sql/db-schema.sql"))
		initializer.setDatabasePopulator(populator)
		return initializer
	}

	@Bean
	override fun getCustomConverters(): MutableList<Any> {
		return mutableListOf(UUIDToByteArrayConverter(), ByteArrayToUUIDConverter())
	}
}