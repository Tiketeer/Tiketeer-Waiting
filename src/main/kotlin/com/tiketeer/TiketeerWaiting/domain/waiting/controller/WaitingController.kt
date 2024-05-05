package com.tiketeer.TiketeerWaiting.domain.waiting.controller

import com.tiketeer.TiketeerWaiting.domain.waiting.controller.dto.GetRankAndTokenResponseDto
import com.tiketeer.TiketeerWaiting.domain.waiting.usecase.GetRankAndToken
import com.tiketeer.TiketeerWaiting.domain.waiting.usecase.dto.GetRankAndTokenCommandDto
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono
import java.util.UUID

@RestController
@RequestMapping("/waiting")
class WaitingController(
        private val getRankAndTokenUseCase: GetRankAndToken
) {
    @GetMapping
    fun getRankAndToken(
            authentication: Mono<Authentication>,
            @RequestParam(required = true) ticketingId: UUID
    ): Mono<GetRankAndTokenResponseDto> {
        return authentication
                .map { auth -> auth.name }
                .map { email -> GetRankAndTokenCommandDto(email, ticketingId, System.currentTimeMillis()) }
                .flatMap(getRankAndTokenUseCase::getRankAndToken)
                .map(GetRankAndTokenResponseDto::convertFromDto)
    }
}