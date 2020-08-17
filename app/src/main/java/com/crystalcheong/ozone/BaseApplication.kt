package com.crystalcheong.ozone

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

//TODO: Tell the app to use Dagger-Hilt as the dependency injection tool
@HiltAndroidApp
class BaseApplication: Application() {

    override fun onCreate() {
        super.onCreate()

        //TODO: Setup Timber logging library
        Timber.plant(Timber.DebugTree())
    }
}