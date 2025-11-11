package com.aithink.studycompanion

import android.app.Application
import android.util.Log
import com.aithink.studycompanion.data.local.PreferencesManager
import com.aithink.studycompanion.data.service.AIService
import com.aithink.studycompanion.data.service.LLMInference
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class AiThinkApplication : Application() {

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    lateinit var preferencesManager: PreferencesManager
        private set

    lateinit var llmInference: LLMInference
        private set
        
    lateinit var aiService: AIService
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this

        Log.i(TAG, "🚀 AiThink Study Companion starting...")

        // Initialize PreferencesManager
        try {
            preferencesManager = PreferencesManager(this)
            Log.d(TAG, "✅ PreferencesManager initialized")
        } catch (e: Exception) {
            Log.e(TAG, "❌ PreferencesManager failed: ${e.message}", e)
        }

        // Initialize LLM Inference
        llmInference = LLMInference(this)
        
        // Initialize AI Service with RunAnywhere SDK support
        try {
            aiService = AIService.createWithContext(this)
            Log.d(TAG, "✅ AIService initialized: ${aiService.getSDKStatus()}")
        } catch (e: Exception) {
            Log.e(TAG, "❌ AIService initialization failed: ${e.message}", e)
            // Fallback to basic AIService
            aiService = AIService()
        }

        applicationScope.launch(Dispatchers.IO) {
            val success = llmInference.initialize()
            if (success) {
                Log.i(TAG, "✅ LLM Inference ready!")
            } else {
                Log.w(TAG, "⚠️ LLM Inference initialization failed")
            }
        }
    }

    companion object {
        private const val TAG = "AiThinkApp"

        lateinit var instance: AiThinkApplication
            private set

        fun getLLMInference(): LLMInference {
            return instance.llmInference
        }

        fun getPreferencesManager(): PreferencesManager {
            return instance.preferencesManager
        }
        
        fun getAIService(): AIService {
            return instance.aiService
        }
    }
}
