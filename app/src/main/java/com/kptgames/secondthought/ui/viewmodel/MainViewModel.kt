package com.kptgames.secondthought.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kptgames.secondthought.DEV_BYPASS_LOGIN
import com.kptgames.secondthought.data.local.TokenManager
import com.kptgames.secondthought.data.model.*
import com.kptgames.secondthought.data.repository.Repository
import com.kptgames.secondthought.data.repository.Result
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

// UI State for Auth screens
data class AuthUiState(
    val isLoading: Boolean = false,
    val isLoggedIn: Boolean = false,
    val errorMessage: String? = null
)

// UI State for Settings screen
data class SettingsUiState(
    val isLoading: Boolean = false,
    val userName: String = "",
    val remindBefore: Boolean = true,
    val remindOnStart: Boolean = true,
    val nudgeDuring: Boolean = true,
    val congratulate: Boolean = true,
    val defaultSlotDuration: Int = 60,
    val saveSuccess: Boolean = false,
    val errorMessage: String? = null,
    val telegramLinked: Boolean = false,
    val telegramLinkCode: String? = null,
    val telegramCodeExpiry: String? = null,
    val telegramMessage: String? = null,
    val isTelegramLoading: Boolean = false
)

// UI State for Main screen
data class MainUiState(
    val tasks: List<TaskBlock> = listOf(TaskBlock()),
    val isSaving: Boolean = false,
    val saveMessage: String? = null
)

