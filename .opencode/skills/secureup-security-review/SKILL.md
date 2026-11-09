---
name: secureup-security-review
description: Revisión de seguridad de SecureUP basada en OWASP MASVS. Úsala cuando una tarea toque criptografía, autenticación, almacenamiento, red, portapapeles, lectura de QR o URLs, logs, AndroidManifest o dependencias, y antes de dar por terminada cualquier tarea que lo haga.
---

# Revisión de seguridad — SecureUP

Fuente de verdad: las reglas de AGENTS.md §6. Esta skill es el procedimiento para verificarlas. Si algo aquí contradice AGENTS.md, repórtalo como hallazgo; no elijas una de las dos.

Rol: **revisor, no autor**. No modifiques código durante la revisión salvo que se te pida. No inventes hallazgos: si no puedes verificar algo, márcalo como "NO VERIFICADO".

## Procedimiento

1. Lista los archivos cambiados (`git status` y `git diff --name-only`).
2. Recorre solo las secciones de la lista que apliquen a esos archivos.
3. Ejecuta las comprobaciones automáticas del final y reporta la salida real.
4. Entrega un informe: tabla con severidad (Crítica / Alta / Media / Baja), archivo:línea, qué ocurre, por qué importa y corrección propuesta. Termina con una sección "Lo que NO pude verificar".

## Lista de revisión

### Criptografía (MASVS-CRYPTO)
- Solo `javax.crypto` y Android Keystore. Nada de implementaciones propias ni librerías sin aprobar.
- AES-GCM: nonce de 12 bytes de `SecureRandom` por cada cifrado, nunca reutilizado con la misma clave. Un fallo de la etiqueta GCM es manipulación: abortar sin devolver datos parciales.
- `PBKDF2WithHmacSHA256`: iteraciones según el valor vigente de OWASP (verificar en el cheat sheet; no menos de 600 000 sin aprobación); salt aleatorio y único por usuario. `auth_hash` y la clave del vault se separan con **HKDF-Expand** (`HMAC-SHA256(masterKey, etiqueta || 0x01)`), nunca con dos PBKDF2 ni reciclando el mismo salt entre propósitos.
- Ninguna clave, salt, IV ni secreto fijo en código, recursos o BuildConfig.
- Prohibido: `Random`, MD5, ECB, DES. SHA-1 solo para el k-anonimato de Have I Been Pwned.
- Comparar hashes con `MessageDigest.isEqual` (tiempo constante).
- Manejar contraseñas como `CharArray`/`ByteArray` cuando sea posible y sobrescribirlas al terminar. Limitación a declarar: la JVM no garantiza el borrado total de memoria.

### Autenticación y sesión (MASVS-AUTH)
- Mensaje de error de login genérico y sin enumeración de usuarios (mismo mensaje y mismo flujo si el correo no existe).
- La contraseña maestra nunca se persiste ni se envía. La clave del vault vive solo en RAM y se descarta al cerrar sesión o tras inactividad.
- Token de sesión cifrado con una clave de Android Keystore (DataStore + AES-GCM). Prohibido `androidx.security:security-crypto` (deprecada) y SharedPreferences en claro.
- Política de intentos fallidos del desbloqueo: la que decida DESIGN.md (pendiente 3). No la inventes.

### Almacenamiento (MASVS-STORAGE)
- Nada sensible en claro en disco: ni Room, ni DataStore, ni archivos, ni caché, ni logs.
- `AndroidManifest.xml`: `android:allowBackup="false"` (o reglas de exclusión en `dataExtractionRules` y `fullBackupContent`) para que el respaldo automático de Android no copie datos del vault.
- Portapapeles: marcar el contenido como sensible (`ClipDescription.EXTRA_IS_SENSITIVE`, API 33+) y limpiarlo a los 30 s.

### Red (MASVS-NETWORK)
- Solo HTTPS. `android:usesCleartextTraffic="false"`; la excepción de `10.0.2.2` solo en debug mediante `network_security_config`.
- Ningún `TrustManager` que acepte todo, ningún `HostnameVerifier` permisivo, ningún error SSL ignorado.
- La app no contiene API keys de terceros: HIBP y Safe Browsing se consumen a través del backend.
- No enviar la contraseña completa ni el hash completo a ningún servicio: solo el prefijo de 5 caracteres del SHA-1.

### Plataforma e interfaz (MASVS-PLATFORM)
- Componentes exportados mínimos: solo la actividad launcher. Todo `android:exported="true"` nuevo requiere justificación.
- `FLAG_SECURE` en las pantallas que muestran secretos (bóveda, detalle, nueva credencial) mediante un helper reutilizable.
- Nada sensible en Intents, extras, notificaciones, deep links ni `savedInstanceState`.
- Permisos mínimos: `CAMERA` (en tiempo de ejecución) e `INTERNET`.

### QR y URLs (entrada no confiable)
- Parsear con `Uri`; abrir solo esquemas `http` y `https`. Rechazar `intent:`, `javascript:`, `file:`, `content:`, `tel:`, etc.
- Mostrar la URL completa antes de abrir y nunca abrir automáticamente. Limitar la longitud. Normalizar punycode/IDN antes de aplicar heurísticas.
- El veredicto "seguro" significa "sin amenazas conocidas", no "verificado".

### Código y dependencias (MASVS-CODE)
- Sin `Log.*` con datos sensibles (contraseñas, correos, tokens, resultados de autenticación, URLs escaneadas).
- Dependencias nuevas solo con aprobación explícita, con versión fija desde `libs.versions.toml` y sin versiones dinámicas (`+`, `latest`).
- Build release: `debuggable=false`. Hoy `isMinifyEnabled=false`; activarlo (R8) requiere revisar reglas de keep y no es una medida de seguridad por sí mismo.
- Validar y acotar toda entrada externa.

### Backend (cuando exista)
- Consultas parametrizadas, nunca SQL concatenado. Límite de peticiones en login y breach-check. bcrypt o Argon2id sobre `auth_hash`. JWT de vida corta. No registrar cuerpos de peticiones de auth ni vault. Errores sin detalles internos.

### Fuera de alcance (no implementar ni proponer sin que se pida)
Detección de root, anti-debugging, ofuscación avanzada y certificate pinning (este último se evalúa aparte como trade-off).

## Comprobaciones automáticas

- `.\gradlew lintDebug` y revisar las advertencias de seguridad.
- `.\gradlew testDebugUnitTest`.
- `git grep -nE "Log\.(d|i|v|w|e)\("` sobre el código cambiado.
- `git grep -nEi "password|secret|apikey|token"` para revisar usos sospechosos.
- Revisar `AndroidManifest.xml`: exported, allowBackup, permisos, cleartext.

## Referencias
- OWASP MASVS: https://mas.owasp.org/MASVS/
- OWASP MASTG (guía de pruebas): https://mas.owasp.org/MASTG/
- OWASP Password Storage Cheat Sheet: https://cheatsheetseries.owasp.org/cheatsheets/Password_Storage_Cheat_Sheet.html
