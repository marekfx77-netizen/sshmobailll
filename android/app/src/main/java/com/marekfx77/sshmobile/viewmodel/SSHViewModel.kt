package com.marekfx77.sshmobile.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.marekfx77.sshmobile.data.AppPreferences
import com.marekfx77.sshmobile.data.SecureStorage
import com.marekfx77.sshmobile.model.AppThemeMode
import com.marekfx77.sshmobile.model.FactoryResetProgress
import com.marekfx77.sshmobile.model.SFTPItem
import com.marekfx77.sshmobile.model.SSHAuthMethod
import com.marekfx77.sshmobile.model.SSHServer
import com.marekfx77.sshmobile.model.SSHServerGroup
import com.marekfx77.sshmobile.model.SSHSnippet
import com.marekfx77.sshmobile.model.TerminalColorScheme
import com.marekfx77.sshmobile.ssh.SFTPManager
import com.marekfx77.sshmobile.ssh.SSHTerminalPTY
import com.marekfx77.sshmobile.ssh.StatsPoller
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.util.UUID

data class ActiveSession(
    val id: String = UUID.randomUUID().toString(),
    val server: SSHServer,
    val terminal: SSHTerminalPTY,
    val statsPoller: StatsPoller
)

class SSHViewModel(application: Application) : AndroidViewModel(application) {
    val prefs = AppPreferences(application)
    val secureStorage = SecureStorage(application)

    private val _servers = MutableStateFlow<List<SSHServer>>(emptyList())
    val servers: StateFlow<List<SSHServer>> = _servers.asStateFlow()

    private val _groups = MutableStateFlow<List<SSHServerGroup>>(emptyList())
    val groups: StateFlow<List<SSHServerGroup>> = _groups.asStateFlow()

    private val _snippets = MutableStateFlow<List<SSHSnippet>>(emptyList())
    val snippets: StateFlow<List<SSHSnippet>> = _snippets.asStateFlow()

    private val _activeSessions = MutableStateFlow<List<ActiveSession>>(emptyList())
    val activeSessions: StateFlow<List<ActiveSession>> = _activeSessions.asStateFlow()

    private val _selectedSessionId = MutableStateFlow<String?>(null)
    val selectedSessionId: StateFlow<String?> = _selectedSessionId.asStateFlow()

    private val _currentTab = MutableStateFlow(0) // 0: Servers, 1: Terminal, 2: SFTP, 3: Snippets, 4: Settings
    val currentTab: StateFlow<Int> = _currentTab.asStateFlow()

    private val _isLocked = MutableStateFlow(false)
    val isLocked: StateFlow<Boolean> = _isLocked.asStateFlow()

    private val _factoryResetProgress = MutableStateFlow<FactoryResetProgress?>(null)
    val factoryResetProgress: StateFlow<FactoryResetProgress?> = _factoryResetProgress.asStateFlow()

    // SFTP State
    private val _sftpSelectedServer = MutableStateFlow<SSHServer?>(null)
    val sftpSelectedServer: StateFlow<SSHServer?> = _sftpSelectedServer.asStateFlow()

    private val _sftpManager = MutableStateFlow<SFTPManager?>(null)
    val sftpManager: StateFlow<SFTPManager?> = _sftpManager.asStateFlow()

    private val _sftpItems = MutableStateFlow<List<SFTPItem>>(emptyList())
    val sftpItems: StateFlow<List<SFTPItem>> = _sftpItems.asStateFlow()

    private val _sftpCurrentPath = MutableStateFlow("/")
    val sftpCurrentPath: StateFlow<String> = _sftpCurrentPath.asStateFlow()

    private val _sftpIsLoading = MutableStateFlow(false)
    val sftpIsLoading: StateFlow<Boolean> = _sftpIsLoading.asStateFlow()

    private val _sftpError = MutableStateFlow<String?>(null)
    val sftpError: StateFlow<String?> = _sftpError.asStateFlow()

    // Settings State
    private val _appearanceTheme = MutableStateFlow(prefs.appearanceTheme)
    val appearanceTheme: StateFlow<AppThemeMode> = _appearanceTheme.asStateFlow()

    private val _terminalColorScheme = MutableStateFlow(prefs.terminalColorScheme)
    val terminalColorScheme: StateFlow<TerminalColorScheme> = _terminalColorScheme.asStateFlow()

    private val _terminalFontSize = MutableStateFlow(prefs.terminalFontSize)
    val terminalFontSize: StateFlow<Float> = _terminalFontSize.asStateFlow()

    private val _sshTimeout = MutableStateFlow(prefs.sshTimeoutSeconds)
    val sshTimeout: StateFlow<Int> = _sshTimeout.asStateFlow()

