package com.nextgen.expend.dialog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

data class ExpenseInput(
    val amount: Double = 0.0,
    val category: String = "",
    val imageUri: String? = null,
    val note: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Composable
fun QuickExpenseTopDialog(
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit,
    onSave: (ExpenseInput) -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.20f)
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Quick Expense")

            OutlinedTextField(
                value = "",
                onValueChange = {},
                label = { Text("Amount") }
            )

            OutlinedTextField(
                value = "",
                onValueChange = {},
                label = { Text("Category") }
            )

            Button(onClick = {
                onSave(ExpenseInput())
            }) {
                Text("Save")
            }
        }
    }
}


@Composable
fun QuickExpenseTopPopup(
    onSave: (ExpenseInput) -> Unit,
    onDismiss: () -> Unit
) {
    var amount by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Transparent)
            .padding(16.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White.copy(alpha = 0.95f)
            )
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Quick Expense")

                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Amount") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Category") },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }

                    Button(
                        enabled = amount.toDoubleOrNull() != null && category.isNotBlank(),
                        onClick = {
                            val amountVal = amount.toDoubleOrNull() ?: 0.0
                            onSave(
                                ExpenseInput(
                                    amount = amountVal,
                                    category = category
                                )
                            )
                        }
                    ) {
                        Text("Save")
                    }
                }
            }
        }
    }
}