package io.github.geeksforgeinc.localemanager

import android.content.ComponentCallbacks2
import android.content.res.Configuration
import kotlinx.coroutines.*

class LocaleComponentCallback(private val onLocaleChange: suspend () -> Unit) :
    ComponentCallbacks2 {
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    override fun onConfigurationChanged(newConfig: Configuration) {
        applicationScope.launch {
            onLocaleChange()
        }
    }

    override fun onLowMemory() {
        applicationScope.cancel()
    }

    override fun onTrimMemory(level: Int) {

    }
}