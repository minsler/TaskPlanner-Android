package com.example.taskplanner

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.taskplanner.data.local.TokenManager
import com.example.taskplanner.data.local.UserPreferencesManager
import com.example.taskplanner.presentation.navigation.AppNavigation
import com.example.taskplanner.ui.theme.TaskPlannerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        TokenManager.init(applicationContext)
        val preferencesManager = UserPreferencesManager(applicationContext)

        setContent {
            val isDarkThemeEnabled by preferencesManager.isDarkThemeEnabled.collectAsState(initial = false)

            TaskPlannerTheme(darkTheme = isDarkThemeEnabled) {
                AppNavigation(
                    preferencesManager = preferencesManager,
                    isDarkThemeEnabled = isDarkThemeEnabled
                )
            }
        }
    }
}
