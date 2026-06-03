package com.imaginebowl.qurandaily.core.data

import java.time.Instant
import java.util.UUID
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.contextual

object JsonCodec {
    val json: Json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        serializersModule = SerializersModule {
            contextual(UUID::class, UuidSerializer)
            contextual(Instant::class, InstantSerializer)
        }
    }
}
