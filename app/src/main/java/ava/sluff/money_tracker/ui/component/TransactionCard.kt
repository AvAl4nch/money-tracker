package ava.sluff.money_tracker.ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ava.sluff.money_tracker.domain.model.Category
import ava.sluff.money_tracker.domain.model.Transaction
import ava.sluff.money_tracker.domain.model.TransactionType
import ava.sluff.money_tracker.util.MoneyFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TransactionCard(
    transaction: Transaction,
    categories: List<Category>,
    currency: String,
    onCategorySelected: (Long) -> Unit,
    onDelete: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var showCategoryPicker by remember { mutableStateOf(false) }
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable { expanded = !expanded }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    val sign = if (transaction.type == TransactionType.CREDIT) "+" else "-"
                    Text(
                        text = "$sign${MoneyFormat.amount(transaction.amount)} $currency",
                        style = MaterialTheme.typography.titleMedium,
                        color = if (transaction.type == TransactionType.CREDIT)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = dateFormat.format(Date(transaction.timestamp)),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
                if (transaction.categoryName != null && transaction.categoryColor != null) {
                    CategoryChip(name = transaction.categoryName, color = transaction.categoryColor)
                } else {
                    CategoryChip(name = "Uncategorized", color = 0xFF9E9E9EL)
                }
            }

            AnimatedVisibility(visible = expanded) {
                Column {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    transaction.merchantName?.let { DetailRow("Merchant", it) }
                    transaction.description?.let { DetailRow("Description", it) }
                    DetailRow("Source", transaction.smsSender)
                    transaction.balanceAfter?.let { DetailRow("Balance after", MoneyFormat.amount(it)) }
                    transaction.aiConfidence?.let {
                        if (transaction.isCategorizedByAi) DetailRow("AI confidence", "%.0f%%".format(it * 100))
                    }
                    transaction.note?.let { DetailRow("Note", it) }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        TextButton(onClick = { showCategoryPicker = !showCategoryPicker }) {
                            Text(if (showCategoryPicker) "Hide categories" else "Change category")
                        }
                        TextButton(onClick = onDelete) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete")
                            Text("Delete")
                        }
                    }

                    AnimatedVisibility(visible = showCategoryPicker) {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            categories.forEach { category ->
                                CategoryChip(
                                    name = category.name,
                                    color = category.color,
                                    onClick = {
                                        showCategoryPicker = false
                                        onCategorySelected(category.id)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
        Text(value, style = MaterialTheme.typography.bodyMedium)
    }
}
