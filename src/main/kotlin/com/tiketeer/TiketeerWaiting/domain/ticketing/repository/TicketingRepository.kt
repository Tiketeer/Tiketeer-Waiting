package com.tiketeer.TiketeerWaiting.domain.ticketing.repository

import com.tiketeer.TiketeerWaiting.domain.ticketing.Ticketings
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import java.util.UUID

interface TicketingRepository : ReactiveCrudRepository<Ticketings, UUID> {
}