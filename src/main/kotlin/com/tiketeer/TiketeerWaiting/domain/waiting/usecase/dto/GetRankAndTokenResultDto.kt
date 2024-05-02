package com.tiketeer.TiketeerWaiting.domain.waiting.usecase.dto

data class GetRankAndTokenResultDto(
    val rank: Int,
    val token: String? = null,
)
