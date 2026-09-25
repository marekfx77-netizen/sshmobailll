package com.marekfx77.sshmobile.ui.components

import android.view.HapticFeedbackConstants
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.marekfx77.sshmobile.ui.theme.AccentBlue
import com.marekfx77.sshmobile.ui.theme.AccentGreen
import com.marekfx77.sshmobile.ui.theme.AccentOrange
import com.marekfx77.sshmobile.ui.theme.AccentPurple
import com.marekfx77.sshmobile.ui.theme.AccentRed
import com.marekfx77.sshmobile.ui.theme.AccentYellow

data class KeyItem(
    val label: String,
    val bytes: ByteArray,
    val color: Color
)

@Composable
fun TerminalKeyToolbar(
    onSendBytes: (ByteArray) -> Unit,
    modifier: Modifier = Modifier
) {
    val view = LocalView.current
    var isExpanded by remember { mutableStateOf(true) }

    fun send(key: KeyItem) {
        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
        onSendBytes(key.bytes)
    }

    val ctrlKeys = remember {
        listOf(
            KeyItem("Ctrl+C", byteArrayOf(0x03), AccentRed),
            KeyItem("Ctrl+D", byteArrayOf(0x04), AccentOrange),
            KeyItem("Ctrl+Z", byteArrayOf(0x1A), AccentYellow),
            KeyItem("Ctrl+L", byteArrayOf(0x0C), AccentGreen),
            KeyItem("Ctrl+A", byteArrayOf(0x01), AccentBlue),
            KeyItem("Ctrl+E", byteArrayOf(0x05), AccentBlue),
            KeyItem("Ctrl+R", byteArrayOf(0x12), AccentPurple),
            KeyItem("Ctrl+W", byteArrayOf(0x17), Color(0xFFFF7597)),
            KeyItem("Ctrl+U", byteArrayOf(0x15), Color(0xFFFF7597)),
            KeyItem("Ctrl+K", byteArrayOf(0x0B), Color(0xFFFF7597))
        )
    }

    val ctrlPlusKeys = remember {
        listOf(
            KeyItem("Ctrl+T", byteArrayOf(0x14), Color(0xFF64D2FF)),
            KeyItem("Ctrl+P", byteArrayOf(0x10), Color(0xFF5E5CE6)),
            KeyItem("Ctrl+N", byteArrayOf(0x0E), Color(0xFF5E5CE6)),
            KeyItem("Ctrl+B", byteArrayOf(0x02), Color(0xFF63E6E2)),
            KeyItem("Ctrl+F", byteArrayOf(0x06), Color(0xFF63E6E2)),
            KeyItem("Ctrl+H", byteArrayOf(0x08), Color(0xFF98989D)),
            KeyItem("Ctrl+Y", byteArrayOf(0x19), AccentGreen),
            KeyItem("Ctrl+O", byteArrayOf(0x0F), Color(0xFF64D2FF)),
            KeyItem("Ctrl+X", byteArrayOf(0x18), AccentRed),
            KeyItem("Ctrl+\\", byteArrayOf(0x1C), AccentRed)
        )
    }

    val navKeys = remember {
        listOf(
            KeyItem("Tab", byteArrayOf(0x09), AccentGreen),
            KeyItem("Esc", byteArrayOf(0x1B), AccentOrange),
            KeyItem("↑", byteArrayOf(0x1B, 0x5B, 0x41), AccentBlue),
            KeyItem("↓", byteArrayOf(0x1B, 0x5B, 0x42), AccentBlue),
            KeyItem("←", byteArrayOf(0x1B, 0x5B, 0x44), AccentBlue),
            KeyItem("→", byteArrayOf(0x1B, 0x5B, 0x43), AccentBlue),
            KeyItem("Home", byteArrayOf(0x1B, 0x5B, 0x48), Color(0xFF64D2FF)),
            KeyItem("End", byteArrayOf(0x1B, 0x5B, 0x46), Color(0xFF64D2FF)),
            KeyItem("PgUp", byteArrayOf(0x1B, 0x5B, 0x35, 0x7E), AccentPurple),
            KeyItem("PgDn", byteArrayOf(0x1B, 0x5B, 0x36, 0x7E), AccentPurple),
            KeyItem("Ins", byteArrayOf(0x1B, 0x5B, 0x32, 0x7E), Color(0xFF98989D)),
            KeyItem("Del", byteArrayOf(0x1B, 0x5B, 0x33, 0x7E), AccentRed)
        )
    }

    val altKeys = remember {
        listOf(
            KeyItem("Alt+B", byteArrayOf(0x1B, 'b'.code.toByte()), Color(0xFF64D2FF)),
            KeyItem("Alt+F", byteArrayOf(0x1B, 'f'.code.toByte()), Color(0xFF64D2FF)),
            KeyItem("Alt+D", byteArrayOf(0x1B, 'd'.code.toByte()), Color(0xFFFF7597)),
            KeyItem("Alt+.", byteArrayOf(0x1B, '.'.code.toByte()), AccentOrange),
            KeyItem("Alt+U", byteArrayOf(0x1B, 'u'.code.toByte()), AccentGreen),
            KeyItem("Alt+L", byteArrayOf(0x1B, 'l'.code.toByte()), AccentGreen),
            KeyItem("Alt+T", byteArrayOf(0x1B, 't'.code.toByte()), Color(0xFF64D2FF)),
            KeyItem("Alt+R", byteArrayOf(0x1B, 'r'.code.toByte()), AccentPurple),
            KeyItem("Alt+H", byteArrayOf(0x1B, 'h'.code.toByte()), Color(0xFF98989D)),
            KeyItem("Alt+⌫", byteArrayOf(0x1B, 0x7F), AccentRed)
        )
    }

    val fnKeys = remember {
        listOf(
            KeyItem("F1", byteArrayOf(0x1B, 0x4F, 0x50), Color(0xFF5E5CE6)),
            KeyItem("F2", byteArrayOf(0x1B, 0x4F, 0x51), Color(0xFF5E5CE6)),
            KeyItem("F3", byteArrayOf(0x1B, 0x4F, 0x52), Color(0xFF5E5CE6)),
            KeyItem("F4", byteArrayOf(0x1B, 0x4F, 0x53), Color(0xFF5E5CE6)),
            KeyItem("F5", byteArrayOf(0x1B, 0x5B, 0x31, 0x35, 0x7E), Color(0xFF5E5CE6)),
            KeyItem("F6", byteArrayOf(0x1B, 0x5B, 0x31, 0x37, 0x7E), Color(0xFF5E5CE6)),
            KeyItem("F7", byteArrayOf(0x1B, 0x5B, 0x31, 0x38, 0x7E), Color(0xFF5E5CE6)),
            KeyItem("F8", byteArrayOf(0x1B, 0x5B, 0x31, 0x39, 0x7E), Color(0xFF5E5CE6)),
            KeyItem("F9", byteArrayOf(0x1B, 0x5B, 0x32, 0x30, 0x7E), Color(0xFF5E5CE6)),
            KeyItem("F10", byteArrayOf(0x1B, 0x5B, 0x32, 0x31, 0x7E), Color(0xFF5E5CE6)),
            KeyItem("F11", byteArrayOf(0x1B, 0x5B, 0x32, 0x33, 0x7E), Color(0xFF5E5CE6)),
            KeyItem("F12", byteArrayOf(0x1B, 0x5B, 0x32, 0x34, 0x7E), Color(0xFF5E5CE6))
        )
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFF000000).copy(alpha = 0.95f))
    ) {
        // Toggle bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { isExpanded = !isExpanded }
                .padding(horizontal = 12.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (isExpanded) Icons.Default.KeyboardArrowDown else Icons.Default.Keyboard,
                contentDescription = null,
                tint = AccentBlue,
                modifier = Modifier.padding(end = 6.dp)
            )
            Text(
                text = if (isExpanded) "Ukryj klawisze specjalne" else "Pokaż klawisze specjalne (Ctrl, Alt, Fn, Nav)",
                color = AccentBlue,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
        }

        AnimatedVisibility(
            visible = isExpanded,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                KeyRow(keys = ctrlKeys, label = "Ctrl", onKeyClick = { send(it) })
                KeyRow(keys = ctrlPlusKeys, label = "Ctrl+", onKeyClick = { send(it) })
                KeyRow(keys = navKeys, label = "Nav", onKeyClick = { send(it) })
                KeyRow(keys = altKeys, label = "Alt", onKeyClick = { send(it) })
                KeyRow(keys = fnKeys, label = "Fn", onKeyClick = { send(it) })
            }
        }
    }
}

@Composable
private fun KeyRow(
    keys: List<KeyItem>,
    label: String,
    onKeyClick: (KeyItem) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = Color.Gray,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.width(36.dp)
        )

        keys.forEach { key ->
            Box(
                modifier = Modifier
                    .padding(horizontal = 2.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color.White.copy(alpha = 0.08f))
                    .border(0.5.dp, key.color.copy(alpha = 0.35f), RoundedCornerShape(6.dp))
                    .clickable { onKeyClick(key) }
                    .padding(horizontal = 8.dp, vertical = 5.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = key.label,
                    color = key.color,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}
