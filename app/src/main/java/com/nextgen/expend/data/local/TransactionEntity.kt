package com.nextgen.expend.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.nextgen.expend.data.model.Category
import com.nextgen.expend.data.model.Transaction
import com.nextgen.expend.data.model.TransactionType

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey val id: Int,
    val merchant: String,
    val amount: Double,
    val type: String,
    val category: String,
    val dateLabel: String,
    val timeLabel: String,
    val currency: String = "USD",
    val createdAt: Long = System.currentTimeMillis()
)

fun Transaction.toEntity(): TransactionEntity = TransactionEntity(
    id = id,
    merchant = merchant,
    amount = amount,
    type = type.name,
    category = category.name,
    dateLabel = dateLabel,
    timeLabel = timeLabel,
    currency = currency
)

fun TransactionEntity.toDomain(): Transaction = Transaction(
    id = id,
    merchant = merchant,
    amount = amount,
    type = TransactionType.valueOf(type),
    category = Category.valueOf(category),
    dateLabel = dateLabel,
    timeLabel = timeLabel,
    currency = currency
)
