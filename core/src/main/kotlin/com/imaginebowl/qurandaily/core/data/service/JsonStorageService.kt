package com.imaginebowl.qurandaily.core.data.service

import com.imaginebowl.qurandaily.core.domain.repository.StorageService
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.exists
import kotlin.io.path.isDirectory
import kotlin.io.path.isRegularFile
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.readBytes
import kotlin.io.path.writeBytes

class JsonStorageService(
    private val rootDirectory: Path,
) : StorageService {
    override suspend fun saveBytes(filename: String, data: ByteArray) {
        val path = rootDirectory.resolve(filename)
        path.parent?.createDirectories()
        rootDirectory.createDirectories()
        path.writeBytes(data)
    }

    override suspend fun loadBytes(filename: String): ByteArray? {
        val path = rootDirectory.resolve(filename)
        if (!path.exists()) return null
        return path.readBytes()
    }

    override suspend fun fileExists(filename: String): Boolean =
        rootDirectory.resolve(filename).exists()

    override suspend fun deleteFile(filename: String) {
        val path = rootDirectory.resolve(filename)
        if (path.exists()) {
            Files.delete(path)
        }
    }

    override suspend fun directorySize(relativePath: String): Long {
        val path = rootDirectory.resolve(relativePath)
        if (!path.exists()) return 0L
        return calculateSize(path)
    }

    override suspend fun ensureDirectory(relativePath: String) {
        rootDirectory.resolve(relativePath).createDirectories()
    }

    override suspend fun resolvePath(relativePath: String): Path =
        rootDirectory.resolve(relativePath)

    private fun calculateSize(path: Path): Long {
        if (!path.exists()) return 0L
        if (path.isRegularFile()) {
            return Files.size(path)
        }
        if (!path.isDirectory()) return 0L
        return path.listDirectoryEntries().sumOf { calculateSize(it) }
    }
}
