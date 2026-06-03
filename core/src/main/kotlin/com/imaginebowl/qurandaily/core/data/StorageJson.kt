package com.imaginebowl.qurandaily.core.data

import com.imaginebowl.qurandaily.core.domain.repository.StorageService
import kotlinx.serialization.serializer

suspend inline fun <reified T> StorageService.save(value: T, filename: String) {
    val bytes = JsonCodec.json.encodeToString(serializer<T>(), value).encodeToByteArray()
    saveBytes(filename, bytes)
}

suspend inline fun <reified T> StorageService.load(filename: String): T? {
    val bytes = loadBytes(filename) ?: return null
    return JsonCodec.json.decodeFromString(serializer<T>(), bytes.decodeToString())
}
