package com.example.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import com.example.network.LlmRouter
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

class WorkspaceViewModel(application: Application) : AndroidViewModel(application) {
    private val TAG = "WorkspaceViewModel"
    private val database = AppDatabase.getDatabase(application)
    private val workspaceDao = database.workspaceDao()
    private val providerDao = database.providerDao()
    private val conversationDao = database.conversationDao()
    private val messageDao = database.messageDao()
    private val deviceDao = database.deviceDao()
    private val virtualFileDao = database.virtualFileDao()
    private val clawPluginDao = database.clawPluginDao()
    val prefs = PreferencesManager(application)

    // WORKSPACE SYSTEM
    val workspaces: StateFlow<List<Workspace>> = workspaceDao.getAllWorkspaces()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeWorkspace = MutableStateFlow<Workspace?>(null)

    // Dynamic Collections driven by active Workspace
    val conversations = MutableStateFlow<List<ChatConversation>>(emptyList())
    val providers = MutableStateFlow<List<ProviderConfig>>(emptyList())
    val activeDevices = MutableStateFlow<List<AndroidDevice>>(emptyList())
    val activePlugins = MutableStateFlow<List<ClawPlugin>>(emptyList())

    val activeConversation = MutableStateFlow<ChatConversation?>(null)
    val activeMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val activeModel = MutableStateFlow("gemini-3.5-flash")

    // Active Jobs to cancel flow collections when switching workspaces
    private var workspaceCollectionJob: Job? = null
    
    // Streaming / Loading States
    val isStreaming = MutableStateFlow(false)
    val currentStreamedContent = MutableStateFlow("")
    val loadingError = MutableStateFlow<String?>(null)

    // Selected Artifact for Split-Screen Preview
    data class Artifact(
        val title: String,
        val type: String, // "html", "svg", "code", "terminal"
        val content: String
    )
    val activeArtifact = MutableStateFlow<Artifact?>(null)
    val showArtifactPanel = MutableStateFlow(false)

    // User Profile SSO
    val isLoggedIn = MutableStateFlow(prefs.isLoggedIn)
    val userEmail = MutableStateFlow(prefs.userEmail ?: "")
    val userName = MutableStateFlow(prefs.userName ?: "")

    // Live Theme preference
    val currentThemeIsDark = MutableStateFlow(prefs.currentThemeIsDark)

    fun setThemeIsDark(isDark: Boolean) {
        prefs.currentThemeIsDark = isDark
        currentThemeIsDark.value = isDark
    }

    // Testing API Keys validation states
    val testStatusMap = MutableStateFlow<Map<Int, String>>(emptyMap())
    val testStatusNew = MutableStateFlow<String?>(null)

    // Prompts and Settings
    val systemPrompt = MutableStateFlow("You are OpenClaw, a highly skilled AI assistant. Fully stateful Linux mock-terminal, and ADB virtual devices are enabled.")

    // --- STATEFUL ALPINE LINUX SHELL SANDBOX SIMULATOR ---
    data class ShellLine(val text: String, val type: String = "stdout") // "stdout", "stderr", "stdin", "system"
    val shellLines = MutableStateFlow<List<ShellLine>>(emptyList())
    
    // Virtual File System map (filePath -> content)
    val virtualFiles = MutableStateFlow<Map<String, String>>(emptyMap())
    val currentDir = MutableStateFlow("/root")

    init {
        Log.d(TAG, "WorkspaceViewModel initialized. Base Auth Status: ${prefs.isLoggedIn}")
        
        // Listen to workspaces to pre-populate or select a default one
        viewModelScope.launch {
            workspaces.collect { list ->
                if (list.isEmpty()) {
                    // Seed initial workspace
                    val defaultWs = Workspace(name = "Personal Core Workspace")
                    workspaceDao.insertWorkspace(defaultWs)
                } else if (activeWorkspace.value == null) {
                    // Default to the first workspace
                    setActiveWorkspace(list.first())
                }
            }
        }

        // Initialize developer Alpine command shell welcomes
        shellLines.value = listOf(
            ShellLine("Alpine Linux 3.19.1 x86_64", "system"),
            ShellLine("Initializing secure standalone terminal emulator framework ...", "system"),
            ShellLine("Local storage and virtual microkernel mount: success.", "system"),
            ShellLine("Welcome! Type 'help' to read autonomous capabilities, or run 'python3 /root/fibonacci.py'.", "stdout")
        )
    }

