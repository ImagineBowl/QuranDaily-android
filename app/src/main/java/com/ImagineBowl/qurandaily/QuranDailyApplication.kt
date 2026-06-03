package com.imaginebowl.qurandaily

import android.app.Application
import com.imaginebowl.qurandaily.di.AppContainer
import com.imaginebowl.qurandaily.di.AppContainerOwner

class QuranDailyApplication : Application(), AppContainerOwner {
    override lateinit var appContainer: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        appContainer = AppContainer(this)
    }

    override fun onTerminate() {
        appContainer.tipJarService.destroy()
        super.onTerminate()
    }
}
