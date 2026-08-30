package ava.sluff.money_tracker.ui.screen.transactions

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import ava.sluff.money_tracker.domain.model.Category
import ava.sluff.money_tracker.domain.model.Transaction
import ava.sluff.money_tracker.domain.model.TransactionEdits
import ava.sluff.money_tracker.domain.model.TransactionType
import ava.sluff.money_tracker.util.MoneyFormat
import ava.sluff.money_tracker.util.PickedDate
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Creates a manual transaction when [existing] is null, corrects one when it is not.
 *
 * The raw SMS and its sender are deliberately absent from this form: an edit fixes what the
 * AI got wrong, it does not rewrite what the bank actually sent.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionEditorSheet(
    categories: List<Category>,
    existing: Transaction? = null,
    onSave: (TransactionEdits) -> Unit,
    onDismiss: () -> Unit
) {
    var amountText by remember {
        mutableStateOf(existing?.let { MoneyFormat.amount(it.amount).replace(",", "") } ?: "")
    }
    var amountError by remember { mutableStateOf(false) }
    var type by remember { mutableStateOf(existing?.type ?: TransactionType.DEBIT) }
    var merchant by remember { mutableStateOf(existing?.merchantName ?: "") }
    var description by remember { mutableStateOf(existing?.description ?: "") }
    var note by remember { mutableStateOf(existing?.note ?: "") }
    var selectedCategory by remember {
        mutableStateOf(categories.firstOrNull { it.id == existing?.categoryId })
    }
    var categoryMenuOpen by remember { mutableStateOf(false) }
    var timestamp by remember { mutableStateOf(existing?.timestamp ?: System.currentTimeMillis()) }
    var showDatePicker by remember { mutableStateOf(false) }
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                if (existing == null) "Add transaction" else "Edit transaction",
                style = MaterialTheme.typography.titleMedium
            )

            OutlinedTextField(
                value = amountText,
                onValueChange = { amountText = it; amountError = false },
                label = { Text("Amount") },
                isError = amountError,
                supportingText = { if (amountError) Text("Enter an amount greater than 0") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = type == TransactionType.DEBIT,
                    onClick = { type = TransactionType.DEBIT },
                    label = { Text("Expense") }
                )
                FilterChip(
                    selected = type == TransactionType.CREDIT,
                    onClick = { type = TransactionType.CREDIT },
                    label = { Text("Income") }
                )
            }

            OutlinedTextField(
                value = merchant,
                onValueChange = { merchant = it },
                label = { Text("Merchant (optional)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description (optional)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            ExposedDropdownMenuBox(
                expanded = categoryMenuOpen,
                onExpandedChange = { categoryMenuOpen = it }
            ) {
                OutlinedTextField(
                    value = selectedCategory?.name ?: "Uncategorized",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Category") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryMenuOpen) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                )
                ExposedDropdownMenu(
                    expanded = categoryMenuOpen,
                    onDismissRequest = { categoryMenuOpen = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Uncategorized") },
                        onClick = { selectedCategory = null; categoryMenuOpen = false }
                    )
                    categories.forEach { category ->
                        DropdownMenuItem(
                            text = { Text(category.name) },
                            onClick = { selectedCategory = category; categoryMenuOpen = false }
                        )
                    }
                }
            }

            OutlinedButton(onClick = { showDatePicker = true }, modifier = Modifier.fillMaxWidth()) {
                Text(dateFormat.format(Date(timestamp)))
            }

            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("Note (optional)") },
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = {
                    val amount = amountText.trim().replace(',', '.').toDoubleOrNull()
                    if (amount == null || amount <= 0.0) {
                        amountError = true
                    } else {
                        onSave(
                            TransactionEdits(
                                amount = amount,
                                type = type,
                                merchantName = merchant,
                                description = description,
                                categoryId = selectedCategory?.id,
                                note = note,
                                timestamp = timestamp
                            )
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)
            ) { Text("Save") }
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = timestamp)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        timestamp = PickedDate.applyTo(original = timestamp, pickedUtcMidnight = it)
                    }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Cancel") } }
        ) { DatePicker(state = datePickerState) }
    }
}
