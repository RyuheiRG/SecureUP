# DESIGN.md — SecureUP

Fuente de verdad visual de la app. Extraído de capturas del archivo de Figma "SecureUP" (páginas Design System, Wireframes - Lote A y Lote B).
Lo que no está aquí o está marcado **POR CONFIRMAR** no se inventa: se pregunta.

## 1. Reglas generales

- La app es **solo tema oscuro**. Sin tema claro ni color dinámico.
- Ningún hex, tamaño de fuente ni dp "suelto" en pantallas. Todo sale de `ui/theme` (`SecureUpColors`, `Spacing`, `Radius`, `MaterialTheme.typography`).
- Las pantallas **no definen estilos propios**: componen los componentes de `ui/components/`. Si falta un componente, se crea ahí primero.
- Textos de interfaz en **español**, en `res/values/strings.xml` (nada de `Text("...")` con literales). Identificadores de código en inglés ("Bóveda" en la UI = `Vault` en código).
- Barras del sistema: la app es oscura, así que los íconos de la barra de estado deben ser claros aunque el sistema esté en tema claro (verificar en la documentación de `enableEdgeToEdge` / `SystemBarStyle`).
- Estados `pressed` y `disabled` de los componentes: usar los de Material 3 por defecto, salvo que se lean valores exactos en Figma.

## 2. Tokens

### Color (confirmado en Figma)

| Token | Hex | Uso |
|---|---|---|
| Background | `#0F1115` | Fondo de pantalla |
| Surface | `#181B22` | Barras superior/inferior, tarjetas, campos |
| SurfaceVariant | `#222733` | Tarjetas internas, campos, botón secundario |
| Primary | `#4F8CFF` | Acción principal, elemento activo, enlaces |
| OnPrimary | `#0A1020` | Texto sobre Primary, Success, Warning y Danger |
| Success | `#3DDC97` | Seguro |
| Warning | `#FFB84D` | Sospechoso / débil |
| Danger | `#FF5C6C` | Peligroso / error / acción destructiva |
| TextPrimary | `#E8EAF0` | Texto principal |
| TextSecondary | `#9AA3B5` | Texto secundario, etiquetas, placeholders |
| Outline | `#4D5973` | Borde del botón secundario (en Figma es un hex literal, **sin variable**) |

### Tipografía: Inter (confirmado en Figma)

| Estilo Figma | Slot Material 3 | Tamaño / peso | Line-height | Letter-spacing |
|---|---|---|---|---|
| Display | `headlineLarge` | 28 / Bold | 36 | -0.5 |
| Title | `titleLarge` | 20 / SemiBold | 28 | -0.2 |
| Body | `bodyLarge` | 16 / Regular | 24 | 0 |
| Caption | `bodySmall` | 12 / Regular | 16 | 0.2 |
| Etiqueta de campo | `labelMedium` | 12 / Medium | 16 | — |
| (botones y campos) | `labelLarge` | 16 / SemiBold — **POR CONFIRMAR** (peso) | 20 | — |

Line-heights y letter-spacing: **confirmados en Figma** (Typography, nodo `4:72`).
`labelLarge` (16 / 20) es el override visto en botones y campos del login; el peso SemiBold sigue **POR CONFIRMAR**.
Archivos requeridos: `res/font/inter_regular.ttf`, `inter_medium.ttf`, `inter_semibold.ttf`, `inter_bold.ttf`.
- **Licencia:** Inter se distribuye bajo **SIL Open Font License 1.1 (SIL OFL)**. El texto de la licencia está en `licenses/OFL-Inter.txt`.

### Espaciado (confirmado): 4, 8, 12, 16, 24, 32 dp → `Spacing.Xs/S/M/L/Xl/Xxl`

**Regla de ajuste:** los valores de espaciado fuera de la escala se ajustan al token más cercano y cada ajuste se registra aquí.

