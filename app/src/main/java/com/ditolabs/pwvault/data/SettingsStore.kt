package com.ditolabs.pwvault.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "pwvault_settings")

enum class ThemeMode { SYSTEM, LIGHT, DARK }
enum class AppLanguage { ID, EN }

class SettingsStore(private val context: Context) {
    private val languageKey = stringPreferencesKey("language")
    private val themeKey = stringPreferencesKey("theme_mode")

    val language: Flow<AppLanguage> = context.dataStore.data.map { prefs ->
        AppLanguage.valueOf(prefs[languageKey] ?: AppLanguage.ID.name)
    }
    val themeMode: Flow<ThemeMode> = context.dataStore.data.map { prefs ->
        ThemeMode.valueOf(prefs[themeKey] ?: ThemeMode.SYSTEM.name)
    }

    suspend fun setLanguage(lang: AppLanguage) {
        context.dataStore.edit { it[languageKey] = lang.name }
    }
    suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { it[themeKey] = mode.name }
    }
}
