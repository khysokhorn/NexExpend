package com.nextgen.expend.di

import com.nextgen.expend.data.TransactionRepository
import com.nextgen.expend.network.localllm.LocalLlmService
import com.nextgen.expend.ui.viewmodel.TransactionViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val appModule = module {
    single { LocalLlmService(androidContext()) }
    single { TransactionRepository(get(), get()) }
    viewModelOf(::TransactionViewModel)
}

