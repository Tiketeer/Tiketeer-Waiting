package com.tiketeer.TiketeerWaiting.configuration

import io.asyncer.r2dbc.mysql.MySqlConnectionConfiguration
import io.asyncer.r2dbc.mysql.MySqlConnectionFactory
import io.r2dbc.spi.ConnectionFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.r2dbc.config.AbstractR2dbcConfiguration
import org.springframework.r2dbc.connection.init.ConnectionFactoryInitializer

@Configuration
class R2dbcConfiguration : AbstractR2dbcConfiguration() {
	@Value("\${spring.r2dbc.host}")
	lateinit var host: String

	@Value("\${spring.r2dbc.port}")
	lateinit var port: Number

	@Value("\${spring.r2dbc.username}")
	lateinit var username: String

	@Value("\${spring.r2dbc.password}")
	lateinit var password: String

	@Value("\${spring.r2dbc.database}")
	lateinit var database: String

	@Bean
	override fun connectionFactory(): ConnectionFactory {
		val config = MySqlConnectionConfiguration.builder()
			.host(host)
			.port(port.toInt())
			.username(username)
			.password(password)
			.database(database)
			.build()
		return MySqlConnectionFactory.from(config)
	}

	@Bean
	fun initializer(connectionFactory: ConnectionFactory): ConnectionFactoryInitializer {
		val initializer = ConnectionFactoryInitializer()
		initializer.setConnectionFactory(connectionFactory)
		return initializer
	}

	@Bean
	override fun getCustomConverters(): MutableList<Any> {
		return mutableListOf(UUIDToByteArrayConverter(), ByteArrayToUUIDConverter())
	}
}