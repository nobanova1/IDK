package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.os.state.OsViewModel
import com.example.os.ui.DesktopScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val osViewModel: OsViewModel = viewModel()
                val uiState by osViewModel.uiState.collectAsState()

                BackHandler(enabled = uiState.isStartMenuOpen || uiState.isQuickSettingsOpen || uiState.isNotificationCenterOpen || uiState.windows.any { !it.isMinimized }) {
                    when {
                        uiState.isStartMenuOpen || uiState.isQuickSettingsOpen || uiState.isNotificationCenterOpen -> {
                            osViewModel.dismissOverlays()
                        }
                        uiState.activeWindowId != null -> {
                            osViewModel.minimizeWindow(uiState.activeWindowId!!)
                        }
                    }
                }

                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .safeDrawingPadding(),
                    color = Color(0xFF0F172A)
                ) {
                    DesktopScreen(viewModel = osViewModel)
                }
            }
        }
    }
}