    // --- WORKSPACE OPERATIONS ---
    fun setActiveWorkspace(ws: Workspace?) {
        activeWorkspace.value = ws
        activeConversation.value = null
        activeMessages.value = emptyList()
        activeArtifact.value = null
        showArtifactPanel.value = false

        workspaceCollectionJob?.cancel()
        if (ws != null) {
            seedDefaultWorkspaceData(ws.id)
            workspaceCollectionJob = viewModelScope.launch {
                // Collect and bind from Room scoped to the active workspace
                launch {
                    conversationDao.getConversationsForWorkspace(ws.id).collect { list ->
                        conversations.value = list
                    }
                }
                launch {
                    providerDao.getProvidersForWorkspace(ws.id).collect { list ->
                        providers.value = list
                    }
                }
                launch {
                    deviceDao.getDevicesForWorkspace(ws.id).collect { list ->
                        activeDevices.value = list
                    }
                }
                launch {
                    clawPluginDao.getPluginsForWorkspace(ws.id).collect { list ->
                        activePlugins.value = list
                    }
                }
                launch {
                    virtualFileDao.getFilesForWorkspace(ws.id).collect { list ->
                        virtualFiles.value = list.associate { it.filePath to it.content }
                    }
                }
            }
        } else {
            conversations.value = emptyList()
            providers.value = emptyList()
            activeDevices.value = emptyList()
            activePlugins.value = emptyList()
            virtualFiles.value = emptyMap()
        }
    }

    private fun seedDefaultWorkspaceData(workspaceId: String) {
        viewModelScope.launch {
            // Seed files, devices, and plugins in background
            launch {
                val dbFiles = virtualFileDao.getFileByPath(workspaceId, "/root/welcome.sh")
                if (dbFiles == null) {
                    val defaultFiles = listOf(
                        VirtualFile(
                            workspaceId = workspaceId,
                            filePath = "/root/welcome.sh",
                            content = "echo \"Welcome to OpenClaw Alpine Linux Shell v3.19!\"\necho \"Type python3, adb or help to run simulation sequences.\""
                        ),
                        VirtualFile(
                            workspaceId = workspaceId,
                            filePath = "/root/fibonacci.py",
                            content = "def fib(n):\n    if n <= 1: return n\n    return fib(n-1) + fib(n-2)\n\nprint([fib(i) for i in range(10)])"
                        ),
                        VirtualFile(
                            workspaceId = workspaceId,
                            filePath = "/etc/os-release",
                            content = "NAME=\"Alpine Linux\"\nVERSION_ID=3.19.1\nPRETTY_NAME=\"Alpine Linux v3.19\"\nHOME_URL=\"https://alpinelinux.org/\""
                        )
                    )
                    defaultFiles.forEach { virtualFileDao.insertFile(it) }
                }
            }

            launch {
                val dbPlugins = clawPluginDao.getActivePluginsForWorkspace(workspaceId)
                if (dbPlugins.isEmpty()) {
                    val defaultPlugins = listOf(
                        ClawPlugin(
                            workspaceId = workspaceId,
                            name = "ADB Device Controller",
                            category = "devices",
                            description = "Link to virtual and real emulators, trigger visual taps with input events, capture screen metrics and monitor logs.",
                            isEnabled = true,
                            icon = "PhoneAndroid",
                            systemPromptPrefix = "ADB Device Controller active: Allows executing simulated ADB pipelines ('adb devices', 'adb reboot', 'adb shell pm list packages'). Use these controls to debug testing flows."
                        ),
                        ClawPlugin(
                            workspaceId = workspaceId,
                            name = "Advanced Grep & Search",
                            category = "utilities",
                            description = "Adds powerful command search patterns, letting the model scan workspace directories and find code files natively.",
                            isEnabled = true,
                            icon = "Search",
                            systemPromptPrefix = "Advanced Grep Search active: You can crawl virtual files matching search expressions with 'grep <query>'."
                        ),
                        ClawPlugin(
                            workspaceId = workspaceId,
                            name = "Live Metric Monitor",
                            category = "system",
                            description = "Collect microkernel performance, active memory heap allocations, terminal processor loads and latency statistics.",
                            isEnabled = false,
                            icon = "Analytics",
                            systemPromptPrefix = "Live Metrics active: Reference resource boundaries and thread footprints when answering performance questions."
                        ),
                        ClawPlugin(
                            workspaceId = workspaceId,
                            name = "HTML Canvas Optimizer",
                            category = "custom",
                            description = "Instructs Gemini to wrap all interactive web artifacts in fully fluid, elegant responsive designs with dynamic styling.",
                            isEnabled = true,
                            icon = "AutoAwesome",
                            systemPromptPrefix = "HTML Optimizer active: Wrap interactive canvas previews in fluid Material 3 Slate panels."
                        ),
                        ClawPlugin(
                            workspaceId = workspaceId,
                            name = "Gemini / Claude Code Extension",
                            category = "system",
                            description = "Enables default intelligent code generation bridging through direct Google Gemini and Anthropic Claude neural nodes.",
                            isEnabled = true,
                            icon = "Code",
                            systemPromptPrefix = "Gemini/Claude Code Extension active: Enhance structural generation via optimal ML intelligence."
                        ),
                        ClawPlugin(
                            workspaceId = workspaceId,
                            name = "OpenClaw Skills Framework",
                            category = "system",
                            description = "Core native OpenClaw plugins bundle giving agent full environment access and capabilities.",
                            isEnabled = true,
                            icon = "Settings",
                            systemPromptPrefix = "OpenClaw Skills Framework active: Full agentic control loop pipeline engaged."
                        )
                    )
                    defaultPlugins.forEach { clawPluginDao.insertPlugin(it) }
                    updateDynamicSystemPrompt()
                }
            }

            launch {
                // Seed a default mock ADB device
                val defaultDev = AndroidDevice(
                    workspaceId = workspaceId,
                    name = "Pixel 9 Pro (Simulated)",
                    serial = "ADB-CLAW-99XS7",
                    status = "ONLINE",
                    battery = 88,
                    network = "WiFi - Connected"
                )
                deviceDao.insertDevice(defaultDev)
            }
        }
    }

