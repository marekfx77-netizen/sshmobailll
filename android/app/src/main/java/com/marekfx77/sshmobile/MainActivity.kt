package com.marekfx77.sshmobile

import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.marekfx77.sshmobile.ui.components.FactoryResetProgressDialog
import com.marekfx77.sshmobile.ui.screens.LockScreen
import com.marekfx77.sshmobile.ui.screens.SFTPScreen
import com.marekfx77.sshmobile.ui.screens.ServersScreen
import com.marekfx77.sshmobile.ui.screens.SettingsScreen
import com.marekfx77.sshmobile.ui.screens.SnippetsScreen
import com.marekfx77.sshmobile.ui.screens.TerminalScreen
import com.marekfx77.sshmobile.ui.theme.AccentBlue
import com.marekfx77.sshmobile.ui.theme.BgDark
import com.marekfx77.sshmobile.ui.theme.SSHMobileTheme
import com.marekfx77.sshmobile.ui.theme.SurfaceCardDark
import com.marekfx77.sshmobile.viewmodel.SSHViewModel

class MainActivity : FragmentActivity() {
    private val viewModel: SSHViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val themeMode by viewModel.appearanceTheme.collectAsState()
            val isLocked by viewModel.isLocked.collectAsState()
            val resetProgress by viewModel.factoryResetProgress.collectAsState()

            SSHMobileTheme(themeMode = themeMode) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(BgDark)
                ) {
                    if (isLocked) {
                        LockScreen(
                            viewModel = viewModel,
                            onTriggerBiometrics = { triggerBiometrics() }
                        )
                    } else {
                        MainScreen(viewModel = viewModel)
                    }

                    // Real factory reset progress overlay if active
                    resetProgress?.let { progress ->
                        FactoryResetProgressDialog(progress = progress)
                    }
                }
            }
        }

        if (viewModel.isLocked.value) {
            triggerBiometrics()
        }
    }

    private fun triggerBiometrics() {
        val executor = ContextCompat.getMainExecutor(this)
        val biometricPrompt = BiometricPrompt(
            this,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    viewModel.unlockApp()
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    // If error or user canceled, remain locked
                }
            }
        )

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("SSH Mobile")
            .setSubtitle("Zaloguj się za pomocą biometrii")
            .setNegativeButtonText("Anuluj")
            .build()

        biometricPrompt.authenticate(promptInfo)
    }
}

@Composable
fun MainScreen(viewModel: SSHViewModel) {
    val currentTab by viewModel.currentTab.collectAsState()

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = SurfaceCardDark,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = currentTab == 0,
                    onClick = { viewModel.selectTab(0) },
                    icon = { Icon(imageVector = Icons.Default.Storage, contentDescription = "Serwery") },
                    label = { Text("Serwery", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = AccentBlue,
                        selectedTextColor = AccentBlue,
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray,
                        indicatorColor = AccentBlue.copy(alpha = 0.15f)
                    )
                )
                NavigationBarItem(
                    selected = currentTab == 1,
                    onClick = { viewModel.selectTab(1) },
                    icon = { Icon(imageVector = Icons.Default.Terminal, contentDescription = "Terminal") },
                    label = { Text("Terminal", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = AccentBlue,
                        selectedTextColor = AccentBlue,
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray,
                        indicatorColor = AccentBlue.copy(alpha = 0.15f)
                    )
                )
                NavigationBarItem(
                    selected = currentTab == 2,
                    onClick = { viewModel.selectTab(2) },
                    icon = { Icon(imageVector = Icons.Default.Folder, contentDescription = "Pliki") },
                    label = { Text("Pliki", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = AccentBlue,
                        selectedTextColor = AccentBlue,
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray,
                        indicatorColor = AccentBlue.copy(alpha = 0.15f)
                    )
                )
                NavigationBarItem(
                    selected = currentTab == 3,
                    onClick = { viewModel.selectTab(3) },
                    icon = { Icon(imageVector = Icons.Default.Bolt, contentDescription = "Snippety") },
                    label = { Text("Snippety", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = AccentBlue,
                        selectedTextColor = AccentBlue,
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray,
                        indicatorColor = AccentBlue.copy(alpha = 0.15f)
                    )
                )
                NavigationBarItem(
                    selected = currentTab == 4,
                    onClick = { viewModel.selectTab(4) },
                    icon = { Icon(imageVector = Icons.Default.Settings, contentDescription = "Ustawienia") },
                    label = { Text("Ustawienia", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = AccentBlue,
                        selectedTextColor = AccentBlue,
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray,
                        indicatorColor = AccentBlue.copy(alpha = 0.15f)
                    )
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (currentTab) {
                0 -> ServersScreen(viewModel = viewModel)
                1 -> TerminalScreen(viewModel = viewModel)
                2 -> SFTPScreen(viewModel = viewModel)
                3 -> SnippetsScreen(viewModel = viewModel)
                4 -> SettingsScreen(viewModel = viewModel)
            }
        }
    }
}
