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
object BuzonRoute

@Serializable
object SettingsRoute

@Serializable
data class IdeaDetailRoute(val notaId: String)
