package com.example.ui.screens

import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.viewmodel.WorkspaceViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArtifactPanel(
    viewModel: WorkspaceViewModel,
    artifact: WorkspaceViewModel.Artifact,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = if (artifact.type == "terminal") {
        listOf("Alpine CLI", "VFS Explorer", "Android Bridge (ADB)", "Skills & Plugins")
    } else {
        listOf("Interactive Preview", "Source Code")
    }
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    Surface(
        color = Color(0xFF0F172A), // Slate 900 panel
        modifier = modifier
            .fillMaxHeight()
            .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(16.dp))
            .testTag("artifacts_panel_surface")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Visual Preview Logo",
                    tint = Color(0xFF10B981),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = artifact.title,
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Interactive Render Sandbox (${artifact.type.uppercase()})",
                        color = Color(0xFF64748B),
                        fontSize = 10.sp
                    )
                }

                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close Preview SplitPane",
                        tint = Color.White
                    )
                }
            }

            // Tab Rows
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color(0xFF1E293B),
                contentColor = Color.White,
                indicator = { tabPositions ->
                    if (selectedTab < tabPositions.size) {
                        TabRowDefaults.Indicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = Color(0xFF10B981)
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(36.dp)
                    .background(Color(0xFF1E293B), RoundedCornerShape(8.dp))
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, maxLines = 1) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Tab Content
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(Color(0xFF020617), RoundedCornerShape(12.dp))
                    .padding(4.dp)
            ) {
                if (artifact.type == "terminal") {
                    // TERMINAL RENDER PIPELINES
                    when (selectedTab) {
                        0 -> AlpineTerminalConsole(viewModel = viewModel)
                        1 -> VirtualFileSystemBrowser(viewModel = viewModel)
                        2 -> AndroidAdbBridgePanel(viewModel = viewModel)
                        3 -> PluginExtensionManagerPanel(viewModel = viewModel)
                    }
                } else {
                    // STANDARD ARTIFACT ROUTING
                    if (selectedTab == 0) {
                        // TAB 0: INTERACTIVE PREVIEW (WebView or SVG Box)
                        if (artifact.type == "svg") {
                            val htmlWrappedSvg = """
                                <!DOCTYPE html>
                                <html>
                                <head>
                                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                                    <style>
                                        body {
                                            background-color: #020617;
                                            display: flex;
                                            justify-content: center;
                                            align-items: center;
                                            height: 95vh;
                                            margin: 0;
                                            overflow: hidden;
                                        }
                                        svg {
                                            max-width: 95%;
                                            max-height: 95%;
                                            width: auto;
                                            height: auto;
                                        }
                                    </style>
                                </head>
                                <body>
                                    ${artifact.content}
                                </body>
                                </html>
                            """.trimIndent()

                            AndroidXmlWebView(htmlContent = htmlWrappedSvg)
                        } else if (artifact.type == "html") {
                            AndroidXmlWebView(htmlContent = artifact.content)
                        } else {
                            // General info for unrecognized type code
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Code output of type '${artifact.type.uppercase()}' cannot be visually simulated.\nRefer to the Source Code tab.",
                                    color = Color(0xFF475569),
                                    fontSize = 12.sp,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    } else {
                        // TAB 1: SOURCE CODE VIEW
                        Column(modifier = Modifier.fillMaxSize()) {
                            Row(
                                horizontalArrangement = Arrangement.End,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        clipboardManager.setText(AnnotatedString(artifact.content))
                                        Toast.makeText(context, "Copied code to dashboard clipboard!", Toast.LENGTH_SHORT).show()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A2E26)),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier.height(30.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Share,
                                            contentDescription = "Copy source code",
                                            tint = Color(0xFF10B981),
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Copy Code", color = Color(0xFF10B981), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                    .verticalScroll(rememberScrollState())
                                    .horizontalScroll(rememberScrollState())
                            ) {
                                Text(
                                    text = artifact.content,
                                    color = Color(0xFFF1F5F9),
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    lineHeight = 16.sp,
                                    modifier = Modifier.padding(bottom = 16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AlpineTerminalConsole(
    viewModel: WorkspaceViewModel
) {
    val shellLines by viewModel.shellLines.collectAsState()
    val currentDir by viewModel.currentDir.collectAsState()
    var terminalInput by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    // Auto-scroll when outputs are appended
    LaunchedEffect(shellLines.size) {
        if (shellLines.isNotEmpty()) {
            listState.animateScrollToItem(shellLines.size - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF020617))
            .padding(6.dp)
    ) {
        // Log stream screen
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(Color(0xFF030712), RoundedCornerShape(8.dp))
                .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(8.dp))
                .padding(8.dp)
        ) {
            items(shellLines) { line ->
                val color = when (line.type) {
                    "stdin" -> Color(0xFF38BDF8) // Bright Sky Blue
                    "stderr" -> Color(0xFFF87171) // Red error warning
                    "system" -> Color(0xFFA78BFA) // Purple virtual status log
                    else -> Color(0xFF34D399) // Phosphor Emerald Green
                }
                Text(
                    text = line.text,
                    color = color,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(vertical = 1.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Quick Command Launcher chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf("help", "ls", "pwd", "uname -a", "cat /etc/os-release", "python3 /root/fibonacci.py", "apk add curl", "clear").forEach { quickCmd ->
                Surface(
                    onClick = {
                        viewModel.executeShellCommand(quickCmd)
                    },
                    color = Color(0xFF1E293B),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = quickCmd,
                        color = Color(0xFF38BDF8),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Interactive command feed input
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color(0xFF334155), RoundedCornerShape(8.dp))
                .background(Color(0xFF0F172A))
                .padding(horizontal = 8.dp)
        ) {
            Text(
                text = "localhost:${currentDir}$ ",
                color = Color(0xFF64748B),
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace
            )
            BasicTextField(
                value = terminalInput,
                onValueChange = { terminalInput = it },
                textStyle = LocalTextStyle.current.copy(
                    color = Color.White,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                ),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
                keyboardActions = KeyboardActions(
                    onGo = {
                        if (terminalInput.trim().isNotEmpty()) {
                            viewModel.executeShellCommand(terminalInput)
                            terminalInput = ""
                        }
                    }
                ),
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 8.dp)
            )
            IconButton(
                onClick = {
                    if (terminalInput.trim().isNotEmpty()) {
                        viewModel.executeShellCommand(terminalInput)
                        terminalInput = ""
                    }
                },
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Trigger shell action",
                    tint = Color(0xFF10B981),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun VirtualFileSystemBrowser(
    viewModel: WorkspaceViewModel
) {
    val virtualFiles by viewModel.virtualFiles.collectAsState()
    val currentDir by viewModel.currentDir.collectAsState()
    var selectedFilePath by remember { mutableStateOf<String?>(null) }
    var inEditMode by remember { mutableStateOf(false) }
    var editableContent by remember { mutableStateOf("") }
    
    // Auto-update editable content when switching files
    LaunchedEffect(selectedFilePath, virtualFiles) {
        if (!inEditMode) {
            editableContent = virtualFiles[selectedFilePath] ?: ""
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Alpine Workspace Memory System",
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 12.sp
                )
                Text(
                    text = "These virtual files persist securely isoled inside your workspace environment memory stream.",
                    color = Color(0xFF64748B),
                    fontSize = 10.sp
                )
            }
            if (selectedFilePath != null) {
                if (inEditMode) {
                    Row {
                        TextButton(onClick = { inEditMode = false; editableContent = virtualFiles[selectedFilePath] ?: "" }) {
                            Text("Cancel", color = Color(0xFF94A3B8), fontSize = 11.sp)
                        }
                        Button(
                            onClick = {
                                viewModel.createVirtualFile(selectedFilePath!!, editableContent)
                                inEditMode = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            modifier = Modifier.height(30.dp)
                        ) {
                            Text("Save File", fontSize = 11.sp, color = Color.White)
                        }
                    }
                } else {
                    Button(
                        onClick = { inEditMode = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6)),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier.height(30.dp)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit File", tint = Color.White, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Edit", fontSize = 11.sp, color = Color.White)
                    }
                }
            }
        }

        Row(modifier = Modifier.weight(1f)) {
            // File List pane Left
            LazyColumn(
                modifier = Modifier
                    .weight(0.35f)
                    .background(Color(0xFF0F172A), RoundedCornerShape(8.dp))
                    .padding(4.dp)
            ) {
                items(virtualFiles.keys.toList()) { path ->
                    val isSelected = selectedFilePath == path
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                color = if (isSelected) Color(0xFF1E293B) else Color.Transparent,
                                shape = RoundedCornerShape(4.dp)
                            )
                            .clickable {
                                if (!inEditMode) {
                                    selectedFilePath = path
                                }
                            }
                            .padding(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = path.split("/").last(),
                            color = if (isSelected) Color(0xFF34D399) else Color(0xFF94A3B8),
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // File Content Editor Preview pane Right
            Box(
                modifier = Modifier
                    .weight(0.65f)
                    .background(Color(0xFF030712), RoundedCornerShape(8.dp))
                    .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(8.dp))
                    .padding(8.dp)
            ) {
                val path = selectedFilePath
                if (path != null) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        Text(
                            text = path,
                            color = Color(0xFF64748B),
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                        Box(
                            modifier = Modifier
                                .weight(1f)
                        ) {
                            if (inEditMode) {
                                TextField(
                                    value = editableContent,
                                    onValueChange = { editableContent = it },
                                    textStyle = androidx.compose.ui.text.TextStyle(
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace,
                                        lineHeight = 15.sp
                                    ),
                                    colors = TextFieldDefaults.colors(
                                        focusedContainerColor = Color.Transparent,
                                        unfocusedContainerColor = Color.Transparent,
                                        focusedIndicatorColor = Color.Transparent,
                                        unfocusedIndicatorColor = Color.Transparent
                                    ),
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                val content = virtualFiles[path] ?: ""
                                Text(
                                    text = content,
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    lineHeight = 15.sp,
                                    modifier = Modifier.verticalScroll(rememberScrollState())
                                )
                            }
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Select a VFS file to inspect or edit context",
                            color = Color(0xFF475569),
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AndroidXmlWebView(htmlContent: String) {
    AndroidView(
        factory = { context ->
            WebView(context).apply {
                webViewClient = WebViewClient()
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.useWideViewPort = true
                settings.loadWithOverviewMode = true
                setBackgroundColor(0xFF020617.toInt()) // Sync backdrop background with app matching dark style
            }
        },
        update = { webView ->
            webView.loadDataWithBaseURL(null, htmlContent, "text/html", "utf-8", null)
        },
        modifier = Modifier.fillMaxSize()
    )
}

@Composable
fun AndroidAdbBridgePanel(viewModel: WorkspaceViewModel) {
    val devices by viewModel.activeDevices.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var deviceName by remember { mutableStateOf("") }
    var deviceSerial by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "ADB Devices Bridge (${devices.size} active)",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            )
            Button(
                onClick = { showAddDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier.height(28.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Connect device", tint = Color.White, modifier = Modifier.size(12.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Connect Device", fontSize = 10.sp, color = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (showAddDialog) {
            ElevatedCard(
                colors = CardDefaults.elevatedCardColors(containerColor = Color(0xFF1E293B)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
                    .border(1.dp, Color(0xFF334155), RoundedCornerShape(12.dp))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Register Simulated Device Bridge", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = deviceName,
                        onValueChange = { deviceName = it },
                        label = { Text("Device Name", fontSize = 10.sp, color = Color.Gray) },
                        textStyle = LocalTextStyle.current.copy(color = Color.White, fontSize = 11.sp),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = deviceSerial,
                        onValueChange = { deviceSerial = it },
                        label = { Text("Serial Address IP", fontSize = 10.sp, color = Color.Gray) },
                        textStyle = LocalTextStyle.current.copy(color = Color.White, fontSize = 11.sp),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                        TextButton(onClick = { showAddDialog = false }) {
                            Text("Cancel", fontSize = 10.sp, color = Color.Gray)
                        }
                        Button(
                            onClick = {
                                if (deviceName.isNotEmpty() && deviceSerial.isNotEmpty()) {
                                    viewModel.addDevice(deviceName, deviceSerial)
                                    deviceName = ""
                                    deviceSerial = ""
                                    showAddDialog = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text("Save", fontSize = 10.sp, color = Color.White)
                        }
                    }
                }
            }
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(devices) { dev ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(8.dp))
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Build,
                                    contentDescription = "Android Controller",
                                    tint = if (dev.status == "ONLINE") Color(0xFF10B981) else Color.Gray,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(dev.name, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            Surface(
                                color = when (dev.status) {
                                    "ONLINE" -> Color(0xFF064E3B)
                                    "REBOOTING" -> Color(0xFF78350F)
                                    else -> Color(0xFF374151)
                                },
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = dev.status,
                                    color = when (dev.status) {
                                        "ONLINE" -> Color(0xFF34D399)
                                        "REBOOTING" -> Color(0xFFFBBF24)
                                        else -> Color(0xFF9CA3AF)
                                    },
                                    fontSize = 8.sp,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Serial: ${dev.serial}  |  Battery: ${dev.battery}%  |  ${dev.network}", color = Color(0xFF64748B), fontSize = 9.sp)
                        Spacer(modifier = Modifier.height(8.dp))

                        // Actions row
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Button(
                                onClick = {
                                    viewModel.executeShellCommand("adb reboot")
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(24.dp),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = "ADB Reboot", tint = Color(0xFF38BDF8), modifier = Modifier.size(10.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Reboot", fontSize = 9.sp, color = Color(0xFF38BDF8))
                            }

                            Button(
                                onClick = {
                                    viewModel.executeShellCommand("adb shell input tap 450 125")
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(24.dp),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = "Tap input event", tint = Color(0xFF34D399), modifier = Modifier.size(10.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Tap (450,125)", fontSize = 9.sp, color = Color(0xFF34D399))
                            }

                            Button(
                                onClick = {
                                    viewModel.executeShellCommand("adb shell getprop")
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(24.dp),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Icon(Icons.Default.Settings, contentDescription = "Get properties", tint = Color(0xFFA78BFA), modifier = Modifier.size(10.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("GetProp", fontSize = 9.sp, color = Color(0xFFA78BFA))
                            }

                            Button(
                                onClick = {
                                    viewModel.deleteDevice(dev.id)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2D1616)),
                                modifier = Modifier
                                    .weight(0.6f)
                                    .height(24.dp),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Disconnect Device", tint = Color(0xFFF87171), modifier = Modifier.size(10.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PluginExtensionManagerPanel(viewModel: WorkspaceViewModel) {
    val plugins by viewModel.activePlugins.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var pluginName by remember { mutableStateOf("") }
    var pluginDesc by remember { mutableStateOf("") }
    var pluginPrompt by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Claw Plugin Extensions Manager",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            )
            Button(
                onClick = { showAddDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFA78BFA)),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier.height(28.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Create Custom Extension", tint = Color.Black, modifier = Modifier.size(12.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add Plugin", fontSize = 10.sp, color = Color.Black, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (showAddDialog) {
            ElevatedCard(
                colors = CardDefaults.elevatedCardColors(containerColor = Color(0xFF1E293B)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
                    .border(1.dp, Color(0xFF334155), RoundedCornerShape(12.dp))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Register Custom Prompt Skill Injector", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = pluginName,
                        onValueChange = { pluginName = it },
                        label = { Text("Extension Name", fontSize = 10.sp, color = Color.Gray) },
                        textStyle = LocalTextStyle.current.copy(color = Color.White, fontSize = 11.sp),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = pluginDesc,
                        onValueChange = { pluginDesc = it },
                        label = { Text("Description", fontSize = 10.sp, color = Color.Gray) },
                        textStyle = LocalTextStyle.current.copy(color = Color.White, fontSize = 11.sp),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = pluginPrompt,
                        onValueChange = { pluginPrompt = it },
                        label = { Text("System Prompt Instructions Prefix", fontSize = 10.sp, color = Color.Gray) },
                        textStyle = LocalTextStyle.current.copy(color = Color.White, fontSize = 11.sp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                        TextButton(onClick = { showAddDialog = false }) {
                            Text("Cancel", fontSize = 10.sp, color = Color.Gray)
                        }
                        Button(
                            onClick = {
                                if (pluginName.isNotEmpty() && pluginPrompt.isNotEmpty()) {
                                    viewModel.addCustomPlugin(pluginName, pluginDesc, pluginPrompt)
                                    pluginName = ""
                                    pluginDesc = ""
                                    pluginPrompt = ""
                                    showAddDialog = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFA78BFA)),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text("Install", fontSize = 10.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(plugins) { plg ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(8.dp))
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                val vectorIcon = when (plg.icon) {
                                    "PhoneAndroid" -> Icons.Default.Build
                                    "Search" -> Icons.Default.Search
                                    "Analytics" -> Icons.Default.Info
                                    "AutoAwesome" -> Icons.Default.Share
                                    else -> Icons.Default.Build
                                }
                                Icon(
                                    imageVector = vectorIcon,
                                    contentDescription = plg.name,
                                    tint = if (plg.isEnabled) Color(0xFFA78BFA) else Color.Gray,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(plg.name, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            
                            Switch(
                                checked = plg.isEnabled,
                                onCheckedChange = { viewModel.togglePlugin(plg) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color(0xFFA78BFA),
                                    checkedTrackColor = Color(0xFF4C1D95)
                                )
                            )
                        }
                        
                        Text(plg.description, color = Color(0xFF94A3B8), fontSize = 10.sp, modifier = Modifier.padding(top = 4.dp))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Prompt: ${plg.systemPromptPrefix.take(80)}...", color = Color(0xFF64748B), fontSize = 8.sp, fontFamily = FontFamily.Monospace)
                        
                        if (plg.category == "custom") {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                TextButton(
                                    onClick = { viewModel.deletePlugin(plg.id) },
                                    contentPadding = PaddingValues(0.dp),
                                    modifier = Modifier.height(20.dp)
                                ) {
                                    Text("Uninstall Extension", color = Color(0xFFF87171), fontSize = 9.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
