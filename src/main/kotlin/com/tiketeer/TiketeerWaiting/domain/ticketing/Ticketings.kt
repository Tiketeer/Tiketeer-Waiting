package com.tiketeer.TiketeerWaiting.domain.ticketing

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime
import java.util.UUID

@Table("ticketings")
data class Ticketings(
	@Id
	val ticketingId: UUID? = null,
	val saleStart: LocalDateTime,
	val saleEnd: LocalDateTime
)
