package com.aithink.studycompanion.ui

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.aithink.studycompanion.AiThinkApplication
import com.aithink.studycompanion.ui.navigation.AppNavigation
import com.aithink.studycompanion.ui.theme.AiThinkStudyCompanionTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    
    companion object {
        private const val TAG = "MainActivity"
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val app = application as AiThinkApplication
        val prefsManager = app.preferencesManager
        val aiService = app.aiService
        
        // Log SDK status
        Log.i(TAG, "🔍 RunAnywhere SDK Status: ${aiService.getSDKStatus()}")
        Log.i(TAG, "📱 SDK Available: ${aiService.isSDKAvailable()}")
        Log.i(TAG, "🤖 Available Models: ${aiService.getAvailableModels()}")

        // Update last active date for streak
        lifecycleScope.launch {
            prefsManager.updateLastActiveDate()
        }

        setContent {
            AiThinkStudyCompanionTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation(preferencesManager = prefsManager)
                }
            }
        }
    }
}
