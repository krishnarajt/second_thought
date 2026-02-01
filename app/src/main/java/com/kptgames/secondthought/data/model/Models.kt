package com.kptgames.secondthought.data.model

// Auth Request/Response models
data class LoginRequest(
    val username: String,
    val password: String
)

data class SignupRequest(
    val username: String,
    val password: String
)

data class AuthResponse(
    val accessToken: String,
    val refreshToken: String,
    val message: String? = null
)

data class RefreshRequest(
    val refreshToken: String
)

data class RefreshResponse(
    val accessToken: String
)

// User settings
data class UserSettings(
    val name: String,
    val remindBeforeActivity: Boolean = true,
    val remindOnStart: Boolean = true,
    val nudgeDuringActivity: Boolean = true,
    val congratulateOnFinish: Boolean = true,
    val defaultSlotDuration: Int = 60,
    val telegramLinked: Boolean = false
)

data class UpdateSettingsRequest(
    val name: String,
    val remindBeforeActivity: Boolean,
    val remindOnStart: Boolean,
    val nudgeDuringActivity: Boolean,
    val congratulateOnFinish: Boolean,
    val defaultSlotDuration: Int
)

data class ApiResponse(
    val success: Boolean,
    val message: String? = null
)

// Duration options for settings dropdown
data class DurationOption(
    val minutes: Int,
    val label: String
)

val durationOptions = listOf(
    DurationOption(15, "15 minutes"),
    DurationOption(30, "30 minutes"),
    DurationOption(45, "45 minutes"),
    DurationOption(60, "1 hour"),
    DurationOption(90, "1.5 hours"),
    DurationOption(120, "2 hours")
)

// Task block for the schedule
data class TaskBlock(
    val id: String = java.util.UUID.randomUUID().toString(),
    val startHour: Int = 9,
    val startMinute: Int = 0,
    val endHour: Int = 10,
    val endMinute: Int = 0,
    val task: String = ""
)

// Daily schedule - this is what gets saved as JSON
data class DailySchedule(
    val date: String, // ISO format: "2025-01-25"
    val createdAt: String, // ISO timestamp
    val updatedAt: String, // ISO timestamp
    val tasks: List<TaskBlockJson>
)

// Simplified task for JSON serialization
data class TaskBlockJson(
    val id: String,
    val startTime: String, // "HH:mm" format
    val endTime: String,   // "HH:mm" format
    val task: String
)

// API request for sending schedule
data class SaveScheduleRequest(
    val schedule: DailySchedule
)

// Telegram link response
data class TelegramLinkResponse(
    val code: String,
    val expiresAt: String,
    val message: String
)

// Telegram unlink response
data class TelegramUnlinkResponse(
    val success: Boolean,
    val message: String
)