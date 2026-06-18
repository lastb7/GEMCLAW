package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ProviderConfig

data class PresetProvider(
    val name: String,
    val defaultUrl: String,
    val defaultModels: List<String>
)

val presetProviders = listOf(
    PresetProvider("Gemini API", "https://generativelanguage.googleapis.com", listOf("gemini-1.5-flash", "gemini-1.5-pro", "gemini-2.5-flash", "gemini-2.5-pro")),
    PresetProvider("OpenAI", "https://api.openai.com/v1/", listOf("gpt-4o", "gpt-4o-mini", "gpt-4-turbo", "o1-mini", "o1-preview")),
    PresetProvider("Claude", "https://api.anthropic.com/v1/", listOf("claude-3-5-sonnet-latest", "claude-3-5-haiku-latest", "claude-3-opus-20240229")),
    PresetProvider("Ollama Local", "http://10.0.2.2:11434/", listOf("llama3", "mistral", "phi3", "gemma2", "qwen2")),
    PresetProvider("Ollama Cloud", "https://api.ollama.com", listOf("llama3", "gemma2")),
    PresetProvider("LocalAI", "http://10.0.2.2:8080/v1/", listOf("llama-3-8b", "phi-2")),
    PresetProvider("Groq", "https://api.groq.com/openai/v1/", listOf("llama-3-3-70b-versatile", "llama3-8b-8192", "mixtral-8x7b-32768", "gemma2-9b-it")),
    PresetProvider("Cohere", "https://api.cohere.com/v1/", listOf("command-r-plus", "command-r", "command")),
    PresetProvider("Tavily", "https://api.tavily.com/", listOf("tavily-search", "tavily-extract")),
    PresetProvider("OpenRouter", "https://openrouter.ai/api/v1/", listOf("meta-llama/llama-3-8b-instruct:free", "google/gemini-2.5-pro", "anthropic/claude-3.5-sonnet")),
    PresetProvider("GitHub", "https://models.inference.ai.azure.com", listOf("gpt-4o", "gpt-4o-mini", "meta-llama-3-70b-instruct", "cohere-command-r-plus"))
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProviderSettingsScreen(
    providers: List<ProviderConfig>,
    testStatusMap: Map<Int, String>,
    testStatusNew: String?,
    themeIsDark: Boolean,
    onSetThemeIsDark: (Boolean) -> Unit,
    onAddProvider: (providerName: String, apiKey: String, baseUrl: String, modelName: String) -> Unit,
    onToggleProvider: (ProviderConfig) -> Unit,
    onDeleteProvider: (Int) -> Unit,
    onTestSavedProvider: (ProviderConfig) -> Unit,
    onTestNewProvider: (providerName: String, apiKey: String, baseUrl: String, modelName: String) -> Unit,
    onDismiss: () -> Unit
) {
    // Local Inputs
    var selectedPresetIndex by remember { mutableStateOf(0) }
    var pName by remember { mutableStateOf(presetProviders[0].name) }
    var bUrl by remember { mutableStateOf(presetProviders[0].defaultUrl) }
    var apiKey by remember { mutableStateOf("") }
    var modelAlias by remember { mutableStateOf(presetProviders[0].defaultModels[0]) }

    var pDropdownExpanded by remember { mutableStateOf(false) }
    var mDropdownExpanded by remember { mutableStateOf(false) }
    var showAddForm by remember { mutableStateOf(false) }

    // Multi Theme Palette mappings
    val bgColor = if (themeIsDark) Color(0xFF0F172A) else Color(0xFFF8FAFC)
    val textColor = if (themeIsDark) Color.White else Color(0xFF0F172A)
    val cardColor = if (themeIsDark) Color(0xFF1E293B) else Color.White
    val cardBorderColor = if (themeIsDark) Color(0xFF334155) else Color(0xFFE2E8F0)
    val helpCardColor = if (themeIsDark) Color(0xFF1E293B) else Color(0xFFEFF6FF)
    val textMuted = if (themeIsDark) Color(0xFF94A3B8) else Color(0xFF475569)
    val fieldBgColor = if (themeIsDark) Color(0xFF0F172A) else Color(0xFFF1F5F9)

    Surface(
        color = bgColor,
        modifier = Modifier
            .fillMaxSize()
            .testTag("provider_settings_surface")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Header row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = "Cloud Pluggable API router logo",
                    tint = Color(0xFF3B82F6),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Provider Console",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )
                    Text(
                        text = "Secure LLM Gateway Admin",
                        fontSize = 10.sp,
                        color = textMuted
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close settings pane",
                        tint = textColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // THEME SWITCHER TAB/CARD
            Card(
                colors = CardDefaults.cardColors(containerColor = cardColor),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, cardBorderColor, RoundedCornerShape(12.dp))
            ) {
                Row(
                    modifier = Modifier
                        .padding(12.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Theme style indicator",
                            tint = if (themeIsDark) Color(0xFFA78BFA) else Color(0xFFEAB308),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Interface Theme",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = textColor
                            )
                            Text(
                                text = if (themeIsDark) "Deep Galactic Theme active" else "Clean Daylight Theme active",
                                fontSize = 10.sp,
                                color = textMuted
                            )
                        }
                    }

                    // Toggler Switch
                    Switch(
                        checked = themeIsDark,
                        onCheckedChange = onSetThemeIsDark,
                        colors = SwitchDefaults.colors(
                            checkedTrackColor = Color(0xFF3B82F6),
                            checkedThumbColor = Color.White,
                            uncheckedTrackColor = Color(0xFFCBD5E1),
                            uncheckedThumbColor = Color.White
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Encryption diagnostic info
            Card(
                colors = CardDefaults.cardColors(containerColor = helpCardColor),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, cardBorderColor, RoundedCornerShape(12.dp))
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "AES-256 Info info icon",
                        tint = Color(0xFF3B82F6),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Sandbox End-to-End Encryption",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = textColor
                        )
                        Text(
                            text = "Credential storage keys are fully encrypted via client-side AES-256 hardware keys. Verified end-points can serve directly into any chat thread session.",
                            fontSize = 9.sp,
                            color = textMuted,
                            lineHeight = 13.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action list buttons
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "CUSTOM INSTANCES",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = textMuted,
                    letterSpacing = 1.sp
                )
                if (!showAddForm) {
                    Button(
                        onClick = { showAddForm = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6)),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier
                            .height(32.dp)
                            .testTag("add_provider_form_button")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add Icon", tint = Color.White, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Provider", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Form container
            if (showAddForm) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = cardColor),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, cardBorderColor, RoundedCornerShape(12.dp))
                        .padding(8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(8.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Text(
                            text = "New Endpoint Setup",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        // STEP 1: PRESET QUICK CONFIG Dropdown
                        Text("Select Provider Preset Template:", fontSize = 10.sp, color = textMuted, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        Box(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(fieldBgColor, RoundedCornerShape(8.dp))
                                    .border(1.dp, cardBorderColor, RoundedCornerShape(8.dp))
                                    .clickable { pDropdownExpanded = true }
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Share,
                                        contentDescription = "Cloud Icon",
                                        tint = Color(0xFF3B82F6),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(text = pName, color = textColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                                Icon(Icons.Default.ArrowDropDown, contentDescription = "Dropdown indicator", tint = textColor)
                            }

                            DropdownMenu(
                                expanded = pDropdownExpanded,
                                onDismissRequest = { pDropdownExpanded = false },
                                modifier = Modifier
                                    .background(cardColor)
                                    .border(1.dp, cardBorderColor)
                            ) {
                                presetProviders.forEachIndexed { idx, preset ->
                                    DropdownMenuItem(
                                        text = { Text(preset.name, color = textColor, fontSize = 12.sp) },
                                        onClick = {
                                            selectedPresetIndex = idx
                                            pName = preset.name
                                            bUrl = preset.defaultUrl
                                            modelAlias = preset.defaultModels.first()
                                            pDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Base URL Endpoint Textfield
                        OutlinedTextField(
                            value = bUrl,
                            onValueChange = { bUrl = it },
                            label = { Text("API Endpoint Base URL", fontSize = 10.sp) },
                            textStyle = LocalTextStyle.current.copy(fontSize = 11.sp, color = textColor),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = textColor,
                                unfocusedTextColor = textColor,
                                focusedBorderColor = Color(0xFF3B82F6),
                                unfocusedBorderColor = cardBorderColor
                            ),
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("provider_url_input"),
                            shape = RoundedCornerShape(8.dp)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // API Authentication Key Secret
                        OutlinedTextField(
                            value = apiKey,
                            onValueChange = { apiKey = it },
                            label = { Text("Authentication Secret Key / Token (E.g. sk-...)", fontSize = 10.sp) },
                            textStyle = LocalTextStyle.current.copy(fontSize = 11.sp, color = textColor),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = textColor,
                                unfocusedTextColor = textColor,
                                focusedBorderColor = Color(0xFF3B82F6),
                                unfocusedBorderColor = cardBorderColor
                            ),
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("provider_key_input"),
                            shape = RoundedCornerShape(8.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Model selection Selector or Custom override
                        Text("Active ID Model Selector:", fontSize = 10.sp, color = textMuted, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(4.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(modifier = Modifier.weight(1f)) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(fieldBgColor, RoundedCornerShape(8.dp))
                                        .border(1.dp, cardBorderColor, RoundedCornerShape(8.dp))
                                        .clickable { mDropdownExpanded = true }
                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = modelAlias, color = textColor, fontSize = 11.sp)
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = "Dropdown indicator", tint = textColor, modifier = Modifier.size(16.dp))
                                }

                                DropdownMenu(
                                    expanded = mDropdownExpanded,
                                    onDismissRequest = { mDropdownExpanded = false },
                                    modifier = Modifier
                                        .background(cardColor)
                                        .border(1.dp, cardBorderColor)
                                ) {
                                    presetProviders[selectedPresetIndex].defaultModels.forEach { presetModel ->
                                        DropdownMenuItem(
                                            text = { Text(presetModel, color = textColor, fontSize = 11.sp) },
                                            onClick = {
                                                modelAlias = presetModel
                                                mDropdownExpanded = false
                                            }
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            // Or input manually if custom alias
                            OutlinedTextField(
                                value = modelAlias,
                                onValueChange = { modelAlias = it },
                                label = { Text("Custom Model Override", fontSize = 9.sp) },
                                textStyle = LocalTextStyle.current.copy(fontSize = 11.sp, color = textColor),
                                singleLine = true,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(52.dp),
                                shape = RoundedCornerShape(8.dp)
                            )
                        }

                        // NEW TESTING STATE LOG INSIDE NEW CARD
                        if (testStatusNew != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Surface(
                                color = when {
                                    testStatusNew == "Testing handshake..." -> Color(0xFF1E293B)
                                    testStatusNew == "SUCCESS" -> Color(0xFF064E3B)
                                    else -> Color(0xFF450A0A)
                                },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (testStatusNew == "Testing handshake...") {
                                        CircularProgressIndicator(color = Color(0xFF3B82F6), modifier = Modifier.size(14.dp), strokeWidth = 1.5.dp)
                                    } else {
                                        Icon(
                                            imageVector = if (testStatusNew == "SUCCESS") Icons.Default.CheckCircle else Icons.Default.Warning,
                                            contentDescription = "Test flag design status",
                                            tint = if (testStatusNew == "SUCCESS") Color(0xFF34D399) else Color(0xFFF87171),
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = when {
                                            testStatusNew == "Testing handshake..." -> "Connecting to $pName route..."
                                            testStatusNew == "SUCCESS" -> "Handshake SECURE: API Key accepted and active!"
                                            else -> "Authentication Failed: ${testStatusNew.replace("ERR: ", "")}"
                                        },
                                        color = if (testStatusNew == "SUCCESS") Color(0xFF34D399) else if (testStatusNew == "Testing handshake...") Color.White else Color(0xFFFBBF24),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        lineHeight = 13.sp,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // Test Key Button
                            Button(
                                onClick = {
                                    if (apiKey.isNotEmpty()) {
                                        onTestNewProvider(pName, apiKey, bUrl, modelAlias)
                                    }
                                },
                                enabled = apiKey.isNotEmpty(),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF475569)),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.height(32.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                            ) {
                                Icon(Icons.Default.Check, contentDescription = "Test Key Speed Connection", tint = Color.White, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Test Connection", fontSize = 11.sp, color = Color.White)
                            }

                            Row {
                                TextButton(
                                    onClick = { showAddForm = false },
                                    modifier = Modifier.height(32.dp)
                                ) {
                                    Text("Discard", color = textMuted, fontSize = 11.sp)
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Button(
                                    onClick = {
                                        if (pName.isNotEmpty() && apiKey.isNotEmpty() && bUrl.isNotEmpty() && modelAlias.isNotEmpty()) {
                                            onAddProvider(pName, apiKey, bUrl, modelAlias)
                                            apiKey = ""
                                            showAddForm = false
                                        }
                                    },
                                    enabled = apiKey.isNotEmpty() && bUrl.isNotEmpty() && modelAlias.isNotEmpty(),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier
                                        .height(32.dp)
                                        .testTag("submit_provider_button"),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                                ) {
                                    Text("Save Provider", color = Color.White, fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Plotted List of user configs
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                if (providers.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No custom endpoints registered.\nDefaulting model execution path to server Google AI Studio APIs.",
                                color = textMuted,
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center,
                                lineHeight = 18.sp
                            )
                        }
                    }
                } else {
                    items(providers) { provider ->
                        val savedStatus = testStatusMap[provider.id]

                        Card(
                            colors = CardDefaults.cardColors(containerColor = cardColor),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, cardBorderColor, RoundedCornerShape(12.dp))
                                .padding(vertical = 5.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.Share,
                                                contentDescription = "Cloud logo",
                                                tint = Color(0xFF10B981),
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "${provider.providerName} (${provider.modelName})",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                color = textColor
                                            )
                                        }
                                        Text(
                                            text = provider.baseUrl,
                                            fontSize = 10.sp,
                                            color = textMuted,
                                            maxLines = 1,
                                            modifier = Modifier.padding(top = 2.dp)
                                        )
                                        Text(
                                            text = "Keys securely encrypted in sandbox sqlite",
                                            fontSize = 9.sp,
                                            color = Color(0xFF10B981),
                                            modifier = Modifier.padding(top = 2.dp)
                                        )
                                    }

                                    // Status toggle switch
                                    Switch(
                                        checked = provider.isActive,
                                        onCheckedChange = { onToggleProvider(provider) },
                                        colors = SwitchDefaults.colors(
                                            checkedTrackColor = Color(0xFF10B981),
                                            checkedThumbColor = Color.White
                                        )
                                    )

                                    Spacer(modifier = Modifier.width(8.dp))

                                    // Delete instance button
                                    IconButton(
                                        onClick = { onDeleteProvider(provider.id) },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Delete instance configuration",
                                            tint = Color(0xFFEF4444),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }

                                // TESTING STATES INTERFACE FOR SAVED CORES
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    // Inline status diagnostic view
                                    Box(modifier = Modifier.weight(1f)) {
                                        if (savedStatus == null) {
                                            Text(
                                                text = "Test coverage status: Untested",
                                                fontSize = 9.sp,
                                                color = textMuted
                                            )
                                        } else {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                if (savedStatus == "Testing handshake...") {
                                                    CircularProgressIndicator(color = Color(0xFF3B82F6), modifier = Modifier.size(12.dp), strokeWidth = 1.dp)
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text("Routing API verification test...", fontSize = 9.sp, color = textMuted)
                                                } else {
                                                    Icon(
                                                        imageVector = if (savedStatus == "SUCCESS") Icons.Default.CheckCircle else Icons.Default.Warning,
                                                        contentDescription = "Status logo label",
                                                        tint = if (savedStatus == "SUCCESS") Color(0xFF10B981) else Color(0xFFEF4444),
                                                        modifier = Modifier.size(12.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text(
                                                        text = if (savedStatus == "SUCCESS") "Handshake Secured" else savedStatus.replace("ERR: ", "Error: "),
                                                        fontSize = 9.sp,
                                                        color = if (savedStatus == "SUCCESS") Color(0xFF10B981) else Color(0xFFEF4444),
                                                        fontWeight = FontWeight.SemiBold,
                                                        maxLines = 2
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    // Interactive test connection trigger button
                                    Button(
                                        onClick = { onTestSavedProvider(provider) },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF475569)),
                                        shape = RoundedCornerShape(6.dp),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                        modifier = Modifier.height(24.dp)
                                    ) {
                                        Icon(Icons.Default.PlayArrow, contentDescription = "Test Route Connection icon", tint = Color.White, modifier = Modifier.size(10.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Test Key", fontSize = 9.sp, color = Color.White)
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
