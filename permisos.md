# Informe Técnico: Gestión de Permisos en RunnConnect

Este documento resume las decisiones técnicas y los fundamentos de arquitectura sobre el manejo de permisos en el proyecto **RunnConnect**, considerando la versión mínima de soporte (**Android 8.0 - API 26**), la versión objetivo (**Android 14+ - API 36**) y las directrices del patrón **MVVM**.

---

## 1. Declaración de Permisos en el `AndroidManifest.xml`

### ¿Los permisos van en el Manifest?
**Sí, de forma obligatoria.**

En el sistema operativo Android, **todo permiso** (sea normal o peligroso de tiempo de ejecución) que la aplicación necesite debe estar previamente declarado en el archivo `AndroidManifest.xml` mediante la etiqueta `<uses-permission android:name="..." />`.

> [!IMPORTANT]
> Si un permiso no está declarado en el Manifest, el sistema operativo deniega automáticamente cualquier solicitud en tiempo de ejecución sin mostrar el cuadro de diálogo al usuario, impidiendo que la funcionalidad opere.

---

## 2. Análisis Específico de los 3 Permisos Requeridos

### A. Acceso a Internet (`android.permission.INTERNET`)
* **Declaración en Manifest**:
  ```xml
  <uses-permission android:name="android.permission.INTERNET"/>
  <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE"/>
  ```
* **Nivel de Protección**: `Normal Permission` (Permiso normal).
* **Comportamiento en Runtime**: 
  * Se otorga de forma **automática y silenciosa** al momento en que el usuario instala la aplicación.
  * **No requiere ni permite** diálogo de solicitud en tiempo de ejecución (`requestPermissions`). Si se intentara solicitar mediante código, el sistema ignora la llamada.

---

### B. Notificaciones Push (`android.permission.POST_NOTIFICATIONS`)
* **Declaración en Manifest**:
  ```xml
  <uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
  ```
* **Nivel de Protección**: `Dangerous / Runtime Permission` (a partir de Android 13 - API 33 TIRAMISU).
* **Comportamiento según la versión del dispositivo**:
  * **Android 13 o superior (API 33+)**: El sistema operativo exige que la Activity solicite explícitamente el permiso mediante un diálogo emergente nativo. Si el usuario lo rechaza, las notificaciones Push de Firebase no se mostrarán en la barra de estado.
  * **Android 8 a 12 (API 26 a 32)**: Las notificaciones están **concedidas por defecto** con la instalación de la app. El permiso `POST_NOTIFICATIONS` no existía en esas versiones, por lo que el sistema no muestra ningún diálogo emergente.

---

### C. Galería al Cargar una Foto (Perfil y Comprobantes de Pago)
* **Implementación actual en RunnConnect**:
  En los fragmentos y activities (`PerfilRunnerFragment`, `PerfilOrganizadorFragment`, `RegisterRunnerActivity`, `RegisterOrganizadorActivity` y `MisInscripcionesFragment`) se utiliza el contrato moderno de AndroidX:
  ```java
  registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> ...);
  // O bien:
  registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> ...);
  ```
* **¿Requiere permisos en Manifest o en Runtime?**: **NO.**
  * Google introdujo el **Android Photo Picker** (`PickVisualMedia`) precisamente para eliminar la necesidad de pedir permisos invasivos de almacenamiento (`READ_EXTERNAL_STORAGE` o `READ_MEDIA_IMAGES`).
  * El selector de fotos se ejecuta en un proceso independiente del sistema. El usuario elige voluntariamente la imagen y el sistema le concede a la aplicación un permiso de lectura temporal exclusivo sobre la URI devuelta.
  * La aplicación **no accede al resto de las fotos del usuario**, mejorando la privacidad y cumpliendo con las políticas más exigentes de Google Play.

> [!NOTE]
> Solo si en una evaluación académica se exigiera explícitamente el enfoque tradicional (pre-Photo Picker), se debería declarar `READ_MEDIA_IMAGES` (API 33+) y `READ_EXTERNAL_STORAGE` (API ≤ 32) y solicitarlos manualmente. El enfoque actual con `PickVisualMedia` es el estándar recomendado por Google.

---

## 3. Momento y Ubicación de la Solicitud de Permisos

| Pantalla | Condición | Justificación |
| :--- | :--- | :--- |
| **`LoginActivity`** | Primera instalación | Es la Activity `LAUNCHER` principal. Si se desea que el usuario vea los permisos nada más abrir la app instalada, debe residir aquí. |
| **`MainActivity`** | Post-login | Es el menú principal tras autenticarse. Solo solicita notificaciones a usuarios activos que ya iniciaron sesión. |

---

## 4. Alineación Estricta con el Patrón MVVM (Reglas del Proyecto)

1. **Regla 13 (La Vista maneja Diálogos y Permisos)**:
   * Los permisos y diálogos dependen estrictamente del `Context` y del ciclo de vida de Android (`Activity` / `Fragment`).
   * **El `ViewModel` nunca debe solicitar permisos ni contener referencias a `Context`**, evitando fugas de memoria y manteniendo la lógica de negocio desacoplada de las APIs del sistema operativo.
2. **Vistas Pasivas (Regla 2)**:
   * La vista se limita a invocar las APIs del sistema (`ActivityResultLauncher`) y notificar al ViewModel si es necesario cambiar algún estado.
