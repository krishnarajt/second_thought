package com.kptgames.secondthought.data.local

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.google.gson.GsonBuilder
import com.kptgames.secondthought.data.model.DailySchedule
import java.io.File
import java.io.FileOutputStream

class FileManager(private val context: Context) {
    
    private val gson = GsonBuilder().setPrettyPrinting().create()
    
    // Save schedule JSON to Downloads folder (accessible by user)
    fun saveScheduleToFile(schedule: DailySchedule): Result<String> {
        return try {
            val jsonString = gson.toJson(schedule)
            val fileName = "schedule_${schedule.date}.json"
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // For Android 10+ use MediaStore
                saveUsingMediaStore(fileName, jsonString)
            } else {
                // For older versions, direct file access
                saveToDownloadsLegacy(fileName, jsonString)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Android 10+ way to save files
    private fun saveUsingMediaStore(fileName: String, content: String): Result<String> {
        val contentValues = ContentValues().apply {
            put(MediaStore.Downloads.DISPLAY_NAME, fileName)
            put(MediaStore.Downloads.MIME_TYPE, "application/json")
            put(MediaStore.Downloads.IS_PENDING, 1)
        }
        
        val resolver = context.contentResolver
        val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
            ?: return Result.failure(Exception("Failed to create file"))
        
        return try {
            resolver.openOutputStream(uri)?.use { outputStream ->
                outputStream.write(content.toByteArray())
            }
            
            contentValues.clear()
            contentValues.put(MediaStore.Downloads.IS_PENDING, 0)
            resolver.update(uri, contentValues, null, null)
            
            Result.success("Saved to Downloads/$fileName")
        } catch (e: Exception) {
            resolver.delete(uri, null, null)
            Result.failure(e)
        }
    }
    
    // Legacy way for older Android versions
    @Suppress("DEPRECATION")
    private fun saveToDownloadsLegacy(fileName: String, content: String): Result<String> {
        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val file = File(downloadsDir, fileName)
        
        FileOutputStream(file).use { outputStream ->
            outputStream.write(content.toByteArray())
        }
        
        return Result.success("Saved to ${file.absolutePath}")
    }
    
    // Also save to internal app storage as backup
    fun saveScheduleInternal(schedule: DailySchedule): Result<String> {
        return try {
            val jsonString = gson.toJson(schedule)
            val fileName = "schedule_${schedule.date}.json"
            val file = File(context.filesDir, fileName)
            
            file.writeText(jsonString)
            Result.success(file.absolutePath)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Load schedule from internal storage
    fun loadScheduleInternal(date: String): DailySchedule? {
        return try {
            val fileName = "schedule_$date.json"
            val file = File(context.filesDir, fileName)
            
            if (file.exists()) {
                val jsonString = file.readText()
                gson.fromJson(jsonString, DailySchedule::class.java)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
}
