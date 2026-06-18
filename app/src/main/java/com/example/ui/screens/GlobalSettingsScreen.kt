package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GlobalSettingsScreen(
    currentUserName: String,
    currentUserEmail: String,
    currentSystemPrompt: String,
    themeIsDark: Boolean,
    onSaveProfile: (String, String) -> Unit,
    onSaveSystemPrompt: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(currentUserName) }
    var email by remember { mutableStateOf(currentUserEmail) }
    var prompt by remember { mutableStateOf(currentSystemPrompt) }

    val bgColor = if (themeIsDark) Color(0xFF0F172A) else Color(0xFFF8FAFC)
    val textColor = if (themeIsDark) Color.White else Color(0xFF0F172A)
    val cardColor = if (themeIsDark) Color(0xFF1E293B) else Color.White
    val cardBorderColor = if (themeIsDark) Color(0xFF334155) else Color(0xFFE2E8F0)

    Surface(
        color = bgColor,
        modifier = Modifier
            .fillMaxSize()
            .testTag("global_settings_surface")
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
                    imageVector = Icons.Default.Person,
                    contentDescription = "Global Profile Configuration",
                    tint = Color(0xFF3B82F6),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Global Identity / Persona",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor
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

            Spacer(modifier = Modifier.height(16.dp))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "User Profile",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = textColor,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                Card(
                    colors = CardDefaults.cardColors(containerColor = cardColor),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, cardBorderColor, RoundedCornerShape(12.dp))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Display Name", fontSize = 11.sp) },
                            textStyle = LocalTextStyle.current.copy(fontSize = 12.sp, color = textColor),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = textColor,
                                unfocusedTextColor = textColor,
                                focusedBorderColor = Color(0xFF3B82F6),
                                unfocusedBorderColor = cardBorderColor
                            ),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("Email Address", fontSize = 11.sp) },
                            textStyle = LocalTextStyle.current.copy(fontSize = 12.sp, color = textColor),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = textColor,
                                unfocusedTextColor = textColor,
                                focusedBorderColor = Color(0xFF3B82F6),
                                unfocusedBorderColor = cardBorderColor
                            ),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = { onSaveProfile(name, email) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text("Update Profile", fontSize = 11.sp, color = Color.White)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "OpenClaw Persona (System Prompt)",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = textColor,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Card(
                    colors = CardDefaults.cardColors(containerColor = cardColor),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, cardBorderColor, RoundedCornerShape(12.dp))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Define the global system personality constraint that OpenClaw will use for all API calls. Note: some capabilities may degrade if core constraints are removed.",
                            fontSize = 11.sp,
                            color = Color(0xFF94A3B8),
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        OutlinedTextField(
                            value = prompt,
                            onValueChange = { prompt = it },
                            label = { Text("System Persona Prompt", fontSize = 11.sp) },
                            textStyle = LocalTextStyle.current.copy(fontSize = 12.sp, color = textColor, lineHeight = 16.sp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = textColor,
                                unfocusedTextColor = textColor,
                                focusedBorderColor = Color(0xFF10B981),
                                unfocusedBorderColor = cardBorderColor
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            shape = RoundedCornerShape(8.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = { onSaveSystemPrompt(prompt) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text("Update System Prompt", fontSize = 11.sp, color = Color.White)
                        }
                    }
                }
            }
        }
    }
}
