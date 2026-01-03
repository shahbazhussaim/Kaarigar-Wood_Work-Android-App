package com.kaarigar.data.remote

data class GroqRequest(
        val model: String = "llama3-70b-8192",
        val messages: List<GroqMessage>,
        val temperature: Double = 0.7
)

data class GroqMessage(val role: String, val content: String)

data class GroqResponse(val id: String, val choices: List<GroqChoice>)

data class GroqChoice(val index: Int, val message: GroqMessage)
