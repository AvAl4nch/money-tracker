package ava.sluff.money_tracker.ui.screen.transactions

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import ava.sluff.money_tracker.ui.component.EmptyState
import ava.sluff.money_tracker.ui.component.TransactionCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionListScreen(viewModel: TransactionListViewModel = hiltViewModel()) {
    val transactions by viewModel.transactions.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val currency by viewModel.currency.collectAsState()
    val activeSort by viewModel.sortMode.collectAsState()
    val activeFilter by viewModel.filterCategoryId.collectAsState()
    val activeQuery by viewModel.searchQuery.collectAsState()
    val activeRange by viewModel.dateRange.collectAsState()
    var sortMenuOpen by remember { mutableStateOf(false) }
    var filterMenuOpen by remember { mutableStateOf(false) }
    var showAddSheet by remember { mutableStateOf(false) }
    var searchOpen by remember { mutableStateOf(false) }
    var showRangePicker by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddSheet = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add transaction")
            }
        },
        topBar = {
            TopAppBar(
                title = {
                    if (searchOpen) {
                        OutlinedTextField(
                            value = activeQuery,
                            onValueChange = { viewModel.setSearchQuery(it) },
                            placeholder = { Text("Search merchant, description, note") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        Text("Transactions")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        if (searchOpen) viewModel.setSearchQuery("")
                        searchOpen = !searchOpen
                    }) {
                        Icon(
                            if (searchOpen) Icons.Default.Close else Icons.Default.Search,
                            contentDescription = if (searchOpen) "Close search" else "Search"
                        )
                    }
                    IconButton(onClick = { filterMenuOpen = true }) {
                        Icon(Icons.Default.FilterList, contentDescription = "Filter")
                    }
                    DropdownMenu(expanded = filterMenuOpen, onDismissRequest = { filterMenuOpen = false }) {
                        DropdownMenuItem(
                            text = { Text(if (activeFilter == null) "✓ All categories" else "All categories") },
                            onClick = { viewModel.setFilterCategory(null); filterMenuOpen = false }
                        )
                        categories.forEach { category ->
                            DropdownMenuItem(
                                text = { Text(if (activeFilter == category.id) "✓ ${category.name}" else category.name) },
                                onClick = { viewModel.setFilterCategory(category.id); filterMenuOpen = false }
                            )
                        }
                    }
                    IconButton(onClick = { sortMenuOpen = true }) {
                        Icon(Icons.AutoMirrored.Filled.Sort, contentDescription = "Sort")
                    }
                    DropdownMenu(expanded = sortMenuOpen, onDismissRequest = { sortMenuOpen = false }) {
                        SortMode.entries.forEach { mode ->
                            DropdownMenuItem(
                                text = { Text(if (mode == activeSort) "✓ ${mode.label}" else mode.label) },
                                onClick = { viewModel.setSortMode(mode); sortMenuOpen = false }
                            )
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            Row(
                modifier = Modifier
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DateRangeFilter.presets.forEach { preset ->
                    FilterChip(
                        selected = activeRange::class == preset::class,
                        onClick = { viewModel.setDateRange(preset) },
                        label = { Text(preset.label) }
                    )
                }
                FilterChip(
                    selected = activeRange is DateRangeFilter.Custom,
                    onClick = { showRangePicker = true },
                    label = { Text("Custom") }
                )
            }
            if (transactions.isEmpty()) {
                EmptyState(message = "No transactions data")
            } else {
                LazyColumn {
                    items(transactions, key = { it.id }) { transaction ->
                        TransactionCard(
                            transaction = transaction,
                            categories = categories,
                            currency = currency,
                            onCategorySelected = { categoryId ->
                                viewModel.updateCategory(transaction.id, categoryId)
                            },
                            onDelete = { viewModel.deleteTransaction(transaction.id) }
                        )
                    }
                }
            }
        }
    }

    if (showAddSheet) {
        AddTransactionSheet(
            categories = categories,
            onSave = { amount, type, merchant, categoryId, note, timestamp ->
                viewModel.addManualTransaction(amount, type, merchant, categoryId, note, timestamp)
                showAddSheet = false
            },
            onDismiss = { showAddSheet = false }
        )
    }

    if (showRangePicker) {
        val rangeState = rememberDateRangePickerState()
        DatePickerDialog(
            onDismissRequest = { showRangePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val start = rangeState.selectedStartDateMillis
                    val end = rangeState.selectedEndDateMillis
                    if (start != null && end != null) {
                        viewModel.setDateRange(DateRangeFilter.Custom(start, end + 86_399_999L))
                    }
                    showRangePicker = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showRangePicker = false }) { Text("Cancel") } }
        ) {
            DateRangePicker(state = rangeState, modifier = Modifier.weight(1f))
        }
    }
}
