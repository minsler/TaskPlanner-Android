package com.example.taskplanner.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.taskplanner.data.local.TokenManager
import com.example.taskplanner.data.local.UserPreferencesManager
import com.example.taskplanner.data.remote.TasksApi
import com.example.taskplanner.data.repository.TaskRepositoryImpl
import com.example.taskplanner.presentation.auth.AuthViewModel
import com.example.taskplanner.presentation.auth.LoginScreen
import com.example.taskplanner.presentation.tasks.TasksScreen
import com.example.taskplanner.presentation.tasks.TasksViewModel

@Composable
fun AppNavigation(
    preferencesManager: UserPreferencesManager,
    isDarkThemeEnabled: Boolean
) {
    val navController = rememberNavController()
    val startDestination = if (TokenManager.getToken() != null) "tasks" else "login"

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable("login") {
            val authViewModel: AuthViewModel = viewModel()
            val state = authViewModel.state.collectAsState().value

            LaunchedEffect(state.token) {
                state.token?.let { token ->
                    TokenManager.saveToken(token)
                    navController.navigate("tasks") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            }

            LoginScreen(
                viewModel = authViewModel,
                onLoginSuccess = { token ->
                    TokenManager.saveToken(token)
                }
            )
        }

        composable("tasks") {
            val tasksApi = TasksApi()
            val repository = TaskRepositoryImpl(tasksApi)

            val tasksViewModel: TasksViewModel = viewModel(
                factory = object : ViewModelProvider.Factory {
                    @Suppress("UNCHECKED_CAST")
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        return TasksViewModel(
                            repository = repository,
                            preferencesManager = preferencesManager
                        ) as T
                    }
                }
            )

            TasksScreen(
                viewModel = tasksViewModel,
                isDarkThemeEnabled = isDarkThemeEnabled,
                onToggleTheme = tasksViewModel::setDarkThemeEnabled,
                onLogout = {
                    TokenManager.clearToken()
                    navController.navigate("login") {
                        popUpTo("tasks") { inclusive = true }
                    }
                }
            )
        }
    }
}
