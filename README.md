# Nitanmal

App móvil **Kotlin Multiplatform (Android + iOS)** con **Compose Multiplatform** para el proyecto web Nitan Mal. Réplica de la arquitectura de `uminer` (Umine Smart ChangeLog): Clean Architecture + atomic design.

## Estado actual

- ✅ Splash → **Login con Google** (Firebase Auth) → `/verify` → selección de portal → Dashboard
- ✅ Dashboard con **navbar inferior** (Inicio / Cursos / Notas / Ajustes) igual a uminer
- ✅ Ajustes: tema oscuro, cambiar de portal, cerrar sesión
- ⏳ Inicio / Cursos / Notas son placeholders

## Flujo de autenticación

1. Google Sign-In (nativo Android / iOS) → Firebase Auth → `firebaseIdToken`
2. `POST https://safe-api-auth-customers.umine.com/prod/verify` con header `Bearer <token>` y body `{core_key, client_key, env}`
3. La respuesta trae `user_id`, `email`, `role`, `roles[]`. Con más de un rol se muestra el selector de portal.

## ⚠️ Datos pendientes (reemplazar antes de que funcione el login real)

| Qué | Dónde | Valor actual |
|---|---|---|
| `google-services.json` (app Android `com.nitanmal.app` en Firebase) | `composeApp/google-services.json` | placeholder |
| `GoogleService-Info.plist` (app iOS `com.nitanmal.app`) | `iosApp/iosApp/GoogleService-Info.plist` | placeholder |
| Web Client ID (OAuth tipo "Web" del proyecto Firebase) | `local.properties` → `WEB_CLIENT_ID` | placeholder |
| Admin API Key del core auth | `local.properties` → `ADMIN_API_KEY` | placeholder |
| iOS CLIENT_ID | `composeApp/src/iosMain/.../core/config/SecureConfig.ios.kt` | placeholder |
| URL scheme (REVERSED_CLIENT_ID) | `iosApp/iosApp/Info.plist` | placeholder |
| `core_key` / `client_key` / `env` asignados a nitanmal | `commonMain/.../data/remote/AuthConfig.kt` | `smart-customers` / `NITANMAL` / `prod` (provisionales) |

Además, en Firebase Console:
1. Crear el proyecto y habilitar **Google** en Authentication → Sign-in method.
2. Registrar la app Android `com.nitanmal.app` **con el SHA-1 de debug**:
   ```
   keytool -list -v -alias androiddebugkey -keystore ~/.android/debug.keystore -storepass android -keypass android
   ```
3. Registrar la app iOS `com.nitanmal.app`.
4. Confirmar con el equipo de `umine-core-auth-customers` que el backend `/verify` acepta tokens del proyecto Firebase de nitanmal (distinto `aud`/`iss` que `umine-prod-clientes`) y que existe el `client_key` de nitanmal con usuarios de prueba.

## Build

```bash
# Android
./gradlew :composeApp:assembleDebug

# iOS (Kotlin framework)
./gradlew :composeApp:compileKotlinIosSimulatorArm64
# App completa: abrir iosApp/iosApp.xcodeproj en Xcode
# (requiere agregar Firebase iOS SDK + GoogleSignIn via SPM y setear TEAM_ID en Configuration/Config.xcconfig)
```

## Estructura

```
composeApp/src/commonMain/kotlin/com/nitanmal/app/
├── core/            # Logger, SecureConfig, localización (ES/EN)
├── theme/           # NitanmalTheme (paleta cyan/purple/green), tipografía
├── data/            # AuthApiService (/verify), AuthRepositoryImpl, modelos
├── domain/          # PlatformAuth (expect), User, use cases, interfaces
└── presentation/
    ├── viewmodel/   # AuthViewModel
    ├── navigation/  # Routes
    └── ui/
        ├── components/       # NitanmalNavigationBar, atoms, molecules, organisms
        ├── icons/            # AppIcons (ImageVector multiplataforma)
        └── screens/          # Splash, Login, ClientSelection, MainDashboard, Home, Settings
```

Plataformas:
- `androidMain`: `MainActivity`, `AndroidPlatformAuth` (Google Sign-In + Firebase Android SDK)
- `iosMain` + `iosApp/`: `IosPlatformAuth` ↔ `FirebaseAuthDelegate.swift` (bridge Kotlin↔Swift)
