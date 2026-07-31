package com.nitanmal.app.core.localization

import androidx.compose.runtime.*

/**
 * LocaleManager - Gestión de idioma de la aplicación
 * Permite cambiar el idioma en tiempo de ejecución
 */
class LocaleManager {
    private val _currentLanguage = mutableStateOf(Language.SPANISH)
    val currentLanguage: State<Language> = _currentLanguage

    val strings: AppStrings
        get() = getStrings(_currentLanguage.value)

    fun setLanguage(language: Language) {
        _currentLanguage.value = language
    }

    fun toggleLanguage() {
        _currentLanguage.value = when (_currentLanguage.value) {
            Language.SPANISH -> Language.ENGLISH
            Language.ENGLISH -> Language.SPANISH
        }
    }
}

// CompositionLocal para acceder al LocaleManager en toda la app
val LocalLocaleManager = compositionLocalOf<LocaleManager> {
    error("LocaleManager not provided")
}

@Composable
fun ProvideLocaleManager(
    localeManager: LocaleManager = remember { LocaleManager() },
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(LocalLocaleManager provides localeManager) {
        content()
    }
}

// Helper para acceder a los strings fácilmente
@Composable
fun rememberStrings(): AppStrings {
    val localeManager = LocalLocaleManager.current
    val language by localeManager.currentLanguage
    return remember(language) { getStrings(language) }
}
