package com.marekfx77.sshmobile.ui.screens

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.RestartAlt
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.marekfx77.sshmobile.model.TerminalColorScheme
import com.marekfx77.sshmobile.ui.components.FactoryResetConfirmDialog
import com.marekfx77.sshmobile.ui.theme.AccentBlue
import com.marekfx77.sshmobile.ui.theme.AccentRed
import com.marekfx77.sshmobile.ui.theme.BgDark
import com.marekfx77.sshmobile.ui.theme.SeparatorDark
import com.marekfx77.sshmobile.ui.theme.SurfaceCardDark
import com.marekfx77.sshmobile.ui.theme.SurfaceDark
import com.marekfx77.sshmobile.ui.theme.TextSecondaryDark
import com.marekfx77.sshmobile.viewmodel.SSHViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SSHViewModel,
    modifier: Modifier = Modifier
) {
    val terminalScheme by viewModel.terminalColorScheme.collectAsState()
    val fontSize by viewModel.terminalFontSize.collectAsState()
    val isPinEnabled by viewModel.isPinEnabled.collectAsState()
    val privateKeys by viewModel.privateKeyNames.collectAsState()

    var showKeyImportDialog by remember { mutableStateOf(false) }
    var showResetConfirm by remember { mutableStateOf(false) }
    var showSetPinDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Ustawienia",
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
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
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Security & PIN Section
            item {
                IosSectionHeader("BEZPIECZEŃSTWO I KOD PIN")
                IosGroupedCard {
                    Column {
                        // PIN Lock toggle
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(30.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(AccentBlue),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(text = "Blokada kodem PIN", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                                    Text(
                                        text = if (isPinEnabled) "Włączona (4 cyfry)" else "Wyłączona",
                                        color = TextSecondaryDark,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                            Switch(
                                checked = isPinEnabled,
                                onCheckedChange = { enabled ->
                                    if (enabled) {
                                        showSetPinDialog = true
                                    } else {
                                        viewModel.disablePin()
                                    }
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = AccentBlue,
                                    uncheckedThumbColor = Color.White,
                                    uncheckedTrackColor = Color(0xFF38383A)
                                )
                            )
                        }

                        // Change PIN row
                        if (isPinEnabled) {
                            IosDivider()
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { showSetPinDialog = true }
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = "Zmień kod PIN", color = Color.White, fontSize = 15.sp)
                                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color(0xFF48484A), modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }

            // Appearance Section
            item {
                IosSectionHeader("WYGLĄD TERMINALA")
                IosGroupedCard {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(text = "Paleta kolorów", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)

                        // Theme selector pills
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            TerminalColorScheme.values().take(3).forEach { scheme ->
                                val isSel = scheme == terminalScheme
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSel) AccentBlue else Color(0xFF2C2C2E))
                                        .clickable { viewModel.setTerminalColorScheme(scheme) }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = scheme.displayName,
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            TerminalColorScheme.values().drop(3).forEach { scheme ->
                                val isSel = scheme == terminalScheme
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSel) AccentBlue else Color(0xFF2C2C2E))
                                        .clickable { viewModel.setTerminalColorScheme(scheme) }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = scheme.displayName,
                                        color = Color.White,
                                        fontSize = 12.sp,
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
                            Text(text = "${fontSize.toInt()} pt", color = AccentBlue, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }

                        Slider(
                            value = fontSize,
                            onValueChange = { viewModel.setTerminalFontSize(it) },
                            valueRange = 9f..22f,
                            steps = 12,
                            colors = SliderDefaults.colors(
                                thumbColor = Color.White,
                                activeTrackColor = AccentBlue,
                                inactiveTrackColor = Color(0xFF38383A)
                            )
                        )

                        // Terminal preview
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(terminalScheme.bgHex))
                                .padding(10.dp)
                        ) {
                            Text(
                                text = "root@server:~# uname -a\nLinux server 6.1.0-21-amd64 x86_64",
                                color = Color(terminalScheme.textHex),
                                fontSize = fontSize.sp,
                                fontFamily = FontFamily.Monospace
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
                    IosSectionHeader("KLUCZE SSH")
                    IconButton(onClick = { showKeyImportDialog = true }) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "Importuj klucz", tint = AccentBlue)
                    }
                }

                IosGroupedCard {
                    if (privateKeys.isEmpty()) {
                        Text(
                            text = "Brak zaimportowanych kluczy prywatnych.",
                            color = TextSecondaryDark,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(16.dp)
                        )
                    } else {
                        Column {
                            privateKeys.forEachIndexed { index, keyName ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(imageVector = Icons.Default.Key, contentDescription = null, tint = Color(0xFFFFD60A), modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(text = keyName, color = Color.White, fontSize = 14.sp, fontFamily = FontFamily.Monospace)
                                    }
                                    IconButton(onClick = { viewModel.deletePrivateKey(keyName) }, modifier = Modifier.size(28.dp)) {
                                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Usuń", tint = AccentRed, modifier = Modifier.size(16.dp))
                                    }
                                }
                                if (index < privateKeys.size - 1) {
                                    IosDivider()
                                }
                            }
                        }
                    }
                }
            }

            // Factory Reset Section
            item {
                IosSectionHeader("RESET I DANE")
                IosGroupedCard {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Przywracanie ustawień fabrycznych",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Usunięcie wszystkich haseł z Keystore, kluczy, serwerów i kodu PIN z zachowaniem prawdziwego ładowania postępu.",
                            color = TextSecondaryDark,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                        Button(
                            onClick = { showResetConfirm = true },
                            colors = ButtonDefaults.buttonColors(containerColor = AccentRed),
                            modifier = Modifier.fillMaxWidth().height(44.dp),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(imageVector = Icons.Default.RestartAlt, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "Zresetuj aplikację", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // About Section
            item {
                IosSectionHeader("O APLIKACJI")
                IosGroupedCard {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(text = "SSH Mobile (1:1 iOS Design)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text(text = "Wersja 1.0.0 (Kotlin + Jetpack Compose)", color = TextSecondaryDark, fontSize = 13.sp)
                    }
                }
                Spacer(modifier = Modifier.height(28.dp))
            }
        }
    }

    // Set PIN Dialog
    if (showSetPinDialog) {
        SetPinDialog(
            onDismiss = { showSetPinDialog = false },
            onPinSaved = { pin ->
                viewModel.setPin(pin)
                showSetPinDialog = false
            }
        )
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
                        label = { Text("Nazwa klucza") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = keyPem,
                        onValueChange = { keyPem = it },
                        label = { Text("Klucz PEM") },
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
                    Text("Zapisz")
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
private fun SetPinDialog(
    onDismiss: () -> Unit,
    onPinSaved: (String) -> Unit
) {
    var step by remember { mutableStateOf(1) } // 1: enter, 2: confirm
    var pin1 by remember { mutableStateOf("") }
    var pin2 by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (step == 1) "Ustaw kod PIN" else "Potwierdź kod PIN",
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = if (step == 1) "Wprowadź 4-cyfrowy kod PIN do odblokowywania aplikacji:" else "Wprowadź ponownie ten sam 4-cyfrowy kod PIN:",
                    color = TextSecondaryDark,
                    fontSize = 13.sp
                )

                OutlinedTextField(
                    value = if (step == 1) pin1 else pin2,
                    onValueChange = {
                        val filtered = it.filter { ch -> ch.isDigit() }.take(4)
                        if (step == 1) pin1 = filtered else pin2 = filtered
                        errorMsg = null
                    },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    label = { Text("4 cyfry") },
                    modifier = Modifier.fillMaxWidth()
                )

                errorMsg?.let {
                    Text(text = it, color = AccentRed, fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (step == 1) {
                        if (pin1.length == 4) {
                            step = 2
                        } else {
                            errorMsg = "Kod PIN musi mieć dokładnie 4 cyfry."
                        }
                    } else {
                        if (pin2 == pin1) {
                            onPinSaved(pin1)
                        } else {
                            errorMsg = "Wprowadzone kody PIN nie są identyczne."
                            pin2 = ""
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = AccentBlue)
            ) {
                Text(if (step == 1) "Dalej" else "Zapisz PIN")
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

@Composable
private fun IosSectionHeader(title: String) {
    Text(
        text = title,
        color = TextSecondaryDark,
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(start = 6.dp, bottom = 4.dp)
    )
}

@Composable
private fun IosGroupedCard(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceCardDark)
    ) {
        content()
    }
}

@Composable
private fun IosDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 58.dp)
            .height(0.5.dp)
            .background(SeparatorDark)
    )
}
