# excusApp - Generador de Excusas 🎭

Aplicación Android para generar excusas creativas para cualquier situación del día a día. ¡Nunca más te quedes sin una buena excusa!

## ✨ Características Principales

### 🎲 Generación de Excusas
- **📚 Excusas predefinidas** organizadas por categorías:
  - 💼 Trabajo
  - 📚 Estudio
  - 👨‍👩‍👧‍👦 Familia
  - 🎉 Amigos
  - 💕 Citas
  - 🌟 General
- **🤖 Generación con IA** usando Google Gemini para excusas únicas y personalizadas
- **😄 Frases motivacionales irónicas** que acompañan cada excusa generada para hacerla más divertida

### 🖼️ Crear Memes con tus Excusas (NUEVO)
Convierte tus excusas en memes profesionales con **tres opciones**:

1. **✨ Generar Automático**
   - Crea una imagen con fondo degradado morado
   - Incluye emoji de la categoría
   - Texto profesional con la excusa
   - Frase irónica en la parte inferior
   - Marca de agua "excusApp"

2. **📷 Desde Galería**
   - Selecciona cualquier foto de tu galería
   - La app la convierte en un meme estilo clásico
   - Texto arriba y abajo con fondo negro
   - La excusa se superpone a tu imagen

3. **📸 Tomar Foto**
   - Abre la cámara y toma una foto al instante
   - Convierte la foto en meme automáticamente
   - Perfecto para memes con contexto real

### 📤 Compartir y Copiar
- **📋 Copiar al portapapeles** para pegar en cualquier lugar
- **📱 Compartir como texto** en redes sociales, WhatsApp, etc.
- **🎨 Compartir como imagen/meme** en formato optimizado para redes sociales (1080x1080)

### 🎨 Interfaz y Experiencia
- **🌙 Modo día/noche** automático con Material Design 3
- **🎬 Splash screen** con logo y créditos (3.5 segundos)
- **⚡ Animaciones suaves** al generar y mostrar excusas
- **🔄 Interfaz moderna** e intuitiva

### ⚙️ Configuración Avanzada
- **🔑 API Key personalizable** desde la interfaz de la app
- **✅ Verificación de API** con botón de prueba
- **🔄 Fallback automático** a excusas predefinidas si no hay conexión o API
- **ℹ️ Pantalla "Acerca de"** con información del proyecto y enlace a GitHub

## 🎯 Ejemplos de Frases Irónicas

Cada vez que generas una excusa, se muestra una frase motivacional irónica aleatoria:

- 💪 "La honestidad está sobrevalorada"
- 🎭 "Ser creativo nunca fue tan fácil"
- 🏆 "Campeón de las excusas desde 2025"
- 🎓 "Doctor honoris causa en excusología"
- 🌙 "La verdad duerme, las excusas trabajan 24/7"
- 🎬 "Premio Oscar a mejor actuación improvisada"

...¡y 12 más!

## 📸 Capturas de Pantalla

### Pantalla Principal
- Logo de la app
- Selector de categorías con chips
- Tarjeta de visualización de excusas con emoji
- Switch para alternar entre modo predefinido e IA
- Botones de acción (Copiar, Compartir, Compartir como Imagen)

### Crear Memes
- Diálogo con 3 opciones de creación
- Generación automática con diseño profesional
- Integración con cámara y galería
- Texto estilo meme con fondo negro semi-transparente

### Configuración
- Campo para ingresar API key de Gemini
- Botón para probar la API
- Indicador de API configurada/no configurada
- Instrucciones paso a paso

## 🔐 Configuración de API Key

### ⚠️ NUNCA subas tu API key real al repositorio

