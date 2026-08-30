package ava.sluff.money_tracker.ui.screen.ask

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import ava.sluff.money_tracker.ui.component.EmptyState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AskScreen(viewModel: AskViewModel = hiltViewModel()) {
    val entries by viewModel.entries.collectAsState()
    val busy by viewModel.busy.collectAsState()
    var draft by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ask") },
                actions = {
                    if (entries.isNotEmpty()) {
                        TextButton(onClick = { viewModel.clear() }) { Text("Clear") }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            if (busy) LinearProgressIndicator(modifier = Modifier.fillMaxWidth())

            if (entries.isEmpty()) {
                Column(modifier = Modifier.weight(1f)) {
                    EmptyState(message = "Ask about your spending")
                    Text(
                        "e.g. \"how much did I spend on health and education last month?\"",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 32.dp)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(entries) { entry -> ChatBubble(entry) }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = draft,
                    onValueChange = { draft = it },
                    placeholder = { Text("Ask a question") },
                    modifier = Modifier.weight(1f)
                )
                IconButton(
                    enabled = !busy && draft.isNotBlank(),
                    onClick = {
                        viewModel.ask(draft)
                        draft = ""
                    }
                ) {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send")
                }
            }
        }
    }
}

@Composable
private fun ChatBubble(entry: ChatEntry) {
    var showSql by remember { mutableStateOf(false) }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        colors = CardDefaults.cardColors(
            containerColor = when {
                entry.isError -> MaterialTheme.colorScheme.errorContainer
                entry.fromUser -> MaterialTheme.colorScheme.secondaryContainer
                else -> MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(entry.text, style = MaterialTheme.typography.bodyMedium)
            entry.sql?.let { sql ->
                TextButton(onClick = { showSql = !showSql }) {
                    Text(if (showSql) "Hide SQL" else "Show SQL")
                }
                if (showSql) {
                    Text(
                        sql,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }
    }
}
