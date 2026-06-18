package com.example.ui.screens

import android.content.res.Configuration
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.viewmodel.WorkspaceViewModel

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun WorkspaceMainLayout(
    viewModel: WorkspaceViewModel
) {
    val workspaces by viewModel.workspaces.collectAsState()
    val activeWorkspace by viewModel.activeWorkspace.collectAsState()
    
    val conversations by viewModel.conversations.collectAsState()
    val providers by viewModel.providers.collectAsState()
    val activeConv by viewModel.activeConversation.collectAsState()
    val activeMessages by viewModel.activeMessages.collectAsState()
    val activeModel by viewModel.activeModel.collectAsState()
    
    val isStreaming by viewModel.isStreaming.collectAsState()
    val currentStreamContent by viewModel.currentStreamedContent.collectAsState()
    val loadingError by viewModel.loadingError.collectAsState()
    
    val activeArtifact by viewModel.activeArtifact.collectAsState()
    val showArtifactPanel by viewModel.showArtifactPanel.collectAsState()
    
    val isLoggedIn by viewModel.isLoggedIn.collectAsState()
    val userEmail by viewModel.userEmail.collectAsState()
    val userName by viewModel.userName.collectAsState()

    val testStatusMap by viewModel.testStatusMap.collectAsState()
    val testStatusNew by viewModel.testStatusNew.collectAsState()
    val themeIsDark by viewModel.currentThemeIsDark.collectAsState()

    var showProvidersConf by remember { mutableStateOf(false) }
    var showGlobalSettings by remember { mutableStateOf(false) }
    var showSkillsPanel by remember { mutableStateOf(false) }
    var showMcpPanel by remember { mutableStateOf(false) }
    var sidebarDrawerOpen by remember { mutableStateOf(false) }
    var incognitoMode by remember { mutableStateOf(false) }
    var rightPanelTab by remember { mutableStateOf("Artifact") }

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    if (!isLoggedIn) {
        // Render Login portal
        AuthScreen(onLoginSuccess = { email, name ->
            viewModel.login(email, name)
        })
        return
    }

    if (showProvidersConf) {
        // Show Provider Settings popup model override
        ProviderSettingsScreen(
            providers = providers,
            testStatusMap = testStatusMap,
            testStatusNew = testStatusNew,
            themeIsDark = themeIsDark,
            onSetThemeIsDark = { isDark -> viewModel.setThemeIsDark(isDark) },
            onAddProvider = { pName, apiKey, bUrl, mAlias ->
                viewModel.addProvider(pName, apiKey, bUrl, mAlias)
            },
            onToggleProvider = { provider ->
                viewModel.toggleProviderStatus(provider)
            },
            onDeleteProvider = { id ->
                viewModel.deleteProvider(id)
            },
            onTestSavedProvider = { provider ->
                viewModel.testSavedProvider(provider)
            },
            onTestNewProvider = { p, k, u, m ->
                viewModel.testNewProvider(p, k, u, m)
            },
            onDismiss = { showProvidersConf = false }
        )
        return
    }
    
    if (showGlobalSettings) {
        val sysPrompt by viewModel.systemPrompt.collectAsState()
        GlobalSettingsScreen(
            currentUserName = userName,
            currentUserEmail = userEmail,
            currentSystemPrompt = sysPrompt,
            themeIsDark = themeIsDark,
            onSaveProfile = { newName, newEmail ->
                viewModel.userName.value = newName
                viewModel.userEmail.value = newEmail
                showGlobalSettings = false
            },
            onSaveSystemPrompt = { newPrompt ->
                viewModel.systemPrompt.value = newPrompt
                showGlobalSettings = false
            },
            onDismiss = { showGlobalSettings = false }
        )
        return
    }

    if (showSkillsPanel) {
        val activePlugins by viewModel.activePlugins.collectAsState()
        SkillsScreen(
            plugins = activePlugins,
            themeIsDark = themeIsDark,
            onTogglePlugin = { viewModel.togglePlugin(it) },
            onAddCustomPlugin = { name, desc, prompt -> viewModel.addCustomPlugin(name, desc, prompt) },
            onDeletePlugin = { id -> viewModel.deletePlugin(id) },
            onDismiss = { showSkillsPanel = false }
        )
        return
    }

    if (showMcpPanel) {
        McpScreen(
            themeIsDark = themeIsDark,
            onDismiss = { showMcpPanel = false }
        )
        return
    }

    // Main App Scaffold Core
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF020617))
    ) {
        if (isLandscape) {
            // TABLET / DESKTOP WIDE SCREEN: Render Dual split panels side-by-side
            Row(modifier = Modifier.fillMaxSize()) {
                // Animated Sidebar
                AnimatedVisibility(
                    visible = sidebarDrawerOpen || conversations.isEmpty(),
                    enter = slideInHorizontally { -it } + fadeIn(),
                    exit = slideOutHorizontally { -it } + fadeOut()
                ) {
                    WorkspaceSidebar(
                        workspaces = workspaces,
                        activeWorkspace = activeWorkspace,
                        conversations = conversations,
                        activeConv = activeConv,
                        activeModel = activeModel,
                        providers = providers,
                        onModelSelected = { m ->
                            viewModel.activeModel.value = m
                        },
                        userEmail = userEmail,
                        userName = userName,
                        onSelectWorkspace = { ws -> viewModel.setActiveWorkspace(ws) },
                        onAddWorkspace = { name -> viewModel.addWorkspace(name) },
                        onRenameWorkspace = { id, name -> viewModel.renameWorkspace(id, name) },
                        onDeleteWorkspace = { id -> viewModel.deleteWorkspace(id) },
                        onSelectConv = { conv ->
                            viewModel.setActiveConversation(conv)
                        },
                        onDeleteConv = { id ->
                            viewModel.deleteConversation(id)
                        },
                        onCreateNewChat = {
                            viewModel.createConversation(activeModel)
                        },
                        onOpenProviders = { showProvidersConf = true },
                        onOpenGlobalSettings = { showGlobalSettings = true },
                        onOpenSkills = { showSkillsPanel = true },
                        onOpenMcp = { showMcpPanel = true },
                        onLogout = { viewModel.logout() },
                        modifier = Modifier.width(280.dp)
                    )
                }

                // Main Chat Screen Center Pane
                ChatScreen(
                    messages = activeMessages,
                    activeModel = activeModel,
                    providers = providers,
                    isStreaming = isStreaming,
                    currentStreamContent = currentStreamContent,
                    loadingError = loadingError,
                    hasArtifact = activeArtifact != null,
                    onModelSelected = { m ->
                        viewModel.activeModel.value = m
                        activeConv?.let { c ->
                            // Update model selection on conversation model attribute too
                            // viewModelScope database write handles updating automatically
                        }
                    },
                    onSendMessage = { prompt, imgBase64 ->
                        viewModel.sendMessage(prompt, imgBase64)
                    },
                    onClearChat = {
                        activeConv?.id?.let {
                            viewModel.clearConversationMessages(it)
                        }
                    },
                    onCreateNewChat = {
                        viewModel.createConversation(activeModel)
                    },
                    onDeleteChat = {
                        activeConv?.id?.let {
                            viewModel.deleteConversation(it)
                        }
                    },
                    onOpenSidebar = { sidebarDrawerOpen = !sidebarDrawerOpen },
                    onToggleArtifactPanel = {
                        viewModel.showArtifactPanel.value = !showArtifactPanel
                    },
                    onSelectArtifact = { art ->
                        viewModel.selectArtifact(art)
                    },
                    onToggleIncognito = { incognitoMode = !incognitoMode },
                    isIncognito = incognitoMode,
                    modifier = Modifier.weight(1f)
                )

                // Right Panel Split On Right Side
                AnimatedVisibility(
                    visible = showArtifactPanel,
                    enter = slideInHorizontally { it } + fadeIn(),
                    exit = slideOutHorizontally { it } + fadeOut(),
                    modifier = Modifier.weight(1.2f)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(vertical = 8.dp, horizontal = 12.dp)
                            .background(Color(0xFF1E293B), RoundedCornerShape(16.dp))
                    ) {
                        TabRow(
                            selectedTabIndex = when(rightPanelTab) {
                                "Files" -> 1
                                "Terminal" -> 2
                                else -> 0
                            },
                            containerColor = Color(0xFF0F172A),
                            contentColor = Color(0xFFA78BFA)
                        ) {
                            Tab(selected = rightPanelTab == "Artifact", onClick = { rightPanelTab = "Artifact" }) {
                                Text("Artifacts", modifier = Modifier.padding(12.dp), fontSize = 12.sp)
                            }
                            Tab(selected = rightPanelTab == "Files", onClick = { rightPanelTab = "Files" }) {
                                Text("File Explorer", modifier = Modifier.padding(12.dp), fontSize = 12.sp)
                            }
                            Tab(selected = rightPanelTab == "Terminal", onClick = { rightPanelTab = "Terminal" }) {
                                Text("Terminal / Shell", modifier = Modifier.padding(12.dp), fontSize = 12.sp)
                            }
                        }

                        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                            when (rightPanelTab) {
                                "Artifact" -> {
                                    val currentArtifact = activeArtifact
                                    if (currentArtifact != null) {
                                        ArtifactPanel(
                                            viewModel = viewModel,
                                            artifact = currentArtifact,
                                            onDismiss = { viewModel.showArtifactPanel.value = false },
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    } else {
                                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                            Text("No artifact generated.", color = Color.Gray, fontSize = 14.sp)
                                        }
                                    }
                                }
                                "Files" -> {
                                    FileExplorerPanel(
                                        viewModel = viewModel,
                                        onDismiss = { viewModel.showArtifactPanel.value = false },
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                                "Terminal" -> {
                                    TerminalPanel(
                                        viewModel = viewModel,
                                        onDismiss = { viewModel.showArtifactPanel.value = false },
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // MOBILE SCRIBBLES: Overlay or Drawer structure
            Box(modifier = Modifier.fillMaxSize()) {
                ChatScreen(
                    messages = activeMessages,
                    activeModel = activeModel,
                    providers = providers,
                    isStreaming = isStreaming,
                    currentStreamContent = currentStreamContent,
                    loadingError = loadingError,
                    hasArtifact = activeArtifact != null,
                    onModelSelected = { m ->
                        viewModel.activeModel.value = m
                    },
                    onSendMessage = { prompt, imgBase64 ->
                        viewModel.sendMessage(prompt, imgBase64)
                    },
                    onClearChat = {
                        activeConv?.id?.let {
                            viewModel.clearConversationMessages(it)
                        }
                    },
                    onCreateNewChat = {
                        viewModel.createConversation(activeModel)
                    },
                    onDeleteChat = {
                        activeConv?.id?.let {
                            viewModel.deleteConversation(it)
                        }
                    },
                    onOpenSidebar = { sidebarDrawerOpen = true },
                    onToggleArtifactPanel = {
                        viewModel.showArtifactPanel.value = !showArtifactPanel
                    },
                    onSelectArtifact = { art ->
                        viewModel.selectArtifact(art)
                    },
                    onToggleIncognito = { incognitoMode = !incognitoMode },
                    isIncognito = incognitoMode,
                    modifier = Modifier.fillMaxSize()
                )

                // Animated Overlay Drawer for Mobile Sidebar
                AnimatedVisibility(
                    visible = sidebarDrawerOpen,
                    enter = slideInHorizontally { -it } + fadeIn(),
                    exit = slideOutHorizontally { -it } + fadeOut()
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        // Blurred click backdrop dismisser
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.5f))
                                .clickable { sidebarDrawerOpen = false }
                        )

                        // Real Sidebar Drawer block
                        WorkspaceSidebar(
                            workspaces = workspaces,
                            activeWorkspace = activeWorkspace,
                            conversations = conversations,
                            activeConv = activeConv,
                            activeModel = activeModel,
                            providers = providers,
                            onModelSelected = { m ->
                                viewModel.activeModel.value = m
                            },
                            userEmail = userEmail,
                            userName = userName,
                            onSelectWorkspace = { ws -> viewModel.setActiveWorkspace(ws) },
                            onAddWorkspace = { name -> viewModel.addWorkspace(name) },
                            onRenameWorkspace = { id, name -> viewModel.renameWorkspace(id, name) },
                            onDeleteWorkspace = { id -> viewModel.deleteWorkspace(id) },
                            onSelectConv = { conv ->
                                viewModel.setActiveConversation(conv)
                                sidebarDrawerOpen = false
                            },
                            onDeleteConv = { id ->
                                viewModel.deleteConversation(id)
                            },
                            onCreateNewChat = {
                                viewModel.createConversation(activeModel)
                                sidebarDrawerOpen = false
                            },
                            onOpenProviders = {
                                showProvidersConf = true
                                sidebarDrawerOpen = false
                            },
                            onOpenGlobalSettings = {
                                showGlobalSettings = true
                                sidebarDrawerOpen = false
                            },
                            onOpenSkills = {
                                showSkillsPanel = true
                                sidebarDrawerOpen = false
                            },
                            onOpenMcp = {
                                showMcpPanel = true
                                sidebarDrawerOpen = false
                            },
                            onLogout = {
                                viewModel.logout()
                                sidebarDrawerOpen = false
                            },
                            modifier = Modifier
                                .width(280.dp)
                                .fillMaxHeight()
                        )
                    }
                }

                // Dynamic Slide-up Modal sheet overlay for Mobile Custom visual Artifact panel
                AnimatedVisibility(
                    visible = showArtifactPanel,
                    enter = slideInVertically { it } + fadeIn(),
                    exit = slideOutVertically { BoxSize -> BoxSize } + fadeOut()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.4f))
                    ) {
                        Column(modifier = Modifier.fillMaxSize()) {
                            Spacer(
                                modifier = Modifier
                                    .weight(0.15f)
                                    .fillMaxWidth()
                                    .clickable { viewModel.showArtifactPanel.value = false }
                            )

                            Column(
                                modifier = Modifier
                                    .weight(0.85f)
                                    .fillMaxWidth()
                                    .background(Color(0xFF1E293B), RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                            ) {
                                TabRow(
                                    selectedTabIndex = when(rightPanelTab) {
                                        "Files" -> 1
                                        "Terminal" -> 2
                                        else -> 0
                                    },
                                    containerColor = Color(0xFF0F172A),
                                    contentColor = Color(0xFFA78BFA)
                                ) {
                                    Tab(selected = rightPanelTab == "Artifact", onClick = { rightPanelTab = "Artifact" }) {
                                        Text("Artifacts", modifier = Modifier.padding(12.dp), fontSize = 12.sp)
                                    }
                                    Tab(selected = rightPanelTab == "Files", onClick = { rightPanelTab = "Files" }) {
                                        Text("File Explorer", modifier = Modifier.padding(12.dp), fontSize = 12.sp)
                                    }
                                    Tab(selected = rightPanelTab == "Terminal", onClick = { rightPanelTab = "Terminal" }) {
                                        Text("Terminal / Shell", modifier = Modifier.padding(12.dp), fontSize = 12.sp)
                                    }
                                }

                                Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                                    when (rightPanelTab) {
                                        "Artifact" -> {
                                            val currentArtifactMobile = activeArtifact
                                            if (currentArtifactMobile != null) {
                                                ArtifactPanel(
                                                    viewModel = viewModel,
                                                    artifact = currentArtifactMobile,
                                                    onDismiss = { viewModel.showArtifactPanel.value = false },
                                                    modifier = Modifier.fillMaxSize()
                                                )
                                            } else {
                                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                                    Text("No artifact generated.", color = Color.Gray, fontSize = 14.sp)
                                                }
                                            }
                                        }
                                        "Files" -> {
                                            FileExplorerPanel(
                                                viewModel = viewModel,
                                                onDismiss = { viewModel.showArtifactPanel.value = false },
                                                modifier = Modifier.fillMaxSize()
                                            )
                                        }
                                        "Terminal" -> {
                                            TerminalPanel(
                                                viewModel = viewModel,
                                                onDismiss = { viewModel.showArtifactPanel.value = false },
                                                modifier = Modifier.fillMaxSize()
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
}
