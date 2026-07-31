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
    val navProduccion: String,
    val navReuniones: String,
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
    val ideasEnlaces: String,
    val ideasAudios: String,
    val ideasConvertir: String,
    val ideasConvertirConfirm: String,

    // Notificaciones
    val notifTitle: String,
    val notifVacio: String,

    // Producción
    val prodTitle: String,
    val prodNuevo: String,
    val prodTituloLabel: String,
    val prodIdeaLabel: String,
    val prodCrear: String,
    val prodVacio: String,
    val prodEtapas: String,
    val prodGuardar: String,
    val prodResponsable: String,
    val prodFecha: String,
    val prodSubtareas: String,
    val prodAgregarSubtarea: String,
    val prodArchivoWeb: String,
    val prodBorrar: String,

    // Reuniones
    val reuTitle: String,
    val reuNueva: String,
    val reuProximas: String,
    val reuPasadas: String,
    val reuVacio: String,
    val reuTituloLabel: String,
    val reuFechaLabel: String,
    val reuHoraLabel: String,
    val reuLugarLabel: String,
    val reuDescripcionLabel: String,
    val reuCrear: String,

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
    navProduccion = "Producción",
    navReuniones = "Reuniones",
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
    ideasEnlaces = "Enlaces",
    ideasAudios = "Audios",
    ideasConvertir = "Convertir a episodio",
    ideasConvertirConfirm = "Se creará un episodio en producción con esta idea y la idea quedará como Convertida.",

    notifTitle = "Notificaciones",
    notifVacio = "No tienes notificaciones.",

    prodTitle = "Producción",
    prodNuevo = "Nuevo episodio",
    prodTituloLabel = "Título del episodio",
    prodIdeaLabel = "Brief inicial (tema)",
    prodCrear = "Crear episodio",
    prodVacio = "Aún no hay episodios en producción.",
    prodEtapas = "Etapas",
    prodGuardar = "Guardar etapa",
    prodResponsable = "Responsable",
    prodFecha = "Fecha (AAAA-MM-DD)",
    prodSubtareas = "Subtareas",
    prodAgregarSubtarea = "Agregar subtarea…",
    prodArchivoWeb = "El archivo se sube desde el web",
    prodBorrar = "Borrar episodio",

    reuTitle = "Reuniones",
    reuNueva = "Nueva reunión",
    reuProximas = "Próximas",
    reuPasadas = "Pasadas",
    reuVacio = "No hay reuniones agendadas.",
    reuTituloLabel = "Título",
    reuFechaLabel = "Fecha (AAAA-MM-DD)",
    reuHoraLabel = "Hora (HH:MM)",
    reuLugarLabel = "Lugar (opcional)",
    reuDescripcionLabel = "Descripción (opcional)",
    reuCrear = "Agendar",

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
    navProduccion = "Production",
    navReuniones = "Meetings",
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
    ideasEnlaces = "Links",
    ideasAudios = "Audios",
    ideasConvertir = "Convert to episode",
    ideasConvertirConfirm = "A production episode will be created from this idea and the idea will be marked as Converted.",

    notifTitle = "Notifications",
    notifVacio = "You have no notifications.",

    prodTitle = "Production",
    prodNuevo = "New episode",
    prodTituloLabel = "Episode title",
    prodIdeaLabel = "Initial brief (topic)",
    prodCrear = "Create episode",
    prodVacio = "No episodes in production yet.",
    prodEtapas = "Stages",
    prodGuardar = "Save stage",
    prodResponsable = "Owner",
    prodFecha = "Date (YYYY-MM-DD)",
    prodSubtareas = "Subtasks",
    prodAgregarSubtarea = "Add subtask…",
    prodArchivoWeb = "Files are uploaded from the web app",
    prodBorrar = "Delete episode",

    reuTitle = "Meetings",
    reuNueva = "New meeting",
    reuProximas = "Upcoming",
    reuPasadas = "Past",
    reuVacio = "No meetings scheduled.",
    reuTituloLabel = "Title",
    reuFechaLabel = "Date (YYYY-MM-DD)",
    reuHoraLabel = "Time (HH:MM)",
    reuLugarLabel = "Place (optional)",
    reuDescripcionLabel = "Description (optional)",
    reuCrear = "Schedule",

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
