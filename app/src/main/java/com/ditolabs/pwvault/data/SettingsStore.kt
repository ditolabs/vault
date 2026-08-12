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
enum class AutoLockDelay(val minutes: Int) { IMMEDIATE(0), ONE(1), FIVE(5), NEVER(-1) }

class SettingsStore(private val context: Context) {
    private val languageKey = stringPreferencesKey("language")
    private val themeKey = stringPreferencesKey("theme_mode")
    private val autoLockKey = stringPreferencesKey("auto_lock_delay")

    val language: Flow<AppLanguage> = context.dataStore.data.map { prefs ->
        AppLanguage.valueOf(prefs[languageKey] ?: AppLanguage.ID.name)
    }
    val themeMode: Flow<ThemeMode> = context.dataStore.data.map { prefs ->
        ThemeMode.valueOf(prefs[themeKey] ?: ThemeMode.SYSTEM.name)
    }
    val autoLockDelay: Flow<AutoLockDelay> = context.dataStore.data.map { prefs ->
        AutoLockDelay.valueOf(prefs[autoLockKey] ?: AutoLockDelay.FIVE.name)
    }

    suspend fun setLanguage(lang: AppLanguage) {
        context.dataStore.edit { it[languageKey] = lang.name }
    }
    suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { it[themeKey] = mode.name }
    }
    suspend fun setAutoLockDelay(delay: AutoLockDelay) {
        context.dataStore.edit { it[autoLockKey] = delay.name }
    }
}
