package com.tiketeer.TiketeerWaiting.domain.waiting.usecase

import com.tiketeer.TiketeerWaiting.configuration.EmbeddedRedisConfig
import com.tiketeer.TiketeerWaiting.configuration.R2dbcConfiguration
import com.tiketeer.TiketeerWaiting.domain.waiting.usecase.dto.GetRankAndTokenCommandDto
import com.tiketeer.TiketeerWaiting.domain.waiting.usecase.dto.GetRankAndTokenResultDto
import com.tiketeer.TiketeerWaiting.testHelper.TestHelper
import org.assertj.core.api.Assertions
import org.awaitility.Awaitility
import org.awaitility.Awaitility.await
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.r2dbc.core.DatabaseClient
import reactor.test.StepVerifier
import java.time.Duration
import java.time.LocalDateTime
import java.util.*

@Import(EmbeddedRedisConfig::class, TestHelper::class, R2dbcConfiguration::class)
@SpringBootTest
class GetRankAndTokenUseCaseTest {
    @Autowired
    lateinit var getRankAndTokenUseCase: GetRankAndTokenUseCase

    @Autowired
    lateinit var redisConnectionFactory: ReactiveRedisConnectionFactory

    @Autowired
    lateinit var testHelper: TestHelper

    @Autowired
    lateinit var databaseClient: DatabaseClient

    @Autowired
    lateinit var redisTemplate: ReactiveRedisTemplate<String, String>

    @Value("\${waiting.entry-size}")
    lateinit var entrySize: Number

    @Value("\${waiting.ttl}")
    private lateinit var ttl: Number

    @BeforeEach
    fun init() {
        val flushDb = redisConnectionFactory.reactiveConnection.serverCommands().flushDb()
        flushDb.block()

        databaseClient
            .sql("delete from ticketings")
            .fetch()
            .rowsUpdated()
            .block()
    }

    @Test
    fun `유저 정보 생성 - 빈 대기열에 요청 - 토큰 반환 및 entry ttl 설정`() {
        val email = "test@test.com"
        val ticketingId = UUID.randomUUID()
        val entryTime = System.currentTimeMillis()
        val ttlKey =  "ttl::${ticketingId}::${email}"

        val start = LocalDateTime.now().minusDays(2)
        val end = LocalDateTime.now().plusDays(2)

        testHelper.insertTicketing(ticketingId, start, end)

        val result = getRankAndTokenUseCase.getRankAndToken(GetRankAndTokenCommandDto(email, ticketingId, entryTime))

        StepVerifier.create(result)
            .expectNext(GetRankAndTokenResultDto(0, "${email}:${ticketingId}"))
            .expectComplete()
            .verify()

        await().pollDelay(Duration.ofMillis(ttl.toLong()+10)).untilAsserted {
            val redisTtl = redisTemplate.getExpire(ttlKey).map { v -> println("redis ttl : ${v.toMillis()}")
                v}
            StepVerifier.create(redisTtl)
                    .expectNextCount(1)
                    .expectComplete()
                    .verify();
        }
    }

    @Test
    fun `대기열 길이만큼 유저 생성 - 대기열을 모두 채우도록 요청 후 한 명 더 요청 - 순위만 반환`() {
        val ticketingId = UUID.randomUUID()
        val start = LocalDateTime.now().minusDays(1)
        val end = LocalDateTime.now().plusDays(1)

        testHelper.insertTicketing(ticketingId, start, end)

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

    @Test
    fun `대기열 길이만큼 유저 생성 - 대기열을 모두 채우도록 요청 후 한명 더 요청 - 현재 순위 {entrySize}위 - {ttl+10} 초 후 재요청 - 현재 순위 0위`() {
        val ticketingId = UUID.randomUUID()
        val start = LocalDateTime.now().minusDays(1)
        val end = LocalDateTime.now().plusDays(1)

        testHelper.insertTicketing(ticketingId, start, end)

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

        await().pollDelay(Duration.ofMillis(ttl.toLong()/2+10)).untilAsserted {
            getRankAndTokenUseCase.getRankAndToken(GetRankAndTokenCommandDto(email, ticketingId, entryTime)).subscribe()
        }

        await().pollDelay(Duration.ofMillis(ttl.toLong()+10)).untilAsserted {
            val result2 = getRankAndTokenUseCase.getRankAndToken(GetRankAndTokenCommandDto(email, ticketingId, entryTime))
            StepVerifier.create(result2)
                    .expectNext(GetRankAndTokenResultDto(0, "${email}:${ticketingId}"))
                    .expectComplete()
                    .verify()
        }
    }

    @Test
    fun `유저 정보 생성 - 판매 기간 이외 요청 - 에러 던짐`() {
        val email = "test@test.com"
        val ticketingId = UUID.randomUUID()
        val entryTime = System.currentTimeMillis()

        val start = LocalDateTime.now().plusDays(1)
        val end = LocalDateTime.now().plusDays(1)

        testHelper.insertTicketing(ticketingId, start, end)

        val result = getRankAndTokenUseCase.getRankAndToken(GetRankAndTokenCommandDto(email, ticketingId, entryTime))

        StepVerifier.create(result)
            .expectErrorMessage("not sale period")
            .verify()
    }
}