package com.android.fontfound.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.android.fontfound.MainActivity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsPreferences private constructor(private val dataStore: DataStore<Preferences>) {

    private val themeKey = booleanPreferencesKey("theme_setting")
    private val languageKey = stringPreferencesKey("language_setting")

    // Flow-based theme setting
    fun getThemeSetting(): Flow<Boolean> {
        return dataStore.data.map { preferences ->
            preferences[themeKey] ?: false
        }
    }

    // Save theme setting
    suspend fun saveThemeSetting(isDarkModeActive: Boolean) {
        dataStore.edit { preferences ->
            preferences[themeKey] = isDarkModeActive
        }
    }

    // Fetch initial theme setting synchronously
    fun getInitialThemeSetting(): Boolean {
        return runBlocking {
            dataStore.data.firstOrNull()?.get(themeKey) ?: false
        }
    }

    // Flow-based language setting
    fun getLanguageSetting(): Flow<String> {
        return dataStore.data.map { preferences ->
            preferences[languageKey] ?: "English"
        }
    }

    fun getInitialLanguageSetting(): String {
        return runBlocking {
            dataStore.data.firstOrNull()?.get(languageKey) ?: "English"
        }
    }

    suspend fun saveLanguageSetting(language: String) {
        dataStore.edit { preferences ->
            preferences[languageKey] = language
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: SettingsPreferences? = null

        fun getInstance(dataStore: DataStore<Preferences>): SettingsPreferences {
            return INSTANCE ?: synchronized(this) {
                val instance = SettingsPreferences(dataStore)
                INSTANCE = instance
                instance
            }
        }
    }
}