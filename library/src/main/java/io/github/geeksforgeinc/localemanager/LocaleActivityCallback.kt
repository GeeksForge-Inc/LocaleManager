package io.github.geeksforgeinc.localemanager

import android.app.Activity
import android.app.Application
import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope

class LocaleActivityCallback(
    private val onLocaleChangeInActivity: suspend (Activity?) -> Unit,
    private val onLocaleChangeInComponent: suspend () -> Unit
) : Application.ActivityLifecycleCallbacks {
    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        (activity as LifecycleOwner).lifecycleScope.launchWhenCreated {
            onLocaleChangeInActivity(activity)
        }

        activity.registerComponentCallbacks(LocaleComponentCallback(onLocaleChangeInComponent))

        (activity as? FragmentActivity)
            ?.supportFragmentManager
            ?.registerFragmentLifecycleCallbacks(
                LocaleFragmentCallback(onLocaleChangeInActivity),
                true
            )
    }

    override fun onActivityStarted(activity: Activity) {

    }

    override fun onActivityResumed(activity: Activity) {
        (activity as LifecycleOwner).lifecycleScope.launchWhenResumed {
            onLocaleChangeInActivity(activity)
        }
    }

    override fun onActivityPaused(activity: Activity) {

    }

    override fun onActivityStopped(activity: Activity) {

    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {

    }

    override fun onActivityDestroyed(activity: Activity) {

    }

}