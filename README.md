# Nitanmal

App móvil **Kotlin Multiplatform (Android + iOS)** con **Compose Multiplatform** del proyecto **Nitan Mal**. Réplica de la arquitectura de `uminer`: Clean Architecture + atomic design, pero contra la infraestructura propia de nitanmal.

## Estado actual

- ✅ **Login funcionando end-to-end en Android**: Splash → Google Sign-In → Firebase (`nitanmal-a75de`) → `GET /me` del backend propio → Dashboard
- ✅ Dashboard con **navbar inferior** (Inicio / Cursos / Notas / Ajustes) igual a uminer
- ✅ Ajustes: tema oscuro, cerrar sesión
- ⏳ Inicio / Cursos / Notas son placeholders
- ⏳ iOS: código listo, falta el `GoogleService-Info.plist` real y configurar Xcode (SPM: Firebase + GoogleSignIn, TEAM_ID)

## Infraestructura

| Componente | Valor |
|---|---|
| Firebase | `nitanmal-a75de` (nº 1008342407186) |
| Backend | `https://uhryf0x2jb.execute-api.us-east-2.amazonaws.com` (stack `nitalmal-backend`) |
| AWS | cuenta `970335222766`, región `us-east-2`, perfil CLI `nitalmal` |
| Repo web | `~/Documents/nitalmal` (React + Vite, mismo backend) |
| Paquete/bundle | `com.nitanmal.app` |

## Flujo de autenticación

1. Google Sign-In (nativo) → Firebase Auth → `firebaseIdToken`
2. `GET {API_URL}/me` con `Authorization: Bearer <token>`
   - El **JWT authorizer** de API Gateway valida el token contra `https://securetoken.google.com/nitanmal-a75de`
   - El handler hace upsert del usuario en DynamoDB (`nitalmal-usuarios`), registra `lastLogin` y devuelve `{user: {userId, email, name, role, plan, apodo, photoURL, ...}}`
   - Roles: `miembro` (default) | `admin` | `superadmin` (bootstrap por `SUPERADMIN_EMAIL`)
3. La app mapea el perfil a `User` y navega al Dashboard

## Configuración local (no versionada)

`local.properties`:
```properties
sdk.dir=...
WEB_CLIENT_ID=1008342407186-tbfjsd2p6rj4gkr09o7v6nfh741rnphv.apps.googleusercontent.com
```

El SHA-1 de debug debe estar registrado en Firebase Console (app Android `com.nitanmal.app`).

## Pendientes iOS

1. Descargar `GoogleService-Info.plist` de la app iOS `com.nitanmal.app` en Firebase Console → reemplazar `iosApp/iosApp/GoogleService-Info.plist`
2. Poner el `REVERSED_CLIENT_ID` real como URL scheme en `iosApp/iosApp/Info.plist`
3. Poner el `CLIENT_ID` de iOS en `composeApp/src/iosMain/.../SecureConfig.ios.kt`
4. En Xcode: agregar Firebase iOS SDK + GoogleSignIn vía SPM y setear `TEAM_ID` en `iosApp/Configuration/Config.xcconfig`

## Build

```bash
# Android
./gradlew :composeApp:assembleDebug

# iOS (Kotlin framework)
./gradlew :composeApp:compileKotlinIosSimulatorArm64
# App completa: abrir iosApp/iosApp.xcodeproj en Xcode
```

## Estructura

```
composeApp/src/commonMain/kotlin/com/nitanmal/app/
├── core/            # Logger, SecureConfig, localización (ES/EN)
├── theme/           # NitanmalTheme (paleta cyan/purple/green), tipografía
├── data/            # AuthApiService (GET /me), AuthRepositoryImpl, MeResponse
├── domain/          # PlatformAuth (expect), User, use cases, interfaces
└── presentation/
    ├── viewmodel/   # AuthViewModel
    ├── navigation/  # Routes
    └── ui/
        ├── components/       # NitanmalNavigationBar, atoms, molecules, organisms
        ├── icons/            # AppIcons (ImageVector multiplataforma)
        └── screens/          # Splash, Login, MainDashboard, Home, Settings
```

Plataformas:
- `androidMain`: `MainActivity`, `AndroidPlatformAuth` (Google Sign-In + Firebase Android SDK)
- `iosMain` + `iosApp/`: `IosPlatformAuth` ↔ `FirebaseAuthDelegate.swift` (bridge Kotlin↔Swift)