    private val _privateKeyNames = MutableStateFlow<List<String>>(emptyList())
    val privateKeyNames: StateFlow<List<String>> = _privateKeyNames.asStateFlow()

    private val _isPinEnabled = MutableStateFlow(secureStorage.isPinEnabled())
    val isPinEnabled: StateFlow<Boolean> = _isPinEnabled.asStateFlow()

    init {
        loadData()
        if (secureStorage.isPinEnabled()) {
            _isLocked.value = true
        }
    }

    fun unlockApp() {
        _isLocked.value = false
    }

    fun setPin(pin: String) {
        secureStorage.setPin(pin)
        _isPinEnabled.value = true
    }

    fun verifyPin(pin: String): Boolean {
        return secureStorage.verifyPin(pin)
    }

    fun disablePin() {
        secureStorage.disablePin()
        _isPinEnabled.value = false
    }

    private fun loadData() {
        _servers.value = prefs.getServers()
        _groups.value = prefs.getGroups()
        _snippets.value = prefs.getSnippets()
        _privateKeyNames.value = secureStorage.getAllPrivateKeyNames()
        _appearanceTheme.value = prefs.appearanceTheme
        _terminalColorScheme.value = prefs.terminalColorScheme
        _terminalFontSize.value = prefs.terminalFontSize
        _sshTimeout.value = prefs.sshTimeoutSeconds
        _isPinEnabled.value = secureStorage.isPinEnabled()
    }

    fun selectTab(tabIndex: Int) {
        _currentTab.value = tabIndex
    }

    // --- Server & Group Actions ---

    fun addServer(server: SSHServer, password: String?) {
        val list = _servers.value.toMutableList()
        list.add(server)
        _servers.value = list
        prefs.saveServers(list)
        if (!password.isNullOrEmpty()) {
            secureStorage.savePassword(server.id, password)
        }
    }

    fun updateServer(server: SSHServer, password: String?) {
        val list = _servers.value.toMutableList()
        val idx = list.indexOfFirst { it.id == server.id }
        if (idx != -1) {
            list[idx] = server
            _servers.value = list
            prefs.saveServers(list)
            if (!password.isNullOrEmpty()) {
                secureStorage.savePassword(server.id, password)
            }
        }
    }

    fun deleteServer(server: SSHServer) {
        closeSessionForServer(server.id)
        secureStorage.deletePassword(server.id)
        val list = _servers.value.filter { it.id != server.id }
        _servers.value = list
        prefs.saveServers(list)
    }

    fun addGroup(group: SSHServerGroup) {
        val list = _groups.value.toMutableList()
        list.add(group)
        _groups.value = list
        prefs.saveGroups(list)
    }

    fun updateGroup(group: SSHServerGroup) {
        val list = _groups.value.toMutableList()
        val idx = list.indexOfFirst { it.id == group.id }
        if (idx != -1) {
            list[idx] = group
            _groups.value = list
            prefs.saveGroups(list)
        }
    }

    fun deleteGroup(group: SSHServerGroup) {
        val serverList = _servers.value.map {
            if (it.groupId == group.id) it.copy(groupId = null) else it
        }
        _servers.value = serverList
        prefs.saveServers(serverList)

        val list = _groups.value.filter { it.id != group.id }
        _groups.value = list
        prefs.saveGroups(list)
    }

    fun toggleGroupExpanded(group: SSHServerGroup) {
        updateGroup(group.copy(isExpanded = !group.isExpanded))
    }

    // --- Snippet Actions ---

    fun addSnippet(snippet: SSHSnippet) {
        val list = _snippets.value.toMutableList()
        list.add(snippet)
        _snippets.value = list
        prefs.saveSnippets(list)
    }

    fun updateSnippet(snippet: SSHSnippet) {
        val list = _snippets.value.toMutableList()
        val idx = list.indexOfFirst { it.id == snippet.id }
        if (idx != -1) {
            list[idx] = snippet
            _snippets.value = list
            prefs.saveSnippets(list)
        }
    }

    fun deleteSnippet(snippet: SSHSnippet) {
        val list = _snippets.value.filter { it.id != snippet.id }
        _snippets.value = list
        prefs.saveSnippets(list)
    }

    // --- Active Terminal Sessions ---

    fun connectToServer(server: SSHServer, passwordOverride: String? = null) {
        val existing = _activeSessions.value.firstOrNull { it.server.id == server.id }
        if (existing != null) {
            _selectedSessionId.value = existing.id
            _currentTab.value = 1
            if (!existing.terminal.isConnected.value && !existing.terminal.isConnecting.value) {
                startConnect(existing, passwordOverride)
            }
            return
        }

        val terminal = SSHTerminalPTY(server, viewModelScope)
        val statsPoller = StatsPoller(terminal.sessionManager, viewModelScope)
        val session = ActiveSession(
            server = server,
            terminal = terminal,
            statsPoller = statsPoller
        )

        val list = _activeSessions.value.toMutableList()
        list.add(session)
        _activeSessions.value = list
        _selectedSessionId.value = session.id
        _currentTab.value = 1

        startConnect(session, passwordOverride)
    }

