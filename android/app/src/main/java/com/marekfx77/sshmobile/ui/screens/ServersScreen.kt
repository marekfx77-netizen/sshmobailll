package com.marekfx77.sshmobile.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.marekfx77.sshmobile.model.SSHAuthMethod
import com.marekfx77.sshmobile.model.SSHServer
import com.marekfx77.sshmobile.model.SSHServerGroup
import com.marekfx77.sshmobile.ui.components.ResourceStatsCard
import com.marekfx77.sshmobile.ui.theme.AccentBlue
import com.marekfx77.sshmobile.ui.theme.AccentGreen
import com.marekfx77.sshmobile.ui.theme.AccentRed
import com.marekfx77.sshmobile.ui.theme.BgDark
import com.marekfx77.sshmobile.ui.theme.SeparatorDark
import com.marekfx77.sshmobile.ui.theme.SurfaceCardDark
import com.marekfx77.sshmobile.ui.theme.SurfaceDark
import com.marekfx77.sshmobile.ui.theme.SurfaceSecondaryDark
import com.marekfx77.sshmobile.ui.theme.TextSecondaryDark
import com.marekfx77.sshmobile.viewmodel.SSHViewModel
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServersScreen(
    viewModel: SSHViewModel,
    modifier: Modifier = Modifier
) {
    val servers by viewModel.servers.collectAsState()
    val groups by viewModel.groups.collectAsState()
    val activeSessions by viewModel.activeSessions.collectAsState()

    var showMenu by remember { mutableStateOf(false) }
    var showAddServerDialog by remember { mutableStateOf(false) }
    var showAddGroupDialog by remember { mutableStateOf(false) }
    var selectedServerForDetail by remember { mutableStateOf<SSHServer?>(null) }
    var serverToEdit by remember { mutableStateOf<SSHServer?>(null) }
    var serverToConnectPrompt by remember { mutableStateOf<SSHServer?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Serwery",
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                        color = Color.White
                    )
                },
                actions = {
                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Dodaj",
                                tint = AccentBlue,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false },
                            modifier = Modifier.background(SurfaceDark)
                        ) {
                            DropdownMenuItem(
                                text = { Text("Dodaj serwer", color = Color.White) },
                                leadingIcon = { Icon(Icons.Default.Storage, contentDescription = null, tint = AccentBlue) },
                                onClick = {
                                    showMenu = false
                                    showAddServerDialog = true
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Dodaj grupę", color = Color.White) },
                                leadingIcon = { Icon(Icons.Default.CreateNewFolder, contentDescription = null, tint = AccentBlue) },
                                onClick = {
                                    showMenu = false
                                    showAddGroupDialog = true
                                }
                            )
                        }
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
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.padding(32.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .background(AccentBlue.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Storage,
                            contentDescription = null,
                            tint = AccentBlue,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                    Text(
                        text = "Brak serwerów",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Dodaj swój pierwszy serwer SSH, aby rozpocząć połączenie.",
                        color = TextSecondaryDark,
                        fontSize = 14.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Button(
                        onClick = { showAddServerDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(text = "Dodaj serwer", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Grouped Content (iOS Inset Grouped style)
                groups.forEach { group ->
                    val groupServers = servers.filter { it.groupId == group.id }
                    val groupColor = try {
                        Color(android.graphics.Color.parseColor("#${group.colorHex}"))
                    } catch (_: Exception) {
                        AccentBlue
                    }

                    item(key = group.id) {
                        Column {
                            // Section Header with circle and count
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { viewModel.toggleGroupExpanded(group) }
                                    .padding(vertical = 6.dp, horizontal = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .background(groupColor, CircleShape)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = group.name,
                                    color = Color.White,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "(${groupServers.size})",
                                    color = TextSecondaryDark,
                                    fontSize = 13.sp
                                )
                                Spacer(modifier = Modifier.weight(1f))
                                Icon(
                                    imageVector = if (group.isExpanded) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowRight,
                                    contentDescription = null,
                                    tint = TextSecondaryDark,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            // Inset Grouped Container
                            if (group.isExpanded && groupServers.isNotEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(SurfaceCardDark)
                                ) {
                                    Column {
                                        groupServers.forEachIndexed { index, server ->
                                            val isConnected = activeSessions.any { it.server.id == server.id && it.terminal.isConnected.collectAsState().value }
                                            IosServerRow(
                                                server = server,
                                                isConnected = isConnected,
                                                onClick = { selectedServerForDetail = server }
                                            )
                                            if (index < groupServers.size - 1) {
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(start = 58.dp)
                                                        .height(0.5.dp)
                                                        .background(SeparatorDark)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Ungrouped Servers Section
                val ungroupedServers = servers.filter { it.groupId == null }
                if (ungroupedServers.isNotEmpty()) {
                    item {
                        Column {
                            if (groups.isNotEmpty()) {
                                Text(
                                    text = "INNE",
                                    color = TextSecondaryDark,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(SurfaceCardDark)
                            ) {
                                Column {
                                    ungroupedServers.forEachIndexed { index, server ->
                                        val isConnected = activeSessions.any { it.server.id == server.id && it.terminal.isConnected.collectAsState().value }
                                        IosServerRow(
                                            server = server,
                                            isConnected = isConnected,
                                            onClick = { selectedServerForDetail = server }
                                        )
                                        if (index < ungroupedServers.size - 1) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(start = 58.dp)
                                                    .height(0.5.dp)
                                                    .background(SeparatorDark)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }

    // iOS-Style Server Detail BottomSheet
    selectedServerForDetail?.let { server ->
        val activeSession = activeSessions.firstOrNull { it.server.id == server.id }
        val isConnected = activeSession?.terminal?.isConnected?.collectAsState()?.value == true

        ModalBottomSheet(
            onDismissRequest = { selectedServerForDetail = null },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = SurfaceDark
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 36.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                val serverColor = try {
                    Color(android.graphics.Color.parseColor("#${server.colorHex}"))
                } catch (_: Exception) {
                    AccentBlue
                }

                // Top menu
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    var showDetailMenu by remember { mutableStateOf(false) }
                    Box {
                        IconButton(onClick = { showDetailMenu = true }) {
                            Icon(imageVector = Icons.Default.MoreVert, contentDescription = "Więcej", tint = Color.White)
                        }
                        DropdownMenu(
                            expanded = showDetailMenu,
                            onDismissRequest = { showDetailMenu = false },
                            modifier = Modifier.background(SurfaceSecondaryDark)
                        ) {
                            DropdownMenuItem(
                                text = { Text("Edytuj serwer", color = Color.White) },
                                leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null, tint = AccentBlue) },
                                onClick = {
                                    showDetailMenu = false
                                    serverToEdit = server
                                    selectedServerForDetail = null
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Usuń serwer", color = AccentRed) },
                                leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = AccentRed) },
                                onClick = {
                                    showDetailMenu = false
                                    viewModel.deleteServer(server)
                                    selectedServerForDetail = null
                                }
                            )
                        }
                    }
                }

                // Large Server Icon
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(serverColor.copy(alpha = 0.15f))
                        .border(1.dp, serverColor.copy(alpha = 0.4f), RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Storage,
                        contentDescription = null,
                        tint = serverColor,
                        modifier = Modifier.size(38.dp)
                    )
                }

                // Server Name
                Text(
                    text = server.name,
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )

                // Detail Inset Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(SurfaceSecondaryDark)
                        .padding(14.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        IosDetailRow(label = "Adres serwera", value = server.host)
                        IosDetailRow(label = "Port", value = "${server.port}")
                        IosDetailRow(label = "Użytkownik", value = server.username)
                        IosDetailRow(
                            label = "Autoryzacja",
                            value = if (server.authMethod == SSHAuthMethod.PASSWORD) "Hasło" else "Klucz SSH"
                        )
                        if (server.notes.isNotBlank()) {
                            IosDetailRow(label = "Notatki", value = server.notes)
                        }
                    }
                }

                // Connected Actions or Connect Button
                if (isConnected && activeSession != null) {
                    val stats by activeSession.statsPoller.stats.collectAsState()
                    ResourceStatsCard(stats = stats)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = {
                                selectedServerForDetail = null
                                viewModel.selectSession(activeSession.id)
                                viewModel.selectTab(1)
                            },
                            modifier = Modifier.weight(1f).height(46.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Terminal, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Terminal", fontWeight = FontWeight.SemiBold)
                        }

                        Button(
                            onClick = {
                                selectedServerForDetail = null
                                viewModel.openSftpForServer(server)
                            },
                            modifier = Modifier.weight(1f).height(46.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SurfaceSecondaryDark),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Folder, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Pliki SFTP", color = Color.White, fontWeight = FontWeight.SemiBold)
                        }
                    }
                } else {
                    Button(
                        onClick = {
                            val savedPwd = viewModel.secureStorage.getPassword(server.id)
                            selectedServerForDetail = null
                            if (server.authMethod == SSHAuthMethod.PASSWORD && savedPwd.isNullOrEmpty()) {
                                serverToConnectPrompt = server
                            } else {
                                viewModel.connectToServer(server)
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Połącz", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
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
private fun IosServerRow(
    server: SSHServer,
    isConnected: Boolean,
    onClick: () -> Unit
) {
    val serverColor = try {
        Color(android.graphics.Color.parseColor("#${server.colorHex}"))
    } catch (_: Exception) {
        AccentBlue
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon badge
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(serverColor.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Storage,
                contentDescription = null,
                tint = serverColor,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Name & Address
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = server.name,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "${server.host}:${server.port}",
                color = TextSecondaryDark,
                fontSize = 13.sp
            )
        }

        // Status dot
        if (isConnected) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(AccentGreen, CircleShape)
            )
            Spacer(modifier = Modifier.width(8.dp))
        }

        // Chevron
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = Color(0xFF48484A),
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
private fun IosDetailRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, color = TextSecondaryDark, fontSize = 14.sp)
        Text(text = value, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
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
                text = if (server == null) "Dodaj serwer" else "Edytuj serwer",
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
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
                                text = "Brak kluczy. Zaimportuj klucz w Ustawieniach.",
                                color = Color.Gray,
                                fontSize = 12.sp
                            )
                        } else {
                            Text(text = "Wybierz klucz: ${selectedKeyName ?: ""}", color = Color.White, fontSize = 13.sp)
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
                            id = server?.id ?: UUID.randomUUID().toString(),
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
