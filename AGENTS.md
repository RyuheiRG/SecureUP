# AGENTS.md — SecureUP

> Lee este archivo completo antes de tocar código. Si algo aquí contradice una petición, avísame antes de actuar.
> Responde siempre en español. Los identificadores de código van en inglés.

## 1. Qué es el proyecto

App Android de seguridad digital personal (proyecto universitario, materia Desarrollo de Aplicaciones Móviles, binas).
Tres módulos bajo una sola cuenta:

1. **Vault**: guarda contraseñas cifradas en el dispositivo y verifica si fueron expuestas en brechas (k-anonimato).
2. **Escáner QR**: lee un QR con la cámara, muestra la URL y la verifica contra un servicio de reputación antes de permitir abrirla.
3. **Perfil**: ver cuenta, cerrar sesión, cambiar contraseña maestra, eliminar cuenta.

Pantallas: Login/Registro, Dashboard (Security Score), Vault (lista, detalle, nueva entrada), Escáner, Resultado de escaneo, Historial, Perfil. Navegación inferior: Inicio, Vault, Escáner, Perfil.

## 2. Stack y versiones

- Android Studio Meerkat 2024.3.1. Lenguaje: **Kotlin**. UI: **Jetpack Compose + Material 3** (sin XML ni ViewBinding).
- Paquete base: `com.secureup.app`. Proyecto Gradle con Kotlin DSL (`build.gradle.kts`).
- Verifica `minSdk`, `targetSdk` y versiones reales en `app/build.gradle.kts` y en el version catalog (`gradle/libs.versions.toml`) antes de asumir nada.
- Este repo es **solo la app Android**. El backend (API REST en EC2) es un proyecto aparte que aún no existe aquí.
- Entorno: Windows + PowerShell. Compilar con `.\gradlew assembleDebug`.

## 3. Cómo debes trabajar conmigo (importante)

Soy principiante en Kotlin y Android. Es mi primera app. No puedo detectar por mi cuenta si el código que generas es incorrecto o inseguro, así que:

- **Pasos pequeños**: una funcionalidad o archivo a la vez. No generes la app completa de golpe.
- **Explica cada cambio** en 2-5 líneas: qué hace, por qué así, y qué concepto de Kotlin/Compose usa.
- **Muestra qué archivos creas o modificas** antes de hacerlo. No toques archivos fuera del alcance de la tarea.
- **Pide confirmación antes de agregar una dependencia** nueva. Indica nombre, versión y para qué sirve. No inventes versiones: consúltalas en la documentación oficial o en Maven.
- **No inventes APIs.** Si no estás seguro de que una clase o función existe en la versión usada, consulta la documentación (sección 9). Si no puedes abrir el enlace, dilo.
- Después de cada cambio, ejecuta `.\gradlew assembleDebug` y reporta el resultado real. No digas "debería compilar" sin haberlo comprobado.
- No ejecutes `git commit`, `git push` ni comandos destructivos (borrar carpetas, `git reset --hard`) sin que yo lo pida.
- Si hay varias formas válidas de resolver algo, compáralas en pocas líneas (ventajas/desventajas) y recomienda una.
- Si hay incertidumbre o un supuesto, decláralo explícitamente.

## 4. Arquitectura: MVVM (requisito de la maestra)

Patrón: **View (Compose) → ViewModel → Repository → fuentes de datos (Room local / Retrofit remoto)**.

- Las pantallas Compose **solo pintan estado y emiten eventos**. Sin lógica de negocio, sin red, sin criptografía.
- Cada pantalla tiene su `ViewModel`, que expone estado con `StateFlow` (un `data class` de UiState por pantalla).
- Los `Repository` deciden de dónde vienen los datos. Los ViewModel nunca llaman a Retrofit ni a Room directamente.
- Operaciones de red/BD/cripto en corrutinas (`viewModelScope`, `Dispatchers.IO`), nunca en el hilo principal.
- Una clase, una responsabilidad. Nombres claros, funciones cortas, sin código duplicado ni "God classes".

### Estructura de paquetes objetivo

```
com.secureup.app/
├── ui/
│   ├── screens/        # LoginScreen.kt, DashboardScreen.kt, ...
│   ├── navigation/     # NavHost y rutas
│   └── theme/
├── viewmodel/          # AuthViewModel, VaultViewModel, ScannerViewModel
├── data/
│   ├── remote/         # Retrofit: ApiService, DTOs
│   ├── local/          # Room: entidades, DAOs, base de datos
│   ├── crypto/         # CryptoManager (PBKDF2 + AES-GCM)
│   └── repository/     # AuthRepository, VaultRepository, ScanRepository
└── model/              # modelos de dominio (Usuario, VaultEntry, ScanResult)
```

## 5. Alcance (no te pases de aquí)

Dentro del alcance: registro/login, CRUD de entradas del vault cifradas, verificación de brecha, escaneo de QR + verificación de URL, historial de escaneos, perfil (logout, cambiar contraseña maestra, eliminar cuenta).

**Fuera del alcance** (no lo implementes ni lo propongas como trabajo actual): sincronización en tiempo real entre dispositivos, desbloqueo biométrico, moderación, notificaciones push, iOS, cualquier "mejora" no pedida. El respaldo/restauración del vault es **manual**.

## 6. Reglas de seguridad (no negociables)

