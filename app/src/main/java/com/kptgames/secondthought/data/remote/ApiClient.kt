package com.kptgames.secondthought.data.remote

import com.kptgames.secondthought.data.local.TokenManager
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {

    // TODO: Change this to your actual backend URL
    private const val BASE_URL = "https://second-thought.krishnarajthadesar.in/api/"

    private var apiService: ApiService? = null

    fun getInstance(tokenManager: TokenManager): ApiService {
        if (apiService == null) {
            // Logging interceptor for debugging
            val loggingInterceptor = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }

            // First, create a temporary API service without authenticator for token refresh
            val tempClient = OkHttpClient.Builder()
                .addInterceptor(AuthInterceptor(tokenManager))
                .addInterceptor(loggingInterceptor)
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build()

            val tempRetrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(tempClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            val tempApiService = tempRetrofit.create(ApiService::class.java)

            // Now create the final client with the authenticator
            val client = OkHttpClient.Builder()
                .addInterceptor(AuthInterceptor(tokenManager))
                .addInterceptor(loggingInterceptor)
                .authenticator(TokenAuthenticator(tokenManager, tempApiService))
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build()

            val retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            apiService = retrofit.create(ApiService::class.java)
        }
        return apiService!!
    }
}