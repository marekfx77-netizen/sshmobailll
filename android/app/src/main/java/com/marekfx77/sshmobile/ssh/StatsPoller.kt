package com.marekfx77.sshmobile.ssh

import com.marekfx77.sshmobile.model.ServerResourceStats
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class StatsPoller(
    private val sessionManager: SSHSessionManager,
    private val scope: CoroutineScope
) {
    private var job: Job? = null
    private val _stats = MutableStateFlow<ServerResourceStats?>(null)
    val stats: StateFlow<ServerResourceStats?> = _stats.asStateFlow()

    fun startPolling(intervalSeconds: Long = 10L) {
        stopPolling()
        job = scope.launch(Dispatchers.IO) {
            while (isActive && sessionManager.isConnected) {
                fetchStats()
                delay(intervalSeconds * 1000L)
            }
        }
    }

    fun stopPolling() {
        job?.cancel()
        job = null
    }

    private suspend fun fetchStats() {
        try {
            val cpuCmd = sessionManager.executeCommand("top -bn1 2>/dev/null | grep -i 'cpu' | head -1 | grep -oP '[0-9.]+(?=.*id)' || echo '0'")
            val memCmd = sessionManager.executeCommand("free -m 2>/dev/null | awk 'NR==2{print $2\" \"$3}' || echo '0 0'")
            val diskCmd = sessionManager.executeCommand("df -BG / 2>/dev/null | awk 'NR==2{gsub(/G/,\"\",$2); gsub(/G/,\"\",$3); print $2\" \"$3}' || echo '0 0'")
            val uptimeCmd = sessionManager.executeCommand("cat /proc/uptime 2>/dev/null | awk '{print int($1)}' || echo '0'")

            val idle = cpuCmd.getOrNull()?.trim()?.toDoubleOrNull() ?: 0.0
            val cpu = (100.0 - idle).coerceIn(0.0, 100.0)

            val memParts = memCmd.getOrNull()?.trim()?.split("\\s+".toRegex()) ?: emptyList()
            val memTotal = memParts.getOrNull(0)?.toIntOrNull() ?: 0
            val memUsed = memParts.getOrNull(1)?.toIntOrNull() ?: 0

            val diskParts = diskCmd.getOrNull()?.trim()?.split("\\s+".toRegex()) ?: emptyList()
            val diskTotal = diskParts.getOrNull(0)?.toDoubleOrNull() ?: 0.0
            val diskUsed = diskParts.getOrNull(1)?.toDoubleOrNull() ?: 0.0

            val uptime = uptimeCmd.getOrNull()?.trim()?.toLongOrNull() ?: 0L

            _stats.value = ServerResourceStats(
                cpuPercent = cpu,
                memUsedMiB = memUsed,
                memTotalMiB = memTotal,
                diskUsedGB = diskUsed,
                diskTotalGB = diskTotal,
                uptimeSeconds = uptime
            )
        } catch (_: Exception) {}
    }
}