1. **Nunca implementes criptografía propia.** Solo primitivas estándar de la plataforma (`javax.crypto`, Android Keystore). Prohibido: AES en modo ECB, claves fijas en el código, IV/nonce reutilizado, `Random` en vez de `SecureRandom`.
2. **La contraseña maestra nunca se guarda ni se envía al servidor**, ni siquiera cifrada. Solo vive en memoria durante la sesión.
3. **La clave de cifrado del vault nunca sale del dispositivo ni se persiste.** Se deriva de la contraseña maestra + `salt_vault` y vive solo en RAM.
4. **Dos derivaciones distintas** a partir de la contraseña maestra, con salts distintos: una para autenticación (`auth_hash`, lo que ve el servidor) y otra para la clave del vault (el servidor nunca la ve).
5. **Nunca loggear** contraseñas, hashes, tokens, claves, ni el contenido de requests de auth/vault (`Log.d` incluido). Revisa que no queden logs sensibles.
6. **Sin secretos en el código ni en el repo.** Ninguna API key en la app (las llamadas a HIBP/Safe Browsing las hace el backend como proxy). Si hace falta configuración local, va en `local.properties` (ya está en `.gitignore`).
7. **Solo HTTPS.** Excepción permitida: tráfico en claro a `10.0.2.2` (emulador → backend local) únicamente en build `debug`, mediante `network_security_config`.
8. Tokens de sesión: **no usar `EncryptedSharedPreferences` ni `androidx.security:security-crypto`** (deprecada). Usar Jetpack DataStore + cifrado AES-GCM con clave en Android Keystore (o Tink, si lo aprobamos).
9. Desactivar capturas de pantalla (`FLAG_SECURE`) en pantallas que muestren contraseñas.
10. Validar y sanear toda entrada de usuario. En el QR, tratar la URL como no confiable: nunca abrirla sin mostrar el veredicto antes.
11. Si detectas una vulnerabilidad o mala práctica en código existente, **repórtala aunque no te lo haya pedido**.

## 7. Diseño criptográfico y de API (referencia)

**Cliente**
- Clave del vault: `PBKDF2WithHmacSHA256(contraseña_maestra, salt_vault, iteraciones, 256 bits)`. Usar el valor de iteraciones recomendado hoy por OWASP (verificar en el enlace de la sección 9; no usar menos de 600 000 sin avisarme).
- Cifrado de cada entrada: `AES-256-GCM`, nonce aleatorio de 12 bytes por operación, guardar `ciphertext + nonce`. GCM autentica: si el descifrado falla, tratarlo como manipulación.
- `auth_hash`: `PBKDF2WithHmacSHA256(contraseña_maestra, salt_auth, ...)`, con `salt_auth` distinto de `salt_vault`.
- Brecha (k-anonimato): `SHA-1(password)` → enviar solo los **5 primeros caracteres** del hash; comparar el resto localmente. La contraseña completa nunca sale del dispositivo.
- Cambiar contraseña maestra: descifrar todo con la clave vieja, re-cifrar todo con la nueva, subir el vault completo en **una sola operación atómica**.

**Endpoints previstos (backend)**
`POST /api/auth/register` · `GET /api/auth/salt?email=` · `POST /api/auth/login` · `GET|POST|PUT|DELETE /api/vault` · `POST /api/breach-check` · `POST /api/qr-check` · `DELETE /api/account`
Autenticación con JWT de vida corta en header `Authorization: Bearer`.

**Tablas (backend)**: `usuarios` (id UUID, email único, auth_hash, salt_auth, salt_vault), `vault_entries` (id, user_id, ciphertext, nonce, timestamps), `scan_history` (id, user_id, url, veredicto, scanned_at). FK con `ON DELETE CASCADE`.

**Local (Room)**: caché del vault ya cifrado. Nunca texto plano en disco.

**Escáner QR**: CameraX + ML Kit Barcode Scanning (procesa en el dispositivo). Pedir permiso `CAMERA` en tiempo de ejecución y explicar por qué se necesita. Liberar cada `ImageProxy` al terminar de analizarlo.

## 8. Orden de implementación (no saltes pasos)

1. Reorganizar paquetes y dejar el tema listo.
2. UI de Login/Registro + `AuthViewModel` con `UiState`, usando un repositorio **falso** (sin red).
3. Navegación (Compose Navigation): Login → Dashboard con barra inferior.
4. `CryptoManager` (PBKDF2 + AES-GCM) **con pruebas unitarias**: cifrar→descifrar, nonce distinto por operación, fallo al alterar un byte del ciphertext.
5. Vault local con Room + pantallas Vault (lista, detalle, nueva entrada).
6. Retrofit + `AuthRepository` real contra el backend.
7. Verificación de brecha.
8. Escáner QR + resultado + historial.
9. Perfil (logout, cambiar contraseña maestra, eliminar cuenta).

Marca aquí el paso en el que vamos: **Paso actual: 1**

## 9. Documentación oficial (consultar antes de usar una API)

- Kotlin: https://kotlinlang.org/docs/home.html
- Compose: https://developer.android.com/develop/ui/compose
- Material 3 en Compose: https://developer.android.com/develop/ui/compose/designsystems/material3
- Compose Navigation: https://developer.android.com/develop/ui/compose/navigation
- Guía de arquitectura / ViewModel: https://developer.android.com/topic/architecture
- Room: https://developer.android.com/training/data-storage/room
- DataStore: https://developer.android.com/topic/libraries/architecture/datastore
- Android Keystore: https://developer.android.com/privacy-and-security/keystore
- Retrofit: https://square.github.io/retrofit/
- CameraX: https://developer.android.com/media/camera/camerax
- ML Kit Barcode Scanning: https://developers.google.com/ml-kit/vision/barcode-scanning/android
- OWASP Password Storage: https://cheatsheetseries.owasp.org/cheatsheets/Password_Storage_Cheat_Sheet.html
- Have I Been Pwned (Pwned Passwords): https://haveibeenpwned.com/API/v3#PwnedPasswords
- Google Safe Browsing: https://developers.google.com/safe-browsing

Si la documentación contradice este archivo, dímelo y propón la corrección.
