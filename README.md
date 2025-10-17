# excusApp - Generador de excusas 🎭

<img width="1024" height="1024" alt="logo" src="https://github.com/user-attachments/assets/20661cb6-fdb2-4e1a-b9cf-90b00b4bf96a" />

Esta es una aplicación Android para generar excusas creativas para cualquier situación del día a día. ¡Nunca más te quedes sin una buena excusa!

## ✨ Características principales

### 🎲 Generación de excusas
- **📚 Excusas predefinidas** organizadas por categorías:
  - 💼 Trabajo
  - 📚 Estudio
  - 👨‍👩‍👧‍👦 Familia
  - 🎉 Amigos
  - 💕 Citas
  - 🌟 General
- **🤖 Generación con IA** usando Google Gemini para excusas únicas y personalizadas (Es necesaria una API KEY Gratuita)
- **😄 Frases motivacionales irónicas** que acompañan cada excusa generada para hacerla más divertida

### 🖼️ Crear memes con tus excusas
Convierte tus excusas en memes utilizando estas **cuatro opciones**:

1. **✨ Generar automático**
   - Crea una imagen con fondo degradado morado
   - Incluye emoji de la categoría
   - Texto con la excusa
   - Frase irónica en la parte inferior
   - Marca de agua "excusApp"
   - Formato optimizado para redes sociales (1080x1080)

2. **✍️ Escribir excusa personalizada**
   - Escribe tu propia excusa antes de seleccionar la imagen
   - Elige entre tomar una foto o seleccionar de galería
   - Ideal para situaciones específicas

3. **📷 Desde galería**
   - Selecciona cualquier foto de tu galería
   - Elige usar la excusa generada o escribir una nueva
   - Vista previa editable con opciones de posicionamiento
   - La app la convierte en un meme estilo clásico
   - Texto arriba y abajo con fondo negro semi-transparente
   - La excusa se superpone a tu imagen

4. **📸 Tomar foto**
   - Abre la cámara y toma una foto al instante
   - Elige la excusa o escribe una personalizada
   - Vista previa y edición antes de compartir
   - Perfecto para memes con contexto real

### 🎨 Editor de memes (NUEVO)
Cuando seleccionas una imagen desde galería o cámara, accedes a una pantalla de previsualización donde puedes:
- **📝 Editar el texto** de la excusa antes de generar el meme
- **⬆️⬇️ Elegir posición del texto**: Arriba o Abajo
- **👁️ Previsualizar** el resultado antes de compartir
- **✨ Texto mejorado**: Sombra sutil en lugar de borde grueso para mejor legibilidad
- **🎯 Layout optimizado**: Los elementos se distribuyen correctamente sin solaparse

### 📤 Compartir y copiar
- **📋 Copiar al portapapeles** para pegar en cualquier lugar
- **📱 Compartir como texto** en redes sociales, WhatsApp, email, etc.
- **🎨 Compartir como imagen/meme** en formato optimizado para redes sociales (1080x1080)

### 🎨 Interfaz y experiencia
- **🌓 Selector de tema** personalizable con tres opciones:
  - ☀️ **Tema Claro**: Interfaz luminosa
  - 🌙 **Tema Oscuro**: Modo nocturno para los ojos
  - 📱 **Según el sistema**: Se adapta automáticamente a tu configuración de Android
- **Material Design 3** con componentes modernos
- **🎬 Splash screen** con logo y créditos (3.5 segundos)
- **⚡ Animaciones suaves** al generar y mostrar excusas
- **🔄 Interfaz moderna** e intuitiva

### ⚙️ Configuración avanzada
- **🌓 Selector de tema** en configuración (Claro/Oscuro/Sistema)
- **🔑 API Key personalizable** desde la interfaz de la app
- **✅ Verificación de API** con botón de prueba
- **🔄 Fallback automático** a excusas predefinidas si no hay conexión o API
- **💾 Persistencia de configuración**: Tu tema y API key se guardan automáticamente
- **ℹ️ Pantalla "Acerca de"** con información del proyecto y enlace a GitHub

## 🎯 Ejemplos de frases irónicas

Cada vez que generas una excusa, se muestra una frase motivacional irónica aleatoria:

