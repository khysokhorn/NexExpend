package com.nextgen.expend

import android.app.Application
import com.nextgen.expend.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class NexExpendApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@NexExpendApplication)
            modules(appModule)
        }
    }
}
