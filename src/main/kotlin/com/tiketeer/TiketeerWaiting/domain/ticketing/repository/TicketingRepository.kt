package com.tiketeer.TiketeerWaiting.domain.ticketing.repository

import com.tiketeer.TiketeerWaiting.annotation.RedisCacheable
import com.tiketeer.TiketeerWaiting.domain.ticketing.Ticketings
import org.springframework.cache.annotation.Cacheable
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import reactor.core.publisher.Mono
import java.util.UUID

interface TicketingRepository : ReactiveCrudRepository<Ticketings, UUID> {
	@RedisCacheable(key = "#id", value = "ticketingId")
	override fun findById(id: UUID) : Mono<Ticketings>
}