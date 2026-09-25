package com.marekfx77.sshmobile.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.marekfx77.sshmobile.ui.components.ResourceStatsCard
import com.marekfx77.sshmobile.ui.components.TerminalKeyToolbar
import com.marekfx77.sshmobile.ui.theme.AccentBlue
import com.marekfx77.sshmobile.ui.theme.AccentGreen
import com.marekfx77.sshmobile.ui.theme.AccentRed
import com.marekfx77.sshmobile.ui.theme.BgDark
import com.marekfx77.sshmobile.ui.theme.SurfaceDark
import com.marekfx77.sshmobile.ui.theme.SurfaceBorderDark
import com.marekfx77.sshmobile.ui.theme.SurfaceCardDark
import com.marekfx77.sshmobile.viewmodel.SSHViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TerminalScreen(
    viewModel: SSHViewModel,
    modifier: Modifier = Modifier
) {
    val activeSessions by viewModel.activeSessions.collectAsState()
    val selectedId by viewModel.selectedSessionId.collectAsState()
    val selectedSession = activeSessions.firstOrNull { it.id == selectedId } ?: activeSessions.firstOrNull()

    val terminalScheme by viewModel.terminalColorScheme.collectAsState()
    val fontSize by viewModel.terminalFontSize.collectAsState()

    var commandInput by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Terminal PTY",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = Color.White
                    )
                },
                actions = {
                    if (selectedSession != null) {
                        IconButton(onClick = { selectedSession.terminal.clearLines() }) {
                            Icon(imageVector = Icons.Default.ClearAll, contentDescription = "Wyczyść", tint = Color.Gray)
                        }
                        IconButton(onClick = { viewModel.closeSession(selectedSession.id) }) {
                            Icon(imageVector = Icons.Default.Stop, contentDescription = "Rozłącz", tint = AccentRed)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BgDark)
            )
        },
        containerColor = BgDark,
        modifier = modifier
    ) { paddingValues ->
        if (selectedSession == null) {
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
                        imageVector = Icons.Default.Terminal,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(54.dp)
                    )
                    Text(
                        text = "Brak aktywnych sesji",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Przejdź do zakładki Serwery i kliknij Połącz.",
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                    Button(
                        onClick = { viewModel.selectTab(0) },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentBlue)
                    ) {
                        Text(text = "Przejdź do serwerów")
                    }
                }
            }
        } else {
            val lines by selectedSession.terminal.lines.collectAsState()
            val isConnected by selectedSession.terminal.isConnected.collectAsState()
            val isConnecting by selectedSession.terminal.isConnecting.collectAsState()
            val statusMessage by selectedSession.terminal.statusMessage.collectAsState()
            val stats by selectedSession.statsPoller.stats.collectAsState()

            LaunchedEffect(lines.size) {
                if (lines.isNotEmpty()) {
                    listState.animateScrollToItem(lines.size - 1)
                }
            }

            val bgColor = Color(terminalScheme.bgHex)
            val textColor = Color(terminalScheme.textHex)
            val promptColor = Color(terminalScheme.promptHex)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Session Tabs
                if (activeSessions.size > 1) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .background(SurfaceCardDark)
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        activeSessions.forEach { session ->
                            val isSel = session.id == selectedSession.id
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSel) AccentBlue else Color.White.copy(alpha = 0.08f))
                                    .clickable { viewModel.selectSession(session.id) }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = session.server.name,
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Zamknij",
                                        tint = Color.White.copy(alpha = 0.7f),
                                        modifier = Modifier
                                            .size(14.dp)
                                            .clickable { viewModel.closeSession(session.id) }
                                    )
                                }
                            }
                        }
                    }
                }

                // Status Bar & Compact Stats
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Black.copy(alpha = 0.8f))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(
                                    when {
                                        isConnected -> AccentGreen
                                        isConnecting -> Color(0xFFFF9F0A)
                                        else -> AccentRed
                                    },
                                    CircleShape
                                )
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = statusMessage,
                            color = Color.LightGray,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    stats?.let { s ->
                        Text(
                            text = "CPU: ${s.cpuText} | RAM: ${s.memText}",
                            color = Color.Gray,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                // Terminal lines canvas
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .background(bgColor)
                        .padding(horizontal = 10.dp, vertical = 8.dp)
                ) {
                    items(lines, key = { it.id }) { line ->
                        Text(
                            text = line.text,
                            color = when {
                                line.isError -> AccentRed
                                line.isCommand -> promptColor
                                else -> textColor
                            },
                            fontSize = fontSize.sp,
                            fontFamily = FontFamily.Monospace,
                            lineHeight = (fontSize * 1.3f).sp
                        )
                    }
                }

                // Input Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SurfaceDark)
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = commandInput,
                        onValueChange = { commandInput = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Wpisz komendę…", color = Color.Gray, fontSize = 13.sp) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(onSend = {
                            if (commandInput.isNotBlank()) {
                                selectedSession.terminal.sendCommand(commandInput)
                                commandInput = ""
                            }
                        }),
                        colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedContainerColor = Color.Black.copy(alpha = 0.5f),
                            unfocusedContainerColor = Color.Black.copy(alpha = 0.5f),
                            focusedBorderColor = AccentBlue,
                            unfocusedBorderColor = SurfaceBorderDark
                        ),
                        shape = RoundedCornerShape(8.dp)
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    IconButton(
                        onClick = {
                            if (commandInput.isNotBlank()) {
                                selectedSession.terminal.sendCommand(commandInput)
                                commandInput = ""
                            }
                        },
                        modifier = Modifier
                            .size(44.dp)
                            .background(AccentBlue, RoundedCornerShape(8.dp))
                    ) {
                        Icon(imageVector = Icons.Default.Send, contentDescription = "Wyślij", tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                }

                // Special Keys Toolbar (Ctrl, Nav, Alt, Fn)
                TerminalKeyToolbar(
                    onSendBytes = { bytes ->
                        selectedSession.terminal.sendBytes(bytes)
                    }
                )
            }
        }
    }
}
