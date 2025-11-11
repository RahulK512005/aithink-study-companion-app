package com.aithink.studycompanion.ui.screens.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aithink.studycompanion.AiThinkApplication
import com.aithink.studycompanion.data.local.StudyActivity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

data class HistoryUiState(
    val activities: List<StudyActivityItem> = emptyList(),
    val streak: Int = 0
)

data class StudyActivityItem(
    val subject: String,
    val level: String,
    val timeAgo: String,
    val timestamp: Long
)

class HistoryViewModel : ViewModel() {
    
    private val prefsManager = AiThinkApplication.getPreferencesManager()
    
    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()
    
    init {
        loadHistory()
    }
    
    private fun loadHistory() {
        viewModelScope.launch {
            val activities = prefsManager.getStudyHistory()
            val streak = prefsManager.getStreak()
            
            _uiState.value = HistoryUiState(
                activities = activities.map { it.toActivityItem() },
                streak = streak
            )
        }
    }
    
    private fun StudyActivity.toActivityItem(): StudyActivityItem {
        return StudyActivityItem(
            subject = subject,
            level = level,
            timeAgo = formatTimeAgo(timestamp),
            timestamp = timestamp
        )
    }
    
    private fun formatTimeAgo(timestamp: Long): String {
        val now = System.currentTimeMillis()
        val diff = now - timestamp
        
        return when {
            diff < 60_000 -> "Just now"
            diff < 3600_000 -> "${diff / 60_000}m ago"
            diff < 86400_000 -> "${diff / 3600_000}h ago"
            diff < 604800_000 -> "${diff / 86400_000}d ago"
            else -> SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(timestamp))
        }
    }
}
