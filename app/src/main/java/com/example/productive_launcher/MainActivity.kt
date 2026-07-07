package com.example.productive_launcher

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
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
import com.example.productive_launcher.ui.delay.DelayScreen
import com.example.productive_launcher.ui.delay.DelaySettingsScreen
import com.example.productive_launcher.ui.home.HomeScreen
import com.example.productive_launcher.ui.settings.SettingsScreen
import com.example.productive_launcher.ui.settings.SettingsViewModel
import com.example.productive_launcher.ui.theme.ProductivelauncherTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val settingsRepository = remember {
                SettingsRepository(SettingsDataStore(this@MainActivity))
            }
            val themeMode by settingsRepository.themeModeFlow.collectAsState(
                initial = com.example.productive_launcher.data.model.ThemeMode.SYSTEM
            )

            ProductivelauncherTheme(themeMode = themeMode) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val navController = rememberNavController()
                    val launcherViewModel: LauncherViewModel = viewModel()

                    NavHost(
                        navController = navController,
                        startDestination = "home"
                    ) {
                        composable("home") {
                            HomeScreen(
                                viewModel = launcherViewModel,
                                onAppClick = { appInfo ->
                                    val isProtected =
                                        launcherViewModel.protectedApps.value.contains(appInfo.packageName)
                                    if (isProtected) {
                                        navController.navigate("delay/${appInfo.packageName}")
                                    } else {
                                        launcherViewModel.recordAppLaunch(appInfo.packageName)
                                        launchApp(appInfo)
                                    }
                                },
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
