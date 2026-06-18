package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.viewmodel.WorkspaceViewModel
import kotlinx.coroutines.launch

@Composable
fun TerminalPanel(
    viewModel: WorkspaceViewModel,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shellLines by viewModel.shellLines.collectAsState()
    var cmdInput by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(shellLines.size) {
        if (shellLines.isNotEmpty()) {
            listState.animateScrollToItem(shellLines.size - 1)
        }
    }

    Surface(
        color = Color(0xFF1E293B),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0F172A), RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Text(
                    text = "Ubuntu 24.04 LTS (proot) - Terminal",
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    modifier = Modifier.weight(1f)
                )
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close Terminal",
                        tint = Color(0xFF94A3B8)
                    )
                }
            }

            // Output Log
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(Color.Black)
                    .padding(8.dp)
            ) {
                items(shellLines) { line ->
                    val color = when(line.type) {
                        "stderr" -> Color(0xFFEF4444) // Red
                        "system" -> Color(0xFF3B82F6) // Blue
                        "stdin" -> Color(0xFF10B981) // Green
                        else -> Color(0xFFD1D5DB) // White
                    }
                    Text(
                        text = line.text,
                        color = color,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(vertical = 1.dp)
                    )
                }
            }

            // Input field
            Surface(
                color = Color.Black,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "$ ",
                        color = Color(0xFF10B981),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    OutlinedTextField(
                        value = cmdInput,
                        onValueChange = { cmdInput = it },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        placeholder = { Text("Enter command...", color = Color(0xFF475569), fontSize = 13.sp) },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(
                            onSend = {
                                if (cmdInput.isNotBlank()) {
                                    viewModel.executeShellCommand(cmdInput)
                                    cmdInput = ""
                                }
                            }
                        ),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}
