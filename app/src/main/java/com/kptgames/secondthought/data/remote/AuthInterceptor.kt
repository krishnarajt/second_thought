package com.kptgames.secondthought.data.remote

import com.kptgames.secondthought.data.local.TokenManager
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(private val tokenManager: TokenManager) : Interceptor {
    
    // Endpoints that don't require authentication
    private val publicEndpoints = listOf(
        "auth/login",
        "auth/signup",
        "auth/refresh"
    )
    
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val requestPath = originalRequest.url.encodedPath
        
        // Skip auth header for public endpoints
        if (publicEndpoints.any { requestPath.contains(it) }) {
            return chain.proceed(originalRequest)
        }
        
        // Add auth header for protected endpoints
        val token = runBlocking { tokenManager.getAccessToken() }
        
        val newRequest = if (token != null) {
            originalRequest.newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        } else {
            originalRequest
        }
        
        return chain.proceed(newRequest)
    }
}