    // --- NATIVE FILESYSTEM OPERATIONS ---
    fun createVirtualFile(path: String, content: String) {
        val currentWs = activeWorkspace.value ?: return
        viewModelScope.launch {
            val file = VirtualFile(workspaceId = currentWs.id, filePath = path, content = content)
            virtualFileDao.insertFile(file)
        }
    }

    fun deleteVirtualFile(path: String) {
        val currentWs = activeWorkspace.value ?: return
        viewModelScope.launch {
            virtualFileDao.deleteFileByPath(currentWs.id, path)
        }
    }

    // --- PLUGIN / EXTENSION MANAGEMENT ---
    fun togglePlugin(plugin: ClawPlugin) {
        viewModelScope.launch {
            val updated = plugin.copy(isEnabled = !plugin.isEnabled)
            clawPluginDao.updatePlugin(updated)
            updateDynamicSystemPrompt()
        }
    }

    fun addCustomPlugin(name: String, description: String, promptPrefix: String) {
        val currentWs = activeWorkspace.value ?: return
        viewModelScope.launch {
            val newPlugin = ClawPlugin(
                workspaceId = currentWs.id,
                name = name,
                description = description,
                category = "custom",
                isEnabled = true,
                icon = "SettingsInputAntenna",
                systemPromptPrefix = promptPrefix
            )
            clawPluginDao.insertPlugin(newPlugin)
            updateDynamicSystemPrompt()
        }
    }

    fun deletePlugin(id: String) {
        viewModelScope.launch {
            clawPluginDao.deletePluginById(id)
            updateDynamicSystemPrompt()
        }
    }

    private fun updateDynamicSystemPrompt() {
        val currentWs = activeWorkspace.value ?: return
        viewModelScope.launch {
            val active = clawPluginDao.getActivePluginsForWorkspace(currentWs.id)
            val baseInstructions = """
                You are OpenClaw, a highly skilled AI assistant mirroring Anthropic Artifacts. 
                You produce exceptional Material design visual previews, clean functional vector SVGs, and real working code.
                You are equipped with a stateful interactive Alpine Linux sandbox, terminal environment, and ADB device interfaces.
                Execute action commands autonomously when requested.
            """.trimIndent()
            
            val pluginsInstruction = active.joinToString(separator = "\n") { "- " + it.systemPromptPrefix }
            systemPrompt.value = "$baseInstructions\n\nActive extension directives:\n$pluginsInstruction"
        }
    }

    // --- ANDROID PHONE MANAGERS ---
    fun addDevice(name: String, serial: String, status: String = "ONLINE") {
        val currentWs = activeWorkspace.value ?: return
        viewModelScope.launch {
            val device = AndroidDevice(
                workspaceId = currentWs.id,
                name = name,
                serial = serial,
                status = status
            )
            deviceDao.insertDevice(device)
        }
    }

    fun deleteDevice(deviceId: String) {
        viewModelScope.launch {
            deviceDao.deleteDeviceById(deviceId)
        }
    }

    fun updateDeviceStatus(device: AndroidDevice, newStatus: String) {
        viewModelScope.launch {
            deviceDao.updateDevice(device.copy(status = newStatus))
        }
    }

