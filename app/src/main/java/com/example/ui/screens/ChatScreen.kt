package com.example.ui.screens

import android.net.Uri
import android.util.Base64
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ChatMessage
import com.example.data.ProviderConfig
import com.example.viewmodel.WorkspaceViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    messages: List<ChatMessage>,
    activeModel: String,
    providers: List<ProviderConfig>,
    isStreaming: Boolean,
    currentStreamContent: String,
    loadingError: String?,
    hasArtifact: Boolean,
    onModelSelected: (String) -> Unit,
    onSendMessage: (String, String?) -> Unit,
    onClearChat: () -> Unit,
    onCreateNewChat: () -> Unit,
    onDeleteChat: () -> Unit,
    onOpenSidebar: () -> Unit,
    onToggleArtifactPanel: () -> Unit,
    onSelectArtifact: (WorkspaceViewModel.Artifact) -> Unit,
    onToggleIncognito: () -> Unit,
    isIncognito: Boolean,
    modifier: Modifier = Modifier
) {
    var textInput by remember { mutableStateOf("") }
    var dropdownExpanded by remember { mutableStateOf(false) }

    var showAttachmentMenu by remember { mutableStateOf(false) }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var imageBase64 by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current

    val imagePickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            imageUri = uri
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val bytes = inputStream?.readBytes()
                inputStream?.close()
                if (bytes != null) {
                    val bitmapRaw = android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                    val out = java.io.ByteArrayOutputStream()
                    // Compress scale down to avoid large base64 payload
                    val scale = if (bitmapRaw.width > 800) 800.0f / bitmapRaw.width else 1.0f
                    val w = (bitmapRaw.width * scale).toInt()
                    val h = (bitmapRaw.height * scale).toInt()
                    val scaled = android.graphics.Bitmap.createScaledBitmap(bitmapRaw, w, h, true)
                    scaled.compress(android.graphics.Bitmap.CompressFormat.JPEG, 70, out)
                    imageBase64 = Base64.encodeToString(out.toByteArray(), Base64.NO_WRAP)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    val listState = rememberLazyListState()

    // Auto scroll down as stream outputs or messages length updates
    LaunchedEffect(messages.size, currentStreamContent) {
        if (messages.isNotEmpty() || currentStreamContent.isNotEmpty()) {
            val totalCount = messages.size + (if (currentStreamContent.isNotEmpty()) 1 else 0)
            listState.animateScrollToItem(totalCount - 1)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0F172A),
                    titleContentColor = Color.White
                ),
                navigationIcon = {
                    IconButton(onClick = onOpenSidebar) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Expand Drawer Menu",
                            tint = Color.White
                        )
                    }
                },
                title = {
                    // Custom Grouped Dropdown dropdown menu for model election
                    Box {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clickable { dropdownExpanded = true }
                                .padding(vertical = 4.dp, horizontal = 8.dp)
                                .testTag("model_selector_dropdown")
                        ) {
                            Text(
                                text = activeModel,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "Dropdown selector trigger",
                                tint = Color(0xFF94A3B8),
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        DropdownMenu(
                            expanded = dropdownExpanded,
                            onDismissRequest = { dropdownExpanded = false },
                            modifier = Modifier
                                .background(Color(0xFF1E293B))
                                .border(1.dp, Color(0xFF334155), RoundedCornerShape(8.dp))
                        ) {
                            DropdownMenuItem(
                                text = { Text("DEFAULT GEMINI", fontWeight = FontWeight.Bold, color = Color(0xFF3B82F6), fontSize = 11.sp) },
                                onClick = {},
                                enabled = false
                            )
                            listOf("gemini-3.5-flash", "gemini-3.1-pro-preview", "gemini-2.5-flash-image").forEach { m ->
                                DropdownMenuItem(
                                    text = { Text(m, color = Color.White, fontSize = 13.sp) },
                                    onClick = {
                                        onModelSelected(m)
                                        dropdownExpanded = false
                                    },
                                    leadingIcon = {
                                        if (activeModel == m) {
                                            Icon(Icons.Default.Check, "Checked model", tint = Color(0xFF10B981), modifier = Modifier.size(16.dp))
                                        }
                                    }
                                )
                            }

                            if (providers.isNotEmpty()) {
                                Divider(color = Color(0xFF334155))
                                DropdownMenuItem(
                                    text = { Text("CUSTOM ENDPOINTS", fontWeight = FontWeight.Bold, color = Color(0xFF8B5CF6), fontSize = 11.sp) },
                                    onClick = {},
                                    enabled = false
                                )
                                providers.filter { it.isActive }.forEach { provider ->
                                    val customModel = provider.modelName
                                    DropdownMenuItem(
                                        text = { Text("${provider.providerName}: $customModel", color = Color.White, fontSize = 13.sp) },
                                        onClick = {
                                            onModelSelected(customModel)
                                            dropdownExpanded = false
                                        },
                                        leadingIcon = {
                                            if (activeModel == customModel) {
                                                Icon(Icons.Default.Check, "Checked custom model", tint = Color(0xFF10B981), modifier = Modifier.size(16.dp))
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                },
                actions = {
                    if (isStreaming) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(end = 8.dp)) {
                            CircularProgressIndicator(color = Color(0xFF3B82F6), strokeWidth = 2.dp, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Building...", color = Color(0xFF3B82F6), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    IconButton(onClick = onCreateNewChat, enabled = messages.isNotEmpty() || hasArtifact) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "New Chat", tint = if (messages.isNotEmpty() || hasArtifact) Color.White else Color.Gray)
                    }
                    IconButton(onClick = onClearChat, enabled = messages.isNotEmpty()) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = "Clear Chat Messages", tint = if (messages.isNotEmpty()) Color.White else Color.Gray)
                    }
                    IconButton(onClick = onDeleteChat) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete Chat", tint = Color(0xFFEF4444))
                    }
                    IconButton(onClick = onToggleIncognito) {
                        Icon(imageVector = Icons.Default.Lock, contentDescription = "Toggle Incognito Mode", tint = if (isIncognito) Color(0xFF3B82F6) else Color.White)
                    }
                    TextButton(
                        onClick = onToggleArtifactPanel,
                        enabled = hasArtifact || isStreaming,
                        modifier = Modifier.testTag("toggle_artifacts_preview_button")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "View changes",
                                tint = if (hasArtifact || isStreaming) Color(0xFF10B981) else Color.Gray,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("View changes", color = if (hasArtifact || isStreaming) Color.White else Color.Gray, fontSize = 13.sp)
                        }
                    }
                }
            )
        },
        containerColor = Color(0xFF020617), // Deepest dark slate black
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Chat history stream
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .fillMaxWidth()
            ) {
                if (messages.isEmpty() && currentStreamContent.isEmpty()) {
                    // Suggestion Grid empty state
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Face,
                            contentDescription = "Bot suggestion state",
                            tint = Color(0xFF334155),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Welcome to OpenClaw Workspace",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Select a deployment model at the top. Ask any code, architecture layout, or request visual artifacts.",
                            fontSize = 12.sp,
                            color = Color(0xFF64748B),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            modifier = Modifier.padding(top = 4.dp, bottom = 28.dp)
                        )

                        // Action chips suggestions
                        val suggestions = listOf(
                            "Render an interactive HTML digital timer dashboard" to "html_timer",
                            "Generate an SVG interactive canvas of planetary systems" to "planet_svg",
                            "Write a CSS visual styled calculator panel" to "html_calc",
                            "Generate a high-contrast futuristic custom landing layout" to "land_css"
                        )

                        suggestions.forEach { (title, key) ->
                            Card(
                                onClick = { onSendMessage(title, null) },
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(12.dp))
                                    .testTag("suggestion_chip_$key")
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.KeyboardArrowRight,
                                        contentDescription = "Pick code tag",
                                        tint = Color(0xFF3B82F6)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(text = title, color = Color(0xFF94A3B8), fontSize = 12.sp)
                                }
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp)
                    ) {
                        items(messages) { msg ->
                            ChatMessageRow(
                                message = msg,
                                onSelectArtifact = { type, content ->
                                    onSelectArtifact(WorkspaceViewModel.Artifact("Interactive Preview", type, content))
                                }
                            )
                        }

                        // Stream text in progress
                        if (currentStreamContent.isNotEmpty()) {
                            item {
                                ChatMessageRow(
                                    message = ChatMessage(
                                        conversationId = "",
                                        role = "assistant",
                                        content = currentStreamContent
                                    ),
                                    onSelectArtifact = { _, _ -> }
                                )
                            }
                        }

                        if (isStreaming && currentStreamContent.isEmpty()) {
                            item {
                                Row(
                                    modifier = Modifier.padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    CircularProgressIndicator(
                                        color = Color(0xFF3B82F6),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text("OpenClaw parsing nodes...", color = Color(0xFF64748B), fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }

            if (loadingError != null) {
                Text(
                    text = loadingError,
                    color = Color.Red,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }

            var showTermuxDialog by remember { mutableStateOf(false) }
            var showGeminiclawDialog by remember { mutableStateOf(false) }
            var showAlpineDialog by remember { mutableStateOf(false) }

            Column(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
                // Image Preview Area
                if (imageUri != null && imageBase64 != null) {
                    Surface(
                        color = Color(0xFF1E293B),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.padding(bottom = 8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(8.dp)) {
                            Icon(Icons.Default.AddCircle, contentDescription = "Attached image", tint = Color.White, modifier = Modifier.size(24.dp)) // Using AddCircle instead of Image as fallback to ensure it builds
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Image attached (${imageBase64!!.length / 1024} KB)", color = Color.White, fontSize = 12.sp)
                            Spacer(modifier = Modifier.weight(1f))
                            IconButton(onClick = { imageUri = null; imageBase64 = null }, modifier = Modifier.size(24.dp)) {
                                Icon(Icons.Default.Close, contentDescription = "Remove Image", tint = Color.Gray, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }

                // Text input container
                Surface(
                    color = Color(0xFF0F172A),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                            .fillMaxWidth()
                    ) {
                        Box {
                            IconButton(onClick = { showAttachmentMenu = true }) {
                                Icon(
                                    imageVector = Icons.Default.AddCircle,
                                    contentDescription = "Attachments visual logo simulations",
                                    tint = Color(0xFF94A3B8)
                                )
                            }
                            
                            DropdownMenu(
                                expanded = showAttachmentMenu,
                                onDismissRequest = { showAttachmentMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Upload Photo", color = Color.Black) },
                                    onClick = { 
                                        showAttachmentMenu = false
                                        imagePickerLauncher.launch("image/*") 
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Use Alpine Workspace", color = Color.Black) },
                                    onClick = { 
                                        showAttachmentMenu = false
                                        showAlpineDialog = true
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Connect to Termux", color = Color.Black) },
                                    onClick = { 
                                        showAttachmentMenu = false
                                        showTermuxDialog = true
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Connect Geminiclaw Link", color = Color.Black) },
                                    onClick = { 
                                        showAttachmentMenu = false
                                        showGeminiclawDialog = true
                                    }
                                )
                            }
                        }

                        TextField(
                            value = textInput,
                            onValueChange = { textInput = it },
                            placeholder = { Text("Message OpenClaw workspace...", color = Color(0xFF64748B)) },
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            ),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                            keyboardActions = KeyboardActions(onSend = {
                                if (textInput.isNotEmpty() || imageBase64 != null) {
                                    onSendMessage(textInput, imageBase64)
                                    textInput = ""
                                    imageUri = null
                                    imageBase64 = null
                                }
                            }),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("chat_input_text_field")
                        )

                        IconButton(
                            onClick = {
                                if (textInput.isNotEmpty() || imageBase64 != null) {
                                    onSendMessage(textInput, imageBase64)
                                    textInput = ""
                                    imageUri = null
                                    imageBase64 = null
                                }
                            },
                            modifier = Modifier.testTag("send_message_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Send,
                                contentDescription = "Send Message Icon",
                                tint = if (textInput.isNotEmpty() || imageBase64 != null) Color(0xFF3B82F6) else Color(0xFF64748B)
                            )
                        }
                    }
                }
                
                // Extra UI improvements area: Speech to text, Insert Files
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 4.dp)
                ) {
                    Surface(
                        color = Color(0xFF1E293B),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.clickable { }
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)) {
                            Icon(Icons.Default.Call, contentDescription = "Speech to text", tint = Color(0xFF38BDF8), modifier = Modifier.size(16.dp)) // Using Call because Mic might not be imported
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Speech text", color = Color.White, fontSize = 12.sp)
                        }
                    }

                    Surface(
                        color = Color(0xFF1E293B),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.clickable { showAttachmentMenu = true }
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)) {
                            Icon(Icons.Default.AddCircle, contentDescription = "Insert files", tint = Color(0xFF10B981), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Insert files", color = Color.White, fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    IconButton(onClick = {}, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Build, contentDescription = "Open app debug panel tooltip", tint = Color(0xFF94A3B8), modifier = Modifier.size(18.dp))
                    }
                }
            }

            if (showTermuxDialog) {
                AlertDialog(
                    onDismissRequest = { showTermuxDialog = false },
                    title = { Text("Connect to Termux") },
                    text = { Text("Termux integration allows OpenClaw to execute true native Android shell commands. Download the Termux API addon to proceed.") },
                    confirmButton = {
                        TextButton(onClick = { showTermuxDialog = false; textInput = "Initialize Termux root permissions request block" }) { Text("Initialize") }
                    },
                    dismissButton = {
                        TextButton(onClick = { showTermuxDialog = false }) { Text("Cancel") }
                    }
                )
            }
            
            if (showGeminiclawDialog) {
                AlertDialog(
                    onDismissRequest = { showGeminiclawDialog = false },
                    title = { Text("Connect Geminiclaw Remote") },
                    text = { Text("Bind the active Geminiclaw desktop or web agent instance to your OpenClaw session for distributed capability sharing.") },
                    confirmButton = {
                        TextButton(onClick = { showGeminiclawDialog = false; textInput = "Initiate Geminiclaw websocket bridge protocol" }) { Text("Connect") }
                    },
                    dismissButton = {
                        TextButton(onClick = { showGeminiclawDialog = false }) { Text("Cancel") }
                    }
                )
            }

            if (showAlpineDialog) {
                AlertDialog(
                    onDismissRequest = { showAlpineDialog = false },
                    title = { Text("Alpine Virtual Workspace") },
                    text = { Text("You are currently configured to use Alpine Workspace. All virtual file generation will persist into the Alpine VFS bounds.") },
                    confirmButton = {
                        TextButton(onClick = { showAlpineDialog = false; textInput = "Show me the Alpine Workspace root structure state." }) { Text("Scan Environment") }
                    },
                    dismissButton = {
                        TextButton(onClick = { showAlpineDialog = false }) { Text("Cancel") }
                    }
                )
            }
        }
    }
}

@Composable
fun ChatMessageRow(
    message: ChatMessage,
    onSelectArtifact: (String, String) -> Unit
) {
    val isUser = message.role == "user"

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        Row(
            verticalAlignment = Alignment.Top,
            horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
            modifier = Modifier.fillMaxWidth(0.85f)
        ) {
            if (!isUser) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .background(Color(0xFF3B82F6), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "AI profile avatar",
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
            }

            Column {
                Text(
                    text = if (isUser) "You" else "OpenClaw",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF64748B),
                    modifier = Modifier.padding(bottom = 2.dp)
                )

                Surface(
                    color = if (isUser) Color(0xFF1E293B) else Color(0xFF0F172A),
                    shape = RoundedCornerShape(
                        topStart = if (isUser) 16.dp else 4.dp,
                        topEnd = if (isUser) 4.dp else 16.dp,
                        bottomStart = 16.dp,
                        bottomEnd = 16.dp
                    ),
                    modifier = Modifier.border(
                        width = 1.dp,
                        color = if (isUser) Color(0xFF334155) else Color(0xFF1E293B),
                        shape = RoundedCornerShape(
                            topStart = if (isUser) 16.dp else 4.dp,
                            topEnd = if (isUser) 4.dp else 16.dp,
                            bottomStart = 16.dp,
                            bottomEnd = 16.dp
                        )
                    )
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        ParsableMessageContent(
                            text = message.content,
                            textColor = Color.White
                        )

                        // Render action card if message possesses a parsed preview artifact
                        if (message.isArtifact && message.artifactContent != null) {
                            val aType = message.artifactType ?: "html"
                            val aContent = message.artifactContent

                            Spacer(modifier = Modifier.height(10.dp))

                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onSelectArtifact(aType, aContent) }
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = "Artifact active icon logo",
                                        tint = Color(0xFF10B981),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = "Workspace Visual Artifact",
                                            color = Color.White,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 12.sp
                                        )
                                        Text(
                                            text = "Click to run the ${aType.uppercase()} code interactively in split pane",
                                            color = Color(0xFF94A3B8),
                                            fontSize = 10.sp
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
}

@Composable
fun ParsableMessageContent(text: String, textColor: Color) {
    if (!text.contains("```")) {
        Text(text = text, color = textColor, fontSize = 13.sp, lineHeight = 18.sp)
        return
    }

    val blocks = text.split("```")
    Column {
        blocks.forEachIndexed { index, block ->
            if (index % 2 == 0) {
                if (block.isNotEmpty()) {
                    Text(text = block.trim('\n'), color = textColor, fontSize = 13.sp, lineHeight = 18.sp)
                }
            } else {
                val lines = block.lines()
                val lang = lines.firstOrNull() ?: ""
                val code = lines.drop(1).joinToString("\n").trimEnd()

                Surface(
                    color = Color(0xFF030712), // Deep black background for code block
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        if (lang.isNotBlank()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFF1E293B))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = lang.trim(),
                                    color = Color(0xFF94A3B8),
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Text(
                            text = code,
                            color = Color(0xFF34D399), // Neon green code colors
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            lineHeight = 16.sp,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            }
        }
    }
}