    private fun startConnect(session: ActiveSession, passwordOverride: String?) {
        val pwd = passwordOverride ?: secureStorage.getPassword(session.server.id)
        val keyPem = session.server.keyName?.let { secureStorage.getPrivateKey(it) }
        val timeout = prefs.sshTimeoutSeconds

        session.terminal.connect(pwd, keyPem, timeout)
        viewModelScope.launch {
            session.terminal.isConnected.collect { connected ->
                if (connected) {
                    session.statsPoller.startPolling(10)
                } else {
                    session.statsPoller.stopPolling()
                }
            }
        }
    }

    fun selectSession(sessionId: String) {
        _selectedSessionId.value = sessionId
    }

    fun closeSession(sessionId: String) {
        val session = _activeSessions.value.firstOrNull { it.id == sessionId }
        session?.terminal?.disconnect()
        session?.statsPoller?.stopPolling()
        val list = _activeSessions.value.filter { it.id != sessionId }
        _activeSessions.value = list
        if (_selectedSessionId.value == sessionId) {
            _selectedSessionId.value = list.lastOrNull()?.id
        }
    }

    private fun closeSessionForServer(serverId: String) {
        val session = _activeSessions.value.firstOrNull { it.server.id == serverId }
        session?.let { closeSession(it.id) }
    }

    fun sendSnippetToActiveTerminal(snippet: SSHSnippet) {
        val active = _activeSessions.value.firstOrNull { it.id == _selectedSessionId.value }
        if (active != null) {
            active.terminal.sendCommand(snippet.command)
            _currentTab.value = 1
        }
    }

    // --- SFTP Operations ---

    fun openSftpForServer(server: SSHServer) {
        _sftpSelectedServer.value = server
        _currentTab.value = 2

        val pwd = secureStorage.getPassword(server.id)
        val keyPem = server.keyName?.let { secureStorage.getPrivateKey(it) }
        val timeout = prefs.sshTimeoutSeconds

        val manager = SFTPManager(server)
        _sftpManager.value = manager
        _sftpIsLoading.value = true
        _sftpError.value = null

        viewModelScope.launch {
            val res = manager.connect(pwd, keyPem, timeout)
            if (res.isSuccess) {
                loadSftpDirectory("/")
            } else {
                _sftpIsLoading.value = false
                _sftpError.value = res.exceptionOrNull()?.localizedMessage ?: "Błąd połączenia SFTP"
            }
        }
    }

    fun loadSftpDirectory(path: String) {
        val manager = _sftpManager.value ?: return
        _sftpIsLoading.value = true
        _sftpError.value = null
        viewModelScope.launch {
            val res = manager.listDirectory(path)
            _sftpIsLoading.value = false
            if (res.isSuccess) {
                _sftpCurrentPath.value = path
                _sftpItems.value = res.getOrDefault(emptyList())
            } else {
                _sftpError.value = res.exceptionOrNull()?.localizedMessage ?: "Nie można odczytać katalogu"
            }
        }
    }

    fun navigateSftpUp() {
        val current = _sftpCurrentPath.value
        if (current == "/" || current.isEmpty()) return
        val parent = current.substringBeforeLast('/', "").ifEmpty { "/" }
        loadSftpDirectory(parent)
    }

    fun createSftpFolder(name: String) {
        val manager = _sftpManager.value ?: return
        val current = _sftpCurrentPath.value
        val fullPath = if (current == "/") "/$name" else "$current/$name"
        viewModelScope.launch {
            val res = manager.createDirectory(fullPath)
            if (res.isSuccess) {
                loadSftpDirectory(current)
            } else {
                _sftpError.value = "Błąd tworzenia folderu: ${res.exceptionOrNull()?.localizedMessage}"
            }
        }
    }

    fun deleteSftpItem(item: SFTPItem) {
        val manager = _sftpManager.value ?: return
        viewModelScope.launch {
            val res = if (item.isDirectory) manager.deleteDirectory(item.path) else manager.deleteFile(item.path)
            if (res.isSuccess) {
                loadSftpDirectory(_sftpCurrentPath.value)
            } else {
                _sftpError.value = "Błąd usuwania: ${res.exceptionOrNull()?.localizedMessage}"
            }
        }
    }

