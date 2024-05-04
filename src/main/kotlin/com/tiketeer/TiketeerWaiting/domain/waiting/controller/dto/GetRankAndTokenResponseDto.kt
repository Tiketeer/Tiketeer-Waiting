package com.tiketeer.TiketeerWaiting.domain.waiting.controller.dto

import com.tiketeer.TiketeerWaiting.domain.waiting.usecase.dto.GetRankAndTokenResultDto
import reactor.core.publisher.Mono

data class GetRankAndTokenResponseDto(
    val rank: Long,
    val token: String? = null,
) {
    companion object {
        fun convertFromDto(dto: Mono<GetRankAndTokenResultDto>): Mono<GetRankAndTokenResponseDto> {
            return dto.flatMap { r ->
                Mono.just(GetRankAndTokenResponseDto(r.rank, r.token))
            }
        }
    }
}