- 💪 "La honestidad está sobrevalorada"
- 🎭 "Ser creativo nunca fue tan fácil"
- 🏆 "Campeón de las excusas desde 2025"
- 🎓 "Doctor honoris causa en excusología"
- 🌙 "La verdad duerme, las excusas trabajan 24/7"
- 🎬 "Premio Oscar a mejor actuación improvisada"

...¡y 12 más!

## 📸 Capturas de pantalla

<img width="405" height="897" alt="pantalla-inicial-excusapp" src="https://github.com/user-attachments/assets/b7bfc71a-bc59-4bf3-a6d2-11682d31780f" />

### Pantalla principal
- Logo de la app
- Selector de categorías con chips
- Tarjeta de visualización de excusas con emoji
- Switch para alternar entre modo predefinido e IA
- Botones de acción (Copiar, Compartir, Compartir como Imagen)

### Crear memes

<img width="398" height="896" alt="compartir-excusa" src="https://github.com/user-attachments/assets/5020d95c-d78f-444c-9599-1c2c83dad251" />

- Diálogo con 3 opciones de creación
- Generación automática con diseño
- Integración con cámara y galería
- Texto estilo meme con fondo negro semi-transparente

### Configuración

<img width="403" height="889" alt="configuracion-excusapp" src="https://github.com/user-attachments/assets/150e4bbe-17e7-4a24-8d46-8066009865b0" />

- Campo para ingresar API key de Gemini
- Botón para probar la API
- Indicador de API configurada/no configurada
- Instrucciones paso a paso

## 🔐 Configuración de API Key

Hay dos formas de configurar tu API key:

### Opción 1: Configurar desde la App (RECOMENDADO ✅)
1. Instala la aplicación
2. Ve a **Configuración** (botón de engranaje flotante)
3. Ingresa tu API key de Gemini
4. Pulsa "Guardar"
5. (Opcional) Pulsa "Probar API" para verificar que funciona

**Ventaja:** Tu API key se guarda localmente en el dispositivo y NUNCA se sube a Git.

### Opción 2: Configurar en el Código (Solo para desarrollo)
Si quieres tener una API key por defecto en el código:

1. Ve a `app/src/main/java/com/example/excusas/utils/PreferencesManager.kt`
2. Busca la línea:
   ```kotlin
   private const val DEFAULT_API_KEY = "AIzaSyBTUAPIKeyAquí"
   ```
3. Reemplaza `"AIzaSyBTUAPIKeyAquí"` con tu API key real


### 🔑 Cómo obtener una API Key de Gemini (GRATIS)