    fun uploadFileToSftp(uri: Uri, fileName: String) {
        val manager = _sftpManager.value ?: return
        val current = _sftpCurrentPath.value
        val fullPath = if (current == "/") "/$fileName" else "$current/$fileName"

        _sftpIsLoading.value = true
        viewModelScope.launch {
            try {
                val inputStream: InputStream? = getApplication<Application>().contentResolver.openInputStream(uri)
                if (inputStream == null) {
                    _sftpIsLoading.value = false
                    _sftpError.value = "Nie można otworzyć pliku do wysłania."
                    return@launch
                }
                val res = manager.uploadStream(fullPath, inputStream)
                inputStream.close()
                _sftpIsLoading.value = false
                if (res.isSuccess) {
                    loadSftpDirectory(current)
                } else {
                    _sftpError.value = "Błąd wysyłania: ${res.exceptionOrNull()?.localizedMessage}"
                }
            } catch (e: Exception) {
                _sftpIsLoading.value = false
                _sftpError.value = "Błąd wysyłania: ${e.localizedMessage}"
            }
        }
    }

    // --- Settings Actions ---

    fun setAppearanceTheme(theme: AppThemeMode) {
        prefs.appearanceTheme = theme
        _appearanceTheme.value = theme
    }

    fun setTerminalColorScheme(scheme: TerminalColorScheme) {
        prefs.terminalColorScheme = scheme
        _terminalColorScheme.value = scheme
    }

    fun setTerminalFontSize(size: Float) {
        prefs.terminalFontSize = size
        _terminalFontSize.value = size
    }

    fun setSshTimeout(seconds: Int) {
        prefs.sshTimeoutSeconds = seconds
        _sshTimeout.value = seconds
    }

    fun importPrivateKey(name: String, pemData: String) {
        secureStorage.savePrivateKey(name, pemData)
        _privateKeyNames.value = secureStorage.getAllPrivateKeyNames()
    }

    fun deletePrivateKey(name: String) {
        secureStorage.deletePrivateKey(name)
        _privateKeyNames.value = secureStorage.getAllPrivateKeyNames()
    }

    // --- Real Multi-Stage Factory Reset ---

    fun performFactoryReset(onComplete: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            // Stage 1: Close active connections
            _factoryResetProgress.value = FactoryResetProgress(
                stage = 1,
                totalStages = 5,
                title = "Zamykanie połączeń",
                detail = "Trwa bezpieczne rozłączanie wszystkich sesji SSH i SFTP…",
                progress = 0.2f
            )
            _activeSessions.value.forEach { session ->
                session.terminal.disconnect()
                session.statsPoller.stopPolling()
            }
            _activeSessions.value = emptyList()
            _selectedSessionId.value = null
            _sftpManager.value?.disconnect()
            _sftpManager.value = null
            _sftpItems.value = emptyList()
            delay(600)

            // Stage 2: Erase Keystore & Secure Credentials
            _factoryResetProgress.value = FactoryResetProgress(
                stage = 2,
                totalStages = 5,
                title = "Czyszczenie magazynu haseł",
                detail = "Usuwanie zaszyfrowanych haseł i kluczy prywatnych z Android Keystore…",
                progress = 0.45f
            )
            secureStorage.deleteAll()
            _privateKeyNames.value = emptyList()
            delay(600)

            // Stage 3: Wipe server configs & snippets
            _factoryResetProgress.value = FactoryResetProgress(
                stage = 3,
                totalStages = 5,
                title = "Usuwanie bazy danych",
                detail = "Usuwanie profili serwerów, grup oraz własnych snippetów…",
                progress = 0.70f
            )
            prefs.clearAll()
            _servers.value = emptyList()
            _groups.value = emptyList()
            _snippets.value = emptyList()
            delay(500)

            // Stage 4: Reset UI & preferences
            _factoryResetProgress.value = FactoryResetProgress(
                stage = 4,
                totalStages = 5,
                title = "Resetowanie ustawień",
                detail = "Przywracanie domyślnych kolorów terminala, rozmiaru czcionki i zabezpieczeń…",
                progress = 0.90f
            )
            _appearanceTheme.value = AppThemeMode.DARK
            _terminalColorScheme.value = TerminalColorScheme.DRACULA
            _terminalFontSize.value = 13f
            _sshTimeout.value = 30
            _isPinEnabled.value = false
            _isLocked.value = false
            delay(500)

            // Stage 5: Finalizing
            _factoryResetProgress.value = FactoryResetProgress(
                stage = 5,
                totalStages = 5,
                title = "Ukończono reset",
                detail = "Aplikacja została pomyślnie zresetowana do ustawień fabrycznych.",
                progress = 1.0f,
                isFinished = true
            )
            delay(700)

            // Re-seed clean demo data
            loadData()
            _currentTab.value = 0
            _factoryResetProgress.value = null

            withContext(Dispatchers.Main) {
                onComplete()
            }
        }
    }
}
