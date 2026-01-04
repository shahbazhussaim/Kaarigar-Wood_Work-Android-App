package com.kaarigar.ui.gemini

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kaarigar.data.Resource
import com.kaarigar.data.repository.GeminiRepository
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class GeminiViewModel(private val repository: GeminiRepository) : ViewModel() {

    private val _geminiResponse = MutableLiveData<Resource<String>>()
    val geminiResponse: LiveData<Resource<String>> = _geminiResponse

    fun askGemini(prompt: String) {
        val woodworkPrompt =
                "You are a woodwork expert assistant for the 'Karigar' app. " +
                        "Answer the following query related to woodwork, carpentry, or furniture: $prompt"
        viewModelScope.launch {
            repository.generateText(woodworkPrompt).collect { result ->
                _geminiResponse.value = result
            }
        }
    }

    private val _pricePrediction = MutableLiveData<Resource<String>>()
    val pricePrediction: LiveData<Resource<String>> = _pricePrediction

    fun predictPrice(category: String, description: String, image: android.graphics.Bitmap? = null) {
        viewModelScope.launch {
            try {
                repository.predictPrice(category, description, image).collect { result ->
                    _pricePrediction.value = result
                }
            } catch (e: Exception) {
                _pricePrediction.value = Resource.error("Expert service unavailable", null)
            }
        }
    }
}
