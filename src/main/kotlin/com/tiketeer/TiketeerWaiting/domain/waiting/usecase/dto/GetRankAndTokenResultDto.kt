package com.tiketeer.TiketeerWaiting.domain.waiting.usecase.dto

import com.tiketeer.TiketeerWaiting.domain.waiting.controller.dto.RankAndToken
import reactor.core.publisher.Mono

data class GetRankAndTokenResultDto(
    val rankAndToken: Mono<RankAndToken>
)
