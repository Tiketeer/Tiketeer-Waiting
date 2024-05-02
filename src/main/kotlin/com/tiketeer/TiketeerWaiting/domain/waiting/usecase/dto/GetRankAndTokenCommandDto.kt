package com.tiketeer.TiketeerWaiting.domain.waiting.usecase.dto

data class GetRankAndTokenCommandDto(
    val email: String,
    val ticketingId: String
)