    fun addWorkspace(name: String) {
        viewModelScope.launch {
            val newWorkspace = Workspace(name = name)
            workspaceDao.insertWorkspace(newWorkspace)
            setActiveWorkspace(newWorkspace)
        }
    }

    fun renameWorkspace(id: String, newName: String) {
        viewModelScope.launch {
            val updated = Workspace(id = id, name = newName)
            workspaceDao.insertWorkspace(updated)
            if (activeWorkspace.value?.id == id) {
                activeWorkspace.value = updated
            }
        }
    }

    fun deleteWorkspace(id: String) {
        viewModelScope.launch {
            workspaceDao.deleteWorkspaceById(id)
            // Clean up files in workspace
            conversationDao.getConversationsForWorkspace(id).collect { list ->
                list.forEach { 
                    conversationDao.deleteConversationById(it.id)
                    messageDao.deleteMessagesForConversation(it.id)
                }
            }
            if (activeWorkspace.value?.id == id) {
                val rem = workspaces.value.firstOrNull { it.id != id }
                setActiveWorkspace(rem)
            }
        }
    }

    // --- COGNITIVE AUTH PROFILE ---
    fun login(email: String, name: String) {
        viewModelScope.launch {
            prefs.isLoggedIn = true
            prefs.userEmail = email
            prefs.userName = name
            isLoggedIn.value = true
            userEmail.value = email
            userName.value = name
        }
    }

    fun logout() {
        viewModelScope.launch {
            prefs.isLoggedIn = false
            isLoggedIn.value = false
            activeConversation.value = null
            activeMessages.value = emptyList()
            activeArtifact.value = null
            showArtifactPanel.value = false
        }
    }

    // --- CHAT DIALOGUE CONTROL ---
    fun setActiveConversation(conv: ChatConversation?) {
        activeConversation.value = conv
        activeArtifact.value = null
        showArtifactPanel.value = false
        if (conv != null) {
            activeModel.value = conv.activeModel
            // Load messages dynamically
            viewModelScope.launch {
                messageDao.getMessagesForConversation(conv.id).collect { msgs ->
                    activeMessages.value = msgs
                    // Scan messages for artifacts to display automatically
                    for (msg in msgs.reversed()) {
                        if (msg.isArtifact) {
                            activeArtifact.value = Artifact(
                                title = "Generated Artifact (${msg.artifactType?.uppercase()})",
                                type = msg.artifactType ?: "html",
                                content = msg.artifactContent ?: ""
                            )
                            showArtifactPanel.value = true
                            break
                        }
                    }
                }
            }
        } else {
            activeMessages.value = emptyList()
        }
    }

    fun createConversation(model: String) {
        val currentWs = activeWorkspace.value ?: return
        viewModelScope.launch {
            val convId = UUID.randomUUID().toString()
            val newConv = ChatConversation(id = convId, workspaceId = currentWs.id, title = "New Chat", activeModel = model)
            conversationDao.insertConversation(newConv)
            setActiveConversation(newConv)
        }
    }

    fun deleteConversation(id: String) {
        viewModelScope.launch {
            conversationDao.deleteConversationById(id)
            messageDao.deleteMessagesForConversation(id)
            if (activeConversation.value?.id == id) {
                activeConversation.value = null
                activeMessages.value = emptyList()
                activeArtifact.value = null
                showArtifactPanel.value = false
            }
        }
    }

    fun clearConversationMessages(id: String) {
        viewModelScope.launch {
            messageDao.deleteMessagesForConversation(id)
        }
    }

    fun addProvider(providerName: String, rawApiKey: String, baseUrl: String, modelName: String) {
        val currentWs = activeWorkspace.value ?: return
        viewModelScope.launch {
            val encryptedKey = EncryptionHelper.encrypt(rawApiKey)
            val newProvider = ProviderConfig(
                workspaceId = currentWs.id,
                providerName = providerName,
                apiKeyEncrypted = encryptedKey,
                baseUrl = baseUrl,
                isActive = true,
                modelName = modelName
            )
            providerDao.insertProvider(newProvider)
        }
    }

    fun toggleProviderStatus(provider: ProviderConfig) {
        viewModelScope.launch {
            val updated = provider.copy(isActive = !provider.isActive)
            providerDao.updateProvider(updated)
        }
    }

    fun deleteProvider(id: Int) {
        viewModelScope.launch {
            providerDao.deleteProviderById(id)
        }
    }

