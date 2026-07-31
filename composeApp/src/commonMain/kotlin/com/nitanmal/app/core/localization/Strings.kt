package com.nitanmal.app.core.localization

enum class Language {
    SPANISH,
    ENGLISH
}

/**
 * Strings de la app. Solo se incluyen los que usa el flujo actual
 * (login + navbar + ajustes). Agregar aquí los nuevos al crecer la app.
 */
data class AppStrings(
    // Navigation bar
    val navHome: String,
    val navCourses: String,
    val navGrades: String,
    val navSettings: String,

    // Login
    val appName: String,
    val loginTitle: String,
    val loginName: String,
    val loginEmail: String,
    val loginButton: String,
    val loginGoogleButton: String,

    // Settings
    val settingsDarkTheme: String,
    val settingsSwitchSession: String,
    val settingsSignOut: String
)

private val SpanishStrings = AppStrings(
    navHome = "Inicio",
    navCourses = "Cursos",
    navGrades = "Notas",
    navSettings = "Ajustes",

    appName = "Nitanmal",
    loginTitle = "Iniciar Sesión",
    loginName = "Nombre",
    loginEmail = "Correo electrónico",
    loginButton = "Iniciar Sesión",
    loginGoogleButton = "Continuar con Google",

    settingsDarkTheme = "Tema oscuro",
    settingsSwitchSession = "Cambiar de portal",
    settingsSignOut = "Cerrar sesión"
)

private val EnglishStrings = AppStrings(
    navHome = "Home",
    navCourses = "Courses",
    navGrades = "Grades",
    navSettings = "Settings",

    appName = "Nitanmal",
    loginTitle = "Sign In",
    loginName = "Name",
    loginEmail = "Email",
    loginButton = "Sign In",
    loginGoogleButton = "Continue with Google",

    settingsDarkTheme = "Dark theme",
    settingsSwitchSession = "Switch workspace",
    settingsSignOut = "Sign out"
)

fun getStrings(language: Language): AppStrings = when (language) {
    Language.SPANISH -> SpanishStrings
    Language.ENGLISH -> EnglishStrings
}
