package com.example.productive_launcher

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.productive_launcher.data.model.AppInfo
import com.example.productive_launcher.launcher.LauncherViewModel
import com.example.productive_launcher.ui.home.HomeScreen
import com.example.productive_launcher.ui.theme.ProductivelauncherTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ProductivelauncherTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val viewModel: LauncherViewModel = viewModel()
                    HomeScreen(
                        viewModel = viewModel,
                        onAppClick = { appInfo -> launchApp(appInfo) }
                    )
                }
            }
        }
    }

    private fun launchApp(appInfo: AppInfo) {
        val intent: Intent? = appInfo.launchIntent
        if (intent != null) {
            try {
                startActivity(intent)
            } catch (e: Exception) {
                // App may have been uninstalled since listing
            }
        }
    }
}
