package com.aithink.studycompanion.ui.screens.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.aithink.studycompanion.data.local.PreferencesManager
import com.aithink.studycompanion.data.models.LearningPurpose
import com.aithink.studycompanion.data.models.UserProfile
import com.aithink.studycompanion.data.models.UserRole
import kotlinx.coroutines.launch

class LoginViewModel(private val prefsManager: PreferencesManager) : ViewModel() {

    fun saveProfile(name: String, email: String, role: UserRole, purpose: LearningPurpose) {
        viewModelScope.launch {
            val profile = UserProfile(
                name = name,
                email = email,
                role = role,
                purpose = purpose
            )
            prefsManager.saveUserProfile(profile)
        }
    }
}

class LoginViewModelFactory(
    private val preferencesManager: PreferencesManager
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
            return LoginViewModel(preferencesManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
