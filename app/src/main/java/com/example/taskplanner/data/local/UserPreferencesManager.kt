package com.example.taskplanner.data.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.userPreferencesDataStore by preferencesDataStore(name = "user_preferences")

class UserPreferencesManager(
    private val context: Context
) {
    private val darkThemeKey = booleanPreferencesKey("dark_theme_enabled")
    private val searchHistoryKey = stringSetPreferencesKey("search_history")

    val isDarkThemeEnabled: Flow<Boolean> = context.userPreferencesDataStore.data.map { preferences ->
        preferences[darkThemeKey] ?: false
    }

    val searchHistory: Flow<List<String>> = context.userPreferencesDataStore.data.map { preferences ->
        preferences[searchHistoryKey]
            ?.sortedBy { item -> item.substringBefore("|").toIntOrNull() ?: Int.MAX_VALUE }
            ?.map { item -> item.substringAfter("|") }
            ?: emptyList()
    }

    suspend fun setDarkThemeEnabled(enabled: Boolean) {
        context.userPreferencesDataStore.edit { preferences ->
            preferences[darkThemeKey] = enabled
        }
    }

    suspend fun saveSearchHistory(history: List<String>) {
        context.userPreferencesDataStore.edit { preferences ->
            preferences[searchHistoryKey] = history
                .take(10)
                .mapIndexed { index, query -> "$index|$query" }
                .toSet()
        }
    }
}
