package ava.sluff.money_tracker.ui.screen.ask

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ava.sluff.money_tracker.ai.AskResult
import ava.sluff.money_tracker.ai.DataQueryAgent
import ava.sluff.money_tracker.data.datastore.SettingsDataStore
import ava.sluff.money_tracker.data.remote.Message
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

/** One line in the conversation. [sql] is present on answers the user can audit. */
data class ChatEntry(
    val fromUser: Boolean,
    val text: String,
    val sql: String? = null,
    val isError: Boolean = false
)

@HiltViewModel
class AskViewModel @Inject constructor(
    private val agent: DataQueryAgent,
    private val settings: SettingsDataStore
) : ViewModel() {

    val entries = MutableStateFlow<List<ChatEntry>>(emptyList())
    val busy = MutableStateFlow(false)

    fun ask(question: String) {
        val trimmed = question.trim()
        if (trimmed.isEmpty() || busy.value) return

        entries.value = entries.value + ChatEntry(fromUser = true, text = trimmed)
        busy.value = true

        viewModelScope.launch {
            val apiKey = settings.apiKey.first()
            val model = settings.modelName.first()
            val reply = if (apiKey.isBlank() || model.isBlank()) {
                AskResult.NeedsConfiguration
            } else {
                agent.ask(
                    question = trimmed,
                    history = historyForModel(),
                    currency = settings.currency.first(),
                    today = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
                )
            }
            entries.value = entries.value + reply.toEntry()
            busy.value = false
        }
    }

    fun clear() {
        entries.value = emptyList()
    }

    /** Only successful turns are replayed; errors would just confuse the next query. */
    private fun historyForModel(): List<Message> =
        entries.value.dropLast(1).filter { !it.isError }.map {
            Message(if (it.fromUser) "user" else "assistant", it.text)
        }

    private fun AskResult.toEntry(): ChatEntry = when (this) {
        is AskResult.Answer -> ChatEntry(fromUser = false, text = text, sql = sql)
        AskResult.NeedsConfiguration -> ChatEntry(
            fromUser = false,
            text = "Set your API key and model in Settings first.",
            isError = true
        )
        AskResult.NetworkError -> ChatEntry(
            fromUser = false,
            text = "Couldn't reach the AI. Check your connection and try again.",
            isError = true
        )
        is AskResult.CouldNotAnswer -> ChatEntry(
            fromUser = false,
            text = "I couldn't answer that — try rephrasing. ($reason)",
            isError = true
        )
    }
}
