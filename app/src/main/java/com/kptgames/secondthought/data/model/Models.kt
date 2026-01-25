package com.kptgames.secondthought.data.model

import java.time.LocalDate
import java.time.LocalTime

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
    val congratulateOnFinish: Boolean = true
)

data class UpdateSettingsRequest(
    val name: String,
    val remindBeforeActivity: Boolean,
    val remindOnStart: Boolean,
    val nudgeDuringActivity: Boolean,
    val congratulateOnFinish: Boolean
)

data class ApiResponse(
    val success: Boolean,
    val message: String? = null
)

// Task/Schedule models
data class TimeSlot(
    val label: String,
    val startHour: Int,
    val startMinute: Int,
    val endHour: Int,
    val endMinute: Int
)

// Predefined slots
val predefinedSlots = listOf(
    TimeSlot("Early Morning", 5, 0, 7, 0),
    TimeSlot("Morning", 7, 0, 9, 0),
    TimeSlot("Late Morning", 9, 0, 11, 0),
    TimeSlot("Midday", 11, 0, 13, 0),
    TimeSlot("Early Afternoon", 13, 0, 15, 0),
    TimeSlot("Afternoon", 15, 0, 17, 0),
    TimeSlot("Evening", 17, 0, 19, 0),
    TimeSlot("Night", 19, 0, 21, 0),
    TimeSlot("Late Night", 21, 0, 23, 0),
    TimeSlot("Midnight", 23, 0, 0, 0)
)

// Task block for the schedule
data class TaskBlock(
    val id: String = java.util.UUID.randomUUID().toString(),
    val startHour: Int = 9,
    val startMinute: Int = 0,
    val endHour: Int = 10,
    val endMinute: Int = 0,
    val task: String = "",
    val slotIndex: Int? = null // null means custom time
)

// Daily schedule - this is what gets saved as JSON
data class DailySchedule(
    val date: String, // ISO format: "2025-01-25"
    val createdAt: String, // ISO timestamp
    val updatedAt: String, // ISO timestamp
    val useSlots: Boolean,
    val tasks: List<TaskBlockJson>
)

// Simplified task for JSON serialization
data class TaskBlockJson(
    val id: String,
    val startTime: String, // "HH:mm" format
    val endTime: String,   // "HH:mm" format
    val task: String,
    val slotName: String?  // null if custom time
)

// API request for sending schedule
data class SaveScheduleRequest(
    val schedule: DailySchedule
)
