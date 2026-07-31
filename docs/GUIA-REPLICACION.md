# Guía de la app Nitanmal — arquitectura, endpoints y cómo replicarla

> **Propósito**: documentar cómo funciona esta app de punta a punta para poder
> **replicar su base en otros proyectos** (otra marca, otro backend, mismo esqueleto).
> La app es el cliente móvil del proyecto web Nitan Mal: mismas APIs, mismos datos, en vivo.

---

## 1. Stack

| Capa | Tecnología |
|---|---|
| Lenguaje | Kotlin Multiplatform (Android + iOS) |
| UI | Compose Multiplatform + Material 3 |
| Navegación | `org.jetbrains.androidx.navigation:navigation-compose` (rutas `@Serializable`) |
| HTTP | Ktor 3 (OkHttp en Android, Darwin en iOS) |
| JSON | kotlinx-serialization (`ignoreUnknownKeys = true`) |
| Imágenes | Coil 3 |
| Audio (reproducción) | ExoPlayer/media3 (Android) · AVPlayer (iOS) |
| Audio (grabación) | MediaRecorder AAC/m4a (Android) · AVAudioRecorder (iOS) |
| Auth | Google Sign-In nativo → Firebase Auth |
| Backend | API Gateway (HTTP API) + Lambda + DynamoDB + S3 (SAM, repo del web) |
| Build | Gradle 8.14, AGP 8.11, Kotlin 2.3, compileSdk 36 / minSdk 24 |

## 2. Arquitectura (Clean Architecture)

```
composeApp/src/commonMain/kotlin/com/nitanmal/app/
├── core/
│   ├── config/SecureConfig.kt        # expect: secretos por plataforma
│   ├── logger/Logger.kt              # expect: Log.d / println
│   ├── localization/                 # AppStrings ES/EN + LocaleManager (CompositionLocal)
│   └── util/                         # formatFecha, todayIsoDate (expect)
├── data/
│   ├── remote/
│   │   ├── ApiClient.kt              # HttpClient singleton (timeouts, logging, JSON)
│   │   ├── AuthConfig.kt             # ★ URL del backend — ÚNICO punto a cambiar
│   │   ├── AuthApiService.kt         # GET /me
│   │   ├── TeamApiService.kt         # resto de endpoints + upload S3
│   │   └── model/                    # DTOs @Serializable (requests/responses)
│   └── repository/                   # Impl de repositorios (token + apiService)
├── domain/
│   ├── model/                        # User, Nota, Pregunta, Episodio, Reunion, Notificacion
│   ├── auth/PlatformAuth.kt          # expect vía interface + CompositionLocal
│   ├── repository/                   # Interfaces (AuthRepository, TeamRepository)
│   ├── usecase/                      # SignInWithGoogle, SelectClient, SignOut
│   └── util/                         # AudioPlayer, AudioRecorder (expect)
└── presentation/
    ├── viewmodel/                    # AuthViewModel, IdeasViewModel, BuzonViewModel,
    │                                 # ProduccionViewModel, ReunionesViewModel, NotificacionesViewModel
    ├── navigation/Routes.kt          # rutas @Serializable
    └── ui/
        ├── components/               # NitanmalNavigationBar, atoms, molecules
        ├── icons/                    # AppIcons (ImageVector a mano — sin libs de iconos)
        └── screens/                  # Splash, Login, MainDashboard, Home, Ideas(+Detail),
                                      # Produccion(+Detail), Reuniones, Buzon, Settings
```

**Regla de dependencias**: `presentation → domain ← data`. Los ViewModels solo
conocen interfaces de `domain/repository`; las impl. de `data/` piden el token a
`PlatformAuth` en cada llamada.

### Plataformas (expect/actual)

| Común (`expect`) | Android (`actual`) | iOS (`actual`) |
|---|---|---|
| `PlatformAuth` | GoogleSignIn + Firebase SDK (`AndroidPlatformAuth`) | Bridge Kotlin↔Swift (`IosAuthBridge` ← `FirebaseAuthDelegate.swift`) |
| `SecureConfig` | `BuildConfig` (desde `local.properties`) | `Info.plist` / hardcode |
| `Logger` | `android.util.Log` | `println` |
| `AudioPlayer` | ExoPlayer (media3) | AVPlayer |
| `AudioRecorder` | MediaRecorder (AAC/m4a) | AVAudioRecorder |
| `todayIsoDate()` | `java.util.Calendar` | `NSDateFormatter` |

Android además usa `AppContextHolder` (context para ExoPlayer/MediaRecorder) y
`PermissionBridge` (permiso de micrófono vía launcher registrado en `MainActivity`).

