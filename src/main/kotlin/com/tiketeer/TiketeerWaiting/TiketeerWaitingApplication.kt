package com.tiketeer.TiketeerWaiting

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.r2dbc.config.EnableR2dbcAuditing
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories

@SpringBootApplication
@EnableR2dbcAuditing
@EnableR2dbcRepositories
class TiketeerWaitingApplication

fun main(args: Array<String>) {
	runApplication<TiketeerWaitingApplication>(*args)
}
