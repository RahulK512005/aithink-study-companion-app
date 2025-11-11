package com.aithink.studycompanion.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aithink.studycompanion.AiThinkApplication
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class Achievement(
    val emoji: String,
    val title: String,
    val description: String
)

data class ProfileUiState(
    val userName: String = "Student",
    val streak: Int = 0,
    val level: Int = 1,
    val totalQuizzes: Int = 0,
    val averageScore: Int = 0,
    val topicsLearned: Int = 0,
    val studyTime: String = "0h",
    val achievements: List<Achievement> = emptyList()
)

class ProfileViewModel : ViewModel() {
    
    private val preferencesManager = AiThinkApplication.getPreferencesManager()
    
    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()
    
    init {
        loadProfileData()
    }
    
    private fun loadProfileData() {
        viewModelScope.launch {
            val streak = preferencesManager.getStreak()
            val subjectsCount = preferencesManager.getSubjectsStudiedCount()
            
            _uiState.value = ProfileUiState(
                userName = preferencesManager.getUserName(),
                streak = streak,
                level = calculateLevel(subjectsCount),
                totalQuizzes = 12,
                averageScore = 85,
                topicsLearned = subjectsCount,
                studyTime = "2h 30m",
                achievements = getAchievements(streak, subjectsCount)
            )
        }
    }
    
    private fun calculateLevel(totalQuizzes: Int): Int {
        return when {
            totalQuizzes < 5 -> 1
            totalQuizzes < 15 -> 2
            totalQuizzes < 30 -> 3
            totalQuizzes < 50 -> 4
            else -> 5
        }
    }
    
    private fun formatStudyTime(minutes: Long): String {
        val hours = minutes / 60
        return if (hours > 0) "${hours}h" else "${minutes}m"
    }
    
    private fun getAchievements(streak: Int, subjectsCount: Int): List<Achievement> {
        val achievements = mutableListOf<Achievement>()
        
        if (streak >= 7) {
            achievements.add(Achievement("🔥", "Week Warrior", "$streak-day learning streak"))
        } else if (streak >= 3) {
            achievements.add(Achievement("🔥", "Getting Started", "$streak-day streak"))
        }
        
        if (subjectsCount >= 10) {
            achievements.add(Achievement("📚", "Subject Master", "Studied $subjectsCount subjects"))
        } else if (subjectsCount >= 5) {
            achievements.add(Achievement("📚", "Explorer", "Studied $subjectsCount subjects"))
        }
        
        if (subjectsCount >= 3 && streak >= 3) {
            achievements.add(Achievement("🎓", "Dedicated Learner", "Consistent progress"))
        }
        
        return achievements.ifEmpty {
            listOf(Achievement("🌟", "Just Started", "Begin your learning journey!"))
        }
    }
    
    fun editProfile() {
        // TODO: Navigate to edit profile screen
    }
    
    fun toggleNotifications() {
        // TODO: Toggle notification settings
    }
    
    fun toggleTheme() {
        // TODO: Toggle theme
    }
    
    fun showAbout() {
        // TODO: Show about dialog
    }
}