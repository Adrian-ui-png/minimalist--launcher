package com.example.productive_launcher

import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import android.view.WindowManager
import android.os.Build
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.productive_launcher.data.local.SettingsDataStore
import com.example.productive_launcher.data.model.AppInfo
import com.example.productive_launcher.data.repository.SettingsRepository
import com.example.productive_launcher.launcher.LauncherViewModel
import com.example.productive_launcher.mindful.DelayViewModel
import com.example.productive_launcher.ui.components.WallpaperBackground
import com.example.productive_launcher.ui.delay.DelayScreen
import com.example.productive_launcher.ui.delay.DelaySettingsScreen
import com.example.productive_launcher.ui.home.AppPagerScreen
import com.example.productive_launcher.ui.settings.SettingsScreen
import com.example.productive_launcher.ui.settings.SettingsViewModel
import com.example.productive_launcher.ui.theme.ProductivelauncherTheme

class MainActivity : ComponentActivity() {
    private var launcherViewModel: LauncherViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        window.setBackgroundDrawable(ColorDrawable(0))
        Log.d("Wallpaper", "MainActivity: window background set to transparent")

        // Clear custom wallpaper cache to force the new default wallpaper
        val wallpaperFile = java.io.File(filesDir, "current_wallpaper.png")
        if (wallpaperFile.exists()) {
            wallpaperFile.delete()
        }

        setContent {
            val settingsRepository = remember {
                SettingsRepository(SettingsDataStore(this@MainActivity))
            }
            val themeMode by settingsRepository.themeModeFlow.collectAsState(
                initial = com.example.productive_launcher.data.model.ThemeMode.SYSTEM
            )
            val launcherViewModel: LauncherViewModel = viewModel()
            this@MainActivity.launcherViewModel = launcherViewModel
            val isFocusActiveState by launcherViewModel.isFocusActive.collectAsState()

            Box(modifier = Modifier.fillMaxSize()) {
                WallpaperBackground(
                    isFocusActive = isFocusActiveState,
                    viewModel = launcherViewModel,
                    modifier = Modifier.fillMaxSize()
                )

                ProductivelauncherTheme(themeMode = themeMode) {
                    Log.d("Wallpaper", "THEME CHECK: ProductivelauncherTheme composed — MaterialTheme does NOT paint background")
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = Color.Transparent
                    ) {
                        Log.d("Wallpaper", "THEME CHECK: Surface color=Color.Transparent — background is transparent")
                        val navController = rememberNavController()

                        LaunchedEffect(isFocusActiveState) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                if (isFocusActiveState) {
                                    window.addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND)
                                    window.attributes = window.attributes.apply {
                                        blurBehindRadius = 60
                                    }
                                } else {
                                    window.clearFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND)
                                    window.attributes = window.attributes.apply {
                                        blurBehindRadius = 0
                                    }
                                }
                            }
                        }

                        NavHost(
                            navController = navController,
                            startDestination = "home"
                        ) {
                            composable("home") {
                                val onAppClick: (AppInfo) -> Unit = { appInfo ->
                                    val isProtected =
                                        launcherViewModel.protectedApps.value.contains(appInfo.packageName)
                                    if (isProtected) {
                                        navController.navigate("delay/${appInfo.packageName}")
                                    } else {
                                        launcherViewModel.recordAppLaunch(appInfo.packageName)
                                        this@MainActivity.launchApp(appInfo)
                                    }
                                }

                                AppPagerScreen(
                                    viewModel = launcherViewModel,
                                    onAppClick = onAppClick,
                                    onNavigateToSettings = {
                                        navController.navigate("settings")
                                    }
                                )
                            }

                            composable(
                                route = "delay/{packageName}",
                                arguments = listOf(
                                    navArgument("packageName") { type = NavType.StringType }
                                )
                            ) { backStackEntry ->
                                val packageName =
                                    backStackEntry.arguments?.getString("packageName") ?: return@composable
                                val context = LocalContext.current
                                val delayViewModel: DelayViewModel = viewModel(
                                    factory = DelayViewModel.factory(context.applicationContext as android.app.Application, packageName)
                                )
                                DelayScreen(
                                    viewModel = delayViewModel,
                                    onGoBack = { navController.popBackStack() }
                                )
                            }

                            composable("settings") {
                                val settingsViewModel: SettingsViewModel = viewModel()
                                SettingsScreen(
                                    viewModel = settingsViewModel,
                                    onNavigateBack = { navController.popBackStack() },
                                    onNavigateToProtectedApps = {
                                        navController.navigate("settings/protected-apps")
                                    }
                                )
                            }

                            composable("settings/protected-apps") {
                                DelaySettingsScreen(
                                    onNavigateBack = { navController.popBackStack() }
                                )
                            }
                    }
                }

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
            }
        }
    }
}
