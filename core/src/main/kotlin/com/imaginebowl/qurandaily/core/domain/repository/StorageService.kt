package com.imaginebowl.qurandaily.core.domain.repository

import java.nio.file.Path

interface StorageService {
    suspend fun saveBytes(filename: String, data: ByteArray)

    suspend fun loadBytes(filename: String): ByteArray?

    suspend fun fileExists(filename: String): Boolean

    suspend fun deleteFile(filename: String)

    suspend fun directorySize(relativePath: String): Long

    suspend fun ensureDirectory(relativePath: String)

    suspend fun resolvePath(relativePath: String): Path
}