| Valor en Figma | Token aplicado | Valor del token |
|---|---|---|
| gap 36 | `Spacing.Xxl` | 32 |
| gap 28 | `Spacing.Xl` | 24 |
| padding vertical 40 (registro) | `Spacing.Xxl` | 32 |
| gap 20 (registro) | `Spacing.Xl` | 24 |
| padding vertical 40 (tarjeta del score, `62:221`) | `Spacing.Xxl` | 32 |
| gap 20 (etiqueta → anillo del score) | `Spacing.Xl` | 24 |

### Márgenes y padding (regla global)

Ninguna pantalla define márgenes propios. **El margen lateral depende del tipo de pantalla**, según Figma:

| Contexto | Valor | Token | Origen en Figma |
|---|---|---|---|
| Pantallas de autenticación (Login, Registro) | 24 | `Spacing.Xl` | `auth02-login` (`87:204`): contenido en x=24, ancho 312 |
| Pantallas con tarjeta y `SecureUpTopBar` (Inicio, Bóveda, Escáner, Perfil) | 16 | `Spacing.L` | `A09a - Dashboard Home` (`49:283`): tarjetas en x=16, ancho 328 |
| Separación entre bloques de una pantalla | 16 | `Spacing.L` | |
| Padding interno de tarjetas de estadísticas (Contraseñas, Brechas) | 16 | `Spacing.L` | `62:230` |
| Padding interno de la tarjeta destacada (Security Score) | 24 horizontal / 32 vertical | `Spacing.Xl` / `Spacing.Xxl` | `62:221` (40 vertical ajustado) |
| Separación entre botones de una fila | 12 | `Spacing.M` | `62:238` |

### Radios (`Dimens.kt`): controles (botones y campos) **12 dp** (confirmado en Figma); tarjetas y avisos **16 dp** (confirmado en el aviso Danger `87:197`); badges en píldora.

## 3. Componentes (`ui/components/`)

| Componente | Variantes / estados vistos en Figma | Notas |
|---|---|---|
| `SecureUpButton` | Primary (relleno azul), Secondary (fondo SurfaceVariant, borde `Outline` `#4D5973`), Danger (relleno rojo); cada uno con 3 estados: normal, pressed, disabled | Texto oscuro (`OnPrimary`) sobre Primary y Danger. Secondary: borde `Outline` y estados por **opacidad** → Default 100 %, Pressed 80 %, Disabled 40 % (confirmado en Figma). Primary y Danger: **CONFIRMADOS** con los mismos estados por opacidad → Pressed 80 %, Disabled 40 %. Ancho completo en la mayoría de pantallas. |
| `SecureUpTextField` | Normal, enfocado (borde Primary), error (borde Danger + mensaje con ícono de alerta), con ícono de ojo (mostrar/ocultar) | Etiqueta arriba del campo (TextSecondary, 12). Campos de contraseña con `PasswordVisualTransformation` y toggle del ojo (área táctil 48 dp, ícono visual 20 dp). Espécimen detallado abajo. |
| `InfoBanner` | Warning (ámbar) y Danger (rojo) | Aviso con `ic_alert_triangle`; el tint cambia por variante (`Warning` / `Danger`). Contenedor, radio, padding y texto distintos por variante. Espécimen abajo. |
| `CredentialCard` | Avatar circular (placeholder), título (sitio), subtítulo (usuario), punto de estado a la derecha | Punto verde / ámbar / rojo según estado de la credencial (ver pendiente 2). |
| `StatusBadge` | Seguro (verde), Sospechoso (ámbar), Peligroso (rojo) | Píldora con punto y texto oscuro. |
| `PasswordStrengthBar` | 4 segmentos + etiqueta: Weak (1 rojo), Fair (2 ámbar), Good (3 azul), Strong (4 verde) | Segmentos inactivos en SurfaceVariant. Etiquetas en inglés en Figma: **confirmar idioma**. |
| `SecureUpTopBar` | Título alineado a la izquierda, fondo Surface | Capa "Top App Bar" en Figma. |
| `SecureUpBottomBar` | 4 destinos: Inicio, Bóveda, Escáner, Perfil. Activo = Primary con punto indicador sobre el ícono | Capa "Bottom Navigation". En Figma una etiqueta dice "Boveda" sin tilde: corregir. Se muestra en todas las pantallas **excepto** splash, onboarding, registro, login y desbloqueo. |
| `ConfirmDialog` | Título, texto, botón Cancelar (secundario) + acción (danger) | Ejemplo: "¿Cerrar sesión?". Fondo atenuado detrás. |
| `ScoreRing` | Anillo continuo de 160 dp con banda de 12 dp y el puntaje al centro | Un solo arco (no 4 segmentos): avanza 360° × score, empieza arriba y crece en sentido horario, punta redondeada. Interior relleno con el token Background. Color por rango: ≥ 70 % Success (verde), ≥ 40 % Warning (ámbar), < 40 % Danger (rojo) — **sin azul**. Número en Inter Bold 48/58 y "/ 100" en `bodySmall`, sin separación entre ambos. Medidas confirmadas en Figma (`62:221`, `62:223`). |
| FAB | Botón circular Primary con "+" | Solo en la lista de la Bóveda. |

