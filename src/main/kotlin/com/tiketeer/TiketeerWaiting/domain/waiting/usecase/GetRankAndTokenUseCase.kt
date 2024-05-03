package com.tiketeer.TiketeerWaiting.domain.waiting.usecase

import com.tiketeer.TiketeerWaiting.domain.waiting.controller.dto.RankAndToken
import com.tiketeer.TiketeerWaiting.domain.waiting.usecase.dto.GetRankAndTokenCommandDto
import com.tiketeer.TiketeerWaiting.domain.waiting.usecase.dto.GetRankAndTokenResultDto
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import java.util.UUID

@Service
class GetRankAndTokenUseCase @Autowired constructor(private val redisTemplate: ReactiveRedisTemplate<String, String>) : GetRankAndToken {
    @Value("\${waiting.entry-size}")
    private lateinit var entrySize: Number

    override fun getRankAndToken(dto: GetRankAndTokenCommandDto): GetRankAndTokenResultDto {
        val currentTime = dto.entryTime
        val token = generateToken(dto.email, dto.ticketingId, currentTime)
        val mono = redisTemplate.opsForZSet().rank(dto.ticketingId.toString(), token)
            .switchIfEmpty(
                redisTemplate.opsForZSet().add(dto.ticketingId.toString(), token, currentTime.toDouble())
                    .then(redisTemplate.opsForZSet().rank(dto.ticketingId.toString(), token))
            )

        val ret: Mono<RankAndToken> = mono
            .flatMap { l ->
            if (l < entrySize.toInt()) {
                Mono.just(RankAndToken(l, generateToken(dto.email, dto.ticketingId, currentTime)))
            } else {
                Mono.just(RankAndToken(l))
            }
        }

        println(ret.block())

        return GetRankAndTokenResultDto(ret)
    }

    private fun generateToken(email: String, ticketingId: UUID, entryTime: Long) : String {
        return "${email}:${ticketingId}:${entryTime}"
    }
}