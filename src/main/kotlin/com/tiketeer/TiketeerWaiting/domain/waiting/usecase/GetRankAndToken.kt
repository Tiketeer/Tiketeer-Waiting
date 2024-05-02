package com.tiketeer.TiketeerWaiting.domain.waiting.usecase

import com.tiketeer.TiketeerWaiting.domain.waiting.usecase.dto.GetRankAndTokenCommandDto
import com.tiketeer.TiketeerWaiting.domain.waiting.usecase.dto.GetRankAndTokenResultDto

interface GetRankAndToken {
    fun getRankAndToken(dto: GetRankAndTokenCommandDto): GetRankAndTokenResultDto
}