## 3. Flujo de autenticación

```
┌──────────┐   1. Google Sign-In    ┌──────────┐  2. credential   ┌───────────────┐
│ LoginScreen├──────────────────────▶│ Google    ├────────────────▶│ Firebase Auth │
└──────────┘   (picker nativo)      └──────────┘                  │ nitanmal-a75de│
                                                                   └──────┬────────┘
                                                          3. firebaseIdToken (JWT)
                                                                          ▼
┌─────────────────────────  4. GET {API_URL}/me  ──────────────────────────────┐
│ Authorization: Bearer <idToken>                                              │
│ · API Gateway (JWT authorizer) valida iss=securetoken.google.com/<proyecto>  │
│ · Lambda hace upsert del usuario en DynamoDB y devuelve el perfil            │
└──────────────────────────────────────────────────────────────────────────────┘
                                  5. {user:{userId,email,name,role,plan,...}}
                                                  ▼
                                        MainDashboardScreen
```

- **No hay backend de auth propio**: el *JWT authorizer* de API Gateway hace la
  validación; `GET /me` crea/actualiza el usuario (rol default `miembro`,
  bootstrap de `superadmin` por email vía env `SUPERADMIN_EMAIL`).
- **Roles**: `miembro` → solo puede enviar preguntas · `participante`/`admin`/`superadmin`
  → todo el contenido de equipo (el backend lo valida con `canParticipate`).
- El token se re-obtiene en **cada request** con `platformAuth.getFirebaseIdToken()`
  (Firebase lo cachea y refresca solo).

## 4. Infraestructura de este proyecto

| Componente | Valor |
|---|---|
| Firebase | proyecto `nitanmal-a75de` (nº 1008342407186), proveedor Google habilitado |
| API | `https://uhryf0x2jb.execute-api.us-east-2.amazonaws.com` (HTTP API, stage `$default`) |
| Stack AWS | `nitalmal-backend` (SAM) — Lambda `nitalmal-api` (node22) — cuenta `970335222766`, `us-east-2` |
| DynamoDB | `nitalmal-usuarios`, `-notas`, `-preguntas`, `-produccion`, `-reuniones`, `-notificaciones`, … |
| S3 | `nitalmal-archivos-<cuenta>` (media de notas/producción), `nitalmal-avatars-<cuenta>` |
| Repo web | `~/Documents/nitalmal` (React+Vite; backend en `backend/src/index.mjs` — **fuente de verdad del contrato**) |
| Config Android | `composeApp/google-services.json` + `local.properties → WEB_CLIENT_ID` |

## 5. Endpoints que consume la app

Todas las llamadas llevan `Authorization: Bearer <firebaseIdToken>`.
Errores: `{ "error": "mensaje" }` (+ `faltantes: []` en el gate de producción).

### Perfil
| Método | Ruta | Descripción |
|---|---|---|
| GET | `/me` | Upsert + perfil: `{user:{userId,email,name,role,plan,apodo,photoURL,…}}` |
| PUT | `/me` | Actualiza perfil (no usado aún en la app) |

### Ideas (el backend las llama "notas")
| Método | Ruta | Body → Respuesta |
|---|---|---|
| GET | `/notas` | → `{notas:[Nota]}` (audios/imágenes con `url` firmada 1 h) |
| POST | `/notas` | `{titulo?,contenido?,etiquetas?,audios?:[{key,nombre}]}` → `{nota}` |
| POST | `/notas-upload` | `{filename,contentType}` → `{uploadUrl,key}` (PUT del binario a `uploadUrl`) |
| POST | `/notas/{id}/reaccion` | `{emoji}` (👍🔥❤️😂💡, alterna la del usuario) → `{nota}` |
| POST | `/notas/{id}/comentario` | `{texto}` → `{nota}` |
| DELETE | `/notas/{id}/comentario/{cid}` | (autor o admin) → `{nota}` |
| PUT | `/notas/{id}/estado` | `{estado}` ∈ nueva/revision/aprobada/descartada/convertida → `{nota}` |
| PUT | `/notas/{id}/pin` | alterna fijada → `{nota}` |
| POST | `/notas/{id}/convertir` | crea episodio de producción → `{nota, episodioId}` |
| DELETE | `/notas/{id}` | (autor o admin; borra media de S3) |

`Nota`: `{id, titulo, contenido, audios[], imagenes[], enlaces[], etiquetas[],
estado, pinned, reacciones{userId:emoji}, comentarios[], responsable(Id),
fechaObjetivo, episodioId, createdByName/UserId, createdAt}`

