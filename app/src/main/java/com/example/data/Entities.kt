package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "workspaces")
data class Workspace(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "user_providers")
data class ProviderConfig(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val workspaceId: String, // Scoped to workspace for isolation
    val providerName: String,
    val apiKeyEncrypted: String,
    val baseUrl: String,
    val isActive: Boolean = true,
    val modelName: String
)

@Entity(tableName = "conversations")
data class ChatConversation(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val workspaceId: String, // Scoped to workspace
    val title: String,
    val createdAt: Long = System.currentTimeMillis(),
    val activeModel: String = "gemini-3.5-flash"
)

@Entity(tableName = "chat_messages")
data class ChatMessage(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val conversationId: String,
    val role: String, // "user", "assistant"
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isArtifact: Boolean = false,
    val artifactType: String? = null, // "html", "svg", "code", "terminal"
    val artifactContent: String? = null,
    val thinkingText: String? = null,
    val imageBase64: String? = null
)

@Entity(tableName = "android_devices")
data class AndroidDevice(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val workspaceId: String,
    val name: String,
    val serial: String,
    val status: String = "ONLINE", // "ONLINE", "OFFLINE", "REBOOTING"
    val battery: Int = 85,
    val network: String = "WiFi - Connected",
    val modelSignature: String = "Claw-VirtualPhone-ARM64"
)

@Entity(tableName = "virtual_files")
data class VirtualFile(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val workspaceId: String,
    val filePath: String,
    val content: String,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "claw_plugins")
data class ClawPlugin(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val workspaceId: String,
    val name: String,
    val description: String,
    val category: String, // "system", "terminals", "devices", "utilities", "custom"
    val isEnabled: Boolean = true,
    val icon: String = "Build", // Material icons representation string
    val systemPromptPrefix: String = ""
)

