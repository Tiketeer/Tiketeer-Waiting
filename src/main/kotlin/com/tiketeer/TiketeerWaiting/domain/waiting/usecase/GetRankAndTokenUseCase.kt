package com.tiketeer.TiketeerWaiting.domain.waiting.usecase

import com.tiketeer.TiketeerWaiting.domain.ticketing.repository.TicketingRepository
import com.tiketeer.TiketeerWaiting.domain.waiting.usecase.dto.GetRankAndTokenCommandDto
import com.tiketeer.TiketeerWaiting.domain.waiting.usecase.dto.GetRankAndTokenResultDto
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.switchIfEmpty
import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.util.UUID
private val logger = KotlinLogging.logger {}
@Service
class GetRankAndTokenUseCase @Autowired constructor(private val redisTemplate: ReactiveRedisTemplate<String, String>, private val ticketingRepository: TicketingRepository) : GetRankAndToken {
    @Value("\${waiting.entry-size}")
    private lateinit var entrySize: Number

    @Value("\${waiting.ttl}")
    private lateinit var ttl: Number

    @Value("\${waiting.entry-ttl}")
    private lateinit var entryTtl: Number

    override fun getRankAndToken(dto: GetRankAndTokenCommandDto): Mono<GetRankAndTokenResultDto> {
        val currentTime = dto.entryTime

        val key = "queue::${dto.ticketingId}"
        val ttlKey = "ttl::${dto.ticketingId}::${dto.email}"

        val validateMono = validateSalePeriod(dto.ticketingId, currentTime)
        val redisMono = validateMono.flatMap { _ ->
            redisTemplate.opsForValue().get(ttlKey)
        }.flatMap {
            redisTemplate.expire(ttlKey, Duration.ofMillis(ttl.toLong()))
        }.switchIfEmpty {
            logger.info{ "new redis token is added (ticketingId: ${dto.ticketingId}, email: ${dto.email})" }
            redisTemplate.opsForValue().set(ttlKey, "",Duration.ofMillis(ttl.toLong()))
                    .then(redisTemplate.opsForZSet().add(key, dto.email, currentTime.toDouble()))
        }.flatMap { _ ->
            redisTemplate.opsForZSet().rank(key, dto.email)
        }

        val ret: Mono<GetRankAndTokenResultDto> = redisMono
            .flatMap { l ->
            if (l < entrySize.toInt()) {
                redisTemplate.expire(ttlKey, Duration.ofMillis(entryTtl.toLong())).subscribe()
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