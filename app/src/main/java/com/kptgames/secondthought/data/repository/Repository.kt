package com.kptgames.secondthought.data.repository

import com.kptgames.secondthought.DEV_BYPASS_LOGIN
import com.kptgames.secondthought.data.local.FileManager
import com.kptgames.secondthought.data.local.TokenManager
import com.kptgames.secondthought.data.model.*
import com.kptgames.secondthought.data.remote.ApiService

// Sealed class for handling API results
sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val message: String) : Result<Nothing>()
    object Loading : Result<Nothing>()
}

class Repository(
    private val apiService: ApiService,
    private val tokenManager: TokenManager,
    private val fileManager: FileManager
) {
    
    // Login
    suspend fun login(username: String, password: String): Result<AuthResponse> {
        // Skip API in dev mode
        if (DEV_BYPASS_LOGIN) {
            return Result.Success(AuthResponse("dev_token", "dev_refresh", "Dev mode"))
        }
        
        return try {
            val response = apiService.login(LoginRequest(username, password))
            if (response.isSuccessful && response.body() != null) {
                val authResponse = response.body()!!
                tokenManager.saveTokens(authResponse.accessToken, authResponse.refreshToken)
                Result.Success(authResponse)
            } else {
                Result.Error(response.message() ?: "Login failed")
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Network error")
        }
    }
    
    // Signup
    suspend fun signup(username: String, password: String): Result<AuthResponse> {
        // Skip API in dev mode
        if (DEV_BYPASS_LOGIN) {
            return Result.Success(AuthResponse("dev_token", "dev_refresh", "Dev mode"))
        }
        
        return try {
            val response = apiService.signup(SignupRequest(username, password))
            if (response.isSuccessful && response.body() != null) {
                val authResponse = response.body()!!
                tokenManager.saveTokens(authResponse.accessToken, authResponse.refreshToken)
                Result.Success(authResponse)
            } else {
                Result.Error(response.message() ?: "Signup failed")
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Network error")
        }
    }
    
    // Refresh token
    suspend fun refreshToken(): Result<RefreshResponse> {
        // Skip API in dev mode
        if (DEV_BYPASS_LOGIN) {
            return Result.Success(RefreshResponse("dev_token"))
        }
        
        return try {
            val refreshToken = tokenManager.getRefreshToken() ?: return Result.Error("No refresh token")
            val response = apiService.refreshToken(RefreshRequest(refreshToken))
            if (response.isSuccessful && response.body() != null) {
                val refreshResponse = response.body()!!
                tokenManager.updateAccessToken(refreshResponse.accessToken)
                Result.Success(refreshResponse)
            } else {
                Result.Error("Token refresh failed")
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Network error")
        }
    }
    
    // Logout
    suspend fun logout() {
        tokenManager.clearTokens()
    }
    
    // Get user settings
    suspend fun getSettings(): Result<UserSettings> {
        // Skip API in dev mode
        if (DEV_BYPASS_LOGIN) {
            return Result.Success(UserSettings("Dev User"))
        }
        
        return try {
            val response = apiService.getSettings()
            if (response.isSuccessful && response.body() != null) {
                Result.Success(response.body()!!)
            } else {
                Result.Error(response.message() ?: "Failed to get settings")
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Network error")
        }
    }
    
    // Update user settings (name + notification preferences + slot duration)
    suspend fun updateSettings(
        name: String,
        remindBefore: Boolean,
        remindOnStart: Boolean,
        nudgeDuring: Boolean,
        congratulate: Boolean,
        slotDuration: Int
    ): Result<ApiResponse> {
        // Always save locally first
        tokenManager.saveUserName(name)
        tokenManager.saveNotificationSettings(remindBefore, remindOnStart, nudgeDuring, congratulate)
        tokenManager.saveDefaultSlotDuration(slotDuration)
        
        // Skip API in dev mode
        if (DEV_BYPASS_LOGIN) {
            return Result.Success(ApiResponse(true, "Saved locally (dev mode)"))
        }
        
        return try {
            val response = apiService.updateSettings(
                UpdateSettingsRequest(name, remindBefore, remindOnStart, nudgeDuring, congratulate, slotDuration)
            )
            if (response.isSuccessful && response.body() != null) {
                Result.Success(response.body()!!)
            } else {
                Result.Success(ApiResponse(true, "Saved locally"))
            }
        } catch (e: Exception) {
            Result.Success(ApiResponse(true, "Saved locally (offline)"))
        }
    }
    
    // Save schedule - saves to file AND sends to API
    suspend fun saveSchedule(schedule: DailySchedule): Result<String> {
        // Save to internal storage first (always works)
        fileManager.saveScheduleInternal(schedule)
        
        // Save to Downloads folder (user accessible)
        val fileResult = fileManager.saveScheduleToFile(schedule)
        val filePath = fileResult.getOrElse { "Saved to app storage" }
        
        // Skip API in dev mode
        if (DEV_BYPASS_LOGIN) {
            return Result.Success("$filePath\n(Dev mode - API skipped)")
        }
        
        // Send to backend API
        return try {
            val response = apiService.saveSchedule(SaveScheduleRequest(schedule))
            if (response.isSuccessful) {
                Result.Success("$filePath\nSynced to server")
            } else {
                Result.Success("$filePath\n(Offline - will sync later)")
            }
        } catch (e: Exception) {
            Result.Success("$filePath\n(Offline - will sync later)")
        }
    }
    
    // Load today's schedule from local storage
    fun loadScheduleFromFile(date: String): DailySchedule? {
        return fileManager.loadScheduleInternal(date)
    }
}
