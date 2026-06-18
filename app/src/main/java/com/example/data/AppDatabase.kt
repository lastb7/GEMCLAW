package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [Workspace::class, ProviderConfig::class, ChatConversation::class, ChatMessage::class, AndroidDevice::class, VirtualFile::class, ClawPlugin::class],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun workspaceDao(): WorkspaceDao
    abstract fun providerDao(): ProviderDao
    abstract fun conversationDao(): ConversationDao
    abstract fun messageDao(): MessageDao
    abstract fun deviceDao(): AndroidDeviceDao
    abstract fun virtualFileDao(): VirtualFileDao
    abstract fun clawPluginDao(): ClawPluginDao


    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "openclaw_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