### Espécimen: `SecureUpTextField` (Figma `87:216`; error `87:220`)

- **Columna** con gap 6 entre etiqueta y caja.
- **Etiqueta:** estilo `labelMedium` (Inter Medium 12/16), color `TextSecondary`.
- **Caja:** fondo `SurfaceVariant` con borde de 1 px del mismo color (invisible en estado normal), padding 16 × 14, radio 12 (`Radius.Control`). Alto resultante: **48 dp**; el `IconButton` del ojo (48 dp) ocupa todo el alto y el ícono visual mide 20 dp.
- **Texto del campo:** `FieldTextStyle` (Inter 16/20). El color del texto escrito es `TextPrimary` (**SUPUESTO**, no re-leído en Figma).
- **Foco:** borde `Primary` (**PROVISIONAL**, no re-leído en Figma).
- **Error:** borde `Danger` (leído en `87:220`) + mensaje con ícono debajo.
- **Ícono del mensaje de error:** `ic_alert_triangle` (Vector Asset exportado de Figma, trazo), 16 dp, tint `Danger`.

**Nombres de íconos de estado:** `ic_alert_triangle` = triángulo de alerta (trazo de 2, exportado de Figma); lo usan `InfoBanner` y el mensaje de error de `SecureUpTextField`. `ic_danger` = círculo rojo con "!" (Danger) e `ic_warning` = triángulo ámbar con "!" (Warning) se conservan como assets, pero ya no se usan en esos dos componentes.

### Espécimen: `InfoBanner` (Figma `87:197` Danger; `96:278` Warning)

- **Estructura:** `Row` con caja de ícono de 20 dp (ícono de 18 dp), separación de 12 dp y texto.
- **Warning** (leído en `96:278`):
  - Fondo `Surface` (`#181B22`), **sin borde**, radio **8 dp**.
  - Padding: 12 dp a los lados, **6 dp arriba** y **12 dp abajo**.
  - Ícono `ic_alert_triangle` con tint `Warning`, 18 dp.
  - Texto: `bodySmall` (Inter Regular 12 / 16, letter-spacing 0.2), color `TextSecondary`.
- **Danger** (leído en `87:197`):
  - Fondo `Danger` con **8 % de opacidad** (`rgba(255,92,108,0.08)`), borde de **1 dp** `Danger`, radio **16 dp** (`Radius.Card`).
  - Padding: **14 dp** en los cuatro lados.
  - Ícono `ic_alert_triangle` con tint `Danger`, 18 dp.
  - Texto: Inter Regular **13 / 18**, color `Danger`.
