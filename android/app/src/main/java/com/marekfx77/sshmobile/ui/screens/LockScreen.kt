package com.marekfx77.sshmobile.ui.screens

import android.view.HapticFeedbackConstants
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.keyframes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.marekfx77.sshmobile.ui.components.FactoryResetConfirmDialog
import com.marekfx77.sshmobile.ui.theme.AccentBlue
import com.marekfx77.sshmobile.ui.theme.AccentRed
import com.marekfx77.sshmobile.viewmodel.SSHViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun LockScreen(
    viewModel: SSHViewModel,
    modifier: Modifier = Modifier
) {
    val view = LocalView.current
    val scope = rememberCoroutineScope()

    var enteredPin by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }
    var showResetDialog by remember { mutableStateOf(false) }

    val shakeOffset = remember { Animatable(0f) }

    fun onDigitPress(digit: String) {
        if (enteredPin.length < 4 && !isError) {
            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
            val newPin = enteredPin + digit
            enteredPin = newPin

            if (newPin.length == 4) {
                // Check PIN
                if (viewModel.verifyPin(newPin)) {
                    view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                    viewModel.unlockApp()
                } else {
                    view.performHapticFeedback(HapticFeedbackConstants.REJECT)
                    isError = true
                    scope.launch {
                        shakeOffset.animateTo(
                            targetValue = 0f,
                            animationSpec = keyframes {
                                durationMillis = 400
                                -25f at 50
                                25f at 100
                                -20f at 150
                                20f at 200
                                -10f at 250
                                10f at 300
                                -5f at 350
                                0f at 400
                            }
                        )
                        delay(300)
                        enteredPin = ""
                        isError = false
                    }
                }
            }
        }
    }

    fun onDeletePress() {
        if (enteredPin.isNotEmpty() && !isError) {
            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
            enteredPin = enteredPin.dropLast(1)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        // iOS Style PIN Keypad
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 48.dp, horizontal = 24.dp)
        ) {
            // Top Header + Dots
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .offset { IntOffset(shakeOffset.value.roundToInt(), 0) }
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = if (isError) "Nieprawidłowy kod PIN" else "Wprowadź kod PIN",
                    color = if (isError) AccentRed else Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "SSH Mobile",
                    color = Color(0xFF8E8E93),
                    fontSize = 13.sp
                )

                Spacer(modifier = Modifier.height(32.dp))

                // 4 iOS PIN Dots
                Row(
                    horizontalArrangement = Arrangement.spacedBy(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (i in 0 until 4) {
                        val isFilled = i < enteredPin.length
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .clip(CircleShape)
                                .background(
                                    when {
                                        isError -> AccentRed
                                        isFilled -> Color.White
                                        else -> Color.Transparent
                                    }
                                )
                                .border(
                                    width = 1.5.dp,
                                    color = when {
                                        isError -> AccentRed
                                        isFilled -> Color.White
                                        else -> Color(0xFF48484A)
                                    },
                                    shape = CircleShape
                                )
                        )
                    }
                }
            }

            // Center iOS Keypad (1 to 9, 0, Backspace)
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Row 1
                Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                    IosKeyButton(digit = "1", letters = "", onClick = { onDigitPress("1") })
                    IosKeyButton(digit = "2", letters = "A B C", onClick = { onDigitPress("2") })
                    IosKeyButton(digit = "3", letters = "D E F", onClick = { onDigitPress("3") })
                }
                // Row 2
                Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                    IosKeyButton(digit = "4", letters = "G H I", onClick = { onDigitPress("4") })
                    IosKeyButton(digit = "5", letters = "J K L", onClick = { onDigitPress("5") })
                    IosKeyButton(digit = "6", letters = "M N O", onClick = { onDigitPress("6") })
                }
                // Row 3
                Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                    IosKeyButton(digit = "7", letters = "P Q R S", onClick = { onDigitPress("7") })
                    IosKeyButton(digit = "8", letters = "T U V", onClick = { onDigitPress("8") })
                    IosKeyButton(digit = "9", letters = "W X Y Z", onClick = { onDigitPress("9") })
                }
                // Row 4 (Empty, 0, Delete)
                Row(horizontalArrangement = Arrangement.spacedBy(24.dp), verticalAlignment = Alignment.CenterVertically) {
                    Spacer(modifier = Modifier.size(76.dp))

                    IosKeyButton(digit = "0", letters = "", onClick = { onDigitPress("0") })

                    if (enteredPin.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .size(76.dp)
                                .clip(CircleShape)
                                .clickable { onDeletePress() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Backspace,
                                contentDescription = "Usuń",
                                tint = Color.White,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.size(76.dp))
                    }
                }
            }

            // Bottom Emergency Reset Button
            TextButton(onClick = { showResetDialog = true }) {
                Text(
                    text = "Nie pamiętasz PIN-u? Zresetuj aplikację",
                    color = Color(0xFF8E8E93),
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }

    if (showResetDialog) {
        FactoryResetConfirmDialog(
            onConfirm = {
                showResetDialog = false
                viewModel.performFactoryReset {
                    viewModel.unlockApp()
                }
            },
            onDismiss = { showResetDialog = false }
        )
    }
}

@Composable
private fun IosKeyButton(
    digit: String,
    letters: String,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    Box(
        modifier = Modifier
            .size(76.dp)
            .clip(CircleShape)
            .background(if (isPressed) Color(0xFF48484A) else Color(0xFF1C1C1E))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = digit,
                color = Color.White,
                fontSize = 32.sp,
                fontWeight = FontWeight.Normal,
                lineHeight = 34.sp
            )
            if (letters.isNotEmpty()) {
                Text(
                    text = letters,
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp
                )
            }
        }
    }
}
