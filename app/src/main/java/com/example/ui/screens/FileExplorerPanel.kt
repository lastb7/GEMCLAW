package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.viewmodel.WorkspaceViewModel

@Composable
fun FileExplorerPanel(
    viewModel: WorkspaceViewModel,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val virtualFiles by viewModel.virtualFiles.collectAsState()
    
    // Convert Map<String, String> to a simulated tree/list
    val fileList = virtualFiles.keys.toList().sorted()

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
                    text = "File Explorer (RootFS)",
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
                        contentDescription = "Close File Explorer",
                        tint = Color(0xFF94A3B8)
                    )
                }
            }

            if (fileList.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No files in workspace directory.", color = Color(0xFF94A3B8), fontSize = 13.sp)
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(12.dp)
                ) {
                    items(fileList) { fileName ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable {
                                    // Trigger open or view content
                                }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info, // Generic file icon fallback
                                contentDescription = "File Type",
                                tint = Color(0xFF38BDF8),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = fileName,
                                color = Color.White,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 13.sp
                            )
                        }
                        HorizontalDivider(color = Color(0xFF334155))
                    }
                }
            }
        }
    }
}
