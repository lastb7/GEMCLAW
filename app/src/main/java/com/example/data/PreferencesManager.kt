package com.example.data

import android.content.Context
import android.content.SharedPreferences

class PreferencesManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("openclaw_prefs", Context.MODE_PRIVATE)

    var isLoggedIn: Boolean
        get() = prefs.getBoolean("is_logged_in", false)
        set(value) = prefs.edit().putBoolean("is_logged_in", value).apply()

    var userEmail: String?
        get() = prefs.getString("user_email", "s.dantabawa@gmail.com") // Pre-populate with user email from session!
        set(value) = prefs.edit().putString("user_email", value).apply()

    var userName: String?
        get() = prefs.getString("user_name", "S. Dantabawa")
        set(value) = prefs.edit().putString("user_name", value).apply()

    var userPhotoUrl: String?
        get() = prefs.getString("user_photo", "https://api.dicebear.com/7.x/adventurer/svg?seed=OpenClaw")
        set(value) = prefs.edit().putString("user_photo", value).apply()

    var currentThemeIsDark: Boolean
        get() = prefs.getBoolean("theme_is_dark", true) // Default dark theme for OpenClaw's sleek dark vibe!
        set(value) = prefs.edit().putBoolean("theme_is_dark", value).apply()
}
