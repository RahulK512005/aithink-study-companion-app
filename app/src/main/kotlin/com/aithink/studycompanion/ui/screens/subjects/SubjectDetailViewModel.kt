package com.aithink.studycompanion.ui.screens.subjects

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aithink.studycompanion.AiThinkApplication
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SubjectDetailUiState(
    val subject: Subject? = null,
    val level: EducationLevel? = null,
    val messages: List<ChatMessage> = emptyList(),
    val generatedContent: String = "",
    val isLoading: Boolean = false
)

class SubjectDetailViewModel : ViewModel() {
    
    private val aiService = AiThinkApplication.getAIService()
    private val prefsManager = AiThinkApplication.getPreferencesManager()
    
    private val _uiState = MutableStateFlow(SubjectDetailUiState())
    val uiState: StateFlow<SubjectDetailUiState> = _uiState.asStateFlow()
    
    fun setSubject(subject: Subject, level: EducationLevel) {
        // Clear previous chat when switching subjects
        _uiState.value = SubjectDetailUiState(
            subject = subject,
            level = level,
            messages = emptyList(),
            generatedContent = ""
        )
        
        // Track study activity
        prefsManager.addStudyActivity(subject.name, level.name)
    }
    
    fun generateKidsContent() {
        val subject = _uiState.value.subject ?: return
        val level = _uiState.value.level ?: return
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            // Track activity
            prefsManager.addStudyActivity(subject.name, level.name)
            
            try {
                val content = aiService.generateKidsContent(
                    subject.name,
                    com.aithink.studycompanion.data.models.AIModel.GEMMA_3_1B
                )
                
                _uiState.value = _uiState.value.copy(
                    generatedContent = content,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    generatedContent = "Error generating content. Please try again.",
                    isLoading = false
                )
            }
        }
    }
    
    suspend fun sendMessage(message: String) {
        val subject = _uiState.value.subject ?: return
        
        val userMessage = ChatMessage(message, true)
        _uiState.value = _uiState.value.copy(
            messages = _uiState.value.messages + userMessage,
            isLoading = true
        )
        
        try {
            val prompt = "Subject: ${subject.name}\nQuestion: $message\nProvide a clear, educational answer."
            
            val responseBuilder = StringBuilder()
            aiService.chat(
                prompt,
                com.aithink.studycompanion.data.models.AIModel.GEMMA_3_1B
            ).collect { token ->
                responseBuilder.append(token)
            }
            
            val aiMessage = ChatMessage(responseBuilder.toString().trim(), false)
            _uiState.value = _uiState.value.copy(
                messages = _uiState.value.messages + aiMessage,
                isLoading = false
            )
        } catch (e: Exception) {
            val errorMessage = ChatMessage("Sorry, I couldn't process that. Please try again.", false)
            _uiState.value = _uiState.value.copy(
                messages = _uiState.value.messages + errorMessage,
                isLoading = false
            )
        }
    }
}
