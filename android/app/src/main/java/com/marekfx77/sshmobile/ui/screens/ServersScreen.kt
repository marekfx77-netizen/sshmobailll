package com.marekfx77.sshmobile.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.marekfx77.sshmobile.model.SSHAuthMethod
import com.marekfx77.sshmobile.model.SSHServer
import com.marekfx77.sshmobile.model.SSHServerGroup
import com.marekfx77.sshmobile.ui.components.ServerCard
import com.marekfx77.sshmobile.ui.theme.AccentBlue
import com.marekfx77.sshmobile.ui.theme.BgDark
import com.marekfx77.sshmobile.ui.theme.SurfaceDark
import com.marekfx77.sshmobile.viewmodel.SSHViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServersScreen(
    viewModel: SSHViewModel,
    modifier: Modifier = Modifier
) {
    val servers by viewModel.servers.collectAsState()
    val groups by viewModel.groups.collectAsState()
    val activeSessions by viewModel.activeSessions.collectAsState()

    var showAddServerDialog by remember { mutableStateOf(false) }
    var showAddGroupDialog by remember { mutableStateOf(false) }
    var serverToEdit by remember { mutableStateOf<SSHServer?>(null) }
    var serverToConnectPrompt by remember { mutableStateOf<SSHServer?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Serwery",
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        color = Color.White
                    )
                },
                actions = {
                    IconButton(onClick = { showAddGroupDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.CreateNewFolder,
                            contentDescription = "Dodaj grupę",
                            tint = AccentBlue
                        )
                    }
                    IconButton(onClick = { showAddServerDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Dodaj serwer",
                            tint = AccentBlue
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BgDark)
            )
        },
        containerColor = BgDark,
        modifier = modifier
    ) { paddingValues ->
        if (servers.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Storage,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(54.dp)
                    )
                    Text(
                        text = "Brak serwerów",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Dodaj swój pierwszy serwer SSH, aby rozpocząć.",
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Button(
                        onClick = { showAddServerDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentBlue)
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Dodaj serwer")
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Grouped Servers
                groups.forEach { group ->
                    val groupServers = servers.filter { it.groupId == group.id }
                    item(key = group.id) {
                        GroupHeader(
                            group = group,
                            count = groupServers.size,
                            onToggle = { viewModel.toggleGroupExpanded(group) },
                            onDelete = { viewModel.deleteGroup(group) }
                        )
                    }

                    if (group.isExpanded) {
                        items(groupServers, key = { it.id }) { server ->
                            val isConnected = activeSessions.any { it.server.id == server.id && it.terminal.isConnected.collectAsState().value }
                            ServerCard(
                                server = server,
                                isConnected = isConnected,
                                onConnectClick = {
                                    val savedPwd = viewModel.secureStorage.getPassword(server.id)
                                    if (server.authMethod == SSHAuthMethod.PASSWORD && savedPwd.isNullOrEmpty()) {
                                        serverToConnectPrompt = server
                                    } else {
                                        viewModel.connectToServer(server)
                                    }
                                },
                                onSftpClick = { viewModel.openSftpForServer(server) },
                                onEditClick = { serverToEdit = server },
                                onDeleteClick = { viewModel.deleteServer(server) }
                            )
                        }
                    }
                }

                // Ungrouped Servers
                val ungroupedServers = servers.filter { it.groupId == null }
                if (ungroupedServers.isNotEmpty()) {
                    if (groups.isNotEmpty()) {
                        item {
                            Text(
                                text = "Pozostałe serwery",
                                color = Color.Gray,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                    }
                    items(ungroupedServers, key = { it.id }) { server ->
                        val isConnected = activeSessions.any { it.server.id == server.id && it.terminal.isConnected.collectAsState().value }
                        ServerCard(
                            server = server,
                            isConnected = isConnected,
                            onConnectClick = {
                                val savedPwd = viewModel.secureStorage.getPassword(server.id)
                                if (server.authMethod == SSHAuthMethod.PASSWORD && savedPwd.isNullOrEmpty()) {
                                    serverToConnectPrompt = server
                                } else {
                                    viewModel.connectToServer(server)
                                }
                            },
                            onSftpClick = { viewModel.openSftpForServer(server) },
                            onEditClick = { serverToEdit = server },
                            onDeleteClick = { viewModel.deleteServer(server) }
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }

    // Add Server Dialog
    if (showAddServerDialog) {
        ServerFormDialog(
            server = null,
            groups = groups,
            keys = viewModel.privateKeyNames.collectAsState().value,
            onDismiss = { showAddServerDialog = false },
            onSave = { newServer, pwd ->
                viewModel.addServer(newServer, pwd)
                showAddServerDialog = false
            }
        )
    }

    // Edit Server Dialog
    serverToEdit?.let { s ->
        ServerFormDialog(
            server = s,
            groups = groups,
            keys = viewModel.privateKeyNames.collectAsState().value,
            onDismiss = { serverToEdit = null },
            onSave = { updated, pwd ->
                viewModel.updateServer(updated, pwd)
                serverToEdit = null
            }
        )
    }

    // Add Group Dialog
    if (showAddGroupDialog) {
        var groupName by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showAddGroupDialog = false },
            title = { Text(text = "Nowa grupa serwerów", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = groupName,
                    onValueChange = { groupName = it },
                    label = { Text("Nazwa grupy") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (groupName.isNotBlank()) {
                            viewModel.addGroup(SSHServerGroup(name = groupName.trim()))
                            showAddGroupDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentBlue)
                ) {
                    Text(text = "Utwórz")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddGroupDialog = false }) {
                    Text(text = "Anuluj", color = Color.Gray)
                }
            },
            containerColor = SurfaceDark
        )
    }

    // Prompt for password if not saved
    serverToConnectPrompt?.let { s ->
        var passwordInput by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { serverToConnectPrompt = null },
            title = { Text(text = "Połącz z ${s.name}", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = "Podaj hasło dla użytkownika ${s.username}:", color = Color.Gray, fontSize = 13.sp)
                    OutlinedTextField(
                        value = passwordInput,
                        onValueChange = { passwordInput = it },
                        label = { Text("Hasło SSH") },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val pwd = passwordInput
                        serverToConnectPrompt = null
                        viewModel.secureStorage.savePassword(s.id, pwd)
                        viewModel.connectToServer(s, pwd)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentBlue)
                ) {
                    Text(text = "Połącz")
                }
            },
            dismissButton = {
                TextButton(onClick = { serverToConnectPrompt = null }) {
                    Text(text = "Anuluj", color = Color.Gray)
                }
            },
            containerColor = SurfaceDark
        )
    }
}

@Composable
private fun GroupHeader(
    group: SSHServerGroup,
    count: Int,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (group.isExpanded) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowRight,
            contentDescription = null,
            tint = Color.Gray,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = group.name,
            color = Color.White,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "($count)",
            color = Color.Gray,
            fontSize = 13.sp
        )
    }
}

