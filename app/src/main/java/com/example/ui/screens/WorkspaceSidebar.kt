package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ChatConversation
import com.example.data.Workspace

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkspaceSidebar(
    workspaces: List<Workspace>,
    activeWorkspace: Workspace?,
    conversations: List<ChatConversation>,
    activeConv: ChatConversation?,
    activeModel: String,
    providers: List<com.example.data.ProviderConfig>,
    onModelSelected: (String) -> Unit,
    userEmail: String,
    userName: String,
    onSelectWorkspace: (Workspace) -> Unit,
    onAddWorkspace: (String) -> Unit,
    onRenameWorkspace: (String, String) -> Unit,
    onDeleteWorkspace: (String) -> Unit,
    onSelectConv: (ChatConversation?) -> Unit,
    onDeleteConv: (String) -> Unit,
    onCreateNewChat: () -> Unit,
    onOpenProviders: () -> Unit,
    onOpenGlobalSettings: () -> Unit,
    onOpenSkills: () -> Unit,
    onOpenMcp: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    var workspaceDropdownExpanded by remember { mutableStateOf(false) }
    var showCreateWorkspaceDialog by remember { mutableStateOf(false) }
    var showRenameWorkspaceDialog by remember { mutableStateOf(false) }
    var newWorkspaceName by remember { mutableStateOf("") }
    var renameWorkspaceName by remember { mutableStateOf("") }

    Surface(
        color = Color(0xFF0F172A), // Tailored dark slate 900
        modifier = modifier
            .fillMaxHeight()
            .testTag("workspace_sidebar")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Header Group & Icon
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(Color(0xFF10B981), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Build,
                        contentDescription = "OpenClaw Icon",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "OpenClaw Pro",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Secure Sandbox IDE v2.0",
                        color = Color(0xFF64748B),
                        fontSize = 10.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Workspace Selector Card
            Text(
                text = "ACTIVE WORKSPACE",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF475569),
                letterSpacing = 1.sp,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
            )

            Box(modifier = Modifier.fillMaxWidth()) {
                Surface(
                    onClick = { workspaceDropdownExpanded = true },
                    color = Color(0xFF1E293B),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("workspace_selector_pill")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Home,
                            contentDescription = "Workspace Active",
                            tint = Color(0xFF10B981),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = activeWorkspace?.name ?: "No workspace selected",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "Expand Workspaces Menu",
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                DropdownMenu(
                    expanded = workspaceDropdownExpanded,
                    onDismissRequest = { workspaceDropdownExpanded = false },
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .background(Color(0xFF1E293B))
                ) {
                    DropdownMenuItem(
                        text = { Text("SWITCH WORKSPACE", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF10B981)) },
                        onClick = {},
                        enabled = false
                    )
                    workspaces.forEach { ws ->
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.List,
                                        contentDescription = "WS Selection",
                                        tint = if (ws.id == activeWorkspace?.id) Color(0xFF10B981) else Color(0xFF94A3B8),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(ws.name, color = Color.White, fontSize = 13.sp)
                                }
                            },
                            onClick = {
                                onSelectWorkspace(ws)
                                workspaceDropdownExpanded = false
                            }
                        )
                    }

                    Divider(color = Color(0xFF334155))

                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Add, contentDescription = "New Ws", tint = Color.Green, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Create Workspace", color = Color.White, fontSize = 13.sp)
                            }
                        },
                        onClick = {
                            newWorkspaceName = ""
                            showCreateWorkspaceDialog = true
                            workspaceDropdownExpanded = false
                        }
                    )

                    activeWorkspace?.let { currentWs ->
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Edit, contentDescription = "Rename Ws", tint = Color.Yellow, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Edit App Name (Admin)", color = Color.White, fontSize = 13.sp)
                                }
                            },
                            onClick = {
                                renameWorkspaceName = currentWs.name
                                showRenameWorkspaceDialog = true
                                workspaceDropdownExpanded = false
                            }
                        )

                        if (workspaces.size > 1) {
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete Ws", tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Delete Current WS", color = Color(0xFFEF4444), fontSize = 13.sp)
                                    }
                                },
                                onClick = {
                                    onDeleteWorkspace(currentWs.id)
                                    workspaceDropdownExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // AI Model Provider Selection Dropdown
            Text(
                text = "ACTIVE AI MODEL",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF475569),
                letterSpacing = 1.sp,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
            )

            var modelDropdownExpanded by remember { mutableStateOf(false) }

            Box(modifier = Modifier.fillMaxWidth()) {
                Surface(
                    onClick = { modelDropdownExpanded = true },
                    color = Color(0xFF1E293B),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Face, // Using Face for Model
                            contentDescription = "Active Model",
                            tint = Color(0xFFA78BFA),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = activeModel,
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "Expand Models Menu",
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                DropdownMenu(
                    expanded = modelDropdownExpanded,
                    onDismissRequest = { modelDropdownExpanded = false },
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .background(Color(0xFF1E293B))
                ) {
                    // Collect active providers
                    val activeProvidersList = providers.filter { it.isActive }.flatMap {
                        when (it.providerName) {
                            "Gemini" -> listOf("gemini-1.5-pro", "gemini-1.5-flash")
                            "OpenAI" -> listOf("gpt-4o", "gpt-3.5-turbo")
                            "Claude" -> listOf("claude-3-opus", "claude-3-sonnet")
                            else -> listOf(it.modelName.takeIf { m -> m.isNotEmpty() } ?: "unknown-model")
                        }
                    }.distinct()

                    if (activeProvidersList.isEmpty()) {
                        DropdownMenuItem(
                            text = { Text("No active providers", color = Color(0xFF94A3B8), fontSize = 13.sp) },
                            onClick = { modelDropdownExpanded = false }
                        )
                    } else {
                        DropdownMenuItem(
                            text = { Text("SWITCH MODEL", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFA78BFA)) },
                            onClick = {},
                            enabled = false
                        )
                        activeProvidersList.forEach { modelName ->
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.List,
                                            contentDescription = "Model Selection",
                                            tint = if (modelName == activeModel) Color(0xFFA78BFA) else Color(0xFF94A3B8),
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(modelName, color = Color.White, fontSize = 13.sp)
                                    }
                                },
                                onClick = {
                                    onModelSelected(modelName)
                                    modelDropdownExpanded = false
                                }
                            )
                        }
                    }

                    Divider(color = Color(0xFF334155))

                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Settings, contentDescription = "Manage APIs", tint = Color.Green, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Manage Provider APIs", color = Color.White, fontSize = 13.sp)
                            }
                        },
                        onClick = {
                            modelDropdownExpanded = false
                            onOpenProviders()
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // New Chat Trigger
            Button(
                onClick = { onCreateNewChat() },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF10B981),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .testTag("new_chat_button"),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "New Chat Logo",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Start Dialogue",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "CONVERSATIONS",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF475569),
                letterSpacing = 1.sp,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 6.dp)
            )

            // Conversations List
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                if (conversations.isEmpty()) {
                    item {
                        Text(
                            text = "No prior chats active in workspace.",
                            color = Color(0xFF475569),
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp)
                        )
                    }
                } else {
                    items(conversations) { conv ->
                        val isActive = conv.id == activeConv?.id
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp)
                                .background(
                                    color = if (isActive) Color(0xFF1E293B) else Color.Transparent,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable { onSelectConv(conv) }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                                .testTag("conversation_row_${conv.id}")
                        ) {
                            Icon(
                                imageVector = if (isActive) Icons.Default.MailOutline else Icons.Default.Menu,
                                contentDescription = "Tab Thread Logo",
                                tint = if (isActive) Color(0xFF10B981) else Color(0xFF64748B),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = conv.title,
                                color = if (isActive) Color.White else Color(0xFF94A3B8),
                                fontSize = 12.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = { onDeleteConv(conv.id) },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete Thread",
                                    tint = Color(0xFF64748B),
                                    modifier = Modifier.size(13.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Divider(color = Color(0xFF1E293B))
            Spacer(modifier = Modifier.height(8.dp))

            // Action Items (Provider Manager)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpenGlobalSettings() }
                    .padding(vertical = 8.dp, horizontal = 4.dp)
                    .testTag("sidebar_global_settings")
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Global Profile Configuration",
                    tint = Color(0xFF94A3B8),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Global Identity / Persona",
                    color = Color(0xFFF1F5F9),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpenProviders() }
                    .padding(vertical = 8.dp, horizontal = 4.dp)
                    .testTag("sidebar_providers_admin")
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Providers Configuration",
                    tint = Color(0xFF94A3B8),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Provider Settings",
                    color = Color(0xFFF1F5F9),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpenSkills() }
                    .padding(vertical = 8.dp, horizontal = 4.dp)
                    .testTag("sidebar_skills_admin")
            ) {
                Icon(
                    imageVector = Icons.Default.Build,
                    contentDescription = "Skills and Extensions",
                    tint = Color(0xFF94A3B8),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Skills & Plugins",
                    color = Color(0xFFF1F5F9),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpenMcp() }
                    .padding(vertical = 8.dp, horizontal = 4.dp)
                    .testTag("sidebar_mcp_admin")
            ) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "MCP Configuration",
                    tint = Color(0xFFA78BFA),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "MCP (Model Context Protocol)",
                    color = Color(0xFFA78BFA),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            // Logout row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onLogout() }
                    .padding(vertical = 8.dp, horizontal = 4.dp)
                    .testTag("sidebar_logout_action")
            ) {
                Icon(
                    imageVector = Icons.Default.ExitToApp,
                    contentDescription = "Exit App",
                    tint = Color(0xFFEF4444),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Sign Out Session",
                    color = Color(0xFFEF4444),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Profile info row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1E293B).copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                    .padding(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .background(Color(0xFF8B5CF6), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = userName.takeOrEmpty(1).uppercase(),
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = userName,
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = userEmail,
                        color = Color(0xFF64748B),
                        fontSize = 9.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }

    // CREATE WORKSPACE DIALOG
    if (showCreateWorkspaceDialog) {
        AlertDialog(
            onDismissRequest = { showCreateWorkspaceDialog = false },
            containerColor = Color(0xFF1E293B),
            title = { Text("Create Workspace", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Workspace API keys and chats are sandboxed automatically.", color = Color(0xFF94A3B8), fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = newWorkspaceName,
                        onValueChange = { newWorkspaceName = it },
                        label = { Text("Workspace Name") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF10B981),
                            unfocusedBorderColor = Color(0xFF334155),
                            focusedLabelColor = Color(0xFF10B981),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newWorkspaceName.trim().isNotEmpty()) {
                            onAddWorkspace(newWorkspaceName.trim())
                            showCreateWorkspaceDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                ) {
                    Text("Create")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateWorkspaceDialog = false }) {
                    Text("Cancel", color = Color.White)
                }
            }
        )
    }

    // RENAME WORKSPACE DIALOG
    if (showRenameWorkspaceDialog) {
        AlertDialog(
            onDismissRequest = { showRenameWorkspaceDialog = false },
            containerColor = Color(0xFF1E293B),
            title = { Text("Rename Workspace", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    OutlinedTextField(
                        value = renameWorkspaceName,
                        onValueChange = { renameWorkspaceName = it },
                        label = { Text("New Workspace Name") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF10B981),
                            unfocusedBorderColor = Color(0xFF334155),
                            focusedLabelColor = Color(0xFF10B981),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (renameWorkspaceName.trim().isNotEmpty() && activeWorkspace != null) {
                            onRenameWorkspace(activeWorkspace.id, renameWorkspaceName.trim())
                            showRenameWorkspaceDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                ) {
                    Text("Rename")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRenameWorkspaceDialog = false }) {
                    Text("Cancel", color = Color.White)
                }
            }
        )
    }
}

private fun String.takeOrEmpty(n: Int): String {
    return if (this.length > n) this.substring(0, n) else this
}