La aplicación usa la API de Google Gemini para generar excusas con IA. Hay dos formas de configurar tu API key:

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
   private const val DEFAULT_API_KEY = "AIzaSyBYourAPIKeyHere"
   ```
3. Reemplaza `"AIzaSyBYourAPIKeyHere"` con tu API key real

**⚠️ ADVERTENCIA:** Si haces esto, **NO SUBAS** este cambio a Git. El archivo `.gitignore` está configurado para advertirte, pero ten cuidado.

### 🔑 Cómo obtener una API Key de Gemini (GRATIS)

1. Ve a [Google AI Studio](https://aistudio.google.com/apikey)
2. Inicia sesión con tu cuenta de Google
3. Clic en "Create API Key" o "Get API Key"
4. Copia la clave generada (formato: `AIzaSy...`)
5. Pégala en la configuración de la app

**Nota:** La API de Gemini tiene un tier gratuito generoso para uso personal.

## 🛠️ Tecnologías Utilizadas

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
- **Material Components** - Chips, Cards, Buttons, etc.

### Funcionalidades
- **Google Gemini API** - Generación de texto con IA (modelo gemini-2.5-flash)
- **SharedPreferences** - Almacenamiento local de configuración
- **FileProvider** - Compartir archivos entre apps de forma segura
- **Canvas & Bitmap** - Generación de imágenes y memes
- **ActivityResultContracts** - Manejo moderno de permisos y resultados

### Permisos
- `INTERNET` - Conectar con la API de Gemini
- `ACCESS_NETWORK_STATE` - Verificar conexión
- `CAMERA` - Tomar fotos para memes
- `READ_MEDIA_IMAGES` - Acceder a galería (Android 13+)
- `READ_EXTERNAL_STORAGE` - Acceder a galería (Android 12 y anteriores)

## 📂 Estructura del Proyecto

```
excusApp/
├── app/
│   ├── src/main/
│   │   ├── java/com/example/excusas/
│   │   │   ├── api/
│   │   │   │   ├── GeminiApiService.kt      # Interfaz Retrofit para Gemini API
│   │   │   │   └── GeminiModels.kt          # Modelos de datos para API
│   │   │   ├── model/
│   │   │   │   └── ExcuseCategory.kt        # Modelo de categoría
│   │   │   ├── repository/
│   │   │   │   └── ExcuseRepository.kt      # Lógica de negocio
│   │   │   ├── utils/
│   │   │   │   └── PreferencesManager.kt    # Gestión de SharedPreferences
│   │   │   ├── MainActivity.kt              # Pantalla principal
│   │   │   ├── SplashActivity.kt            # Splash screen inicial
│   │   │   ├── SettingsActivity.kt          # Configuración de API
│   │   │   └── AboutActivity.kt             # Acerca de la app
│   │   └── res/
│   │       ├── drawable/
│   │       │   ├── logo.png                 # Logo de la aplicación
│   │       │   └── ic_launcher_*.xml        # Iconos adaptativos
│   │       ├── layout/
│   │       │   ├── activity_main.xml        # Layout principal
│   │       │   ├── activity_splash.xml      # Layout splash
│   │       │   ├── activity_settings.xml    # Layout configuración
│   │       │   └── activity_about.xml       # Layout acerca de
│   │       ├── mipmap-*/                    # Iconos de launcher
│   │       ├── values/
│   │       │   ├── strings.xml              # Todas las strings
│   │       │   ├── colors.xml               # Paleta de colores
│   │       │   └── themes.xml               # Temas Material 3
│   │       └── xml/
│   │           └── file_paths.xml           # Configuración FileProvider
│   └── build.gradle.kts                     # Dependencias y configuración
├── gradle/
│   └── libs.versions.toml                   # Catálogo de versiones
├── .gitignore                               # Protección de archivos sensibles
└── README.md                                # Este archivo
```

## 🚀 Instalación y Compilación

### Requisitos Previos
- Android Studio Hedgehog (2023.1.1) o superior
- JDK 17 o superior
- Android SDK API 34 (Android 14)
- Gradle 8.2+

### Pasos de Instalación

1. **Clonar el repositorio:**
   ```bash
   git clone https://github.com/sapoclay/excusapp.git
   cd excusapp
   ```

2. **Abrir en Android Studio:**
   - Abre Android Studio
   - Selecciona "Open an Existing Project"
   - Navega a la carpeta clonada

3. **Sincronizar Gradle:**
   - Android Studio sincronizará automáticamente
   - Si no, ve a `File > Sync Project with Gradle Files`

4. **Compilar el proyecto:**
   ```bash
   ./gradlew assembleDebug
   ```

5. **Instalar en dispositivo/emulador:**
   - Conecta un dispositivo Android o inicia un emulador
   - Pulsa el botón "Run" en Android Studio
   - O ejecuta: `./gradlew installDebug`

### Build Variants
- `debug` - Para desarrollo con logs habilitados
- `release` - Para producción (requiere firma)

## 🔒 Seguridad y Privacidad

### Protección de API Keys ✅

**El proyecto está configurado para NO subir API keys a Git:**

1. **SharedPreferences (Recomendado):**
   - Las API keys del usuario se guardan localmente
   - Ubicación: `/data/data/com.example.excusas/shared_prefs/`
   - NUNCA se sincronizan con Git
   - Permanecen en el dispositivo del usuario

2. **`.gitignore` Robusto:**
   El archivo `.gitignore` excluye:
   - `apikeys.properties`
   - `secrets.properties`
   - `**/api_keys.xml`
   - `*.bak` y `*.backup`
   - `local.properties`
   - Todos los archivos de build

3. **Buenas Prácticas:**
   - ✅ NUNCA hardcodear API keys reales en el código
   - ✅ Usar siempre placeholders: `"AIzaSyBYourAPIKeyHere"`
   - ✅ Configurar API keys desde la interfaz de la app
   - ✅ Revisar cambios antes de commit

### Permisos y Privacidad

- **Permisos solicitados dinámicamente** (runtime permissions)
- **Explicación clara** al usuario antes de solicitar permisos
- **Sin recopilación de datos** personales
- **Sin analytics** ni rastreadores
- **API keys locales** - No se envían a servidores propios

## 📱 Uso de la Aplicación

### Generar una Excusa

1. **Selecciona una categoría** (Trabajo, Estudio, etc.)
2. **Elige el modo:**
   - **Predefinidas:** Excusas aleatorias de nuestra base de datos
   - **IA:** Genera excusas únicas con Gemini (requiere API key)
3. **Pulsa "Generar Excusa"**
4. **¡Listo!** Verás:
   - La excusa generada
   - Una frase irónica motivacional
   - Botones para copiar y compartir

### Crear un Meme

1. **Genera una excusa** primero
2. **Pulsa "Compartir como Imagen"**
3. **Selecciona una opción:**
   - **Generar Automático:** Imagen con diseño profesional
   - **Desde Galería:** Usa una foto tuya
   - **Tomar Foto:** Captura una foto nueva
4. **Comparte** en tus redes sociales favoritas

### Configurar la API de Gemini

1. **Obtén tu API key** gratis en [Google AI Studio](https://aistudio.google.com/apikey)
2. **Abre la app** y toca el icono de configuración (⚙️)
3. **Pega tu API key** en el campo de texto
4. **Pulsa "Guardar"**
5. **Opcional:** Pulsa "Probar API" para verificar

## 🤝 Contribuciones

¡Las contribuciones son bienvenidas! Si quieres mejorar excusApp:

### Cómo Contribuir

1. **Fork el proyecto**
2. **Crea una rama** para tu feature:
   ```bash
   git checkout -b feature/AmazingFeature
   ```
3. **Realiza tus cambios** siguiendo las convenciones del proyecto
4. **Commit** tus cambios:
   ```bash
   git commit -m 'Add some AmazingFeature'
   ```
5. **Push** a tu fork:
   ```bash
   git push origin feature/AmazingFeature
   ```
6. **Abre un Pull Request** en GitHub

### Reglas Importantes

- ⚠️ **NO incluyas API keys reales** en tus commits
- ✅ Sigue el estilo de código existente (Kotlin conventions)
- ✅ Añade comentarios explicativos cuando sea necesario
- ✅ Prueba tus cambios antes de hacer PR
- ✅ Actualiza el README si añades nuevas características

### Ideas para Contribuir

- 🌍 Traducciones a otros idiomas
- 🎨 Nuevos diseños de memes
- 📝 Más excusas predefinidas
- 🤖 Mejoras en los prompts de IA
- 🐛 Reportar y corregir bugs
- ✨ Nuevas características creativas

## 🐛 Reportar Bugs

Si encuentras un bug, por favor:

1. **Verifica** que no esté ya reportado en [Issues](https://github.com/sapoclay/excusapp/issues)
2. **Abre un nuevo issue** con:
   - Descripción clara del problema
   - Pasos para reproducirlo
   - Screenshots si es posible
   - Versión de Android y dispositivo
   - Logs relevantes (si aplica)

## 📜 Changelog

### Versión 1.0 (2025-10-16)

#### ✨ Características Principales
- Generación de excusas predefinidas por 6 categorías
- Integración con Google Gemini AI
- Frases motivacionales irónicas (18 frases únicas)
- Crear memes con 3 opciones (auto/galería/cámara)
- Compartir como texto o imagen
- Splash screen con logo
- Configuración de API key desde la app
- Modo día/noche automático

#### 🎨 Diseño
- Material Design 3
- Animaciones suaves
- Interfaz intuitiva
- Logo personalizado

#### 🔧 Técnico
- 100% Kotlin
- Architecture: Repository Pattern
- Retrofit + Coroutines
- View Binding
- FileProvider para compartir

## 👤 Autor

- **Sitio Web:** [entreunosyceros.net](https://entreunosyceros.net)
- **GitHub:** [@sapoclay](https://github.com/sapoclay)
- **Repositorio:** [github.com/sapoclay/excusapp](https://github.com/sapoclay/excusapp)

## 📄 Licencia

Este proyecto es de código abierto y está disponible bajo licencia libre. Puedes:
- ✅ Usar el código libremente
- ✅ Modificarlo según tus necesidades
- ✅ Distribuirlo
- ✅ Usarlo en proyectos comerciales

**Atribución apreciada pero no requerida.**

## ⚠️ Disclaimer

Esta aplicación está diseñada con **fines de entretenimiento y educativos**. 

- El uso de las excusas generadas es **responsabilidad exclusiva del usuario**
- Se recomienda usar la app de **manera ética y responsable**
- No nos hacemos responsables del uso indebido de las excusas generadas
- La IA puede generar contenido inesperado - revisa antes de usar
- Respeta las políticas de uso de Google Gemini API

## 🙏 Agradecimientos

- **Google Gemini AI** por proporcionar una API gratuita y potente
- **Material Design** por el excelente sistema de diseño
- **Android Community** por la documentación y recursos
- **Contribuidores** que mejoran este proyecto

## 📞 Soporte y Contacto

- **Issues:** [GitHub Issues](https://github.com/sapoclay/excusapp/issues)
- **Discussions:** [GitHub Discussions](https://github.com/sapoclay/excusapp/discussions)
- **Email:** Disponible en [entreunosyceros.net](https://entreunosyceros.net)

---

**¿Te gusta el proyecto? ⭐ Dale una estrella en GitHub!**

**¿Encontraste útil una excusa? 😄 Compártela en redes con #excusApp**

---

Hecho con ❤️ por entreunosyceros.net
