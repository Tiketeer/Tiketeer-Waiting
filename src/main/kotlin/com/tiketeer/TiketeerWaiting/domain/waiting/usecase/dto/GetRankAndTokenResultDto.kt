package com.tiketeer.TiketeerWaiting.domain.waiting.usecase.dto

data class GetRankAndTokenResultDto(
    val rank: Long,
    val token: String? = null,
)