- **Nota de adaptación:** el aviso Danger de Figma no tiene un ícono separado: usa el emoji "⚠️" dentro del texto. En Compose se reemplaza por `ic_alert_triangle` de 18 dp, con el mismo tint `Danger`, y se quita el emoji del texto.
- **Provisional:** el tamaño 18 dp y la separación de 12 dp del ícono en Danger se toman del aviso Warning; 13 / 18 no existe como slot de tipografía y se aplica como override dentro del componente; el radio 8 dp de Warning aún no está en `Radius` (constante privada del componente).

Estructura de pantalla: `Scaffold(topBar = SecureUpTopBar, bottomBar = SecureUpBottomBar)` con contenido en `Column` y padding `Spacing.L`.

## 4. Inventario de pantallas

**Los nombres de frame en Figma se repiten** (hay tres pares con el mismo prefijo). Referirse siempre al nombre completo; recomendado renombrar en Figma con un ID único.

| Frame en Figma | Pantalla Compose | Ruta propuesta | Notas |
|---|---|---|---|
| A01 - Splash | `SplashScreen` | `splash` | Logo + "Seguridad sin compromisos" |
| A02 - Onboarding 1 | `OnboardingScreen` (página 1) | `onboarding` | Botón "Siguiente" |
| A03 - Onboarding 2 | `OnboardingScreen` (página 2) | `onboarding` | Botón "Comenzar" |
| auth01-registro | `RegisterScreen` | `register` | Aviso: sin recuperación de contraseña maestra |
| auth02-login | `LoginScreen` | `login` | Error genérico (no revela si el correo existe) |
| auth03-desbloqueo | `UnlockScreen` | `unlock` | Contador de intentos (ver pendiente 3) |
| A09a - Dashboard Home | `HomeScreen` | `home` | Score, contadores, accesos rápidos |
| A05 - Bóveda | `VaultListScreen` | `vault` | Buscador, lista, FAB |
| A06 - Detalle de credencial | `CredentialDetailScreen` | `vault/{id}` | Mostrar/ocultar, copiar, "Verificar brecha" |
| A07 - Crear credencial | `CredentialFormScreen` | `vault/new` | Incluye generador de contraseñas |
| A06 - Verificador de brechas | `BreachCheckScreen` | `breach` | Ver pendiente 10 |
| A07a - Resultado brecha seguro | `BreachResultScreen` (estado seguro) | `breach/result` | |
| A07b - Resultado brecha peligroso | `BreachResultScreen` (estado peligroso) | `breach/result` | Muestra número de apariciones |
| perfil | `ProfileScreen` | `profile` | |
| perfil-cerrar_sesion | diálogo en `ProfileScreen` | — | `ConfirmDialog` |
| cambiar-contraseña-maestra | `ChangeMasterPasswordScreen` | `profile/password` | Estado de carga en el botón |
| A08 - Escáner QR | `ScannerScreen` | `scanner` | Visor de cámara, linterna, botón Historial |
| A09a - Resultado QR seguro | `ScanResultScreen` (seguro) | `scanner/result` | |
| A09b - Resultado QR sospechoso | `ScanResultScreen` (sospechoso) | `scanner/result` | |
| A09c - Resultado QR peligroso | `ScanResultScreen` (peligroso) | `scanner/result` | |
| historial-qr | `ScanHistoryScreen` | `scanner/history` | Buscador + lista |

Las tres variantes de resultado (brecha y QR) son **una sola pantalla** que cambia por estado, no pantallas distintas.

### Nodos de Figma de referencia

| Elemento | Nodo |
|---|---|
| Login (`auth02-login`) | `87:204` |
| Typography (escala de estilos) | `4:72` |
| Button Primary (Default / Pressed / Disabled) | `4:102` / `4:104` / `4:106` |
| Button Secondary (Default / Pressed / Disabled) | `4:108` / `4:110` / `4:112` |

## 5. Notas de implementación

