package com.tiketeer.TiketeerWaiting.testHelper

import com.tiketeer.TiketeerWaiting.domain.ticketing.Ticketings
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.TestComponent
import org.springframework.r2dbc.core.DatabaseClient
import java.time.LocalDateTime
import java.util.*

@TestComponent
class TestHelper(@Autowired private val databaseClient: DatabaseClient) {
	fun insertTicketing(id: UUID, start: LocalDateTime, end: LocalDateTime): Ticketings {
		val ticketings = Ticketings(ticketingId = id, saleStart = start, saleEnd = end)

		databaseClient.sql("INSERT INTO ticketings (ticketing_id, sale_start, sale_end) VALUES (:ticketing_id, :saleStart, :saleEnd)")
			.bind("ticketing_id", id)
			.bind("saleStart", start)
			.bind("saleEnd", end)
			.fetch()
			.rowsUpdated()
			.block()
		return ticketings
	}
}