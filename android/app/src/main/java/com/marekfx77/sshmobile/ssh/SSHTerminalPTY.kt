package com.marekfx77.sshmobile.ssh

import com.jcraft.jsch.ChannelShell
import com.marekfx77.sshmobile.model.SSHServer
import com.marekfx77.sshmobile.model.TerminalLine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.io.OutputStream
import java.nio.charset.StandardCharsets

class SSHTerminalPTY(
    val server: SSHServer,
    private val scope: CoroutineScope
) {
    val sessionManager = SSHSessionManager(server)
    private var shellChannel: ChannelShell? = null
    private var shellIn: OutputStream? = null
    private var shellOut: InputStream? = null
    private var readJob: Job? = null

    private val _lines = MutableStateFlow<List<TerminalLine>>(emptyList())
    val lines: StateFlow<List<TerminalLine>> = _lines.asStateFlow()

    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    private val _isConnecting = MutableStateFlow(false)
    val isConnecting: StateFlow<Boolean> = _isConnecting.asStateFlow()

    private val _statusMessage = MutableStateFlow("Rozłączono")
    val statusMessage: StateFlow<String> = _statusMessage.asStateFlow()

    fun connect(password: String? = null, privateKeyPem: String? = null, timeout: Int = 30) {
        if (_isConnecting.value || _isConnected.value) return

        _isConnecting.value = true
        _statusMessage.value = "Łączenie z ${server.host}…"
        appendLine("Łączenie z ${server.displayAddress}…")

        scope.launch(Dispatchers.IO) {
            val res = sessionManager.connect(password, privateKeyPem, timeout)
            if (res.isFailure) {
                withContext(Dispatchers.Main) {
                    _isConnecting.value = false
                    _isConnected.value = false
                    _statusMessage.value = "Błąd połączenia"
                    appendLine("✗ ${res.exceptionOrNull()?.localizedMessage ?: "Błąd uwierzytelniania"}", isError = true)
                }
                return@launch
            }

            try {
                val channel = sessionManager.openShell(cols = 80, rows = 24)
                shellIn = channel.outputStream
                shellOut = channel.inputStream
                channel.connect(10000)
                shellChannel = channel

                withContext(Dispatchers.Main) {
                    _isConnecting.value = false
                    _isConnected.value = true
                    _statusMessage.value = "Połączono z ${server.name}"
                    appendLine("✓ Połączono pomyślnie z sesją PTY.", isCommand = false)
                }

                startReading()
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _isConnecting.value = false
                    _isConnected.value = false
                    _statusMessage.value = "Błąd PTY: ${e.localizedMessage}"
                    appendLine("✗ Błąd powłoki: ${e.localizedMessage}", isError = true)
                }
                disconnect()
            }
        }
    }

    private fun startReading() {
        readJob?.cancel()
        readJob = scope.launch(Dispatchers.IO) {
            val stream = shellOut ?: return@launch
            val buffer = ByteArray(4096)
            val lineBuilder = StringBuilder()

            try {
                while (isActive && _isConnected.value) {
                    val read = stream.read(buffer)
                    if (read == -1) break
                    if (read > 0) {
                        val text = String(buffer, 0, read, StandardCharsets.UTF_8)
                        for (ch in text) {
                            if (ch == '\n') {
                                val line = cleanAnsi(lineBuilder.toString().trimEnd('\r'))
                                if (line.isNotEmpty()) {
                                    appendLine(line)
                                }
                                lineBuilder.clear()
                            } else {
                                lineBuilder.append(ch)
                            }
                        }
                        if (lineBuilder.length > 500) {
                            val line = cleanAnsi(lineBuilder.toString().trimEnd('\r'))
                            appendLine(line)
                            lineBuilder.clear()
                        }
                    }
                }
            } catch (_: Exception) {
            } finally {
                if (lineBuilder.isNotEmpty()) {
                    appendLine(cleanAnsi(lineBuilder.toString().trimEnd('\r')))
                }
                withContext(Dispatchers.Main) {
                    _isConnected.value = false
                    _statusMessage.value = "Sesja zakończona"
                    appendLine("[Sesja rozłączona]")
                }
            }
        }
    }

    fun sendCommand(command: String) {
        if (!_isConnected.value) return
        scope.launch(Dispatchers.IO) {
            try {
                val input = shellIn ?: return@launch
                val bytes = (command + "\n").toByteArray(StandardCharsets.UTF_8)
                input.write(bytes)
                input.flush()
            } catch (e: Exception) {
                appendLine("Błąd wysyłania: ${e.localizedMessage}", isError = true)
            }
        }
    }

    fun sendBytes(bytes: ByteArray) {
        if (!_isConnected.value) return
        scope.launch(Dispatchers.IO) {
            try {
                val input = shellIn ?: return@launch
                input.write(bytes)
                input.flush()
            } catch (e: Exception) {
                appendLine("Błąd wysyłania: ${e.localizedMessage}", isError = true)
            }
        }
    }

    fun resize(cols: Int, rows: Int) {
        shellChannel?.setPtySize(cols, rows, 0, 0)
    }

    fun clearLines() {
        _lines.value = emptyList()
    }

    fun disconnect() {
        readJob?.cancel()
        readJob = null
        try {
            shellIn?.close()
            shellOut?.close()
            shellChannel?.disconnect()
            sessionManager.disconnect()
        } catch (_: Exception) {}

        shellChannel = null
        shellIn = null
        shellOut = null
        _isConnected.value = false
        _isConnecting.value = false
        _statusMessage.value = "Rozłączono"
    }

    private fun appendLine(text: String, isCommand: Boolean = false, isError: Boolean = false) {
        val newLine = TerminalLine(text = text, isCommand = isCommand, isError = isError)
        val current = _lines.value.toMutableList()
        current.add(newLine)
        if (current.size > 2000) {
            current.removeAt(0)
        }
        _lines.value = current
    }

    private fun cleanAnsi(input: String): String {
        return input.replace(Regex("\u001B\\[[;?0-9]*[a-zA-Z]"), "")
            .replace(Regex("\u001B\\([a-zA-Z]"), "")
    }
}
