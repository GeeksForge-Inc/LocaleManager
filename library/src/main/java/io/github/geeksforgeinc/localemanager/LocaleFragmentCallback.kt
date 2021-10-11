package io.github.geeksforgeinc.localemanager

import android.app.Activity
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.lifecycleScope

class LocaleFragmentCallback(private val onLocaleChange: suspend (Activity?) -> Unit) :
    FragmentManager.FragmentLifecycleCallbacks() {
    override fun onFragmentCreated(
        fragmentManager: FragmentManager,
        fragment: Fragment,
        savedInstanceState: Bundle?
    ) {
        super.onFragmentPreCreated(fragmentManager, fragment, savedInstanceState)
        fragment.lifecycleScope.launchWhenCreated {
            onLocaleChange(fragment.activity)
        }
    }

    override fun onFragmentViewCreated(
        fragmentManager: FragmentManager,
        fragment: Fragment,
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onFragmentViewCreated(fragmentManager, fragment, view, savedInstanceState)
        fragment.viewLifecycleOwner.lifecycleScope.launchWhenCreated {
            onLocaleChange(fragment.activity)
        }
    }


    override fun onFragmentResumed(fragmentManager: FragmentManager, fragment: Fragment) {
        super.onFragmentResumed(fragmentManager, fragment)
        fragment.lifecycleScope.launchWhenResumed {
            onLocaleChange(fragment.activity)
        }
    }
}