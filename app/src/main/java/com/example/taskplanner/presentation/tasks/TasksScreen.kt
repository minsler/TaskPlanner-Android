package com.example.taskplanner.presentation.tasks

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.example.taskplanner.domain.model.Task

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksScreen(
    viewModel: TasksViewModel,
    isDarkThemeEnabled: Boolean,
    onToggleTheme: (Boolean) -> Unit,
    onLogout: () -> Unit
) {
    val state by viewModel.state.collectAsState()

    var showAddTaskDialog by remember { mutableStateOf(false) }
    var taskToEdit by remember { mutableStateOf<Task?>(null) }
    var selectedTabIndex by rememberSaveable { mutableIntStateOf(0) }
    var isSearchFocused by remember { mutableStateOf(false) }

    val tabs = listOf("Все", "В процессе", "Завершенные")
    val focusManager = LocalFocusManager.current
    val isSearchMode = state.searchResults != null || isSearchFocused

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { Text("Мои задачи") },
                    actions = {
                        IconButton(onClick = viewModel::loadTasks) {
                            Icon(Icons.Filled.Refresh, contentDescription = "Обновить задачи")
                        }
                        IconButton(onClick = onLogout) {
                            Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Выйти")
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(end = 12.dp)
                        ) {
                            Text(
                                text = "Темная тема",
                                style = MaterialTheme.typography.labelLarge
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Switch(
                                checked = isDarkThemeEnabled,
                                onCheckedChange = onToggleTheme
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        actionIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )

                SearchPanel(
                    query = state.searchQuery,
                    onQueryChange = viewModel::updateSearchQuery,
                    onSearch = {
                        viewModel.performSearch()
                        focusManager.clearFocus()
                    },
                    onClear = {
                        viewModel.clearSearch()
                        focusManager.clearFocus()
                    },
                    onFocusChanged = { isSearchFocused = it }
                )

                if (!isSearchMode) {
                    TaskSummary(tasks = state.tasks)
                    TabRow(selectedTabIndex = selectedTabIndex) {
                        tabs.forEachIndexed { index, title ->
                            Tab(
                                selected = selectedTabIndex == index,
                                onClick = { selectedTabIndex = index },
                                text = { Text(title) }
                            )
                        }
                    }
                }
            }
        },
        floatingActionButton = {
            if (!isSearchMode) {
                FloatingActionButton(onClick = { showAddTaskDialog = true }) {
                    Icon(Icons.Filled.Add, contentDescription = "Добавить задачу")
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            val searchResults = state.searchResults
            val filteredTasks = when (selectedTabIndex) {
                1 -> state.tasks.filter { !it.isCompleted }
                2 -> state.tasks.filter { it.isCompleted }
                else -> state.tasks
            }

            when {
                isSearchFocused && state.searchQuery.isEmpty() && state.searchHistory.isNotEmpty() -> {
                    SearchHistoryList(
                        history = state.searchHistory,
                        onHistoryClick = { query ->
                            viewModel.updateSearchQuery(query)
                            viewModel.performSearch(query)
                            focusManager.clearFocus()
                        },
                        onClearHistory = viewModel::clearSearchHistory
                    )
                }

                state.isSearchLoading -> {
                    LoadingState()
                }

                state.searchError != null -> {
                    ErrorState(
                        message = state.searchError,
                        onRetry = { viewModel.performSearch() }
                    )
                }

                searchResults != null -> {
                    if (searchResults.isEmpty()) {
                        EmptyState("По запросу \"${state.searchQuery}\" ничего не найдено")
                    } else {
                        TaskList(
                            tasks = searchResults,
                            onTaskClick = { task ->
                                viewModel.addSearchHistoryItem(task.title)
                                viewModel.toggleTaskStatus(task)
                            },
                            onDelete = { task -> viewModel.deleteTask(task.id) },
                            onEdit = { taskToEdit = it }
                        )
                    }
                }

                state.isLoading && state.tasks.isEmpty() -> {
                    LoadingState()
                }

                state.error != null && state.tasks.isEmpty() -> {
                    ErrorState(
                        message = state.error,
                        onRetry = viewModel::loadTasks
                    )
                }

                filteredTasks.isEmpty() -> {
                    val message = when (selectedTabIndex) {
                        1 -> "Нет задач в процессе"
                        2 -> "Нет завершенных задач"
                        else -> "Здесь пока нет задач"
                    }
                    EmptyState(message)
                }

                else -> {
                    TaskList(
                        tasks = filteredTasks,
                        headerError = state.error,
                        onTaskClick = viewModel::toggleTaskStatus,
                        onDelete = { task -> viewModel.deleteTask(task.id) },
                        onEdit = { taskToEdit = it }
                    )
                }
            }
        }

        if (showAddTaskDialog) {
            AddTaskDialog(
                onDismiss = { showAddTaskDialog = false },
                onConfirm = { title, description ->
                    viewModel.addTask(title, description)
                    showAddTaskDialog = false
                }
            )
        }

        taskToEdit?.let { task ->
            EditTaskDialog(
                task = task,
                onDismiss = { taskToEdit = null },
                onConfirm = { newTitle, newDescription ->
                    viewModel.editTask(task, newTitle, newDescription)
                    taskToEdit = null
                }
            )
        }
    }
}

@Composable
private fun SearchPanel(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    onClear: () -> Unit,
    onFocusChanged: (Boolean) -> Unit
) {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            placeholder = { Text("Поиск задач") },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Поиск") },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = onClear) {
                        Icon(Icons.Filled.Clear, contentDescription = "Очистить")
                    }
                }
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { onSearch() }),
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { onFocusChanged(it.isFocused) },
            singleLine = true
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(
                onClick = onClear,
                enabled = query.isNotEmpty()
            ) {
                Icon(Icons.Filled.Clear, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Очистить")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = onSearch,
                enabled = query.isNotBlank()
            ) {
                Icon(Icons.Filled.Search, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Найти")
            }
        }
    }
}

