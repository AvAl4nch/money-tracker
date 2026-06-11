package ava.sluff.money_tracker.ui.screen.summary

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import ava.sluff.money_tracker.ui.component.BudgetEditDialog
import ava.sluff.money_tracker.ui.component.ChartPalette
import ava.sluff.money_tracker.ui.component.MonthlyBarChart
import ava.sluff.money_tracker.ui.component.SpendingChart
import ava.sluff.money_tracker.util.MoneyFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpendingSummaryScreen(viewModel: SpendingSummaryViewModel = hiltViewModel()) {
    val spending by viewModel.spending.collectAsState()
    val totalSpent by viewModel.totalSpent.collectAsState()
    val currency by viewModel.currency.collectAsState()
    val monthLabel by viewModel.monthLabel.collectAsState()
    val selectedMonth by viewModel.selectedMonth.collectAsState()
    val budgets by viewModel.budgets.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val trend by viewModel.trend.collectAsState()
    var showBudgetDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Summary") }) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { viewModel.previousMonth() }) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Previous month")
                    }
                    Text(monthLabel, style = MaterialTheme.typography.titleMedium)
                    IconButton(onClick = { viewModel.nextMonth() }) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Next month")
                    }
                }
            }

            if (spending.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(vertical = 48.dp), contentAlignment = Alignment.Center) {
                        Text("No spending data this month.", color = MaterialTheme.colorScheme.outline)
                    }
                }
            } else {
                item {
                    Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                        Column(
                            modifier = Modifier.padding(16.dp).fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("Total spent", style = MaterialTheme.typography.labelSmall)
                            Text("${MoneyFormat.amount(totalSpent)} $currency", style = MaterialTheme.typography.headlineSmall)
                        }
                    }
                }
                item {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        SpendingChart(
                            spending = spending,
                            total = totalSpent,
                            modifier = Modifier.size(220.dp).padding(8.dp)
                        )
                    }
                }
                items(spending) { item ->
                    val index = spending.indexOf(item)
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(12.dp)) {
                                androidx.compose.foundation.Canvas(modifier = Modifier.size(12.dp)) {
                                    drawCircle(color = ChartPalette[index % ChartPalette.size])
                                }
                            }
                            Text("  ${item.name}", style = MaterialTheme.typography.bodyLarge)
                        }
                        val pct = if (totalSpent > 0) item.total / totalSpent * 100 else 0.0
                        Text(
                            "${MoneyFormat.amount(item.total)} $currency (${"%.0f".format(pct)}%)",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Budgets", style = MaterialTheme.typography.titleMedium)
                    TextButton(onClick = { showBudgetDialog = true }) { Text("Edit budgets") }
                }
            }
            if (budgets.isEmpty()) {
                item {
                    Text(
                        "No budgets set. Tap Edit budgets to add limits.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            } else {
                items(budgets) { b ->
                    val over = b.spent > b.monthlyLimit
                    val progress = (b.spent / b.monthlyLimit).toFloat().coerceIn(0f, 1f)
                    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(b.categoryName, style = MaterialTheme.typography.bodyLarge)
                            Text(
                                "${MoneyFormat.amount(b.spent)} / ${MoneyFormat.amount(b.monthlyLimit)} $currency",
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (over) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                            )
                        }
                        LinearProgressIndicator(
                            progress = { progress },
                            color = if (over) MaterialTheme.colorScheme.error else Color(b.categoryColor and 0xFFFFFFFFL),
                            modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                        )
                    }
                }
            }

            item {
                Text(
                    "Trends — last 6 months",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
            item {
                MonthlyBarChart(
                    bars = trend,
                    selectedYearMonth = selectedMonth,
                    onBarTap = { bar -> viewModel.selectMonth(bar.year, bar.month) },
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }
    }

    if (showBudgetDialog) {
        BudgetEditDialog(
            categories = categories,
            budgets = budgets,
            onSave = { changes ->
                changes.forEach { (categoryId, limit) -> viewModel.setBudget(categoryId, limit) }
                showBudgetDialog = false
            },
            onDismiss = { showBudgetDialog = false }
        )
    }
}
