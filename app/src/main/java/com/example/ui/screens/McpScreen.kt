package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun McpScreen(
    themeIsDark: Boolean,
    onDismiss: () -> Unit
) {
    var showConnectDialog by remember { mutableStateOf(false) }

    val bgColor = if (themeIsDark) Color(0xFF0F172A) else Color(0xFFF8FAFC)
    val textColor = if (themeIsDark) Color.White else Color(0xFF0F172A)
    val cardColor = if (themeIsDark) Color(0xFF1E293B) else Color.White
    val cardBorderColor = if (themeIsDark) Color(0xFF334155) else Color(0xFFE2E8F0)

    Surface(
        color = bgColor,
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "MCP Configuration",
                    tint = Color(0xFFA78BFA),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Model Context Protocol (MCP)",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )
                    Text(
                        text = "Connect structured contextual integrations to the active model",
                        fontSize = 12.sp,
                        color = Color(0xFF94A3B8)
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close pane",
                        tint = textColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { showConnectDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFA78BFA)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add MCP", modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Connect New MCP Server", fontSize = 13.sp, color = Color.White)
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Active MCP Connections",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = textColor,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.TopCenter
            ) {
                Text(
                    text = "No MCP servers connected. Click above to connect one via stdio or SSE.",
                    color = Color(0xFF64748B),
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }
        }
    }

    if (showConnectDialog) {
        var mcpUrl by remember { mutableStateOf("") }
        var isStdio by remember { mutableStateOf(false) }
        AlertDialog(
            onDismissRequest = { showConnectDialog = false },
            title = { Text("Connect MCP Server") },
            text = { 
                Column {
                    Text("Enter the remote SSE URL or Stdio command for the MCP server:", fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = mcpUrl,
                        onValueChange = { mcpUrl = it },
                        label = { Text("Endpoint / Command") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = isStdio, onCheckedChange = { isStdio = it })
                        Text("Use Stdio (Local Command)", fontSize = 12.sp)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { 
                        showConnectDialog = false
                    }
                ) { Text("Connect") }
            },
            dismissButton = {
                TextButton(onClick = { showConnectDialog = false }) { Text("Cancel") }
            }
        )
    }
}
