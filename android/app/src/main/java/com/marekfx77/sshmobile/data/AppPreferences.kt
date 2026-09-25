package com.marekfx77.sshmobile.data

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.marekfx77.sshmobile.model.AppThemeMode
import com.marekfx77.sshmobile.model.SSHAuthMethod
import com.marekfx77.sshmobile.model.SSHServer
import com.marekfx77.sshmobile.model.SSHServerGroup
import com.marekfx77.sshmobile.model.SSHSnippet
import com.marekfx77.sshmobile.model.TerminalColorScheme

class AppPreferences(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("ssh_mobile_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    companion object {
        private const val KEY_SERVERS = "ssh_servers"
        private const val KEY_GROUPS = "ssh_groups"
        private const val KEY_SNIPPETS = "ssh_snippets"
        private const val KEY_THEME = "appearance_theme"
        private const val KEY_TERM_SCHEME = "terminal_color_scheme"
        private const val KEY_TERM_FONT_SIZE = "terminal_font_size"
        private const val KEY_USE_BIOMETRICS = "use_biometrics"
        private const val KEY_SSH_TIMEOUT = "ssh_timeout"
        private const val KEY_INITIALIZED = "app_initialized_seed"
    }

    init {
        if (!prefs.getBoolean(KEY_INITIALIZED, false)) {
            seedDemoData()
            prefs.edit().putBoolean(KEY_INITIALIZED, true).apply()
        }
    }

    private fun seedDemoData() {
        val demoGroup = SSHServerGroup(name = "Przykładowe", colorHex = "34C759")
        val demoServer = SSHServer(
            name = "Mój serwer",
            host = "192.168.1.1",
            port = 22,
            username = "root",
            authMethod = SSHAuthMethod.PASSWORD,
            groupId = demoGroup.id,
            iconName = "server",
            colorHex = "0A84FF",
            notes = "Domyślny serwer przykładowy"
        )
        val defaultSnippets = listOf(
            SSHSnippet(name = "Informacje o systemie", command = "uname -a && lsb_release -a", description = "Wersja jądra i dystrybucji", groupName = "System"),
            SSHSnippet(name = "Zużycie dysku", command = "df -h", description = "Wolna przestrzeń na partycjach", groupName = "Monitoring"),
            SSHSnippet(name = "Pamięć RAM", command = "free -h", description = "Wykorzystanie RAM i Swap", groupName = "Monitoring"),
            SSHSnippet(name = "Aktywne procesy", command = "top -b -n 1 | head -n 20", description = "Top 20 procesów", groupName = "Monitoring"),
            SSHSnippet(name = "Aktualizacja pakietów", command = "sudo apt update && sudo apt upgrade -y", description = "Debian/Ubuntu update", groupName = "Zarządzanie"),
            SSHSnippet(name = "Otwarte porty", command = "ss -tulpn", description = "Aktywne nasłuchujące porty sieciowe", groupName = "Sieć")
        )
        saveGroups(listOf(demoGroup))
        saveServers(listOf(demoServer))
        saveSnippets(defaultSnippets)
    }

    fun getServers(): List<SSHServer> {
        val json = prefs.getString(KEY_SERVERS, null) ?: return emptyList()
        return try {
            val type = object : TypeToken<List<SSHServer>>() {}.type
            gson.fromJson(json, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun saveServers(servers: List<SSHServer>) {
        prefs.edit().putString(KEY_SERVERS, gson.toJson(servers)).apply()
    }

    fun getGroups(): List<SSHServerGroup> {
        val json = prefs.getString(KEY_GROUPS, null) ?: return emptyList()
        return try {
            val type = object : TypeToken<List<SSHServerGroup>>() {}.type
            gson.fromJson(json, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun saveGroups(groups: List<SSHServerGroup>) {
        prefs.edit().putString(KEY_GROUPS, gson.toJson(groups)).apply()
    }

    fun getSnippets(): List<SSHSnippet> {
        val json = prefs.getString(KEY_SNIPPETS, null) ?: return emptyList()
        return try {
            val type = object : TypeToken<List<SSHSnippet>>() {}.type
            gson.fromJson(json, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun saveSnippets(snippets: List<SSHSnippet>) {
        prefs.edit().putString(KEY_SNIPPETS, gson.toJson(snippets)).apply()
    }

    var appearanceTheme: AppThemeMode
        get() = try {
            AppThemeMode.valueOf(prefs.getString(KEY_THEME, AppThemeMode.DARK.name) ?: AppThemeMode.DARK.name)
        } catch (e: Exception) {
            AppThemeMode.DARK
        }
        set(value) = prefs.edit().putString(KEY_THEME, value.name).apply()

    var terminalColorScheme: TerminalColorScheme
        get() = try {
            TerminalColorScheme.valueOf(prefs.getString(KEY_TERM_SCHEME, TerminalColorScheme.DRACULA.name) ?: TerminalColorScheme.DRACULA.name)
        } catch (e: Exception) {
            TerminalColorScheme.DRACULA
        }
        set(value) = prefs.edit().putString(KEY_TERM_SCHEME, value.name).apply()

    var terminalFontSize: Float
        get() = prefs.getFloat(KEY_TERM_FONT_SIZE, 13f)
        set(value) = prefs.edit().putFloat(KEY_TERM_FONT_SIZE, value).apply()

    var useBiometrics: Boolean
        get() = prefs.getBoolean(KEY_USE_BIOMETRICS, false)
        set(value) = prefs.edit().putBoolean(KEY_USE_BIOMETRICS, value).apply()

    var sshTimeoutSeconds: Int
        get() = prefs.getInt(KEY_SSH_TIMEOUT, 30)
        set(value) = prefs.edit().putInt(KEY_SSH_TIMEOUT, value).apply()

    fun clearAll() {
        prefs.edit().clear().apply()
    }
}
