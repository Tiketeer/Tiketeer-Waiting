package com.tiketeer.TiketeerWaiting.configuration

import org.springframework.core.convert.converter.Converter
import org.springframework.data.convert.WritingConverter
import java.nio.ByteBuffer
import java.util.*

@WritingConverter
class UUIDToByteArrayConverter: Converter<UUID, ByteArray> {
	override fun convert(source: UUID): ByteArray? {
		val byteBuffer = ByteBuffer.wrap(ByteArray(16))
		byteBuffer.putLong(source.mostSignificantBits)
		byteBuffer.putLong(source.leastSignificantBits)
		return byteBuffer.array()
	}
}