package com.nextgen.expend.data.model

data class Transaction(
    val id: Int,
    val merchant: String,
    val amount: Double,
    val type: TransactionType,
    val category: Category,
    val dateLabel: String,   // e.g. "Today", "Yesterday", "Jun 15"
    val timeLabel: String,   // e.g. "02:15 PM"
)
