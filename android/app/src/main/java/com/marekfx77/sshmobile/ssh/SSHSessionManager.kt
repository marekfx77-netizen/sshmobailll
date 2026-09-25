package com.marekfx77.sshmobile.ssh

import com.jcraft.jsch.ChannelExec
import com.jcraft.jsch.ChannelSftp
import com.jcraft.jsch.ChannelShell
import com.jcraft.jsch.JSch
import com.jcraft.jsch.Session
import com.marekfx77.sshmobile.model.SSHAuthMethod
import com.marekfx77.sshmobile.model.SSHServer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.nio.charset.StandardCharsets
import java.util.Properties

class SSHSessionManager(private val server: SSHServer) {
    private var session: Session? = null
    private val jsch = JSch()

    val isConnected: Boolean
        get() = session?.isConnected == true

    suspend fun connect(
        password: String? = null,
        privateKeyPem: String? = null,
        timeoutSeconds: Int = 30
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            disconnect()

            val host = server.host.trim()
            val port = server.port
            val username = server.username.trim()

            if (server.authMethod == SSHAuthMethod.PRIVATE_KEY && !privateKeyPem.isNullOrEmpty()) {
                val keyBytes = privateKeyPem.toByteArray(StandardCharsets.UTF_8)
                jsch.addIdentity(server.name, keyBytes, null, null)
            }

            val newSession = jsch.getSession(username, host, port)
            if (server.authMethod == SSHAuthMethod.PASSWORD && !password.isNullOrEmpty()) {
                newSession.setPassword(password)
            }

            val config = Properties()
            config["StrictHostKeyChecking"] = "no"
            config["PreferredAuthentications"] = if (server.authMethod == SSHAuthMethod.PASSWORD) {
                "password,keyboard-interactive"
            } else {
                "publickey,password"
            }
            newSession.setConfig(config)
            newSession.timeout = timeoutSeconds * 1000
            newSession.connect(timeoutSeconds * 1000)

            session = newSession
            Result.success(Unit)
        } catch (e: Exception) {
            disconnect()
            Result.failure(e)
        }
    }

    suspend fun openShell(cols: Int = 80, rows: Int = 24): ChannelShell = withContext(Dispatchers.IO) {
        val currentSession = session ?: throw IllegalStateException("SSH session is not connected")
        if (!currentSession.isConnected) throw IllegalStateException("SSH session is closed")

        val channel = currentSession.openChannel("shell") as ChannelShell
        channel.setPtyType("xterm-256color", cols, rows, 0, 0)
        channel
    }

    suspend fun openSftp(): ChannelSftp = withContext(Dispatchers.IO) {
        val currentSession = session ?: throw IllegalStateException("SSH session is not connected")
        if (!currentSession.isConnected) throw IllegalStateException("SSH session is closed")

        val channel = currentSession.openChannel("sftp") as ChannelSftp
        channel.connect(15000)
        channel
    }

    suspend fun executeCommand(command: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val currentSession = session ?: return@withContext Result.failure(IllegalStateException("Brak połączenia"))
            if (!currentSession.isConnected) return@withContext Result.failure(IllegalStateException("Rozłączono"))

            val channel = currentSession.openChannel("exec") as ChannelExec
            channel.setCommand(command)

            val outStream = ByteArrayOutputStream()
            channel.outputStream = outStream
            channel.setErrStream(outStream)

            channel.connect(15000)

            while (!channel.isClosed) {
                kotlinx.coroutines.delay(100)
            }

            channel.disconnect()
            val output = outStream.toString("UTF-8")
            Result.success(output)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun disconnect() {
        try {
            session?.disconnect()
        } catch (_: Exception) {}
        session = null
    }
}
