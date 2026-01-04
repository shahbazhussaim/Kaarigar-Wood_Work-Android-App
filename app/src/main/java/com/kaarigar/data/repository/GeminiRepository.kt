package com.kaarigar.data.repository

import com.kaarigar.BuildConfig
import com.kaarigar.data.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content

class GeminiRepository {

    // Use the key from local.properties if available, otherwise fallback to the user provided key
    private val apiKey: String
        get() {
            val configKey = BuildConfig.GEMINI_API_KEY
            // Only use config key if it looks like a valid Google API Key (Starts with AIza)
            // Otherwise fallback to the hardcoded one provided by user
            return if (configKey != "null" && configKey.startsWith("AIza")) configKey 
                   else "AIzaSyCYn56_T_h87Zn06dVw2Jto92-KuNSgano"
        }

    private val generativeModel by lazy {
        com.google.ai.client.generativeai.GenerativeModel(
            modelName = "gemini-1.5-flash",
            apiKey = apiKey
        )
    }

    fun generateText(prompt: String): Flow<Resource<String>> = flow {
        emit(Resource.loading())
        try {
            val response = generativeModel.generateContent(prompt)
            val text = response.text
            
            if (text != null) {
                emit(Resource.success(text))
            } else {
                emit(Resource.error("Empty response from Gemini"))
            }
        } catch (e: Exception) {
            emit(Resource.error("Gemini Error: ${e.localizedMessage}"))
        }
    }

    fun predictPrice(category: String, description: String, image: android.graphics.Bitmap? = null): Flow<Resource<String>> = flow {
        emit(Resource.loading())
        
        val promptText = if (image != null) {
            "As a furniture expert, analyze this image and estimate a fair market price in Indian Rupees (₹) for a $category.\n" +
            "Details: $description\n\n" +
            "Return ONLY a JSON object exactly like this: {\"price\": \"1500\", \"reason\": \"Your reason here based on image analysis\"}. No other text."
        } else {
            "As a furniture expert, estimate a fair market price in Indian Rupees (₹) for a $category.\n" +
            "Description: $description\n\n" +
            "Return ONLY a JSON object exactly like this: {\"price\": \"1500\", \"reason\": \"Your reason here\"}. No other text."
        }

        try {
            val content = com.google.ai.client.generativeai.type.content {
                if (image != null) {
                    image(image)
                }
                text(promptText)
            }

            val response = generativeModel.generateContent(content)
            val text = response.text

            if (text != null) {
                val startIndex = text.indexOf("{")
                val endIndex = text.lastIndexOf("}")
                if (startIndex != -1 && endIndex != -1 && endIndex > startIndex) {
                    val cleanText = text.substring(startIndex, endIndex + 1)
                    emit(Resource.success(cleanText))
                } else {
                    emit(Resource.success(text.trim()))
                }
            } else {
                emit(Resource.error("Price prediction failed: No content"))
            }
        } catch (e: Exception) {
             val msg = e.localizedMessage ?: "Unknown Error"
             if (msg.contains("API key")) {
                 emit(Resource.error("Invalid API Key. Please check local.properties"))
             } else {
                 emit(Resource.error("Gemini Error: $msg"))
             }
        }
    }
}
