package com.marekfx77.sshmobile.model

import java.util.UUID

enum class SSHAuthMethod {
    PASSWORD,
    PRIVATE_KEY;

    val displayName: String
        get() = when (this) {
            PASSWORD -> "Hasło"
            PRIVATE_KEY -> "Klucz prywatny"
        }
}

data class SSHServer(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val host: String,
    val port: Int = 22,
    val username: String,
    val authMethod: SSHAuthMethod = SSHAuthMethod.PASSWORD,
    val keyName: String? = null,
    val groupId: String? = null,
    val iconName: String = "server",
    val colorHex: String = "0A84FF",
    val lastConnectedAt: Long? = null,
    val notes: String = ""
) {
    val displayAddress: String
        get() = "$host:$port"
}

data class SSHServerGroup(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val colorHex: String = "636366",
    val isExpanded: Boolean = true
)

data class SSHSnippet(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val command: String,
    val description: String = "",
    val groupName: String = "Ogólne"
)

data class SFTPItem(
    val path: String,
    val name: String,
    val isDirectory: Boolean,
    val isSymlink: Boolean = false,
    val size: Long = 0L,
    val modifiedAt: Long? = null,
    val permissions: String = if (isDirectory) "755" else "644"
) {
    val displaySize: String
        get() {
            if (isDirectory) return ""
            if (size < 1024) return "$size B"
            val exp = (Math.log(size.toDouble()) / Math.log(1024.0)).toInt()
            val pre = "KMGTPE"[exp - 1]
            return String.format("%.1f %sB", size / Math.pow(1024.0, exp.toDouble()), pre)
        }
}

data class ServerResourceStats(
    val cpuPercent: Double = 0.0,
    val memUsedMiB: Int = 0,
    val memTotalMiB: Int = 0,
    val diskUsedGB: Double = 0.0,
    val diskTotalGB: Double = 0.0,
    val uptimeSeconds: Long = 0L
) {
    val memPercent: Float
        get() = if (memTotalMiB > 0) (memUsedMiB.toFloat() / memTotalMiB.toFloat()).coerceIn(0f, 1f) else 0f

    val diskPercent: Float
        get() = if (diskTotalGB > 0) (diskUsedGB.toFloat() / diskTotalGB.toFloat()).coerceIn(0f, 1f) else 0f

    val cpuText: String
        get() = String.format("%.1f%%", cpuPercent)

    val memText: String
        get() = "$memUsedMiB MiB / $memTotalMiB MiB"

    val diskText: String
        get() = String.format("%.1f GB / %.1f GB", diskUsedGB, diskTotalGB)

    val uptimeText: String
        get() {
            val d = uptimeSeconds / 86400
            val h = (uptimeSeconds % 86400) / 3600
            val m = (uptimeSeconds % 3600) / 60
            return when {
                d > 0 -> "${d}d ${h}h"
                h > 0 -> "${h}h ${m}m"
                else -> "${m}m"
            }
        }
}

enum class TerminalColorScheme(val displayName: String, val bgHex: Long, val textHex: Long, val promptHex: Long) {
    STANDARD("Standard", 0xFF0D1117, 0xFF39FF14, 0xFF58A6FF),
    DRACULA("Dracula", 0xFF282A36, 0xFFF8F8F2, 0xFFBD93F9),
    NORD("Nord", 0xFF2E3440, 0xFFD8DEE9, 0xFF88C0D0),
    SOLARIZED("Solarized Dark", 0xFF002B36, 0xFF839496, 0xFF268BD2),
    MONOKAI("Monokai", 0xFF272822, 0xFFF8F8F2, 0xFFA6E22E),
    ONE_DARK("One Dark", 0xFF282C34, 0xFFABB2BF, 0xFF61AFEF)
}

enum class AppThemeMode(val displayName: String) {
    SYSTEM("Systemowy"),
    LIGHT("Jasny"),
    DARK("Ciemny")
}

data class TerminalLine(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val isCommand: Boolean = false,
    val isError: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

data class FactoryResetProgress(
    val stage: Int,
    val totalStages: Int = 5,
    val title: String,
    val detail: String,
    val progress: Float,
    val isFinished: Boolean = false
)
