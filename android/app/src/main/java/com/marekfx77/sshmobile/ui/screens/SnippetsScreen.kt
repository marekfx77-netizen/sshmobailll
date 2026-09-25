package com.marekfx77.sshmobile.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.marekfx77.sshmobile.model.SSHSnippet
import com.marekfx77.sshmobile.ui.theme.AccentBlue
import com.marekfx77.sshmobile.ui.theme.AccentGreen
import com.marekfx77.sshmobile.ui.theme.AccentRed
import com.marekfx77.sshmobile.ui.theme.AccentYellow
import com.marekfx77.sshmobile.ui.theme.BgDark
import com.marekfx77.sshmobile.ui.theme.SurfaceBorderDark
import com.marekfx77.sshmobile.ui.theme.SurfaceCardDark
import com.marekfx77.sshmobile.ui.theme.SurfaceDark
import com.marekfx77.sshmobile.viewmodel.SSHViewModel
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SnippetsScreen(
    viewModel: SSHViewModel,
    modifier: Modifier = Modifier
) {
    val snippets by viewModel.snippets.collectAsState()
    val activeSessions by viewModel.activeSessions.collectAsState()

    var showAddSnippetDialog by remember { mutableStateOf(false) }
    var snippetToEdit by remember { mutableStateOf<SSHSnippet?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Snippety",
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        color = Color.White
                    )
                },
                actions = {
                    IconButton(onClick = { showAddSnippetDialog = true }) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "Dodaj snippet", tint = AccentBlue)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BgDark)
            )
        },
        containerColor = BgDark,
        modifier = modifier
    ) { paddingValues ->
        if (snippets.isEmpty()) {
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
                        imageVector = Icons.Default.Bolt,
                        contentDescription = null,
                        tint = AccentYellow,
                        modifier = Modifier.size(54.dp)
                    )
                    Text(
                        text = "Brak snippetów",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Dodaj swoje ulubione komendy do szybkiego uruchamiania.",
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                    Button(
                        onClick = { showAddSnippetDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentBlue)
                    ) {
                        Text(text = "Dodaj snippet")
                    }
                }
            }
        } else {
            val groupedSnippets = snippets.groupBy { it.groupName.ifBlank { "Ogólne" } }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                groupedSnippets.forEach { (groupName, list) ->
                    item {
                        Text(
                            text = groupName,
                            color = Color.Gray,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(top = 8.dp, bottom = 2.dp)
                        )
                    }

                    items(list, key = { it.id }) { snippet ->
                        SnippetCard(
                            snippet = snippet,
                            hasActiveSession = activeSessions.isNotEmpty(),
                            onRunClick = { viewModel.sendSnippetToActiveTerminal(snippet) },
                            onEditClick = { snippetToEdit = snippet },
                            onDeleteClick = { viewModel.deleteSnippet(snippet) }
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }

    // Add Snippet Dialog
    if (showAddSnippetDialog) {
        SnippetFormDialog(
            snippet = null,
            onDismiss = { showAddSnippetDialog = false },
            onSave = {
                viewModel.addSnippet(it)
                showAddSnippetDialog = false
            }
        )
    }

    // Edit Snippet Dialog
    snippetToEdit?.let { s ->
        SnippetFormDialog(
            snippet = s,
            onDismiss = { snippetToEdit = null },
            onSave = {
                viewModel.updateSnippet(it)
                snippetToEdit = null
            }
        )
    }
}

@Composable
private fun SnippetCard(
    snippet: SSHSnippet,
    hasActiveSession: Boolean,
    onRunClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceCardDark)
            .border(1.dp, SurfaceBorderDark, RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Bolt,
                    contentDescription = null,
                    tint = AccentYellow,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = snippet.name,
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )

                IconButton(onClick = onEditClick, modifier = Modifier.size(28.dp)) {
                    Icon(imageVector = Icons.Default.Edit, contentDescription = "Edytuj", tint = Color.Gray, modifier = Modifier.size(16.dp))
                }
                IconButton(onClick = onDeleteClick, modifier = Modifier.size(28.dp)) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Usuń", tint = AccentRed.copy(alpha = 0.7f), modifier = Modifier.size(16.dp))
                }
            }

            if (snippet.description.isNotBlank()) {
                Text(
                    text = snippet.description,
                    color = Color.Gray,
                    fontSize = 12.sp
                )
            }

            // Command Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color.Black.copy(alpha = 0.4f))
                    .border(0.5.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(6.dp))
                    .padding(8.dp)
            ) {
                Text(
                    text = snippet.command,
                    color = Color(0xFF64D2FF),
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            // Run Button
            Button(
                onClick = onRunClick,
                modifier = Modifier.fillMaxWidth().height(36.dp),
                colors = ButtonDefaults.buttonColors(containerColor = if (hasActiveSession) AccentGreen else Color.White.copy(alpha = 0.1f)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (hasActiveSession) "Uruchom w terminalu" else "Uruchom (wymaga aktywnej sesji)",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun SnippetFormDialog(
    snippet: SSHSnippet?,
    onDismiss: () -> Unit,
    onSave: (SSHSnippet) -> Unit
) {
    var name by remember { mutableStateOf(snippet?.name ?: "") }
    var command by remember { mutableStateOf(snippet?.command ?: "") }
    var description by remember { mutableStateOf(snippet?.description ?: "") }
    var groupName by remember { mutableStateOf(snippet?.groupName ?: "Ogólne") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (snippet == null) "Nowy snippet" else "Edytuj snippet",
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nazwa") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = command,
                    onValueChange = { command = it },
                    label = { Text("Komenda shell") },
                    modifier = Modifier.fillMaxWidth().height(100.dp)
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Opis (opcjonalnie)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = groupName,
                    onValueChange = { groupName = it },
                    label = { Text("Kategoria / Grupa") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank() && command.isNotBlank()) {
                        val s = SSHSnippet(
                            id = snippet?.id ?: UUID.randomUUID().toString(),
                            name = name.trim(),
                            command = command.trim(),
                            description = description.trim(),
                            groupName = groupName.trim().ifEmpty { "Ogólne" }
                        )
                        onSave(s)
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
