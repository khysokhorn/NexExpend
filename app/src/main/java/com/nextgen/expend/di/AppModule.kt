package com.nextgen.expend.di

import com.nextgen.expend.data.TransactionRepository
import com.nextgen.expend.ui.viewmodel.TransactionViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single { TransactionRepository(get(), get()) }
    viewModel { TransactionViewModel(get()) }
}
