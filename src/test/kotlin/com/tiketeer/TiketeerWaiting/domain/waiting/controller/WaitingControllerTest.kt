package com.tiketeer.TiketeerWaiting.domain.waiting.controller

import com.tiketeer.TiketeerWaiting.auth.constant.JwtMetadata
import com.tiketeer.TiketeerWaiting.configuration.EmbeddedRedisConfig
import com.tiketeer.TiketeerWaiting.configuration.R2dbcConfiguration
import com.tiketeer.TiketeerWaiting.domain.waiting.usecase.GetRankAndTokenUseCase
import com.tiketeer.TiketeerWaiting.domain.waiting.usecase.dto.GetRankAndTokenCommandDto
import com.tiketeer.TiketeerWaiting.testHelper.TestHelper
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.io.Decoders
import io.jsonwebtoken.security.Keys
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.test.web.reactive.server.WebTestClient
import java.time.LocalDateTime
import java.util.Date
import java.util.UUID

@Import(TestHelper::class, R2dbcConfiguration::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class WaitingControllerTest {
	@Autowired
	lateinit var webTestClient: WebTestClient

	@Autowired
	lateinit var redisConnectionFactory: ReactiveRedisConnectionFactory

	@Autowired
	lateinit var getRankAndTokenUseCase: GetRankAndTokenUseCase

	@Autowired
	lateinit var databaseClient: DatabaseClient

	@Autowired
	lateinit var testHelper: TestHelper

	@Value("\${jwt.secret-key}")
	lateinit var jwtSecretKey: String

	@Value("\${waiting.entry-size}")
	lateinit var entrySize: Number

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
	fun `토큰이 없는 유저 - waiting 요청 - 호출 실패`() {
		// given
		val ticketingId = UUID.randomUUID()

		// when
		webTestClient.get().uri("/waiting?ticketingId=$ticketingId")
				// then
				.exchange()
				.expectStatus().isUnauthorized()
	}

	@Test
	fun `토큰이 있는 유저 - 빈 대기열에 waiting 요청 - 토큰 반환`() {
		// given
		val email = "test@test.com"
		val role = "USER"
		val ticketingId = UUID.randomUUID()

		val start = LocalDateTime.now().minusDays(1)
		val end = LocalDateTime.now().plusDays(1)
		testHelper.insertTicketing(ticketingId, start, end)

		// when
		webTestClient.get().uri("/waiting?ticketingId=$ticketingId")
				.cookie(JwtMetadata.ACCESS_TOKEN.value(), createAccessToken(email, role, Date()))
				// then
				.exchange()
				.expectStatus().isOk()
				.expectBody()
				.jsonPath("rank").isEqualTo(0L)
				.jsonPath("token").isEqualTo(createPurchaseToken(email, ticketingId))
	}

	@Test
	fun `토큰이 있는 유저 - 가득찬 대기열에 waiting 요청 - 토큰 반환 X`() {
		// given
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
		val role = "USER"

		// when
		webTestClient.get().uri("/waiting?ticketingId=$ticketingId")
				.cookie(JwtMetadata.ACCESS_TOKEN.value(), createAccessToken(email, role, Date()))
				// then
				.exchange()
				.expectStatus().isOk()
				.expectBody()
				.jsonPath("rank").isEqualTo(entrySize.toLong())
				.jsonPath("token").isEmpty()
	}

	private fun createPurchaseToken(email: String, ticketingId: UUID): String {
		return "$email:$ticketingId"
	}

	private fun createAccessToken(email: String, role: String, issuedAt: Date): String {
		return Jwts.builder()
				.subject(email)
				.claim("role", role)
				.issuer("tester")
				.issuedAt(issuedAt)
				.expiration(Date(issuedAt.time + 3 * 60 * 1000))
				.signWith(Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecretKey)))
				.compact();
	}
}