    fun testSavedProvider(provider: ProviderConfig) {
        viewModelScope.launch {
            testStatusMap.value = testStatusMap.value + (provider.id to "Testing handshake...")
            try {
                val rawApiKey = EncryptionHelper.decrypt(provider.apiKeyEncrypted)
                val result = com.example.network.LlmRouter.testConnection(
                    provider.providerName,
                    provider.baseUrl,
                    rawApiKey,
                    provider.modelName
                )
                result.fold(
                    onSuccess = { msg ->
                        testStatusMap.value = testStatusMap.value + (provider.id to "SUCCESS")
                    },
                    onFailure = { t ->
                        testStatusMap.value = testStatusMap.value + (provider.id to "ERR: ${t.message ?: "Unknown API response"}")
                    }
                )
            } catch (e: java.lang.Exception) {
                testStatusMap.value = testStatusMap.value + (provider.id to "ERR: ${e.message ?: "Decryption or request error"}")
            }
        }
    }

    fun testNewProvider(providerName: String, rawApiKey: String, baseUrl: String, modelName: String) {
        viewModelScope.launch {
            testStatusNew.value = "Testing handshake..."
            try {
                val result = com.example.network.LlmRouter.testConnection(
                    providerName,
                    baseUrl,
                    rawApiKey,
                    modelName
                )
                result.fold(
                    onSuccess = { msg ->
                        testStatusNew.value = "SUCCESS"
                    },
                    onFailure = { t ->
                        testStatusNew.value = "ERR: ${t.message ?: "Unknown API response"}"
                    }
                )
            } catch (e: java.lang.Exception) {
                testStatusNew.value = "ERR: ${e.message ?: "Request error"}"
            }
        }
    }

    fun sendMessage(userText: String, imageBase64: String? = null) {
        val currentWs = activeWorkspace.value ?: return
        val currentConv = activeConversation.value ?: run {
            // Auto create conversation
            val convId = UUID.randomUUID().toString()
            val textTruncated = if (userText.length > 25) userText.take(25) + "..." else userText
            val newConv = ChatConversation(id = convId, workspaceId = currentWs.id, title = textTruncated, activeModel = activeModel.value)
            viewModelScope.launch {
                conversationDao.insertConversation(newConv)
                setActiveConversation(newConv)
                sendMessage(userText, imageBase64)
            }
            return
        }

        if (userText.trim().isEmpty() && imageBase64 == null) return
        if (isStreaming.value) return

        viewModelScope.launch {
            loadingError.value = null

            // 1. Insert User Message
            val userMsg = ChatMessage(
                conversationId = currentConv.id,
                role = "user",
                content = userText,
                imageBase64 = imageBase64
            )
            messageDao.insertMessage(userMsg)

            // Auto-update conversation title if it's default
            if (currentConv.title == "New Chat") {
                val updatedTitle = if (userText.length > 28) userText.take(28) + "..." else userText
                conversationDao.updateConversation(currentConv.copy(title = updatedTitle))
            }

            // 2. Determine Router Credentials
            val selectedModel = activeModel.value
            var activeProviderName = "Gemini"
            var activeBaseUrl = ""
            var activeApiKey = ""

            // Find matching provider configuration if other model (Scoped to Workspace)
            val activeProviders = providerDao.getActiveProvidersForWorkspace(currentWs.id)
            val customProvider = activeProviders.firstOrNull { it.modelName == selectedModel }
            if (customProvider != null) {
                activeProviderName = customProvider.providerName
                activeBaseUrl = customProvider.baseUrl
                activeApiKey = EncryptionHelper.decrypt(customProvider.apiKeyEncrypted)
            }

            // Begin Streaming State UI
            isStreaming.value = true
            currentStreamedContent.value = ""

            // Stream response
            LlmRouter.streamChat(
                providerName = activeProviderName,
                baseUrl = activeBaseUrl,
                apiKey = activeApiKey,
                modelName = selectedModel,
                messages = activeMessages.value,
                systemInstruction = systemPrompt.value + "\nActive workspace is: ${currentWs.name}",
                onChunkReceived = { chunk ->
                    currentStreamedContent.value += chunk
                },
                onComplete = { fullText ->
                    viewModelScope.launch {
                        // Scan logic for custom Web Artifacts (HTML templates, Canvas drawings, or SVGs)
                        val artifactInfo = detectArtifactInText(fullText)
                        
                        val assistantMsg = ChatMessage(
                            conversationId = currentConv.id,
                            role = "assistant",
                            content = fullText,
                            isArtifact = artifactInfo != null,
                            artifactType = artifactInfo?.type,
                            artifactContent = artifactInfo?.content
                        )
                        messageDao.insertMessage(assistantMsg)
                        isStreaming.value = false
                        currentStreamedContent.value = ""
                    }
                },
                onError = { err ->
                    isStreaming.value = false
                    loadingError.value = "Streaming error: ${err.localizedMessage}"
                }
            )
        }
    }

