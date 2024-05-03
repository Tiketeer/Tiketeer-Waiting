package com.tiketeer.TiketeerWaiting.auth.constant

enum class JwtMetadata(private val value: String) {
    ACCESS_TOKEN("accessToken"), REFRESH_TOKEN("refreshToken");

    fun value(): String {
        return this.value
    }
}
