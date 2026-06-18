package com.nextgen.expend.data

import com.nextgen.expend.data.model.Category
import com.nextgen.expend.data.model.Transaction
import com.nextgen.expend.data.model.TransactionType

val sampleTransactions = listOf(
    Transaction(1,  "Apple Store",         129.00, TransactionType.EXPENSE, Category.SHOPPING,  "Today",     "02:15 PM"),
    Transaction(2,  "Blue Bottle Coffee",   13.00, TransactionType.EXPENSE, Category.DINING,    "Today",     "09:40 AM"),
    Transaction(3,  "Salary Deposit",     2500.00, TransactionType.INCOME,  Category.INCOME,    "Yesterday", "08:00 AM"),
    Transaction(4,  "Uber Trip",            50.00, TransactionType.EXPENSE, Category.TRANSPORT, "Yesterday", "06:20 PM"),
    Transaction(5,  "Monthly Rent",       1800.00, TransactionType.EXPENSE, Category.HOUSING,   "Jun 15",    "10:00 AM"),
    Transaction(6,  "The Modern Table",    110.50, TransactionType.EXPENSE, Category.DINING,    "Jun 15",    "08:45 PM"),
    Transaction(7,  "Whole Foods",          87.30, TransactionType.EXPENSE, Category.GROCERIES, "Jun 14",    "05:30 PM"),
    Transaction(8,  "Netflix",              15.99, TransactionType.EXPENSE, Category.BILLS,     "Jun 14",    "12:00 AM"),
    Transaction(9,  "Gym Membership",       45.00, TransactionType.EXPENSE, Category.HEALTH,    "Jun 13",    "07:00 AM"),
    Transaction(10, "Spotify",              10.99, TransactionType.EXPENSE, Category.FUN,       "Jun 13",    "12:00 AM"),
    Transaction(11, "Freelance Payment",   850.00, TransactionType.INCOME,  Category.INCOME,    "Jun 12",    "03:00 PM"),
    Transaction(12, "Gas Station",          62.40, TransactionType.EXPENSE, Category.TRANSPORT, "Jun 12",    "08:15 AM"),
    Transaction(13, "H&M",                  78.60, TransactionType.EXPENSE, Category.SHOPPING,  "Jun 11",    "02:00 PM"),
    Transaction(14, "Electric Bill",        95.00, TransactionType.EXPENSE, Category.BILLS,     "Jun 10",    "09:00 AM"),
    Transaction(15, "Starbucks",             6.50, TransactionType.EXPENSE, Category.DINING,    "Jun 10",    "07:30 AM"),
)

/** Spending totals by category for Insights chart (weekly snapshot, USD) */
data class CategoryStat(val category: Category, val amount: Double, val percent: Float)

val weeklyStats = listOf(
    CategoryStat(Category.HOUSING,   1800.00, 49.0f),
    CategoryStat(Category.SHOPPING,   207.60, 23.8f),
    CategoryStat(Category.DINING,     130.00,  8.9f),
    CategoryStat(Category.TRANSPORT,   62.40,  7.5f),
    CategoryStat(Category.BILLS,       25.99,  5.4f),
    CategoryStat(Category.HEALTH,      45.00,  3.1f),
    CategoryStat(Category.FUN,         10.99,  2.3f),
)

/** Daily spending amounts Mon–Sun for the bar chart */
val weeklyBarData = listOf(142f, 67f, 312f, 55f, 210f, 400f, 180f)
val monthlyBarData = listOf(820f, 640f, 1100f, 580f, 950f, 1280f, 490f, 730f, 890f, 660f, 1050f, 380f)
val yearlyBarData  = listOf(3200f, 2900f, 3800f, 3100f, 4200f, 3600f, 2800f, 3900f, 4100f, 3300f, 3700f, 4500f)
