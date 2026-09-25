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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.marekfx77.sshmobile.model.AppThemeMode
import com.marekfx77.sshmobile.model.TerminalColorScheme
import com.marekfx77.sshmobile.ui.components.FactoryResetConfirmDialog
import com.marekfx77.sshmobile.ui.theme.AccentBlue
import com.marekfx77.sshmobile.ui.theme.AccentRed
import com.marekfx77.sshmobile.ui.theme.BgDark
import com.marekfx77.sshmobile.ui.theme.SurfaceBorderDark
import com.marekfx77.sshmobile.ui.theme.SurfaceCardDark
import com.marekfx77.sshmobile.ui.theme.SurfaceDark
import com.marekfx77.sshmobile.viewmodel.SSHViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SSHViewModel,
    modifier: Modifier = Modifier
) {
    val themeMode by viewModel.appearanceTheme.collectAsState()
    val terminalScheme by viewModel.terminalColorScheme.collectAsState()
    val fontSize by viewModel.terminalFontSize.collectAsState()
    val useBiometrics by viewModel.useBiometrics.collectAsState()
    val timeout by viewModel.sshTimeout.collectAsState()
    val privateKeys by viewModel.privateKeyNames.collectAsState()

    var showKeyImportDialog by remember { mutableStateOf(false) }
    var showResetConfirm by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Ustawienia",
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        color = Color.White
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BgDark)
            )
        },
        containerColor = BgDark,
        modifier = modifier
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Appearance Section
            item {
                SectionHeader("WYGLĄD")
                SettingsCard {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        // Terminal Theme Picker
                        Text(text = "Motyw terminala", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            TerminalColorScheme.values().take(3).forEach { scheme ->
                                val isSel = scheme == terminalScheme
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSel) AccentBlue else Color.White.copy(alpha = 0.08f))
                                        .clickable { viewModel.setTerminalColorScheme(scheme) }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = scheme.displayName,
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            TerminalColorScheme.values().drop(3).forEach { scheme ->
                                val isSel = scheme == terminalScheme
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSel) AccentBlue else Color.White.copy(alpha = 0.08f))
                                        .clickable { viewModel.setTerminalColorScheme(scheme) }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = scheme.displayName,
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        }

                        // Font size slider & preview
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "Rozmiar czcionki", color = Color.White, fontSize = 14.sp)
                            Text(text = "${fontSize.toInt()} sp", color = AccentBlue, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }

                        Slider(
                            value = fontSize,
                            onValueChange = { viewModel.setTerminalFontSize(it) },
                            valueRange = 9f..22f,
                            steps = 12,
                            colors = SliderDefaults.colors(
                                thumbColor = AccentBlue,
                                activeTrackColor = AccentBlue,
                                inactiveTrackColor = Color.White.copy(alpha = 0.1f)
                            )
                        )

                        // Preview box
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(terminalScheme.bgHex))
                                .padding(10.dp)
                        ) {
                            Text(
                                text = "root@server:~# echo \"SSH Mobile terminal preview\"\nSSH Mobile terminal preview",
                                color = Color(terminalScheme.textHex),
                                fontSize = fontSize.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }

            // Security Section
            item {
                SectionHeader("BEZPIECZEŃSTWO")
                SettingsCard {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.Fingerprint, contentDescription = null, tint = AccentBlue)
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(text = "Blokada biometryczna", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                                    Text(text = "Odcisk palca / Rozpoznawanie twarzy", color = Color.Gray, fontSize = 11.sp)
                                }
                            }
                            Switch(
                                checked = useBiometrics,
                                onCheckedChange = { viewModel.setUseBiometrics(it) },
                                colors = SwitchDefaults.colors(checkedThumbColor = AccentBlue, checkedTrackColor = AccentBlue.copy(alpha = 0.5f))
                            )
                        }
                    }
                }
            }

            // SSH Keys Section
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SectionHeader("KLUCZE SSH")
                    IconButton(onClick = { showKeyImportDialog = true }) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "Importuj klucz", tint = AccentBlue)
                    }
                }

                SettingsCard {
                    if (privateKeys.isEmpty()) {
                        Text(
                            text = "Brak zaimportowanych kluczy prywatnych SSH.",
                            color = Color.Gray,
                            fontSize = 13.sp
                        )
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            privateKeys.forEach { keyName ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(imageVector = Icons.Default.Key, contentDescription = null, tint = Color(0xFFFFD60A), modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(text = keyName, color = Color.White, fontSize = 14.sp, fontFamily = FontFamily.Monospace)
                                    }
                                    IconButton(onClick = { viewModel.deletePrivateKey(keyName) }, modifier = Modifier.size(28.dp)) {
                                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Usuń", tint = AccentRed.copy(alpha = 0.7f), modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Factory Reset Section
            item {
                SectionHeader("RESET I DANE")
                SettingsCard {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "Przywracanie stanu początkowego",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Jeśli zapomnisz hasła lub chcesz całkowicie wyczyścić aplikację, możesz przywrócić ustawienia fabryczne.",
                            color = Color.Gray,
                            fontSize = 12.sp
                        )
                        Button(
                            onClick = { showResetConfirm = true },
                            colors = ButtonDefaults.buttonColors(containerColor = AccentRed),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(imageVector = Icons.Default.RestartAlt, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "Zresetuj aplikację do ustawień fabrycznych", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // About App
            item {
                SectionHeader("O APLIKACJI")
                SettingsCard {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(text = "SSH Mobile (Android Native)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text(text = "Wersja 1.0.0 (Kotlin + Jetpack Compose)", color = Color.Gray, fontSize = 12.sp)
                        Text(text = "Silnik SSH: JSch + PTY xterm-256color + SFTP", color = Color.Gray, fontSize = 12.sp)
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // Key Import Dialog
    if (showKeyImportDialog) {
        var keyName by remember { mutableStateOf("") }
        var keyPem by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showKeyImportDialog = false },
            title = { Text(text = "Importuj klucz SSH", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = keyName,
                        onValueChange = { keyName = it },
                        label = { Text("Nazwa klucza (np. id_rsa, id_ed25519)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = keyPem,
                        onValueChange = { keyPem = it },
                        label = { Text("Klucz prywatny PEM") },
                        modifier = Modifier.fillMaxWidth().height(150.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (keyName.isNotBlank() && keyPem.isNotBlank()) {
                            viewModel.importPrivateKey(keyName.trim(), keyPem.trim())
                            showKeyImportDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentBlue)
                ) {
                    Text("Zapisz w Keystore")
                }
            },
            dismissButton = {
                TextButton(onClick = { showKeyImportDialog = false }) {
                    Text("Anuluj", color = Color.Gray)
                }
            },
            containerColor = SurfaceDark
        )
    }

    // Factory Reset Confirmation
    if (showResetConfirm) {
        FactoryResetConfirmDialog(
            onConfirm = {
                showResetConfirm = false
                viewModel.performFactoryReset {
                    // Reset completed
                }
            },
            onDismiss = { showResetConfirm = false }
        )
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        color = Color.Gray,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(bottom = 6.dp)
    )
}

@Composable
private fun SettingsCard(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceCardDark)
            .border(1.dp, SurfaceBorderDark, RoundedCornerShape(12.dp))
            .padding(14.dp)
    ) {
        content()
    }
}
