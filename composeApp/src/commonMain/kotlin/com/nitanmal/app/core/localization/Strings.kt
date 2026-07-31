package com.nitanmal.app.core.localization

enum class Language {
    SPANISH,
    ENGLISH
}

/**
 * Strings de la app. Solo se incluyen los que usa el flujo actual
 * (login + navbar + inicio + ideas + buzón + ajustes).
 */
data class AppStrings(
    // Navigation bar
    val navHome: String,
    val navIdeas: String,
    val navBuzon: String,
    val navSettings: String,

    // Login
    val appName: String,
    val loginTitle: String,
    val loginName: String,
    val loginEmail: String,
    val loginButton: String,
    val loginGoogleButton: String,

    // Home
    val homeIdeasActivas: String,
    val homePreguntasPendientes: String,
    val homeUltimasIdeas: String,
    val homeUltimasPreguntas: String,
    val homeVerTodas: String,
    val homeSinIdeas: String,
    val homeSinPreguntas: String,

    // Ideas
    val ideasTitle: String,
    val ideasNueva: String,
    val ideasTituloLabel: String,
    val ideasContenidoLabel: String,
    val ideasEtiquetas: String,
    val ideasPublicar: String,
    val ideasVacio: String,
    val ideasComentarios: String,
    val ideasComentar: String,
    val ideasBorrar: String,
    val ideasFijar: String,

    // Buzón
    val buzonTitle: String,
    val buzonTodas: String,
    val buzonPendientes: String,
    val buzonRespondidas: String,
    val buzonVacio: String,
    val buzonMarcarRespondida: String,
    val buzonMarcarPendiente: String,

    // Común
    val reintentar: String,
    val cargando: String,

    // Settings
    val settingsDarkTheme: String,
    val settingsSwitchSession: String,
    val settingsSignOut: String
)

private val SpanishStrings = AppStrings(
    navHome = "Inicio",
    navIdeas = "Ideas",
    navBuzon = "Buzón",
    navSettings = "Ajustes",

    appName = "Nitanmal",
    loginTitle = "Iniciar Sesión",
    loginName = "Nombre",
    loginEmail = "Correo electrónico",
    loginButton = "Iniciar Sesión",
    loginGoogleButton = "Continuar con Google",

    homeIdeasActivas = "Ideas activas",
    homePreguntasPendientes = "Preguntas pendientes",
    homeUltimasIdeas = "Últimas ideas",
    homeUltimasPreguntas = "Último del buzón",
    homeVerTodas = "Ver todas →",
    homeSinIdeas = "Aún no hay ideas. ¡Crea la primera!",
    homeSinPreguntas = "No hay preguntas del público todavía.",

    ideasTitle = "Ideas",
    ideasNueva = "Nueva idea",
    ideasTituloLabel = "Título (opcional)",
    ideasContenidoLabel = "¿Cuál es la idea?",
    ideasEtiquetas = "Etiquetas",
    ideasPublicar = "Publicar",
    ideasVacio = "Aún no hay ideas. ¡Crea la primera!",
    ideasComentarios = "Comentarios",
    ideasComentar = "Escribe un comentario…",
    ideasBorrar = "Borrar idea",
    ideasFijar = "Fijar",

    buzonTitle = "Buzón",
    buzonTodas = "Todas",
    buzonPendientes = "Pendientes",
    buzonRespondidas = "Respondidas",
    buzonVacio = "No hay preguntas en este filtro.",
    buzonMarcarRespondida = "Marcar respondida",
    buzonMarcarPendiente = "Marcar pendiente",

    reintentar = "Reintentar",
    cargando = "Cargando…",

    settingsDarkTheme = "Tema oscuro",
    settingsSwitchSession = "Cambiar de portal",
    settingsSignOut = "Cerrar sesión"
)

private val EnglishStrings = AppStrings(
    navHome = "Home",
    navIdeas = "Ideas",
    navBuzon = "Inbox",
    navSettings = "Settings",

    appName = "Nitanmal",
    loginTitle = "Sign In",
    loginName = "Name",
    loginEmail = "Email",
    loginButton = "Sign In",
    loginGoogleButton = "Continue with Google",

    homeIdeasActivas = "Active ideas",
    homePreguntasPendientes = "Pending questions",
    homeUltimasIdeas = "Latest ideas",
    homeUltimasPreguntas = "Latest from inbox",
    homeVerTodas = "See all →",
    homeSinIdeas = "No ideas yet. Create the first one!",
    homeSinPreguntas = "No audience questions yet.",

    ideasTitle = "Ideas",
    ideasNueva = "New idea",
    ideasTituloLabel = "Title (optional)",
    ideasContenidoLabel = "What's the idea?",
    ideasEtiquetas = "Tags",
    ideasPublicar = "Publish",
    ideasVacio = "No ideas yet. Create the first one!",
    ideasComentarios = "Comments",
    ideasComentar = "Write a comment…",
    ideasBorrar = "Delete idea",
    ideasFijar = "Pin",

    buzonTitle = "Inbox",
    buzonTodas = "All",
    buzonPendientes = "Pending",
    buzonRespondidas = "Answered",
    buzonVacio = "No questions in this filter.",
    buzonMarcarRespondida = "Mark answered",
    buzonMarcarPendiente = "Mark pending",

    reintentar = "Retry",
    cargando = "Loading…",

    settingsDarkTheme = "Dark theme",
    settingsSwitchSession = "Switch workspace",
    settingsSignOut = "Sign out"
)

fun getStrings(language: Language): AppStrings = when (language) {
    Language.SPANISH -> SpanishStrings
    Language.ENGLISH -> EnglishStrings
}
