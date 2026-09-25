package com.marekfx77.sshmobile.ssh

import com.jcraft.jsch.ChannelSftp
import com.marekfx77.sshmobile.model.SFTPItem
import com.marekfx77.sshmobile.model.SSHServer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.util.Vector

class SFTPManager(val server: SSHServer) {
    val sessionManager = SSHSessionManager(server)
    private var sftpChannel: ChannelSftp? = null

    val isConnected: Boolean
        get() = sftpChannel?.isConnected == true

    suspend fun connect(password: String? = null, privateKeyPem: String? = null, timeout: Int = 30): Result<Unit> = withContext(Dispatchers.IO) {
        val res = sessionManager.connect(password, privateKeyPem, timeout)
        if (res.isFailure) return@withContext res

        try {
            sftpChannel = sessionManager.openSftp()
            Result.success(Unit)
        } catch (e: Exception) {
            disconnect()
            Result.failure(e)
        }
    }

    suspend fun listDirectory(path: String): Result<List<SFTPItem>> = withContext(Dispatchers.IO) {
        try {
            val channel = sftpChannel ?: return@withContext Result.failure(IllegalStateException("Brak połączenia SFTP"))
            if (!channel.isConnected) return@withContext Result.failure(IllegalStateException("Rozłączono SFTP"))

            val vector: Vector<ChannelSftp.LsEntry> = channel.ls(path) as Vector<ChannelSftp.LsEntry>
            val items = mutableListOf<SFTPItem>()

            for (entry in vector) {
                val filename = entry.filename
                if (filename == "." || filename == "..") continue

                val attrs = entry.attrs
                val isDir = attrs.isDir
                val isLink = attrs.isLink
                val size = attrs.size
                val mTime = attrs.mTime.toLong() * 1000L
                val perm = Integer.toOctalString(attrs.permissions and 0x1FF)

                val fullPath = if (path == "/") "/$filename" else if (path.endsWith("/")) "$path$filename" else "$path/$filename"

                items.add(
                    SFTPItem(
                        path = fullPath,
                        name = filename,
                        isDirectory = isDir,
                        isSymlink = isLink,
                        size = size,
                        modifiedAt = mTime,
                        permissions = perm
                    )
                )
            }

            items.sortWith(compareBy({ !it.isDirectory }, { it.name.lowercase() }))
            Result.success(items)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun readFile(path: String): Result<ByteArray> = withContext(Dispatchers.IO) {
        try {
            val channel = sftpChannel ?: return@withContext Result.failure(IllegalStateException("Brak połączenia SFTP"))
            val out = ByteArrayOutputStream()
            channel.get(path, out)
            Result.success(out.toByteArray())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun writeFile(path: String, bytes: ByteArray): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val channel = sftpChannel ?: return@withContext Result.failure(IllegalStateException("Brak połączenia SFTP"))
            val inStream = ByteArrayInputStream(bytes)
            channel.put(inStream, path, ChannelSftp.OVERWRITE)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun uploadStream(remotePath: String, inputStream: InputStream): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val channel = sftpChannel ?: return@withContext Result.failure(IllegalStateException("Brak połączenia SFTP"))
            channel.put(inputStream, remotePath, ChannelSftp.OVERWRITE)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createDirectory(path: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val channel = sftpChannel ?: return@withContext Result.failure(IllegalStateException("Brak połączenia SFTP"))
            channel.mkdir(path)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteFile(path: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val channel = sftpChannel ?: return@withContext Result.failure(IllegalStateException("Brak połączenia SFTP"))
            channel.rm(path)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteDirectory(path: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val channel = sftpChannel ?: return@withContext Result.failure(IllegalStateException("Brak połączenia SFTP"))
            channel.rmdir(path)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun disconnect() {
        try {
            sftpChannel?.disconnect()
            sessionManager.disconnect()
        } catch (_: Exception) {}
        sftpChannel = null
    }
}