### Buzón (preguntas del público)
| Método | Ruta | Notas |
|---|---|---|
| POST | `/preguntas` | `{contenido}` — cualquier usuario autenticado |
| GET | `/preguntas` | solo participantes → `{preguntas:[{id,contenido,fromName,fromEmail,answered,createdAt}]}` |
| PUT | `/preguntas/{id}` | `{answered:bool}` |
| DELETE | `/preguntas/{id}` | |

### Producción (episodios con 6 etapas tipadas)
Etapas: `idea → guion → grabacion → edicion → programado → publicado`.
Estados por etapa: `pendiente / en_progreso / en_revision / aprobada`.
Cada etapa tiene **plantilla de campos tipados** (`texto, texto-largo, fecha,
numero, select, checkbox, url, file`) — espejo en `domain/model/Produccion.kt`
(`Plantillas`) y fuente de verdad en el backend (`STAGE_TEMPLATES`).

| Método | Ruta | Notas |
|---|---|---|
| GET | `/produccion` | → `{produccion:[Episodio]}` (campos `file` con `archivoUrl` firmada) |
| POST | `/produccion` | `{titulo, idea?}` → `{item}` (brief → `stages.idea.values.tema`) |
| PUT | `/produccion/{id}` | `{stage, stageData:{estado?,responsable?,responsableId?,fecha?,subtareas?,values?}}` → `{item}`. **Gate DoD**: aprobar exige los campos `required` completos (400 + `faltantes[]`). Notifica asignación y handoff |
| POST | `/produccion-upload` | igual que notas-upload (archivos de etapas — solo web por ahora) |
| DELETE | `/produccion/{id}` | |
| GET | `/equipo` | → `{equipo:[{userId,nombre}]}` (para asignar responsables) |
| GET | `/plantillas` | plantillas de etapas (la app usa su espejo local) |

### Reuniones
| Método | Ruta | Notas |
|---|---|---|
| GET | `/reuniones` | → `{reuniones:[{id,date,time,title,description,lugar,createdBy…}]}` orden `date+time` |
| POST | `/reuniones` | `{date:"AAAA-MM-DD", time:"HH:MM", title, description?, lugar?}` |
| DELETE | `/reuniones/{id}` | autor o admin |

### Notificaciones (in-app)
| Método | Ruta | Notas |
|---|---|---|
| GET | `/notificaciones` | últimas 40 del usuario: `{notificaciones:[{id,texto,leida,createdAt,episodioId?,stage?}]}` |
| POST | `/notificaciones/leer` | marca todas como leídas |

Se generan al: comentar una idea ajena, asignar idea/etapa, y handoff al aprobar etapa.

### Subida de media (patrón general)
```
1. POST /notas-upload  {filename, contentType}      → {uploadUrl, key}
2. PUT  {uploadUrl}    (binario, mismo Content-Type) → 200      ← sin Authorization
3. POST /notas         {..., audios:[{key, nombre}]}
```
La app graba **AAC/.m4a** (reproducible en Android, iOS y navegador).
El web graba webm/opus — por eso la reproducción usa **ExoPlayer**, no MediaPlayer.

## 6. UI / Navegación

- **Navbar (5 pestañas)**: Inicio · Ideas · Producción · Reuniones · Buzón.
  Ajustes vive en el ⚙️ del Inicio. Los **detalles marcan su pestaña** en la navbar
  (IdeaDetail → Ideas, EpisodioDetail → Producción) y al abrirlos desde Inicio se
  navega primero a la pestaña para que "atrás" caiga en la lista.
- **Inicio**: contadores 2×2 (ideas activas, preguntas pendientes, episodios en
  curso, próximas reuniones) + secciones En producción / Próxima reunión /
  Últimas ideas / Último del buzón + campana de notificaciones con badge.
- **ViewModels compartidos**: se crean una vez en `MainDashboardScreen` y se pasan
  a las pantallas (Inicio reutiliza los mismos datos que las pestañas).
- **Patrón de estado**: `StateFlow<UiState>` con `isLoading/error/info`;
  mutaciones optimistas donde es barato (buzón) y con respuesta del backend donde
  importa la verdad (ideas/producción reemplazan el ítem con lo que devuelve la API).

## 7. Cómo replicar esta base para OTRO proyecto

Checklist ordenado (≈1 día la primera vez):

