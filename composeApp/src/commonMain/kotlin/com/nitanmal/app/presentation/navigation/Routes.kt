package com.nitanmal.app.presentation.navigation

import kotlinx.serialization.Serializable

// App-level routes
@Serializable
object LoginRoute

@Serializable
object ClientSelectionRoute

@Serializable
object DashboardRoute

// Dashboard-level routes (nested)
@Serializable
object HomeRoute

@Serializable
object IdeasRoute

@Serializable
object ProduccionRoute

@Serializable
object PlanificadorRoute

/** Agenda agrupa Reuniones / Buzón / Métricas (sub-pestañas, como el web). */
@Serializable
data class AgendaRoute(val tab: String = "reuniones")

@Serializable
object SettingsRoute

@Serializable
data class IdeaDetailRoute(val notaId: String)

@Serializable
data class EpisodioDetailRoute(val episodioId: String)

// ── Rutas del modo Fan ──

@Serializable
object FanInicioRoute

@Serializable
object EnVivoRoute

@Serializable
object EpisodiosFanRoute

@Serializable
data class EpisodioFanDetailRoute(val episodioId: String)

@Serializable
object MiZonaRoute

@Serializable
object CuentaRoute
