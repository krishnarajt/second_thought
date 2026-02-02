package com.kptgames.secondthought.data.remote

import com.kptgames.secondthought.data.local.TokenManager
import com.kptgames.secondthought.data.model.RefreshRequest
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route

class TokenAuthenticator(
    private val tokenManager: TokenManager,
    private val apiService: ApiService
) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        // If we've already tried to refresh (avoid infinite loop)
        if (response.request.header("Token-Refreshed") != null) {
            // Token refresh already attempted, clear tokens and force re-login
            runBlocking { tokenManager.clearTokens() }
            return null
        }

        // Get the refresh token
        val refreshToken = runBlocking { tokenManager.getRefreshToken() }

        if (refreshToken == null) {
            // No refresh token, clear everything and force login
            runBlocking { tokenManager.clearTokens() }
            return null
        }

        // Try to refresh the access token
        return runBlocking {
            try {
                val refreshResponse = apiService.refreshToken(RefreshRequest(refreshToken))

                if (refreshResponse.isSuccessful && refreshResponse.body() != null) {
                    // Successfully refreshed the token
                    val newAccessToken = refreshResponse.body()!!.accessToken
                    tokenManager.updateAccessToken(newAccessToken)

                    // Retry the original request with the new token
                    response.request.newBuilder()
                        .header("Authorization", "Bearer $newAccessToken")
                        .header("Token-Refreshed", "true")
                        .build()
                } else {
                    // Refresh failed (refresh token expired or invalid)
                    tokenManager.clearTokens()
                    null
                }
            } catch (e: Exception) {
                // Network error or other issue during refresh
                tokenManager.clearTokens()
                null
            }
        }
    }
}