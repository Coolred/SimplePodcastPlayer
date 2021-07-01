package com.example.podcastplayer

import android.app.Application
import android.content.Context
import koinModuleList
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class App : Application() {
    companion object {
        lateinit var context: Context
            private set
    }

    override fun onCreate() {
        super.onCreate()
        context = this

        initKoin()
    }

    private fun initKoin() {
        startKoin {
            androidContext(this@App)
            modules(koinModuleList)
        }
    }
}