@Composable
private fun ServerFormDialog(
    server: SSHServer?,
    groups: List<SSHServerGroup>,
    keys: List<String>,
    onDismiss: () -> Unit,
    onSave: (SSHServer, String?) -> Unit
) {
    var name by remember { mutableStateOf(server?.name ?: "") }
    var host by remember { mutableStateOf(server?.host ?: "") }
    var port by remember { mutableStateOf(server?.port?.toString() ?: "22") }
    var username by remember { mutableStateOf(server?.username ?: "root") }
    var password by remember { mutableStateOf("") }
    var authMethod by remember { mutableStateOf(server?.authMethod ?: SSHAuthMethod.PASSWORD) }
    var selectedKeyName by remember { mutableStateOf(server?.keyName ?: keys.firstOrNull()) }
    var selectedGroupId by remember { mutableStateOf(server?.groupId) }
    var notes by remember { mutableStateOf(server?.notes ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (server == null) "Dodaj nowy serwer" else "Edytuj serwer",
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Nazwa serwera") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = host,
                        onValueChange = { host = it },
                        label = { Text("Adres IP lub domena") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = port,
                            onValueChange = { port = it },
                            label = { Text("Port") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = username,
                            onValueChange = { username = it },
                            label = { Text("Użytkownik") },
                            singleLine = true,
                            modifier = Modifier.weight(2f)
                        )
                    }
                }
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { authMethod = SSHAuthMethod.PASSWORD },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (authMethod == SSHAuthMethod.PASSWORD) AccentBlue else Color.White.copy(alpha = 0.1f)
                            )
                        ) {
                            Text("Hasło", fontSize = 12.sp)
                        }
                        Button(
                            onClick = { authMethod = SSHAuthMethod.PRIVATE_KEY },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (authMethod == SSHAuthMethod.PRIVATE_KEY) AccentBlue else Color.White.copy(alpha = 0.1f)
                            )
                        ) {
                            Text("Klucz SSH", fontSize = 12.sp)
                        }
                    }
                }
                if (authMethod == SSHAuthMethod.PASSWORD) {
                    item {
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text(if (server == null) "Hasło" else "Nowe hasło (opcjonalne)") },
                            visualTransformation = PasswordVisualTransformation(),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                } else {
                    item {
                        if (keys.isEmpty()) {
                            Text(
                                text = "Brak zaimportowanych kluczy. Zaimportuj klucz w Ustawieniach.",
                                color = Color.Gray,
                                fontSize = 12.sp
                            )
                        } else {
                            Text(text = "Wybierz klucz prywatny: ${selectedKeyName ?: ""}", color = Color.White, fontSize = 13.sp)
                            keys.forEach { key ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { selectedKeyName = key }
                                        .padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = if (key == selectedKeyName) "● $key" else "○ $key",
                                        color = if (key == selectedKeyName) AccentBlue else Color.Gray,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        }
                    }
                }
                item {
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Notatki (opcjonalnie)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank() && host.isNotBlank()) {
                        val s = SSHServer(
                            id = server?.id ?: java.util.UUID.randomUUID().toString(),
                            name = name.trim(),
                            host = host.trim(),
                            port = port.toIntOrNull() ?: 22,
                            username = username.trim(),
                            authMethod = authMethod,
                            keyName = if (authMethod == SSHAuthMethod.PRIVATE_KEY) selectedKeyName else null,
                            groupId = selectedGroupId,
                            notes = notes.trim()
                        )
                        onSave(s, password.ifEmpty { null })
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = AccentBlue)
            ) {
                Text("Zapisz")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Anuluj", color = Color.Gray)
            }
        },
        containerColor = SurfaceDark,
        shape = RoundedCornerShape(16.dp)
    )
}