1. **Clonar el repo** y renombrar:
   - `settings.gradle.kts → rootProject.name`
   - `composeApp/build.gradle.kts → namespace/applicationId` (ej. `com.miapp.app`)
   - Paquete Kotlin `com/nitanmal/app → com/miapp/app` (mover carpetas + sed imports)
   - `iosApp/Configuration/Config.xcconfig → PRODUCT_NAME, PRODUCT_BUNDLE_IDENTIFIER`
   - `strings.xml → app_name`, textos de Splash/Login, `theme/Theme.kt` (paleta)
2. **Firebase** (proyecto nuevo):
   - Authentication → habilitar **Google**
   - Registrar app Android con el applicationId + **SHA-1 debug**
     (`keytool -list -v -alias androiddebugkey -keystore ~/.android/debug.keystore -storepass android`)
   - Descargar `google-services.json` → `composeApp/`
   - Registrar app iOS → `GoogleService-Info.plist` → `iosApp/iosApp/`
     (+ `REVERSED_CLIENT_ID` como URL scheme en `Info.plist`, `CLIENT_ID` en `SecureConfig.ios.kt`)
   - `local.properties → WEB_CLIENT_ID` = OAuth client **tipo Web** del proyecto
3. **Backend**: replicar el patrón del stack `nitalmal-backend` (SAM):
   - HTTP API con **JWT authorizer**: issuer `https://securetoken.google.com/<proyecto>`,
     audience `<proyecto>` → cero código de validación de tokens
   - Lambda con `GET /me` (upsert + roles) — es lo mínimo para que el login funcione
   - `AuthConfig.kt → API_URL` = la URL del nuevo API ← **único cambio en el cliente**
4. **Probar**: `./gradlew :composeApp:assembleDebug`, instalar, login, ver `GET /me → 200`
5. **Dominio**: reemplazar los módulos de negocio (ideas/buzón/producción/reuniones)
   por los del nuevo proyecto siguiendo el patrón:
   `model @Serializable → ApiService (método por endpoint) → Repository → ViewModel → Screen`
6. **iOS** (cuando toque): abrir `iosApp.xcodeproj`, agregar Firebase + GoogleSignIn
   por SPM, setear `TEAM_ID`

### Gotchas aprendidos (no tropezar dos veces)

- **`google-services.json` placeholder** compila bien — la app rompe solo al hacer login.
- El **SHA-1 sin registrar** da error 10 `DEVELOPER_ERROR` en Google Sign-In.
- El token de Firebase lleva `aud=<proyecto>`: si el backend valida con el Admin SDK
  de **otro** proyecto → 401 `incorrect "aud" claim`. Authorizer y app deben apuntar
  al **mismo** proyecto Firebase.
- **MediaPlayer no reproduce webm/opus** por streaming → usar ExoPlayer/media3.
- ExoPlayer **sin AudioAttributes + audio focus** queda mudo después de usar el
  micrófono. Configurar `USAGE_MEDIA` + `handleAudioFocus=true`.
- El **audio del emulador** (QEMU→macOS) se atasca: probar sonido en dispositivo real.
- Un `adb reboot` puede **corromper el AVD** (la activity "no existe" aunque el APK
  está bien) → `emulator -wipe-data -no-snapshot-load`.
- `kotlinx-datetime` 0.5 no resuelve `Clock.System` en iOS con Kotlin 2.3 →
  `todayIsoDate()` expect/actual con Calendar/NSDateFormatter.
- Ktor `DefaultRequest` pone `Content-Type: application/json`: en el PUT a S3
  hay que **sobreescribir** el Content-Type con el declarado al firmar.
- En Compose Multiplatform usar **ImageVectors propios** (AppIcons) — no depender
  de material-icons-extended en común.
- Los detalles de navegación: mapear rutas de detalle a su pestaña en la navbar
  (`currentRoute.startsWith(DetailRoute::class.qualifiedName)`).

## 8. Comandos útiles

```bash
# Build + instalar en emulador/dispositivo
./gradlew :composeApp:assembleDebug
adb install -r composeApp/build/outputs/apk/debug/composeApp-debug.apk

# Compilar target iOS (sin Xcode)
./gradlew :composeApp:compileKotlinIosSimulatorArm64

# Logs de la app (red y auth)
adb logcat | grep -E "TeamApi|AuthApi|NitanmalAuth|NitanmalAudio"

# Inspeccionar el backend (perfil AWS "nitalmal")
aws dynamodb scan --table-name nitalmal-notas --profile nitalmal --region us-east-2
aws lambda invoke --function-name nitalmal-api --payload fileb://event.json out.json \
  --profile nitalmal --region us-east-2   # event con requestContext.authorizer.jwt.claims
```
