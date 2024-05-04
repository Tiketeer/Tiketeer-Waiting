package com.tiketeer.TiketeerWaiting.domain.waiting.usecase

import com.tiketeer.TiketeerWaiting.domain.waiting.usecase.dto.GetRankAndTokenCommandDto
import com.tiketeer.TiketeerWaiting.domain.waiting.usecase.dto.GetRankAndTokenResultDto
import reactor.core.publisher.Mono

interface GetRankAndToken {
    fun getRankAndToken(dto: GetRankAndTokenCommandDto): Mono<GetRankAndTokenResultDto>
}