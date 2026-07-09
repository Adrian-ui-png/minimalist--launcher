package com.example.productive_launcher.data.repository

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import com.example.productive_launcher.data.model.AppInfo

class AppRepository(private val packageManager: PackageManager) {

    fun getInstalledApps(): List<AppInfo> {
        val intent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        val activities = packageManager.queryIntentActivities(intent, 0)

        return activities
            .map { resolveInfo ->
                val activityInfo = resolveInfo.activityInfo
                AppInfo(
                    packageName = activityInfo.packageName,
                    appName = activityInfo.loadLabel(packageManager).toString(),
                    icon = null,
                    launchIntent = buildLaunchIntent(resolveInfo)
                )
            }
            .filter { it.launchIntent != null }
            .sortedBy { it.appName.lowercase() }
    }

    fun getIcon(packageName: String): Drawable? {
        return try {
            packageManager.getApplicationIcon(packageName)
        } catch (_: Exception) {
            null
        }
    }

    private fun buildLaunchIntent(resolveInfo: android.content.pm.ResolveInfo): Intent? {
        val activityInfo = resolveInfo.activityInfo
        return try {
            Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
                setClassName(activityInfo.packageName, activityInfo.name)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
            }
        } catch (_: Exception) {
            null
        }
    }
}