class MainViewModel(
    private val repository: Repository,
    private val tokenManager: TokenManager
) : ViewModel() {

    // Auth state
    private val _authState = MutableStateFlow(AuthUiState())
    val authState: StateFlow<AuthUiState> = _authState.asStateFlow()

    // Settings state
    private val _settingsState = MutableStateFlow(SettingsUiState())
    val settingsState: StateFlow<SettingsUiState> = _settingsState.asStateFlow()

    // Main screen state
    private val _mainState = MutableStateFlow(MainUiState())
    val mainState: StateFlow<MainUiState> = _mainState.asStateFlow()

    init {
        // Check if user is already logged in (skip in dev mode)
        if (!DEV_BYPASS_LOGIN) {
            viewModelScope.launch {
                tokenManager.isLoggedIn.collect { isLoggedIn ->
                    _authState.value = _authState.value.copy(isLoggedIn = isLoggedIn)
                }
            }
        }

        // Load saved user name
        viewModelScope.launch {
            tokenManager.getUserName().collect { name ->
                _settingsState.value = _settingsState.value.copy(userName = name)
            }
        }

        // Load notification settings
        viewModelScope.launch {
            tokenManager.getRemindBefore().collect { value ->
                _settingsState.value = _settingsState.value.copy(remindBefore = value)
            }
        }
        viewModelScope.launch {
            tokenManager.getRemindOnStart().collect { value ->
                _settingsState.value = _settingsState.value.copy(remindOnStart = value)
            }
        }
        viewModelScope.launch {
            tokenManager.getNudgeDuring().collect { value ->
                _settingsState.value = _settingsState.value.copy(nudgeDuring = value)
            }
        }
        viewModelScope.launch {
            tokenManager.getCongratulate().collect { value ->
                _settingsState.value = _settingsState.value.copy(congratulate = value)
            }
        }

        // Load default slot duration
        viewModelScope.launch {
            tokenManager.getDefaultSlotDuration().collect { duration ->
                _settingsState.value = _settingsState.value.copy(defaultSlotDuration = duration)
            }
        }

        // Load Telegram linked status
        viewModelScope.launch {
            tokenManager.getTelegramLinked().collect { isLinked ->
                _settingsState.value = _settingsState.value.copy(telegramLinked = isLinked)
            }
        }

        // Load today's schedule if exists
        loadTodaySchedule()
    }

    // Login
    fun login(username: String, password: String) {
        viewModelScope.launch {
            _authState.value = _authState.value.copy(isLoading = true, errorMessage = null)

            when (val result = repository.login(username, password)) {
                is Result.Success -> {
                    _authState.value = _authState.value.copy(
                        isLoading = false,
                        isLoggedIn = true
                    )
                }
                is Result.Error -> {
                    _authState.value = _authState.value.copy(
                        isLoading = false,
                        errorMessage = result.message
                    )
                }
                is Result.Loading -> {}
            }
        }
    }

    // Signup
    fun signup(username: String, password: String) {
        viewModelScope.launch {
            _authState.value = _authState.value.copy(isLoading = true, errorMessage = null)

            when (val result = repository.signup(username, password)) {
                is Result.Success -> {
                    _authState.value = _authState.value.copy(
                        isLoading = false,
                        isLoggedIn = true
                    )
                }
                is Result.Error -> {
                    _authState.value = _authState.value.copy(
                        isLoading = false,
                        errorMessage = result.message
                    )
                }
                is Result.Loading -> {}
            }
        }
    }

    // Logout
    fun logout() {
        viewModelScope.launch {
            repository.logout()
            _authState.value = AuthUiState(isLoggedIn = false)
            _settingsState.value = SettingsUiState()
            _mainState.value = MainUiState()
        }
    }

    // Clear auth error
    fun clearAuthError() {
        _authState.value = _authState.value.copy(errorMessage = null)
    }

    // Save settings (name + notifications + slot duration)
    fun saveSettings(
        name: String,
        remindBefore: Boolean,
        remindOnStart: Boolean,
        nudgeDuring: Boolean,
        congratulate: Boolean,
        slotDuration: Int
    ) {
        viewModelScope.launch {
            _settingsState.value = _settingsState.value.copy(
                isLoading = true,
                saveSuccess = false,
                errorMessage = null
            )

            when (val result = repository.updateSettings(name, remindBefore, remindOnStart, nudgeDuring, congratulate, slotDuration)) {
                is Result.Success -> {
                    _settingsState.value = _settingsState.value.copy(
                        isLoading = false,
                        userName = name,
                        remindBefore = remindBefore,
                        remindOnStart = remindOnStart,
                        nudgeDuring = nudgeDuring,
                        congratulate = congratulate,
                        defaultSlotDuration = slotDuration,
                        saveSuccess = true
                    )
                }
                is Result.Error -> {
                    _settingsState.value = _settingsState.value.copy(
                        isLoading = false,
                        errorMessage = result.message
                    )
                }
                is Result.Loading -> {}
            }
        }
    }

    // Update a specific task
    fun updateTask(index: Int, task: TaskBlock) {
        val currentTasks = _mainState.value.tasks.toMutableList()
        currentTasks[index] = task

        // Auto-add new block if typing in the last one (and not ending at midnight)
        if (index == currentTasks.size - 1 && task.task.isNotEmpty()) {
            val lastTask = currentTasks.last()
            // Don't add if end time is midnight (0:00) or 23:59
            if (!(lastTask.endHour == 0 || (lastTask.endHour == 23 && lastTask.endMinute >= 59))) {
                // Calculate new end time based on default slot duration
                val durationMinutes = _settingsState.value.defaultSlotDuration
                val startTotalMinutes = lastTask.endHour * 60 + lastTask.endMinute
                var endTotalMinutes = startTotalMinutes + durationMinutes

                // Cap at 23:59
                if (endTotalMinutes >= 24 * 60) {
                    endTotalMinutes = 23 * 60 + 59
                }

                val newEndHour = endTotalMinutes / 60
                val newEndMinute = endTotalMinutes % 60

                val newTask = TaskBlock(
                    startHour = lastTask.endHour,
                    startMinute = lastTask.endMinute,
                    endHour = newEndHour,
                    endMinute = newEndMinute
                )
                currentTasks.add(newTask)
            }
        }

        _mainState.value = _mainState.value.copy(tasks = currentTasks, saveMessage = null)
    }

    // Delete a task
    fun deleteTask(index: Int) {
        val currentTasks = _mainState.value.tasks.toMutableList()
        if (currentTasks.size > 1) {
            currentTasks.removeAt(index)
            _mainState.value = _mainState.value.copy(tasks = currentTasks, saveMessage = null)
        }
    }

    // Save schedule
    fun saveSchedule() {
        viewModelScope.launch {
            _mainState.value = _mainState.value.copy(isSaving = true, saveMessage = null)

            val now = LocalDateTime.now()
            val dateStr = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
            val timestampStr = now.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)

            // Convert tasks to JSON format
            val taskJsonList = _mainState.value.tasks
                .filter { it.task.isNotBlank() }
                .map { task ->
                    TaskBlockJson(
                        id = task.id,
                        startTime = String.format("%02d:%02d", task.startHour, task.startMinute),
                        endTime = String.format("%02d:%02d", task.endHour, task.endMinute),
                        task = task.task
                    )
                }

            val schedule = DailySchedule(
                date = dateStr,
                createdAt = timestampStr,
                updatedAt = timestampStr,
                tasks = taskJsonList
            )

            when (val result = repository.saveSchedule(schedule)) {
                is Result.Success -> {
                    _mainState.value = _mainState.value.copy(
                        isSaving = false,
                        saveMessage = result.data
                    )
                }
                is Result.Error -> {
                    _mainState.value = _mainState.value.copy(
                        isSaving = false,
                        saveMessage = "Error: ${result.message}"
                    )
                }
                is Result.Loading -> {}
            }
        }
    }

    // Load today's schedule from local storage
    private fun loadTodaySchedule() {
        viewModelScope.launch {
            val dateStr = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
            val schedule = repository.loadScheduleFromFile(dateStr)

            if (schedule != null && schedule.tasks.isNotEmpty()) {
                val tasks = schedule.tasks.map { taskJson ->
                    val startParts = taskJson.startTime.split(":")
                    val endParts = taskJson.endTime.split(":")
                    TaskBlock(
                        id = taskJson.id,
                        startHour = startParts[0].toIntOrNull() ?: 9,
                        startMinute = startParts[1].toIntOrNull() ?: 0,
                        endHour = endParts[0].toIntOrNull() ?: 10,
                        endMinute = endParts[1].toIntOrNull() ?: 0,
                        task = taskJson.task
                    )
                }
                _mainState.value = _mainState.value.copy(tasks = tasks)
            }
        }
    }

    // Load settings from backend (including Telegram status)
    fun loadSettingsFromBackend() {
        viewModelScope.launch {
            when (val result = repository.getSettings()) {
                is Result.Success -> {
                    _settingsState.value = _settingsState.value.copy(
                        telegramLinked = result.data.telegramLinked
                    )
                }
                is Result.Error -> {
                    // Settings load failed, keep existing state
                }
                is Result.Loading -> {}
            }
        }
    }

    // Get Telegram link code
    fun getTelegramLinkCode() {
        // Don't allow getting code if already linked
        if (_settingsState.value.telegramLinked) {
            _settingsState.value = _settingsState.value.copy(
                telegramMessage = "Telegram is already linked to your account"
            )
            return
        }

        viewModelScope.launch {
            _settingsState.value = _settingsState.value.copy(
                isTelegramLoading = true,
                telegramMessage = null,
                telegramLinkCode = null
            )

            when (val result = repository.getTelegramLinkCode()) {
                is Result.Success -> {
                    _settingsState.value = _settingsState.value.copy(
                        isTelegramLoading = false,
                        telegramLinkCode = result.data.code,
                        telegramCodeExpiry = result.data.expiresAt,
                        telegramMessage = result.data.message
                    )
                }
                is Result.Error -> {
                    _settingsState.value = _settingsState.value.copy(
                        isTelegramLoading = false,
                        telegramMessage = "Error: ${result.message}"
                    )
                }
                is Result.Loading -> {}
            }
        }
    }

    // Unlink Telegram
    fun unlinkTelegram() {
        viewModelScope.launch {
            _settingsState.value = _settingsState.value.copy(
                isTelegramLoading = true,
                telegramMessage = null
            )

            when (val result = repository.unlinkTelegram()) {
                is Result.Success -> {
                    // Save unlinked status locally
                    tokenManager.saveTelegramLinked(false)

                    _settingsState.value = _settingsState.value.copy(
                        isTelegramLoading = false,
                        telegramLinked = false,
                        telegramMessage = "Telegram account unlinked successfully"
                    )
                }
                is Result.Error -> {
                    _settingsState.value = _settingsState.value.copy(
                        isTelegramLoading = false,
                        telegramMessage = "Error: ${result.message}"
                    )
                }
                is Result.Loading -> {}
            }
        }
    }

    // Clear Telegram message
    fun clearTelegramMessage() {
        _settingsState.value = _settingsState.value.copy(
            telegramMessage = null,
            telegramLinkCode = null
        )
    }

    // ==========================================
    // DEV BYPASS - Comment this out for production
    // ==========================================
    fun devBypassLogin() {
        _authState.value = _authState.value.copy(isLoggedIn = true)
    }
    // ==========================================
}

// Factory for creating ViewModel with dependencies
class MainViewModelFactory(
    private val repository: Repository,
    private val tokenManager: TokenManager
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            return MainViewModel(repository, tokenManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}