package com.tiketeer.TiketeerWaiting.domain.waiting.controller

import com.tiketeer.TiketeerWaiting.domain.waiting.controller.dto.GetRankAndTokenResponseDto
import com.tiketeer.TiketeerWaiting.domain.waiting.usecase.GetRankAndToken
import com.tiketeer.TiketeerWaiting.domain.waiting.usecase.dto.GetRankAndTokenCommandDto
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/waiting")
class WaitingController(
        private val getRankAndTokenUseCase: GetRankAndToken
) {
    @GetMapping
    fun getRankAndToken(@RequestParam(required = true) ticketingId: UUID): GetRankAndTokenResponseDto {
        // TODO: JWT 디코딩 필터 적용 후 JWT 내에서 가져오도록 수정
        val email = "test@test.com"
        val result = getRankAndTokenUseCase.getRankAndToken(GetRankAndTokenCommandDto(email, ticketingId))
        return GetRankAndTokenResponseDto.convertFromDto(result)
    }
}