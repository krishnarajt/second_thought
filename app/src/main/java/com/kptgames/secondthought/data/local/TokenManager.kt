package com.kptgames.secondthought.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

// Extension to create DataStore instance
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "auth_prefs")

class TokenManager(private val context: Context) {
    
    companion object {
        private val ACCESS_TOKEN_KEY = stringPreferencesKey("access_token")
        private val REFRESH_TOKEN_KEY = stringPreferencesKey("refresh_token")
        private val USER_NAME_KEY = stringPreferencesKey("user_name")
        
        // Settings keys
        private val REMIND_BEFORE_KEY = booleanPreferencesKey("remind_before")
        private val REMIND_ON_START_KEY = booleanPreferencesKey("remind_on_start")
        private val NUDGE_DURING_KEY = booleanPreferencesKey("nudge_during")
        private val CONGRATULATE_KEY = booleanPreferencesKey("congratulate")
    }
    
    // Save tokens after login/signup
    suspend fun saveTokens(accessToken: String, refreshToken: String) {
        context.dataStore.edit { preferences ->
            preferences[ACCESS_TOKEN_KEY] = accessToken
            preferences[REFRESH_TOKEN_KEY] = refreshToken
        }
    }
    
    // Get access token
    suspend fun getAccessToken(): String? {
        return context.dataStore.data.first()[ACCESS_TOKEN_KEY]
    }
    
    // Get refresh token
    suspend fun getRefreshToken(): String? {
        return context.dataStore.data.first()[REFRESH_TOKEN_KEY]
    }
    
    // Update access token after refresh
    suspend fun updateAccessToken(accessToken: String) {
        context.dataStore.edit { preferences ->
            preferences[ACCESS_TOKEN_KEY] = accessToken
        }
    }
    
    // Clear tokens on logout
    suspend fun clearTokens() {
        context.dataStore.edit { preferences ->
            preferences.remove(ACCESS_TOKEN_KEY)
            preferences.remove(REFRESH_TOKEN_KEY)
        }
    }
    
    // Check if user is logged in
    val isLoggedIn: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[ACCESS_TOKEN_KEY] != null
    }
    
    // Save user name locally
    suspend fun saveUserName(name: String) {
        context.dataStore.edit { preferences ->
            preferences[USER_NAME_KEY] = name
        }
    }
    
    // Get user name
    fun getUserName(): Flow<String> = context.dataStore.data.map { preferences ->
        preferences[USER_NAME_KEY] ?: ""
    }
    
    // Save notification settings locally
    suspend fun saveNotificationSettings(
        remindBefore: Boolean,
        remindOnStart: Boolean,
        nudgeDuring: Boolean,
        congratulate: Boolean
    ) {
        context.dataStore.edit { preferences ->
            preferences[REMIND_BEFORE_KEY] = remindBefore
            preferences[REMIND_ON_START_KEY] = remindOnStart
            preferences[NUDGE_DURING_KEY] = nudgeDuring
            preferences[CONGRATULATE_KEY] = congratulate
        }
    }
    
    // Get notification settings
    fun getRemindBefore(): Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[REMIND_BEFORE_KEY] ?: true
    }
    
    fun getRemindOnStart(): Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[REMIND_ON_START_KEY] ?: true
    }
    
    fun getNudgeDuring(): Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[NUDGE_DURING_KEY] ?: true
    }
    
    fun getCongratulate(): Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[CONGRATULATE_KEY] ?: true
    }
}
