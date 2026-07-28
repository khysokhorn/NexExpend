package com.nextgen.expend.data.model

data class Transaction(
    val id: Int,
    val merchant: String,
    val amount: Double,
    val type: TransactionType,
    val category: Category,
    val dateLabel: String,   // e.g. "Today", "Yesterday", "Jun 15"
    val timeLabel: String,   // e.g. "02:15 PM"
    val currency: String = "USD",   // "USD" or "KHR"
)

/** "$" for USD, "៛" for KHR (falls back to the raw code for anything else). */
fun Transaction.currencySymbol(): String = when (currency) {
    "KHR" -> "៛"
    "USD" -> "$"
    else -> currency
}

/** Formats [amount] with [currency]'s symbol — KHR has no minor unit, USD has 2 decimals. */
fun Transaction.formattedAmount(): String = when (currency) {
    "KHR" -> "${currencySymbol()}${"%,.0f".format(amount)}"
    else -> "${currencySymbol()}${"%.2f".format(amount)}"
}
