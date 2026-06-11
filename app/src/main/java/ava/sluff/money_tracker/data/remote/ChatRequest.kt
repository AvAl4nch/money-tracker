package ava.sluff.money_tracker.data.remote

data class ChatRequest(
    val model: String,
    val messages: List<Message>,
    val temperature: Double = 0.1
)
