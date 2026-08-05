# Recepción v2 (Android)

App móvil para el módulo de Recepción de poscosecha, conectada a la API de `phytotec_v2` (Symfony).

**Flujo real** (confirmado con el negocio, distinto del diseño original de este proyecto):
1. La recepción se crea **en la web**, en la finca/bloque, cuando cortan la flor — ahí mismo se imprime la etiqueta con el código de barras (el id numérico de la recepción en el servidor).
2. La etiqueta se pega a la caja/malla y viaja a poscosecha.
3. Esta app **solo escanea** ese código de barras ya impreso y confirma que la caja llegó a poscosecha. No crea recepciones nuevas — esa parte quedó descartada del diseño original.

Es offline-first en el paso que importa: escanear y confirmar funciona sin conexión (se guarda localmente y se sincroniza solo cuando vuelve la red), idempotente (reintentos no duplican la confirmación en el servidor).

## Antes de compilar

Este proyecto se generó fuera de Android Studio, así que estos pasos son necesarios antes del primer build:

1. **Configura la URL de la API.** Edita `app/build.gradle`, busca `API_BASE_URL` y cámbiala por la URL real de tu `phytotec_v2` en producción, terminada en `/api/`:
   ```groovy
   buildConfigField "String", "API_BASE_URL", "\"https://tu-dominio-phytotec.com/api/\""
   ```
   Debe terminar en `/` o Retrofit falla en tiempo de ejecución.

2. **Abre la carpeta en Android Studio** (`File > Open`, selecciona esta carpeta `cactus_v2`). Deja que Android Studio genere `local.properties` automáticamente (ahí va la ruta de tu SDK de Android, es específico de tu máquina y no debe compartirse).

3. **Sync Gradle.** La primera vez descargará Gradle 8.2 (definido en `gradle/wrapper/gradle-wrapper.properties`) y todas las dependencias de la app.

4. **Compila y corre** en un dispositivo Zebra con Android 7.0 (API 24) o superior, o en otro Android que pueda recibir códigos por teclado o DataWedge.

## Si el build falla

No pude compilar ni ejecutar este proyecto en el entorno donde lo escribí (no tiene JDK/Android SDK instalados). Escribí el código con mucho cuidado siguiendo los patrones estándar de Kotlin/Compose/Hilt/Room/WorkManager de 2024, pero **la primera compilación real la tienes que hacer tú**. Si Android Studio marca errores, compártemelos (el mensaje completo del error, y en qué archivo) y los corrijo.

Puntos que vale la pena revisar primero si algo falla:
- Versión exacta del plugin de Compose Compiler (`composeOptions.kotlinCompilerExtensionVersion` en `app/build.gradle`) — debe coincidir con la versión de Kotlin del proyecto. Si Android Studio sugiere una versión distinta, acéptala.
- Si usas DataWedge, configura la salida como broadcast con la acción `com.phytotec.recepcion.SCAN` y manda el texto del código en `com.symbol.datawedge.data_string`.

## Sobre el escaneo Zebra

La pantalla de recepción quedó pensada para Zebra de dos formas:
- **Modo teclado:** el escáner escribe el código en el campo enfocado y la app lo procesa sola.
- **DataWedge:** si prefieres broadcast, usa la acción `com.phytotec.recepcion.SCAN`.

La app ya no usa cámara para este flujo.

## Sobre el sidebar y permisos

El menú lateral de la app se arma con los módulos que devuelve la API de navegación (`GET /api/navigation`), filtrados por los roles del usuario.
En el panel web ya existen las pantallas para administrar esto:
- `admin/users` para asignar roles a usuarios
- `admin/roles` para crear/editar roles
- `admin/nav-folders` para definir módulos/carpetas visibles por rol y por contexto (`Web` o `App móvil`)

Si un usuario no tiene el rol adecuado en la web, el módulo correspondiente no aparece en la app móvil.
