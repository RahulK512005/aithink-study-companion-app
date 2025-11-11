package com.aithink.studycompanion.data.local

import android.content.Context
import android.content.SharedPreferences
import com.aithink.studycompanion.data.models.UserProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Manages app preferences and settings
 */
class PreferencesManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(
        "aithink_prefs",
        Context.MODE_PRIVATE
    )

    private val _userProfile = MutableStateFlow<UserProfile?>(null)

    companion object {
        private const val KEY_FIRST_LAUNCH = "first_launch"
        private const val KEY_MODEL_DOWNLOADED = "model_downloaded"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_USER_EMAIL = "user_email"
        private const val KEY_LAST_ACTIVE = "last_active_date"
        private const val KEY_STREAK = "learning_streak"
        private const val KEY_LAST_STREAK_DATE = "last_streak_date"
        private const val KEY_STUDY_HISTORY = "study_history"
        private const val KEY_SUBJECTS_STUDIED = "subjects_studied"
    }

    fun isFirstLaunch(): Boolean {
        return prefs.getBoolean(KEY_FIRST_LAUNCH, true)
    }

    fun setFirstLaunchComplete() {
        prefs.edit().putBoolean(KEY_FIRST_LAUNCH, false).apply()
    }

    fun isModelDownloaded(): Boolean {
        return prefs.getBoolean(KEY_MODEL_DOWNLOADED, false)
    }

    fun setModelDownloaded(downloaded: Boolean) {
        prefs.edit().putBoolean(KEY_MODEL_DOWNLOADED, downloaded).apply()
    }

    fun getUserName(): String {
        return prefs.getString(KEY_USER_NAME, "Student") ?: "Student"
    }

    fun setUserName(name: String) {
        prefs.edit().putString(KEY_USER_NAME, name).apply()
    }

    fun getUserProfile(): Flow<UserProfile?> {
        return _userProfile.asStateFlow()
    }

    suspend fun saveUserProfile(profile: UserProfile) {
        prefs.edit().apply {
            putString(KEY_USER_NAME, profile.name)
            putString(KEY_USER_EMAIL, profile.email)
            apply()
        }
        _userProfile.value = profile
    }

    suspend fun updateLastActiveDate() {
        prefs.edit().putLong(KEY_LAST_ACTIVE, System.currentTimeMillis()).apply()
    }

    fun getLastActiveDate(): Long {
        return prefs.getLong(KEY_LAST_ACTIVE, 0L)
    }
    
    fun getStreak(): Int {
        return prefs.getInt(KEY_STREAK, 0)
    }
    
    fun updateStreak() {
        val today = System.currentTimeMillis() / (24 * 60 * 60 * 1000)
        val lastDate = prefs.getLong(KEY_LAST_STREAK_DATE, 0) / (24 * 60 * 60 * 1000)
        
        val currentStreak = prefs.getInt(KEY_STREAK, 0)
        val newStreak = when {
            lastDate == today -> currentStreak
            lastDate == today - 1 -> currentStreak + 1
            else -> 1
        }
        
        prefs.edit().apply {
            putInt(KEY_STREAK, newStreak)
            putLong(KEY_LAST_STREAK_DATE, System.currentTimeMillis())
            apply()
        }
    }
    
    fun addStudyActivity(subject: String, level: String) {
        val timestamp = System.currentTimeMillis()
        val activity = "$timestamp|$level|$subject"
        
        val history = prefs.getStringSet(KEY_STUDY_HISTORY, mutableSetOf())?.toMutableSet() ?: mutableSetOf()
        history.add(activity)
        
        val subjects = prefs.getStringSet(KEY_SUBJECTS_STUDIED, mutableSetOf())?.toMutableSet() ?: mutableSetOf()
        subjects.add("$level|$subject")
        
        prefs.edit().apply {
            putStringSet(KEY_STUDY_HISTORY, history)
            putStringSet(KEY_SUBJECTS_STUDIED, subjects)
            apply()
        }
        
        updateStreak()
    }
    
    fun getStudyHistory(): List<StudyActivity> {
        val history = prefs.getStringSet(KEY_STUDY_HISTORY, emptySet()) ?: emptySet()
        return history.mapNotNull { entry ->
            val parts = entry.split("|")
            if (parts.size == 3) {
                StudyActivity(
                    timestamp = parts[0].toLongOrNull() ?: 0L,
                    level = parts[1],
                    subject = parts[2]
                )
            } else null
        }.sortedByDescending { it.timestamp }
    }
    
    fun getSubjectsStudiedCount(): Int {
        return prefs.getStringSet(KEY_SUBJECTS_STUDIED, emptySet())?.size ?: 0
    }
}

data class StudyActivity(
    val timestamp: Long,
    val level: String,
    val subject: String
)
