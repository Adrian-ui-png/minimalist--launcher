package com.example.productive_launcher.data.repository

import android.content.Intent
import android.content.pm.PackageManager
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
                    icon = activityInfo.loadIcon(packageManager),
                    launchIntent = packageManager.getLaunchIntentForPackage(activityInfo.packageName)
                )
            }
            .filter { it.launchIntent != null }
            .sortedBy { it.appName.lowercase() }
    }
}
