package com.tiketeer.TiketeerWaiting.domain.waiting.controller.dto

import com.tiketeer.TiketeerWaiting.domain.waiting.usecase.dto.GetRankAndTokenResultDto

data class GetRankAndTokenResponseDto(
    val rank: Int,
    val token: String? = null,
) {
    companion object {
        fun convertFromDto(dto: GetRankAndTokenResultDto): GetRankAndTokenResponseDto {
            return GetRankAndTokenResponseDto(dto.rank, dto.token)
        }
    }
}
