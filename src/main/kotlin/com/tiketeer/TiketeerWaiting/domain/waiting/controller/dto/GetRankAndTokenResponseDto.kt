package com.tiketeer.TiketeerWaiting.domain.waiting.controller.dto

import com.tiketeer.TiketeerWaiting.domain.waiting.usecase.dto.GetRankAndTokenResultDto
import reactor.core.publisher.Mono

data class GetRankAndTokenResponseDto(
    val getRankAndTokenResponse: Mono<RankAndToken>
) {
    companion object {
        fun convertFromDto(dto: GetRankAndTokenResultDto): GetRankAndTokenResponseDto {
            return GetRankAndTokenResponseDto(dto.rankAndToken)
        }
    }
}
