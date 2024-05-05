package com.tiketeer.TiketeerWaiting.domain.waiting.usecase.dto

import java.util.UUID

data class GetRankAndTokenCommandDto(
    val email: String,
    val ticketingId: UUID,
    val entryTime: Long
)
