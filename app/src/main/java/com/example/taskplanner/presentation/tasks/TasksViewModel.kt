package com.example.taskplanner.presentation.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.taskplanner.data.local.UserPreferencesManager
import com.example.taskplanner.domain.model.Task
import com.example.taskplanner.domain.repository.TaskRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class TasksState(
    val tasks: List<Task> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val searchQuery: String = "",
    val searchHistory: List<String> = emptyList(),
    val searchResults: List<Task>? = null,
    val isSearchLoading: Boolean = false,
    val searchError: String? = null
)

class TasksViewModel(
    private val repository: TaskRepository,
    private val preferencesManager: UserPreferencesManager
) : ViewModel() {

    private val _state = MutableStateFlow(TasksState())
    val state: StateFlow<TasksState> = _state.asStateFlow()

    init {
        observeSearchHistory()
        loadTasks()
    }

    fun loadTasks() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            try {
                val tasks = repository.getTasks()
                _state.value = _state.value.copy(tasks = tasks, isLoading = false)
                refreshActiveSearch(tasks)
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = e.message ?: "Не удалось загрузить задачи"
                )
            }
        }
    }

    fun addTask(title: String, description: String) {
        if (title.isBlank()) return

        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            try {
                repository.createTask(title.trim(), description.trim())
                loadTasks()
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = e.message ?: "Не удалось создать задачу"
                )
            }
        }
    }

    fun toggleTaskStatus(task: Task) {
        viewModelScope.launch {
            try {
                repository.updateTask(task.copy(isCompleted = !task.isCompleted))
                loadTasks()
            } catch (e: Exception) {
                _state.value = _state.value.copy(error = e.message ?: "Не удалось обновить статус")
            }
        }
    }

    fun editTask(task: Task, newTitle: String, newDescription: String) {
        if (newTitle.isBlank()) return

        viewModelScope.launch {
            try {
                repository.updateTask(
                    task.copy(
                        title = newTitle.trim(),
                        description = newDescription.trim()
                    )
                )
                loadTasks()
            } catch (e: Exception) {
                _state.value = _state.value.copy(error = e.message ?: "Не удалось сохранить изменения")
            }
        }
    }

    fun deleteTask(taskId: Int) {
        viewModelScope.launch {
            try {
                repository.deleteTask(taskId)
                loadTasks()
            } catch (e: Exception) {
                _state.value = _state.value.copy(error = e.message ?: "Не удалось удалить задачу")
            }
        }
    }

    fun updateSearchQuery(query: String) {
        val trimmedStart = query.trimStart()
        _state.value = _state.value.copy(searchQuery = trimmedStart)

        if (trimmedStart.isBlank()) {
            _state.value = _state.value.copy(searchResults = null, searchError = null)
        }
    }

    fun performSearch(query: String = _state.value.searchQuery) {
        val normalizedQuery = query.trim()
        if (normalizedQuery.isBlank()) return

        viewModelScope.launch {
            _state.value = _state.value.copy(
                searchQuery = normalizedQuery,
                isSearchLoading = true,
                searchError = null
            )

            try {
                val updatedHistory = buildSearchHistory(normalizedQuery)
                preferencesManager.saveSearchHistory(updatedHistory)

                val results = filterTasks(_state.value.tasks, normalizedQuery)
                _state.value = _state.value.copy(
                    searchHistory = updatedHistory,
                    searchResults = results,
                    isSearchLoading = false
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isSearchLoading = false,
                    searchError = "Не удалось выполнить поиск"
                )
            }
        }
    }

    fun clearSearchHistory() {
        viewModelScope.launch {
            preferencesManager.saveSearchHistory(emptyList())
            _state.value = _state.value.copy(searchHistory = emptyList())
        }
    }

    fun addSearchHistoryItem(query: String) {
        val normalizedQuery = query.trim()
        if (normalizedQuery.isBlank()) return

        viewModelScope.launch {
            val updatedHistory = buildSearchHistory(normalizedQuery)
            preferencesManager.saveSearchHistory(updatedHistory)
            _state.value = _state.value.copy(searchHistory = updatedHistory)
        }
    }

    fun clearSearch() {
        _state.value = _state.value.copy(
            searchQuery = "",
            searchResults = null,
            searchError = null,
            isSearchLoading = false
        )
    }

    fun setDarkThemeEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.setDarkThemeEnabled(enabled)
        }
    }

    private fun observeSearchHistory() {
        viewModelScope.launch {
            preferencesManager.searchHistory.collect { history ->
                _state.value = _state.value.copy(searchHistory = history)
            }
        }
    }

    private fun buildSearchHistory(query: String): List<String> {
        return buildList {
            add(query)
            addAll(_state.value.searchHistory.filterNot { it.equals(query, ignoreCase = true) })
        }.take(10)
    }

    private fun refreshActiveSearch(tasks: List<Task>) {
        val query = _state.value.searchQuery.trim()
        if (_state.value.searchResults != null && query.isNotBlank()) {
            _state.value = _state.value.copy(searchResults = filterTasks(tasks, query))
        }
    }

    private fun filterTasks(tasks: List<Task>, query: String): List<Task> {
        return tasks.filter { task ->
            task.title.contains(query, ignoreCase = true) ||
                task.description?.contains(query, ignoreCase = true) == true
        }
    }
}