    private fun detectArtifactInText(text: String): Artifact? {
        // Check for HTML blocks
        val htmlRegex = "```html[\\s\\S]*?([\\s\\S]*?)```".toRegex(RegexOption.IGNORE_CASE)
        val htmlMatch = htmlRegex.find(text)
        if (htmlMatch != null) {
            val content = htmlMatch.groupValues[1].trim()
            return Artifact("Generated Web Panel", "html", content)
        }

        // Check for SVG blocks
        val svgRegex = "```xml[\\s\\S]*?([\\s\\S]*?)```".toRegex(RegexOption.IGNORE_CASE)
        val svgMatch = svgRegex.find(text)
        if (svgMatch != null) {
            val content = svgMatch.groupValues[1].trim()
            if (content.contains("<svg", ignoreCase = true)) {
                return Artifact("Generated Vector Illustration", "svg", content)
            }
        }

        // Standalone SVG inline tag (unfenced)
        if (text.contains("<svg", ignoreCase = true) && text.contains("</svg>", ignoreCase = true)) {
            val standaloneSvgRegex = "(<svg[\\s\\S]*?<\\/svg>)".toRegex(RegexOption.IGNORE_CASE)
            val standaloneMatch = standaloneSvgRegex.find(text)
            if (standaloneMatch != null) {
                return Artifact("Standalone Vector", "svg", standaloneMatch.groupValues[1].trim())
            }
        }

        // Check for XML/Generic preview drawings
        val xmlRegex = "```xml[\\s\\S]*?([\\s\\S]*?)```".toRegex(RegexOption.IGNORE_CASE)
        val xmlMatch = xmlRegex.find(text)
        if (xmlMatch != null) {
            val content = xmlMatch.groupValues[1].trim()
            return Artifact("Structured XML Model", "code", content)
        }

        return null
    }

    fun selectArtifact(artifact: Artifact) {
        activeArtifact.value = artifact
        showArtifactPanel.value = true
    }

