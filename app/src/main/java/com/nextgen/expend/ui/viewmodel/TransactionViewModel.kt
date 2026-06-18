package com.nextgen.expend.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nextgen.expend.data.TransactionRepository
import com.nextgen.expend.data.model.Category
import com.nextgen.expend.data.model.Transaction
import com.nextgen.expend.data.model.TransactionType
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TransactionViewModel(
    private val repository: TransactionRepository
) : ViewModel() {

    val transactions: StateFlow<List<Transaction>> = repository.getTransactionsFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun addExpense(amount: Double, category: Category, note: String) {
        viewModelScope.launch {
            val dateFormat = SimpleDateFormat("MMM dd", Locale.US)
            val timeFormat = SimpleDateFormat("hh:mm a", Locale.US)
            val now = Date()

            val newTx = Transaction(
                id = (System.currentTimeMillis() % Int.MAX_VALUE).toInt(),
                merchant = note.ifBlank { category.label },
                amount = amount,
                type = TransactionType.EXPENSE,
                category = category,
                dateLabel = "Today", // Simple categorization for instant viewing
                timeLabel = timeFormat.format(now)
            )
            repository.addTransaction(newTx)
        }
    }
}
