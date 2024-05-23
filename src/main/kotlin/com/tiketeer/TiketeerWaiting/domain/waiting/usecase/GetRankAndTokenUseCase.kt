package com.tiketeer.TiketeerWaiting.domain.waiting.usecase

import com.tiketeer.TiketeerWaiting.domain.ticketing.repository.TicketingRepository
import com.tiketeer.TiketeerWaiting.domain.waiting.usecase.dto.GetRankAndTokenCommandDto
import com.tiketeer.TiketeerWaiting.domain.waiting.usecase.dto.GetRankAndTokenResultDto
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.util.UUID

@Service
class GetRankAndTokenUseCase @Autowired constructor(private val redisTemplate: ReactiveRedisTemplate<String, String>, private val ticketingRepository: TicketingRepository) : GetRankAndToken {
    @Value("\${waiting.entry-size}")
    private lateinit var entrySize: Number

    override fun getRankAndToken(dto: GetRankAndTokenCommandDto): Mono<GetRankAndTokenResultDto> {
        val currentTime = dto.entryTime
        val token = generateToken(dto.email, dto.ticketingId)
        val key = "queue::${dto.ticketingId}"

        val validateResult = validateSalePeriod(dto.ticketingId, currentTime)
        val mono = validateResult.flatMap { _ ->
            redisTemplate.opsForZSet().rank(key, token)
                .switchIfEmpty(
                    redisTemplate.opsForZSet().add(key, token, currentTime.toDouble())
                        .then(redisTemplate.opsForZSet().rank(key, token))
                )
        }

        val ret: Mono<GetRankAndTokenResultDto> = mono
            .flatMap { l ->
            if (l < entrySize.toInt()) {
                Mono.just(GetRankAndTokenResultDto(l, generateToken(dto.email, dto.ticketingId)))
            } else {
                Mono.just(GetRankAndTokenResultDto(l))
            }
        }

        return ret
    }

    private fun generateToken(email: String, ticketingId: UUID) : String {
        return "${email}:${ticketingId}"
    }

    private fun validateSalePeriod(ticketingId: UUID, currentTime: Long) : Mono<Boolean> {
        val findById = ticketingRepository.findById(ticketingId)
        val local = LocalDateTime.ofInstant(Instant.ofEpochMilli(currentTime), ZoneId.of("Asia/Seoul"))
        return findById.flatMap { ticketing ->
            if (ticketing.saleStart.isBefore(local) && ticketing.saleEnd.isAfter(local)) {
                Mono.just(true)
            } else {
                Mono.error(RuntimeException("not sale period"))
            }
        }
    }

}