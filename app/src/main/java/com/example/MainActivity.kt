package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.ui.screens.WorkspaceMainLayout
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.WorkspaceViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: WorkspaceViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val isDark by viewModel.currentThemeIsDark.collectAsState()
            
            var showOnboarding by remember { mutableStateOf(getSharedPreferences("OpenClawPrefs", MODE_PRIVATE).getBoolean("showOnboarding", true)) }

            MyApplicationTheme(darkTheme = isDark) {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    contentWindowInsets = androidx.compose.foundation.layout.WindowInsets(0, 0, 0, 0)
                ) { innerPadding ->
                    if (showOnboarding) {
                        com.example.ui.screens.OnboardingScreen(
                            onOnboardingComplete = {
                                getSharedPreferences("OpenClawPrefs", MODE_PRIVATE).edit().putBoolean("showOnboarding", false).apply()
                                showOnboarding = false
                            }
                        )
                    } else {
                        WorkspaceMainLayout(viewModel = viewModel)
                    }
                }
            }
        }
    }
}
