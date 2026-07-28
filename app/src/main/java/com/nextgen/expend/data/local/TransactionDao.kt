package com.nextgen.expend.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {

    @Query("SELECT * FROM transactions ORDER BY createdAt DESC")
    fun getAllFlow(): Flow<List<TransactionEntity>>

    @Insert
    suspend fun insert(transaction: TransactionEntity)
}
