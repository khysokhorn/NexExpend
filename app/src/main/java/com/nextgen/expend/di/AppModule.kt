package com.nextgen.expend.di

import androidx.room.Room
import com.nextgen.expend.data.TransactionRepository
import com.nextgen.expend.data.local.AppDatabase
import com.nextgen.expend.network.localllm.LocalLlmService
import com.nextgen.expend.ui.viewmodel.TransactionViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val appModule = module {
    single {
        Room.databaseBuilder(androidContext(), AppDatabase::class.java, "nexexpend.db")
            .build()
    }
    single { get<AppDatabase>().transactionDao() }
    single { LocalLlmService(androidContext()) }
    single { TransactionRepository(get()) }
    viewModelOf(::TransactionViewModel)
}

