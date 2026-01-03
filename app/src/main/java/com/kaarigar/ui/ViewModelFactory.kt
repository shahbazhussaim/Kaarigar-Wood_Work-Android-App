package com.kaarigar.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.kaarigar.data.repository.AuthRepository
import com.kaarigar.data.repository.GeminiRepository
import com.kaarigar.ui.auth.AuthViewModel
import com.kaarigar.ui.gemini.GeminiViewModel

class ViewModelFactory(
        private val authRepository: AuthRepository? = null,
        private val geminiRepository: GeminiRepository? = null
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST") return AuthViewModel(authRepository!!) as T
        }
        if (modelClass.isAssignableFrom(GeminiViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST") return GeminiViewModel(geminiRepository!!) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
