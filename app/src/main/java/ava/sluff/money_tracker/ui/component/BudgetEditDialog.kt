package ava.sluff.money_tracker.ui.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import ava.sluff.money_tracker.data.local.dao.BudgetWithSpending
import ava.sluff.money_tracker.domain.model.Category

@Composable
fun BudgetEditDialog(
    categories: List<Category>,
    budgets: List<BudgetWithSpending>,
    onSave: (Map<Long, Double>) -> Unit,
    onDismiss: () -> Unit
) {
    val limitByCategory = budgets.associate { it.categoryId to it.monthlyLimit }
    val edited = remember {
        mutableStateMapOf<Long, String>().apply {
            categories.forEach { c -> put(c.id, limitByCategory[c.id]?.toString() ?: "") }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Monthly budgets") },
        text = {
            Column(
                modifier = Modifier
                    .heightIn(max = 420.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text("Leave a field empty (or 0) to remove that budget.")
                categories.forEach { category ->
                    OutlinedTextField(
                        value = edited[category.id] ?: "",
                        onValueChange = { edited[category.id] = it },
                        label = { Text(category.name) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val changes = mutableMapOf<Long, Double>()
                categories.forEach { c ->
                    val newVal = edited[c.id]?.trim()?.toDoubleOrNull() ?: 0.0
                    val oldVal = limitByCategory[c.id] ?: 0.0
                    if (newVal != oldVal) changes[c.id] = newVal
                }
                onSave(changes)
            }) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
