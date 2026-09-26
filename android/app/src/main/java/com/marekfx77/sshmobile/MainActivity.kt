package com.marekfx77.sshmobile

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.activity.ComponentActivity
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
import com.marekfx77.sshmobile.ui.theme.SeparatorDark
import com.marekfx77.sshmobile.ui.theme.TextSecondaryDark
import com.marekfx77.sshmobile.viewmodel.SSHViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: SSHViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleIntent(intent)

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
                            viewModel = viewModel
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
    }

    override fun onNewIntent(intent: android.content.Intent?) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: android.content.Intent?) {
        val tab = intent?.getIntExtra("tab", -1) ?: -1
        if (tab in 0..4) {
            viewModel.selectTab(tab)
        }
    }
}

@Composable
fun MainScreen(viewModel: SSHViewModel) {
    val currentTab by viewModel.currentTab.collectAsState()

    Scaffold(
        bottomBar = {
            IosTabBar(
                currentTab = currentTab,
                onTabSelected = { viewModel.selectTab(it) }
            )
        },
        containerColor = BgDark
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

@Composable
fun IosTabBar(
    currentTab: Int,
    onTabSelected: (Int) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF161618))
    ) {
        // Top 0.5dp iOS border
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(0.5.dp)
                .background(SeparatorDark)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(58.dp)
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IosTabItem(
                title = "Serwery",
                icon = Icons.Default.Storage,
                selected = currentTab == 0,
                onClick = { onTabSelected(0) }
            )
            IosTabItem(
                title = "Terminal",
                icon = Icons.Default.Terminal,
                selected = currentTab == 1,
                onClick = { onTabSelected(1) }
            )
            IosTabItem(
                title = "Pliki",
                icon = Icons.Default.Folder,
                selected = currentTab == 2,
                onClick = { onTabSelected(2) }
            )
            IosTabItem(
                title = "Snippety",
                icon = Icons.Default.Bolt,
                selected = currentTab == 3,
                onClick = { onTabSelected(3) }
            )
            IosTabItem(
                title = "Ustawienia",
                icon = Icons.Default.Settings,
                selected = currentTab == 4,
                onClick = { onTabSelected(4) }
            )
        }
    }
}

@Composable
private fun IosTabItem(
    title: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val tint = if (selected) AccentBlue else TextSecondaryDark

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = tint,
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = title,
            color = tint,
            fontSize = 10.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}