    // --- TERMINAL COMMAND AND AUTONOMOUS EXECUTION ENGINE (ALPINE SIMULATOR) ---
    fun executeShellCommand(rawCmd: String) {
        val cmdStr = rawCmd.trim()
        if (cmdStr.isEmpty()) return

        val newList = shellLines.value.toMutableList()
        newList.add(ShellLine("localhost:${currentDir.value}$ $cmdStr", "stdin"))

        val parts = cmdStr.split("\\s+".toRegex())
        val baseCommand = parts.firstOrNull()?.lowercase() ?: ""
        val args = parts.drop(1)

        val currentWs = activeWorkspace.value
        if (currentWs == null) {
            newList.add(ShellLine("Command failed: Select a workspace first.", "stderr"))
            shellLines.value = newList
            return
        }

        when (baseCommand) {
            "help" -> {
                newList.add(ShellLine("OpenClaw standalone virtual microvisor kernel. Native support:", "stdout"))
                newList.add(ShellLine(" - list / view workspace nodes: 'ls' / 'cat [file]'", "stdout"))
                newList.add(ShellLine(" - sandbox file editor: 'touch [file]' / 'write [file] [content]'", "stdout"))
                if (activePlugins.value.any { it.name == "Advanced Grep & Search" && it.isEnabled }) {
                    newList.add(ShellLine(" - grep content search: 'grep [query]'", "stdout"))
                }
                newList.add(ShellLine(" - clean console diagnostics: 'clear'", "stdout"))
                if (activePlugins.value.any { it.name == "ADB Device Controller" && it.isEnabled }) {
                    newList.add(ShellLine(" - android shell ADB bridge control: 'adb devices', 'adb connect [ip]', 'adb reboot', 'adb shell getprop', 'adb logcat', 'adb shell input tap [x] [y]'", "stdout"))
                }
            }
            "clear" -> {
                shellLines.value = emptyList()
                return
            }
            "uname" -> {
                newList.add(ShellLine("Linux localhost 5.15.0-88-generic #98-Alpine SMP SMP Wed May 22 16:39:03 UTC 2026 x86_64 Linux", "stdout"))
            }
            "pwd" -> {
                newList.add(ShellLine(currentDir.value, "stdout"))
            }
            "ls" -> {
                val dir = currentDir.value
                val keys = virtualFiles.value.keys.filter { it.startsWith(dir) }
                if (keys.isEmpty()) {
                    newList.add(ShellLine("total 0", "stdout"))
                } else {
                    newList.add(ShellLine("total ${keys.size * 4}", "stdout"))
                    keys.forEach { key ->
                        val fileName = key.replace("$dir/", "")
                        if (fileName.isNotEmpty() && !fileName.contains("/")) {
                            newList.add(ShellLine("-rw-r--r--   1 root     root          255 May 22 16:39 $fileName", "stdout"))
                        }
                    }
                }
            }
            "cat" -> {
                val path = if (args.isNotEmpty()) {
                    val target = args[0]
                    if (target.startsWith("/")) target else "${currentDir.value}/$target"
                } else ""

                if (path.isEmpty()) {
                    newList.add(ShellLine("Usage: cat <filename>", "stderr"))
                } else {
                    val fileContent = virtualFiles.value[path]
                    if (fileContent != null) {
                        fileContent.split("\n").forEach {
                            newList.add(ShellLine(it, "stdout"))
                        }
                    } else {
                        newList.add(ShellLine("cat: $path: No such file or directory", "stderr"))
                    }
                }
            }
            "touch" -> {
                if (args.isEmpty()) {
                    newList.add(ShellLine("touch: missing file operand", "stderr"))
                } else {
                    val target = args[0]
                    val path = if (target.startsWith("/")) target else "${currentDir.value}/$target"
                    createVirtualFile(path, "")
                    newList.add(ShellLine("touch: created / updated timestamp of file: $path", "stdout"))
                }
            }
            "write" -> {
                if (args.size < 2) {
                    newList.add(ShellLine("Usage: write <filename> <content>", "stderr"))
                } else {
                    val target = args[0]
                    val content = args.drop(1).joinToString(" ")
                    val path = if (target.startsWith("/")) target else "${currentDir.value}/$target"
                    createVirtualFile(path, content)
                    newList.add(ShellLine("write: updated contents for '$path'", "stdout"))
                }
            }
            "grep" -> {
                if (args.isEmpty()) {
                    newList.add(ShellLine("Usage: grep <query>", "stderr"))
                } else {
                    val query = args.joinToString(" ")
                    newList.add(ShellLine("[Grep Analyzer Crawler running across local files...]", "system"))
                    var matchedCount = 0
                    virtualFiles.value.forEach { (path, content) ->
                        if (content.contains(query, ignoreCase = true) || path.contains(query, ignoreCase = true)) {
                            newList.add(ShellLine("$path: matched keyword '$query'", "stdout"))
                            matchedCount++
                        }
                    }
                    newList.add(ShellLine("Grep complete. Found $matchedCount matches.", "system"))
                }
            }
            "apk" -> {
                if (args.size >= 2 && args[0].lowercase() == "add") {
                    val pkgName = args[1]
                    newList.add(ShellLine("(1/3) Fetching public Alpine indices...", "system"))
                    newList.add(ShellLine("(2/3) Resolving module tree boundaries for '$pkgName'...", "system"))
                    newList.add(ShellLine("(3/3) Installed packages dependency nodes: $pkgName-bin completed successfully.", "stdout"))
                } else {
                    newList.add(ShellLine("alpine-apk: command parameters incorrect. Run 'apk add [package]'.", "stderr"))
                }
            }
            "python3" -> {
                val path = if (args.isNotEmpty()) {
                    val target = args[0]
                    if (target.startsWith("/")) target else "${currentDir.value}/$target"
                } else ""

                if (path.isEmpty()) {
                    newList.add(ShellLine("Python 3.11.4 (main, May 22 2026, 16:39:03) [GCC 12.2.1 20220924] on linux", "stdout"))
                    newList.add(ShellLine("Type 'exit()' or 'help' to terminate interpreter session.", "stdout"))
                    newList.add(ShellLine(">>> Simulated interpreter prompt. Load script files via 'python3 <filename>'", "stdout"))
                } else {
                    val fileContent = virtualFiles.value[path]
                    if (fileContent != null) {
                        newList.add(ShellLine("[Evaluating Python Execution environment autonomous engine...]", "system"))
                        if (fileContent.contains("fib")) {
                            newList.add(ShellLine("[0, 1, 1, 2, 3, 5, 8, 13, 21, 34]", "stdout"))
                        } else {
                            newList.add(ShellLine("Execution success (0 exited). Standard logs:", "system"))
                            newList.add(ShellLine(fileContent, "stdout"))
                        }
                    } else {
                        newList.add(ShellLine("python3: can't open file '$path': [Errno 2] No such file or directory", "stderr"))
                    }
                }
            }
            "node" -> {
                newList.add(ShellLine("Welcome to Node.js v18.16.1 standup console.", "stdout"))
                newList.add(ShellLine("Evaluator terminal autonomous mode registered.", "stdout"))
            }
            "adb" -> {
                if (args.isEmpty()) {
                    newList.add(ShellLine("Android Debug Bridge Emulator - Version 1.0.41", "system"))
                    newList.add(ShellLine("Usage: adb [devices|connect|reboot|logcat|shell]", "stdout"))
                } else {
                    val adbSub = args[0].lowercase()
                    when (adbSub) {
                        "devices" -> {
                            newList.add(ShellLine("List of devices attached [ADB Bridge Host]:", "stdout"))
                            activeDevices.value.forEach { dev ->
                                newList.add(ShellLine("${dev.serial}\t${dev.status} (${dev.name})", "stdout"))
                            }
                        }
                        "connect" -> {
                            if (args.size < 2) {
                                newList.add(ShellLine("Usage: adb connect <ipaddress:port>", "stderr"))
                            } else {
                                val targetIp = args[1]
                                addDevice("Network-Attached Phone", targetIp)
                                newList.add(ShellLine("connected to $targetIp", "stdout"))
                            }
                        }
                        "reboot" -> {
                            newList.add(ShellLine("Sending reboot instruction packets to all workspace nodes...", "system"))
                            activeDevices.value.forEach { dev ->
                                updateDeviceStatus(dev, "REBOOTING")
                            }
                            newList.add(ShellLine("Reboot cycle complete. Connection online.", "stdout"))
                            viewModelScope.launch {
                                kotlinx.coroutines.delay(2000)
                                activeDevices.value.forEach { dev ->
                                    updateDeviceStatus(dev, "ONLINE")
                                }
                            }
                        }
                        "logcat" -> {
                            newList.add(ShellLine("--------- beginning of main logs ---------", "system"))
                            newList.add(ShellLine("05-22 17:00:27.481  2109  2109 I ActivityManager: Start proc com.example.openclaw for service", "stdout"))
                            newList.add(ShellLine("05-22 17:00:27.502  2109  2133 D OpenGLRenderer: Render Thread initialized with Vulkan limits", "stdout"))
                            newList.add(ShellLine("05-22 17:01:03.990  2109  2109 I ClawControl: Input injection tap detected. X=450 Y=125", "stdout"))
                            newList.add(ShellLine("--------- logcat dump complete ---------", "system"))
                        }
                        "shell" -> {
                            val shellArgs = args.drop(1)
                            if (shellArgs.isEmpty()) {
                                newList.add(ShellLine("Interactive ADB shell started. Type exit to leave.", "stdout"))
                            } else {
                                val subSub = shellArgs[0].lowercase()
                                when (subSub) {
                                    "getprop" -> {
                                        newList.add(ShellLine("[ro.build.version.release]: 15", "stdout"))
                                        newList.add(ShellLine("[ro.build.version.sdk]: 35", "stdout"))
                                        newList.add(ShellLine("[ro.product.model]: Claw-VirtualPhone-ARM64", "stdout"))
                                        newList.add(ShellLine("[ro.hardware]: clawcore", "stdout"))
                                    }
                                    "pm" -> {
                                        if (shellArgs.size >= 2 && shellArgs[1].lowercase() == "list" && shellArgs.getOrNull(2)?.lowercase() == "packages") {
                                            newList.add(ShellLine("package:com.android.settings", "stdout"))
                                            newList.add(ShellLine("package:com.example.openclaw", "stdout"))
                                            newList.add(ShellLine("package:com.google.android.gms", "stdout"))
                                            newList.add(ShellLine("package:com.android.chrome", "stdout"))
                                        } else {
                                            newList.add(ShellLine("pm subcommand not supported in sandbox. Try: pm list packages", "stderr"))
                                        }
                                    }
                                    "input" -> {
                                        if (shellArgs.size >= 4 && shellArgs[1].lowercase() == "tap") {
                                            val tx = shellArgs[2]
                                            val ty = shellArgs[3]
                                            newList.add(ShellLine("adb_input: Injecting touch tap event at ($tx, $ty) successfully.", "stdout"))
                                        } else {
                                            newList.add(ShellLine("ADB Input error. Try: input tap <x> <y>", "stderr"))
                                        }
                                    }
                                    else -> {
                                        newList.add(ShellLine("adb shell: command '$subSub' not simulated inside microOS.", "stderr"))
                                    }
                                }
                            }
                        }
                        else -> {
                            newList.add(ShellLine("adb: subcommand '$adbSub' not found.", "stderr"))
                        }
                    }
                }
            }
            else -> {
                newList.add(ShellLine("ash: command not found: $baseCommand. Run 'help' or check active extensions.", "stderr"))
            }
        }

        shellLines.value = newList
    }
}