- Onboarding: se muestra en cada arranque de la app de forma intencional en esta fase. Persistir el estado "completado" es un pendiente técnico que se implementará con Jetpack DataStore en pasos posteriores.
- Errores de validación por campo: no están diseñados explícitamente en Figma; se utiliza el estado de error estándar de `SecureUpTextField` (borde rojo `Danger` + mensaje explicativo con ícono `ic_alert_triangle`).
- Generador de contraseñas (A07): longitud ajustable (16 en el ejemplo) y alternadores A-Z, a-z, 0-9, símbolos. Usar `SecureRandom`, nunca `Random`.
- Detalle de credencial: "Se borrará del portapapeles en 30 s". Marcar el contenido copiado como sensible (`ClipDescription.EXTRA_IS_SENSITIVE`, Android 13+) y programar la limpieza; verificar en la documentación cómo se comporta con la app en segundo plano.
- Pantallas de la bóveda que muestran contraseñas: `FLAG_SECURE` mediante un helper reutilizable (ver pendiente 12). No se aplica al login.
- Cerrar sesión (diálogo): "Tu bóveda se borrará de la memoria de este dispositivo". Implica limpiar la clave en RAM y la caché local del vault.

## 6. Pendientes de decisión (no implementar hasta resolverlos)

1. **Fórmula del Security Score.** *Criterio resuelto (2026-10-29): el score depende **solo de la fortaleza** de las contraseñas guardadas, no de las brechas (esas ya se muestran aparte).* Falta la fórmula exacta que convierte la fortaleza en 0-100: se define en el paso 5 (Vault).
2. **Estado de una credencial.** La Bóveda usa punto verde/ámbar/rojo; los badges dicen Seguro/Sospechoso/Peligroso. Para contraseñas, definir qué significa cada color (propuesta: rojo = aparece en brecha, ámbar = débil o repetida, verde = fuerte y sin brecha).
3. **Intentos fallidos en Desbloqueo** ("3 intentos restantes"). No está definido qué pasa al llegar a 0. Un contador solo en la app no protege (la protección real es el costo del KDF); decidir entre retardo creciente o cierre de sesión, y dónde se persiste el contador. Evitar borrar datos.
4. **Eliminar cuenta**: está en el alcance técnico pero **no existe en el diseño** del Perfil. Diseñarla o sacarla del alcance.
5. **Perfil: nombre editable y "Último inicio de sesión"** requieren columnas nuevas en el backend (`display_name`, `last_login_at`) que hoy no están en el esquema.
6. **QR "Sospechoso".** Safe Browsing solo indica coincidencia con listas de amenazas; no sabe "el servicio esperado" (texto de A09b). Definir reglas propias (acortadores de URL, dominios parecidos a una marca, IP como host, HTTP sin TLS, punycode) o cambiar el texto.
7. **QR "Peligroso" con botón "Abrir bajo tu riesgo".** Un solo toque hacia una URL marcada como phishing es una decisión de seguridad discutible. Opciones: quitar el botón, o exigir una confirmación adicional.
8. **Texto "Dominio verificado"** en resultado seguro: sobreafirma. Que no haya coincidencias en listas no verifica nada. Sugerencia: "Sin amenazas conocidas".
9. **Cambiar contraseña maestra:** el medidor de fortaleza aparece bajo la contraseña **actual**; debería estar bajo la **nueva**.
10. **Navegación del verificador de brechas.** Hay un verificador independiente (contraseña escrita a mano) y un botón "Verificar brecha" en el detalle de credencial. Definir cómo se llega a cada uno y si el detalle reutiliza la misma pantalla de resultado.
11. **Pendientes visuales:** peso del texto de botones e idioma de las etiquetas del medidor de fortaleza. (**Resuelto:** el `ScoreRing` es un anillo continuo con verde/ámbar/rojo —sin azul ni 4 arcos—; los estados de Primary/Danger quedaron **CONFIRMADOS**; el espécimen del campo está documentado en la sección 3 y el radio de tarjeta/aviso quedó confirmado en 16 dp.)
12. **`FLAG_SECURE`:** se implementa como helper reutilizable y se aplica **solo** a las pantallas de la bóveda (no al login). Falta definir el nombre/ubicación del helper.