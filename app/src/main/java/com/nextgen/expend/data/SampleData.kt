package com.nextgen.expend.data

import com.nextgen.expend.data.model.Category
import com.nextgen.expend.data.model.Transaction
import com.nextgen.expend.data.model.TransactionType

val sampleTransactions = listOf<Transaction>()

/** Spending totals by category for Insights chart (weekly snapshot, USD) */
data class CategoryStat(val category: Category, val amount: Double, val percent: Float)

val weeklyStats = listOf<CategoryStat>()

/** Daily spending amounts Mon–Sun for the bar chart */
val weeklyBarData = listOf<Float>(0f)
val monthlyBarData = listOf<Float>(0f)
val yearlyBarData  = listOf<Float>(0f)
