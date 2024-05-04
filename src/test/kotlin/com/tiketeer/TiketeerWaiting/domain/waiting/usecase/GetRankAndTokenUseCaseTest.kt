package com.tiketeer.TiketeerWaiting.domain.waiting.usecase

import com.tiketeer.TiketeerWaiting.configuration.EmbeddedRedisConfig
import com.tiketeer.TiketeerWaiting.domain.waiting.usecase.dto.GetRankAndTokenCommandDto
import com.tiketeer.TiketeerWaiting.domain.waiting.usecase.dto.GetRankAndTokenResultDto
import org.junit.jupiter.api.BeforeEach

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory
import reactor.test.StepVerifier
import java.util.UUID

@Import(EmbeddedRedisConfig::class)
@SpringBootTest
class GetRankAndTokenUseCaseTest {
    @Autowired
    lateinit var getRankAndTokenUseCase: GetRankAndTokenUseCase

    @Autowired
    lateinit var redisConnectionFactory: ReactiveRedisConnectionFactory

    @Value("\${waiting.entry-size}")
    lateinit var entrySize: Number

    @BeforeEach
    fun init() {
        val flushDb = redisConnectionFactory.reactiveConnection.serverCommands().flushDb()
        flushDb.block()
    }

    @Test
    fun `유저 정보 생성 - 빈 대기열에 요청 - 결과 검증`() {
        val email = "test@test.com"
        val ticketingId = UUID.randomUUID()
        val entryTime = System.currentTimeMillis()
        val result = getRankAndTokenUseCase.getRankAndToken(GetRankAndTokenCommandDto(email, ticketingId, entryTime))

        StepVerifier.create(result)
            .expectNext(GetRankAndTokenResultDto(0, "${email}:${ticketingId}"))
            .expectComplete()
            .verify()
    }

    @Test
    fun `대기열 길이만큼 유저 생성 - 대기열을 모두 채우도록 요청 후 한 명 더 요청 - 빈 토큰 결과 반환`() {
        val ticketingId = UUID.randomUUID()
        for (i in 1..entrySize.toInt()) {
            val email = "test${i}@test.com"
            val entryTime = System.currentTimeMillis()
            val result = getRankAndTokenUseCase.getRankAndToken(GetRankAndTokenCommandDto(email, ticketingId, entryTime))
            result.block()
        }

        val email = "test@test.com"
        val entryTime = System.currentTimeMillis()
        val result = getRankAndTokenUseCase.getRankAndToken(GetRankAndTokenCommandDto(email, ticketingId, entryTime))

        StepVerifier.create(result)
            .expectNext(GetRankAndTokenResultDto(entrySize.toLong()))
            .expectComplete()
            .verify()
    }
}