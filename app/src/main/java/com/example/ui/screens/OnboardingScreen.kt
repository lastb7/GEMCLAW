package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun OnboardingScreen(
    onOnboardingComplete: () -> Unit
) {
    var currentPhase by remember { mutableStateOf(0) }
    var progress by remember { mutableStateOf(0f) }
    var progressText by remember { mutableStateOf("") }

    LaunchedEffect(currentPhase) {
        when (currentPhase) {
            0 -> {
                progressText = "Connecting to OpenClaw registries..."
                delay(1000)
                currentPhase = 1
            }
            1 -> {
                progressText = "Downloading proot RootFS (Ubuntu 24.04 LTS)..."
                var p = 0f
                while (p < 1f) {
                    p += 0.05f
                    progress = p
                    delay(100)
                }
                currentPhase = 2
            }
            2 -> {
                progressText = "Extracting RootFS and configuring namespace..."
                var p = 0f
                while (p < 1f) {
                    p += 0.08f
                    progress = p
                    delay(100)
                }
                currentPhase = 3
            }
            3 -> {
                progressText = "Setting up Agent Environment and PIP requirements..."
                var p = 0f
                while (p < 1f) {
                    p += 0.1f
                    progress = p
                    delay(150)
                }
                currentPhase = 4
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .width(360.dp)
                .background(Color(0xFF1E293B), RoundedCornerShape(16.dp))
                .padding(24.dp)
        ) {
            Icon(
                imageVector = when (currentPhase) {
                    in 0..1 -> Icons.Default.Build
                    2, 3 -> Icons.Default.Settings
                    else -> Icons.Default.CheckCircle
                },
                contentDescription = "Installation Status",
                tint = if (currentPhase == 4) Color(0xFF10B981) else Color(0xFF38BDF8),
                modifier = Modifier.size(48.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "OpenClaw Sandbox Setup",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = progressText,
                color = Color(0xFF94A3B8),
                fontSize = 12.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            if (currentPhase < 4) {
                LinearProgressIndicator(
                    progress = progress,
                    modifier = Modifier.fillMaxWidth().height(8.dp),
                    color = Color(0xFF10B981),
                    trackColor = Color(0xFF0F172A)
                )
            } else {
                Text(
                    text = "Installation Complete\nAgent Environment Ready",
                    color = Color(0xFF10B981),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Button(
                    onClick = onOnboardingComplete,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF38BDF8)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Launch Workspace", color = Color(0xFF0F172A), fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
