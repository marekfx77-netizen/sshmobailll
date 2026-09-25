package com.marekfx77.sshmobile.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.marekfx77.sshmobile.model.ServerResourceStats
import com.marekfx77.sshmobile.ui.theme.AccentGreen
import com.marekfx77.sshmobile.ui.theme.AccentOrange
import com.marekfx77.sshmobile.ui.theme.AccentRed
import com.marekfx77.sshmobile.ui.theme.SurfaceBorderDark
import com.marekfx77.sshmobile.ui.theme.SurfaceCardDark

@Composable
fun ResourceStatsCard(
    stats: ServerResourceStats?,
    modifier: Modifier = Modifier
) {
    if (stats == null) return

    val cpuColor = when {
        stats.cpuPercent < 60 -> AccentGreen
        stats.cpuPercent < 85 -> AccentOrange
        else -> AccentRed
    }

    val memColor = when {
        stats.memPercent < 0.65f -> AccentGreen
        stats.memPercent < 0.85f -> AccentOrange
        else -> AccentRed
    }

    val diskColor = when {
        stats.diskPercent < 0.70f -> AccentGreen
        stats.diskPercent < 0.90f -> AccentOrange
        else -> AccentRed
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceCardDark)
            .border(1.dp, SurfaceBorderDark, RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(AccentGreen, CircleShape)
                    )
                    Text(
                        text = " MONITORING ZASOBÓW",
                        color = Color.Gray,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Text(
                    text = "Uptime: ${stats.uptimeText}",
                    color = Color.LightGray,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            // CPU
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "CPU", fontSize = 11.sp, color = Color.Gray)
                    Text(text = stats.cpuText, fontSize = 11.sp, color = cpuColor, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(2.dp))
                LinearProgressIndicator(
                    progress = { (stats.cpuPercent / 100.0).toFloat().coerceIn(0f, 1f) },
                    modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
                    color = cpuColor,
                    trackColor = Color.White.copy(alpha = 0.1f)
                )
            }

            // RAM
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Pamięć RAM", fontSize = 11.sp, color = Color.Gray)
                    Text(text = stats.memText, fontSize = 11.sp, color = memColor, fontWeight = FontWeight.Medium)
                }
                Spacer(modifier = Modifier.height(2.dp))
                LinearProgressIndicator(
                    progress = { stats.memPercent },
                    modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
                    color = memColor,
                    trackColor = Color.White.copy(alpha = 0.1f)
                )
            }

            // Disk
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Dysk", fontSize = 11.sp, color = Color.Gray)
                    Text(text = stats.diskText, fontSize = 11.sp, color = diskColor, fontWeight = FontWeight.Medium)
                }
                Spacer(modifier = Modifier.height(2.dp))
                LinearProgressIndicator(
                    progress = { stats.diskPercent },
                    modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
                    color = diskColor,
                    trackColor = Color.White.copy(alpha = 0.1f)
                )
            }
        }
    }
}
