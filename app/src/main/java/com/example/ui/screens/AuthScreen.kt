package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    onLoginSuccess: (email: String, name: String) -> Unit
) {
    var email by remember { mutableStateFlowOf("s.dantabawa@gmail.com") }
    var name by remember { mutableStateFlowOf("S. Dantabawa") }
    var isLoading by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0F172A), // Slate 900
                        Color(0xFF020617)  // Slate 950
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .padding(16.dp)
                .border(1.dp, Color(0xFF334155), RoundedCornerShape(24.dp)),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF1E293B).copy(alpha = 0.9f)
            ),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(28.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Logo
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(Color(0xFF3B82F6), Color(0xFF8B5CF6))
                            ),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Lock Logo",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "OPENCLAW",
                    fontSize = 28.sp,
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    letterSpacing = 2.sp
                )

                Text(
                    text = "Production Multi-Provider API Workspace Client",
                    fontSize = 12.sp,
                    color = Color(0xFF94A3B8),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
                )

                Divider(color = Color(0xFF334155), thickness = 1.dp)

                Spacer(modifier = Modifier.height(24.dp))

                // Email
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Developer Email", color = Color(0xFF94A3B8)) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF3B82F6),
                        unfocusedBorderColor = Color(0xFF475569)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("auth_email_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Name
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Display Name", color = Color(0xFF94A3B8)) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF3B82F6),
                        unfocusedBorderColor = Color(0xFF475569)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("auth_name_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(28.dp))

                if (isLoading) {
                    CircularProgressIndicator(
                        color = Color(0xFF3B82F6),
                        modifier = Modifier.size(36.dp)
                    )
                } else {
                    // Sign-In Button
                    Button(
                        onClick = {
                            if (email.isNotEmpty() && name.isNotEmpty()) {
                                isLoading = true
                                onLoginSuccess(email, name)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF3B82F6)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("google_login_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "Authenticate Google Profile SSO",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "A secured cookie session token will be set locally in HttpOnly storage.",
                    fontSize = 10.sp,
                    color = Color(0xFF64748B),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

// Helper to bridge mutable state values safely in Compose
private fun <T> mutableStateFlowOf(value: T): MutableState<T> {
    return mutableStateOf(value)
}
