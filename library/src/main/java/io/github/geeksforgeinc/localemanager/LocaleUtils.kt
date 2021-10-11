package io.github.geeksforgeinc.localemanager

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import android.os.LocaleList
import android.util.Log
import androidx.annotation.RequiresApi
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.LinkedHashSet


object LocaleUtils {
    private const val TAG = "LocaleUtils"
    fun applyLocale(context: Context, languageCode: String) {
        val locale: Locale = getLocaleFromLanguageCode(languageCode)
        updateResources(context, locale)
        if (context !is Application) {
            val applicationContext: Context = context.applicationContext
            updateResources(applicationContext, locale)
        }
    }

    private fun getLocaleFromLanguageCode(languageCode: String): Locale {
        return Locale(languageCode)
    }

    private fun updateResources(context: Context, locale: Locale) {
        Locale.setDefault(locale)
        val resources: Resources = context.resources
        val configuration = Configuration(resources.configuration)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            setLocaleForApiNougatAndAbove(configuration, locale)
        } else {
            configuration.setLocale(locale)
        }
        resources.updateConfiguration(configuration, resources.displayMetrics)
    }

    private fun getCurrentLocale(context: Context): Locale {
        val configuration = context.resources.configuration
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            configuration.locales.get(0)
        } else {
            configuration.locale
        }
    }

    fun isSelectedLocaleValid(context: Context, selectedLanguageCode: String): Boolean {
        return getCurrentLocale(context) != getLocaleFromLanguageCode(selectedLanguageCode)
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    fun setLocaleForApiNougatAndAbove(configuration: Configuration, locale: Locale) {
        val linkedHashSet: LinkedHashSet<Locale> = LinkedHashSet()
        // bring the target locale to the front of the set
        linkedHashSet.add(locale)
        val defaultLocaleList = LocaleList.getDefault()
        val localeList: ArrayList<Locale> = ArrayList()
        for (i in 0 until defaultLocaleList.size()) {
            localeList.add(defaultLocaleList[i])
        }
        // append other locales supported by the user
        linkedHashSet.addAll(localeList)
        val locales: Array<Locale?> = arrayOfNulls(linkedHashSet.size)
        val finalLocaleList = LocaleList(*linkedHashSet.toArray(locales))
        configuration.setLocales(finalLocaleList)
    }

    fun resetActivityTitle(activity: Activity?) {
        try {
            val info = activity?.packageManager?.getActivityInfo(
                activity.componentName,
                PackageManager.GET_META_DATA
            )
            info?.labelRes?.let { it ->
                if (it != 0) {
                    activity.setTitle(it)
                }
            }
        } catch (exception: PackageManager.NameNotFoundException) {
            exception.message?.let { Log.e(TAG, it) }
        }
    }
}
