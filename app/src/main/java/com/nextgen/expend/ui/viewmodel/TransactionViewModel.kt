package com.nextgen.expend.ui.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nextgen.expend.data.TransactionRepository
import com.nextgen.expend.data.model.Category
import com.nextgen.expend.data.model.Transaction
import com.nextgen.expend.data.model.TransactionType
import com.nextgen.expend.network.localllm.LocalLlmService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TransactionViewModel(
    private val repository: TransactionRepository,
    private val localLlmService: LocalLlmService
) : ViewModel() {

    val transactions: StateFlow<List<Transaction>> = repository.getTransactionsFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _smartTip = MutableStateFlow("Preparing Local AI...")
    val smartTip: StateFlow<String> = _smartTip.asStateFlow()

    val llmStatus: StateFlow<LocalLlmService.Status> = localLlmService.status
    val modelDownloadProgress: StateFlow<Float?> = localLlmService.downloadProgress

    init {
        viewModelScope.launch {
            localLlmService.initialize()
            generateSmartTip()
        }
    }

    fun generateSmartTip() {
        viewModelScope.launch {
            val tip = localLlmService.generateFinancialTip(transactions.value)
            _smartTip.value = tip
        }
    }

    fun loadModelFromUri(uri: Uri) {
        viewModelScope.launch {
            localLlmService.loadModelFromUri(uri)
            generateSmartTip()
        }
    }

    fun downloadModelFromUrl(url: String) {
        viewModelScope.launch {
            localLlmService.downloadModelFromUrl(url)
            generateSmartTip()
        }
    }

    fun addExpense(amount: Double, category: Category, note: String, currency: String = "USD") {
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
                timeLabel = timeFormat.format(now),
                currency = currency
            )
            repository.addTransaction(newTx)
            generateSmartTip()
        }
    }

    override fun onCleared() {
        super.onCleared()
        localLlmService.release()
    }
}