@Composable
private fun TaskSummary(tasks: List<Task>) {
    val completed = tasks.count { it.isCompleted }
    val inProgress = tasks.size - completed

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        SummaryChip(label = "Всего", value = tasks.size, modifier = Modifier.weight(1f))
        SummaryChip(label = "В процессе", value = inProgress, modifier = Modifier.weight(1f))
        SummaryChip(label = "Готово", value = completed, modifier = Modifier.weight(1f))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SummaryChip(
    label: String,
    value: Int,
    modifier: Modifier = Modifier
) {
    AssistChip(
        onClick = {},
        label = {
            Text(
                text = "$label: $value",
                style = MaterialTheme.typography.labelLarge
            )
        },
        modifier = modifier
    )
}

@Composable
private fun SearchHistoryList(
    history: List<String>,
    onHistoryClick: (String) -> Unit,
    onClearHistory: () -> Unit
) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "История поиска",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                TextButton(onClick = onClearHistory) {
                    Text("Очистить историю")
                }
            }
        }

        items(history) { historyItem ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onHistoryClick(historyItem) }
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Filled.History,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(historyItem, style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}

@Composable
private fun TaskList(
    tasks: List<Task>,
    headerError: String? = null,
    onTaskClick: (Task) -> Unit,
    onDelete: (Task) -> Unit,
    onEdit: (Task) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        headerError?.let { error ->
            item {
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }
        }

        items(tasks) { task ->
            TaskItem(
                task = task,
                onToggle = { onTaskClick(task) },
                onDelete = { onDelete(task) },
                onEdit = { onEdit(task) }
            )
        }
    }
}

@Composable
private fun LoadingState() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ErrorState(
    message: String?,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = message ?: "Произошла ошибка",
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodyLarge
        )
        Spacer(modifier = Modifier.height(12.dp))
        Button(onClick = onRetry) {
            Icon(Icons.Filled.Refresh, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Обновить")
        }
    }
}

@Composable
private fun EmptyState(message: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
fun TaskItem(
    task: Task,
    onToggle: () -> Unit,
    onDelete: () -> Unit,
    onEdit: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() },
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = task.isCompleted,
                onCheckedChange = { onToggle() }
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleMedium,
                    textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                )
                if (!task.description.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = task.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            IconButton(onClick = onEdit) {
                Icon(
                    Icons.Filled.Edit,
                    contentDescription = "Редактировать",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Filled.Delete,
                    contentDescription = "Удалить",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
fun AddTaskDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    var title by rememberSaveable { mutableStateOf("") }
    var description by rememberSaveable { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Новая задача") },
        text = {
            TaskForm(
                title = title,
                description = description,
                onTitleChange = { title = it },
                onDescriptionChange = { description = it }
            )
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(title, description) },
                enabled = title.isNotBlank()
            ) {
                Text("Сохранить")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    )
}

@Composable
fun EditTaskDialog(
    task: Task,
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    var title by rememberSaveable(task.id) { mutableStateOf(task.title) }
    var description by rememberSaveable(task.id) { mutableStateOf(task.description ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Редактировать задачу") },
        text = {
            TaskForm(
                title = title,
                description = description,
                onTitleChange = { title = it },
                onDescriptionChange = { description = it }
            )
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(title, description) },
                enabled = title.isNotBlank()
            ) {
                Text("Обновить")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    )
}

@Composable
private fun TaskForm(
    title: String,
    description: String,
    onTitleChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        OutlinedTextField(
            value = title,
            onValueChange = onTitleChange,
            label = { Text("Название") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        OutlinedTextField(
            value = description,
            onValueChange = onDescriptionChange,
            label = { Text("Описание") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )
    }
}
