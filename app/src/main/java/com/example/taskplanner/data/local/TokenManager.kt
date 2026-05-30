package com.example.taskplanner.data.local

import android.content.Context
import android.content.SharedPreferences

// Объект-одиночка для доступа к токену из любой точки приложения
object TokenManager {
    private const val PREFS_NAME = "task_planner_prefs"
    private const val TOKEN_KEY = "jwt_token"

    private var prefs: SharedPreferences? = null

    // Инициализация при старте приложения
    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    // Сохранить токен
    fun saveToken(token: String) {
        prefs?.edit()?.putString(TOKEN_KEY, token)?.apply()
    }

    // Получить токен
    fun getToken(): String? {
        return prefs?.getString(TOKEN_KEY, null)
    }

    // Удалить токен (для кнопки "Выйти")
    fun clearToken() {
        prefs?.edit()?.remove(TOKEN_KEY)?.apply()
    }
}