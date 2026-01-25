package com.kptgames.secondthought.data.remote

import com.kptgames.secondthought.data.model.*
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT

interface ApiService {
    
    // Auth endpoints - no token required
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>
    
    @POST("auth/signup")
    suspend fun signup(@Body request: SignupRequest): Response<AuthResponse>
    
    @POST("auth/refresh")
    suspend fun refreshToken(@Body request: RefreshRequest): Response<RefreshResponse>
    
    // Protected endpoints - token required
    @GET("user/settings")
    suspend fun getSettings(): Response<UserSettings>
    
    @PUT("user/settings")
    suspend fun updateSettings(@Body request: UpdateSettingsRequest): Response<ApiResponse>
    
    // Schedule endpoints
    @POST("schedule/save")
    suspend fun saveSchedule(@Body request: SaveScheduleRequest): Response<ApiResponse>
    
    @GET("schedule/today")
    suspend fun getTodaySchedule(): Response<DailySchedule>
}