1. Ve a [Google AI Studio](https://aistudio.google.com/apikey)
2. Inicia sesión con tu cuenta de Google
3. Clic en "Create API Key" o "Get API Key"
4. Copia la clave generada (formato: `AIzaSy...`)
5. Pégala en la configuración de la app

**Nota:** La API de Gemini tiene un tier gratuito generoso para uso personal.

## 🛠️ Tecnologías utilizadas

### Backend & Lógica
- **Kotlin** - Lenguaje principal (100% Kotlin)
- **Coroutines** - Programación asíncrona
- **Retrofit** - Cliente HTTP para consumir APIs REST
- **OkHttp** - Cliente HTTP con logging
- **Gson** - Serialización/deserialización JSON

### UI/UX
- **Material Design 3** - Sistema de diseño moderno
- **View Binding** - Vinculación de vistas type-safe
- **ConstraintLayout** - Layouts flexibles y responsive
- **CoordinatorLayout** - FABs y comportamientos avanzados
- **Material Components** - Chips, Cards, Buttons, RadioButtons, etc.
- **AppCompatDelegate** - Soporte para temas claro/oscuro

### Funcionalidades
- **Google Gemini API** - Generación de texto con IA (modelo gemini-2.0-flash-exp)
- **SharedPreferences** - Almacenamiento local de configuración y preferencias de tema
- **FileProvider** - Compartir archivos entre apps de forma segura
- **Canvas & Bitmap** - Generación de imágenes y memes con texto
- **Paint & Shadow** - Renderizado de texto con sombras para mejor legibilidad
- **ExifInterface** - Corrección automática de orientación de imágenes
- **ActivityResultContracts** - Manejo moderno de permisos y resultados
- **Application Class** - Aplicación del tema al iniciar la app

### Permisos
- `INTERNET` - Conectar con la API de Gemini
- `ACCESS_NETWORK_STATE` - Verificar conexión
- `CAMERA` - Tomar fotos para memes
- `READ_MEDIA_IMAGES` - Acceder a galería (Android 13+)
- `READ_EXTERNAL_STORAGE` - Acceder a galería (Android 12 y anteriores)

## 📂 Estructura del proyecto

```
excusApp/
├── app/
│   ├── src/main/
│   │   ├── java/com/example/excusas/
│   │   │   ├── api/
│   │   │   │   ├── GeminiApiService.kt      # Interfaz Retrofit para Gemini API
│   │   │   │   ├── GeminiModels.kt          # Modelos de datos para API
│   │   │   │   └── RetrofitClient.kt        # Cliente Retrofit configurado
│   │   │   ├── model/
│   │   │   │   └── ExcuseCategory.kt        # Modelo de categoría
│   │   │   ├── repository/
│   │   │   │   └── ExcuseRepository.kt      # Lógica de negocio
│   │   │   ├── utils/
│   │   │   │   └── PreferencesManager.kt    # Gestión de SharedPreferences y tema
│   │   │   ├── MainActivity.kt              # Pantalla principal
│   │   │   ├── PreviewActivity.kt           # Editor de memes con vista previa (NUEVO)
│   │   │   ├── SplashActivity.kt            # Splash screen inicial
│   │   │   ├── SettingsActivity.kt          # Configuración de API y tema
│   │   │   ├── AboutActivity.kt             # Acerca de la app
│   │   │   ├── ImageUtils.kt                # Utilidades para procesamiento de imágenes (NUEVO)
│   │   │   └── ExcusasApplication.kt        # Clase Application para aplicar tema (NUEVO)
│   │   └── res/
│   │       ├── drawable/
│   │       │   ├── logo.png                 # Logo de la aplicación
│   │       │   └── ic_launcher_*.xml        # Iconos adaptativos
│   │       ├── layout/
│   │       │   ├── activity_main.xml        # Layout principal
│   │       │   ├── activity_preview.xml     # Layout editor de memes (NUEVO)
│   │       │   ├── activity_splash.xml      # Layout splash
│   │       │   ├── activity_settings.xml    # Layout configuración (con selector tema)
│   │       │   └── activity_about.xml       # Layout acerca de
│   │       ├── mipmap-*/                    # Iconos de launcher
│   │       ├── values/
│   │       │   ├── strings.xml              # Todas las strings (incluyendo tema)
│   │       │   ├── colors.xml               # Paleta de colores
│   │       │   └── themes.xml               # Temas Material 3 (claro/oscuro)
│   │       └── xml/
│   │           └── file_paths.xml           # Configuración FileProvider
│   └── build.gradle.kts                     # Dependencias y configuración (APK renombrado)
├── gradle/
│   └── libs.versions.toml                   # Catálogo de versiones
└── README.md                                # Este archivo
```

## 🆕 Novedades en esta versión

### Mejoras de interfaz
- ✨ **Editor de memes completo**: Nueva pantalla PreviewActivity con controles intuitivos
- 🎨 **Texto mejorado**: Sombra sutil en lugar de borde grueso brillante (mucho más legible)
- 📐 **Layout corregido**: Los elementos ahora se distribuyen correctamente sin solaparse
- 🌓 **Selector de tema**: Elige entre claro, oscuro o automático según el sistema
- 🎯 **Mejor UX**: Botón de compartir deshabilitado hasta generar la previsualización

### Funcionalidades nuevas
- 📝 **Edición de texto**: Modifica la excusa antes de generar el meme final
- ⬆️⬇️ **Posicionamiento de texto**: Elige si el texto va arriba o abajo de la imagen
- 👁️ **Vista previa en tiempo real**: Ve el resultado antes de compartir
- 🔄 **Persistencia de tema**: Tu preferencia de tema se guarda automáticamente
- 📦 **APK renombrado**: El archivo se genera como `excusApp-debug.apk` automáticamente

### Mejoras técnicas
- 🏗️ **ExcusasApplication**: Clase Application que aplica el tema al inicio
- 🖼️ **ImageUtils**: Módulo dedicado para procesamiento de imágenes
- 🔐 **Permisos mejorados**: FLAG_GRANT_READ_URI_PERMISSION para compartir URIs
- 📊 **Logging detallado**: Logs para facilitar debugging de imágenes
- ✅ **Validaciones robustas**: Mejor manejo de errores y casos extremos

## 📱 Uso de la aplicación

### Generar una excusa
1. Abre la aplicación
2. Selecciona una categoría (Trabajo, Estudio, etc.)
3. Pulsa el botón "Generar Excusa" o activa el modo IA y pulsa "Generar con IA 🤖"
4. ¡Disfruta de tu excusa creativa con una frase irónica!

### Crear un meme con imagen personalizada
1. Genera o escribe una excusa
2. Pulsa el botón "Compartir como Imagen"
3. Elige una opción:
   - **Generar Automático**: Crea una imagen con diseño predefinido
   - **Escribir mi propia excusa**: Escribe primero, luego elige imagen
   - **Desde Galería**: Selecciona una foto existente
   - **Tomar Foto**: Usa la cámara
4. Si elegiste galería o cámara:
   - Se abre la pantalla de **previsualización**
   - Edita el texto de la excusa si lo deseas
   - Elige si el texto va **Arriba** o **Abajo**
   - Pulsa **"Generar previsualización"** para ver el resultado
   - Si te gusta, pulsa **"Compartir meme"**
5. Comparte en tus redes sociales favoritas

### Cambiar el tema de la aplicación
1. Ve a **Configuración** (botón flotante con ⚙️)
2. En la sección **"Apariencia"**, elige:
   - ☀️ **Claro** - Fondo blanco
   - 🌙 **Oscuro** - Fondo negro
   - 📱 **Según el sistema** - Se adapta automáticamente
3. El cambio se aplica inmediatamente
4. Tu preferencia se guarda para futuras sesiones

### Configurar API de Gemini
1. Ve a Configuración
2. Ingresa tu API key de Gemini
3. (Opcional) Pulsa "Probar API" para verificar
4. Pulsa "Guardar"
5. Activa el switch de IA en la pantalla principal

## 📥 Instalación

### Descargar APK
1. Ve a la sección [Releases](../../releases) de este repositorio
2. Descarga el archivo `excusApp-debug.apk` (o `excusApp-release.apk`)
3. Instala el APK en tu dispositivo Android
4. (Puede que necesites habilitar "Instalar apps de origen desconocido" en configuración)

### Compilar desde código fuente
```bash
# Clonar el repositorio
git clone https://github.com/tuusuario/excusas.git
cd excusas

# Compilar APK de debug
./gradlew assembleDebug

# El APK estará en: app/build/outputs/apk/debug/excusApp-debug.apk

# Compilar APK de release
./gradlew assembleRelease

# El APK estará en: app/build/outputs/apk/release/excusApp-release.apk
```

## 🤝 Contribuir

Las contribuciones son bienvenidas. Si quieres mejorar la app:

1. Haz fork del proyecto
2. Crea una rama para tu feature (`git checkout -b feature/AmazingFeature`)
3. Commit tus cambios (`git commit -m 'Add some AmazingFeature'`)
4. Push a la rama (`git push origin feature/AmazingFeature`)
5. Abre un Pull Request

## 📄 Licencia

Este proyecto está bajo la Licencia MIT - ver el archivo [LICENSE](LICENSE) para más detalles.

## 👨‍💻 Autor

**entreunosyceros.net**

- GitHub: [@entreunosyceros](https://github.com/entreunosyceros)
- Web: [entreunosyceros.net](https://entreunosyceros.net)

## 🙏 Agradecimientos

- [Google Gemini AI](https://ai.google.dev/) por la API gratuita
- [Material Design](https://m3.material.io/) por el sistema de diseño
- Comunidad Android por las excelentes bibliotecas

---

**Nota:** Esta aplicación es solo para fines de entretenimiento y educativos. Úsala responsablemente 😉
