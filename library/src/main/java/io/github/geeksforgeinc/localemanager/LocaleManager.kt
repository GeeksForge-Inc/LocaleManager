package io.github.geeksforgeinc.localemanager

import android.app.Activity
import android.app.Application
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import io.github.geeksforgeinc.localemanager.LocaleUtils.isSelectedLocaleValid
import io.github.geeksforgeinc.localemanager.LocaleUtils.resetActivityTitle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

private const val LOCALE_DATA_STORE_FILE_NAME = "locale_prefs"
private const val SELECTED_LOCALE_KEY = "selected_locale"

class LocaleManager private constructor(private val applicationContext: Application) {
    private companion object : SingletonHolder<LocaleManager, Application>(::LocaleManager)

    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = LOCALE_DATA_STORE_FILE_NAME)
    private var defaultLanguage = "en"

    init {
        registerLifecycleCallbacks()
    }


    private fun registerLifecycleCallbacks() {
        applicationContext.registerActivityLifecycleCallbacks(LocaleActivityCallback(this::applyForActivity) {
            applyForApplication(applicationContext)
        })
        applicationContext.registerComponentCallbacks(LocaleComponentCallback {
            applyForApplication(applicationContext)
        })
    }

    private suspend fun applyForApplication(context: Context) {
        withContext(Dispatchers.Main) {
            val selectedLanguageValue: String =
                withContext(Dispatchers.Default) { readLanguageFromDataStore(context) }
            if (isSelectedLocaleValid(applicationContext, selectedLanguageValue)) {
                LocaleUtils.applyLocale(context, selectedLanguageValue)
            }
        }
    }

    private suspend fun applyForActivity(activity: Activity?) {
        activity?.let { context ->
            withContext(Dispatchers.Main) {
                val applicationContext = activity.applicationContext
                val selectedLanguageValue: String =
                    withContext(Dispatchers.IO) { readLanguageFromDataStore(applicationContext) }
                if (isSelectedLocaleValid(applicationContext, selectedLanguageValue)) {
                    LocaleUtils.applyLocale(context, selectedLanguageValue)
                    resetActivityTitle(activity)
                }
            }
        }
    }

    private suspend fun saveLanguageInDataStore(context: Context, selectedLanguageValue: String) {
        val selectedLanguageKey = stringPreferencesKey(SELECTED_LOCALE_KEY)
        context.dataStore.edit { localeStore ->
            localeStore[selectedLanguageKey] = selectedLanguageValue
        }
    }

    suspend fun saveLanguageAndApply(activity: Activity?, selectedLanguageValue: String) {
        activity?.let {
            withContext(Dispatchers.Main) {
                val applicationContext = activity.applicationContext
                if (isSelectedLocaleValid(applicationContext, selectedLanguageValue)) {
                    LocaleUtils.applyLocale(activity, selectedLanguageValue)
                    resetActivityTitle(activity)
                    withContext(Dispatchers.IO) {
                        saveLanguageInDataStore(applicationContext, selectedLanguageValue)
                    }
                }
            }
        }
    }

    suspend fun saveLanguageAndApply(
        applicationContext: Application,
        selectedLanguageValue: String
    ) {
        withContext(Dispatchers.Main) {
            if (isSelectedLocaleValid(applicationContext, selectedLanguageValue)) {
                LocaleUtils.applyLocale(applicationContext, selectedLanguageValue)
                withContext(Dispatchers.IO) {
                    saveLanguageInDataStore(applicationContext, selectedLanguageValue)
                }
            }
        }
    }

    private suspend fun readLanguageFromDataStore(context: Context): String {
        val selectedLanguageKey = stringPreferencesKey(SELECTED_LOCALE_KEY)
        return context.dataStore.data
            .map { localeStore ->
                // No type safety.
                localeStore[selectedLanguageKey] ?: defaultLanguage
            }.first()
    }


    class Builder(
        private var applicationContext: Application,
        private var defaultLanguageOption: String? = null,
    ) {

        fun setDefaultLanguageCode(value: String) = apply { this.defaultLanguageOption = value }

        fun build(): LocaleManager {
            val localeManager = LocaleManager.getInstance(applicationContext)
            defaultLanguageOption?.let {
                localeManager.defaultLanguage = it
            }
            return localeManager
        }
    }

}


