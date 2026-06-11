package ava.sluff.money_tracker.ui.screen.settings

data class ProviderPreset(
    val name: String,
    val baseUrl: String
)

val PROVIDER_PRESETS = listOf(
    ProviderPreset("OpenRouter", "https://openrouter.ai/api"),
    ProviderPreset("OpenAI", "https://api.openai.com"),
    ProviderPreset("Groq", "https://api.groq.com/openai"),
    ProviderPreset("Together", "https://api.together.xyz"),
    ProviderPreset("Ollama (local)", "http://localhost:11434"),
    ProviderPreset("Custom", "")
)
