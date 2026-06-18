package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkspaceDao {
    @Query("SELECT * FROM workspaces ORDER BY createdAt ASC")
    fun getAllWorkspaces(): Flow<List<Workspace>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkspace(workspace: Workspace)

    @Update
    suspend fun updateWorkspace(workspace: Workspace)

    @Query("DELETE FROM workspaces WHERE id = :id")
    suspend fun deleteWorkspaceById(id: String)
}

@Dao
interface ProviderDao {
    @Query("SELECT * FROM user_providers WHERE workspaceId = :workspaceId ORDER BY id ASC")
    fun getProvidersForWorkspace(workspaceId: String): Flow<List<ProviderConfig>>

    @Query("SELECT * FROM user_providers WHERE workspaceId = :workspaceId AND isActive = 1")
    suspend fun getActiveProvidersForWorkspace(workspaceId: String): List<ProviderConfig>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProvider(provider: ProviderConfig)

    @Update
    suspend fun updateProvider(provider: ProviderConfig)

    @Query("DELETE FROM user_providers WHERE id = :id")
    suspend fun deleteProviderById(id: Int)
}

@Dao
interface ConversationDao {
    @Query("SELECT * FROM conversations WHERE workspaceId = :workspaceId ORDER BY createdAt DESC")
    fun getConversationsForWorkspace(workspaceId: String): Flow<List<ChatConversation>>

    @Query("SELECT * FROM conversations WHERE id = :id LIMIT 1")
    suspend fun getConversationById(id: String): ChatConversation?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConversation(conversation: ChatConversation)

    @Update
    suspend fun updateConversation(conversation: ChatConversation)

    @Query("DELETE FROM conversations WHERE id = :id")
    suspend fun deleteConversationById(id: String)
}

@Dao
interface MessageDao {
    @Query("SELECT * FROM chat_messages WHERE conversationId = :convId ORDER BY timestamp ASC")
    fun getMessagesForConversation(convId: String): Flow<List<ChatMessage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessage)

    @Query("DELETE FROM chat_messages WHERE conversationId = :convId")
    suspend fun deleteMessagesForConversation(convId: String)
}

@Dao
interface AndroidDeviceDao {
    @Query("SELECT * FROM android_devices WHERE workspaceId = :workspaceId ORDER BY serial ASC")
    fun getDevicesForWorkspace(workspaceId: String): Flow<List<AndroidDevice>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDevice(device: AndroidDevice)

    @Update
    suspend fun updateDevice(device: AndroidDevice)

    @Query("DELETE FROM android_devices WHERE id = :deviceId")
    suspend fun deleteDeviceById(deviceId: String)
}

@Dao
interface VirtualFileDao {
    @Query("SELECT * FROM virtual_files WHERE workspaceId = :workspaceId ORDER BY filePath ASC")
    fun getFilesForWorkspace(workspaceId: String): Flow<List<VirtualFile>>

    @Query("SELECT * FROM virtual_files WHERE workspaceId = :workspaceId AND filePath = :path LIMIT 1")
    suspend fun getFileByPath(workspaceId: String, path: String): VirtualFile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFile(file: VirtualFile)

    @Query("DELETE FROM virtual_files WHERE id = :fileId")
    suspend fun deleteFileById(fileId: String)

    @Query("DELETE FROM virtual_files WHERE workspaceId = :workspaceId AND filePath = :filePath")
    suspend fun deleteFileByPath(workspaceId: String, filePath: String)
}

@Dao
interface ClawPluginDao {
    @Query("SELECT * FROM claw_plugins WHERE workspaceId = :workspaceId ORDER BY name ASC")
    fun getPluginsForWorkspace(workspaceId: String): Flow<List<ClawPlugin>>

    @Query("SELECT * FROM claw_plugins WHERE workspaceId = :workspaceId AND isEnabled = 1")
    suspend fun getActivePluginsForWorkspace(workspaceId: String): List<ClawPlugin>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlugin(plugin: ClawPlugin)

    @Update
    suspend fun updatePlugin(plugin: ClawPlugin)

    @Query("DELETE FROM claw_plugins WHERE id = :id")
    suspend fun deletePluginById(id: String)
}

