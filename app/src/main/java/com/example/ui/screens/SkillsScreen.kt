package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ClawPlugin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SkillsScreen(
    plugins: List<ClawPlugin>,
    themeIsDark: Boolean,
    onTogglePlugin: (ClawPlugin) -> Unit,
    onAddCustomPlugin: (String, String, String) -> Unit,
    onDeletePlugin: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var showAddGitDialog by remember { mutableStateOf(false) }
    var showBrowseDialog by remember { mutableStateOf(false) }
    var showUploadDialog by remember { mutableStateOf(false) }

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
                    imageVector = Icons.Default.Search,
                    contentDescription = "Skills Configuration",
                    tint = Color(0xFF3B82F6),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Skills, Extensions & Plugins",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )
                    Text(
                        text = "Enhance the AI's capabilities natively",
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

            Spacer(modifier = Modifier.height(16.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { showBrowseDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B5CF6)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.ShoppingCart, contentDescription = "Marketplace", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Marketplace", fontSize = 11.sp, color = Color.White)
                }

                Button(
                    onClick = { showAddGitDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "From Git", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Install via Git URL", fontSize = 11.sp, color = Color.White)
                }
                
                Button(
                    onClick = { showUploadDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Upload ZIP", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Upload Local", fontSize = 11.sp, color = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Installed Skills & Extensions",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = textColor,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Installed list
            LazyColumn(
                modifier = Modifier.weight(1f)
            ) {
                if (plugins.isEmpty()) {
                    item {
                        Text("No plugins installed currently.", color = Color(0xFF94A3B8), fontSize = 12.sp)
                    }
                } else {
                    items(plugins) { plugin ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = cardColor),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .border(1.dp, cardBorderColor, RoundedCornerShape(8.dp))
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp)
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = plugin.name,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = textColor
                                    )
                                    Text(
                                        text = plugin.description,
                                        fontSize = 12.sp,
                                        color = Color(0xFF64748B),
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Category: ${plugin.category.uppercase()}",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF3B82F6)
                                    )
                                }
                                
                                Switch(
                                    checked = plugin.isEnabled,
                                    onCheckedChange = { onTogglePlugin(plugin) },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = Color(0xFF10B981)
                                    )
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                if (plugin.category != "system") {
                                    IconButton(onClick = { onDeletePlugin(plugin.id) }) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Delete Plugin",
                                            tint = Color(0xFFEF4444)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showBrowseDialog) {
        AlertDialog(
            onDismissRequest = { showBrowseDialog = false },
            title = { Text("Plugin Marketplace") },
            text = { Text("Connecting to OpenClaw remote registry... (This feature requires a network link)") },
            confirmButton = {
                TextButton(onClick = { showBrowseDialog = false }) { Text("Dismiss") }
            }
        )
    }

    if (showAddGitDialog) {
        var gitUrl by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showAddGitDialog = false },
            title = { Text("Install from Git") },
            text = { 
                Column {
                    Text("Enter the remote Git repository URL for the plugin:", fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = gitUrl,
                        onValueChange = { gitUrl = it },
                        label = { Text("https://github.com/...") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { 
                        if (gitUrl.isNotEmpty()) {
                            onAddCustomPlugin("Git Plugin (${gitUrl.substringAfterLast("/")})", "Installed from $gitUrl", "Git plugin constraints rule.")
                            showAddGitDialog = false
                        }
                    }
                ) { Text("Install") }
            },
            dismissButton = {
                TextButton(onClick = { showAddGitDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (showUploadDialog) {
        AlertDialog(
            onDismissRequest = { showUploadDialog = false },
            title = { Text("Upload Local Plugin") },
            text = { Text("Select a local .zip or .json plugin module from your device to inject into the workspace.") },
            confirmButton = {
                Button(
                    onClick = { 
                        onAddCustomPlugin("Local Device Extension", "Manually uploaded from device", "Custom capability added manually.")
                        showUploadDialog = false 
                    }
                ) { Text("Browse Files...") }
            },
            dismissButton = {
                TextButton(onClick = { showUploadDialog = false }) { Text("Cancel") }
            }
        )
    }
}
