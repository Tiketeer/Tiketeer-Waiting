package com.tiketeer.TiketeerWaiting.configuration

import org.springframework.core.convert.converter.Converter
import org.springframework.data.convert.ReadingConverter
import java.nio.ByteBuffer
import java.util.UUID

@ReadingConverter
class ByteArrayToUUIDConverter: Converter<ByteArray, UUID> {
	override fun convert(source: ByteArray): UUID {
		val wrap = ByteBuffer.wrap(source)
		val firstLong = wrap.getLong()
		val secondLong = wrap.getLong()
		return UUID(firstLong, secondLong)
	}
}