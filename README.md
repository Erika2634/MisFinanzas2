# MisFinanzas - Aplicación Móvil Empresarial

Aplicación nativa para Android desarrollada en Java con arquitectura serverless en Google Firebase y mapas interactivos mediante OpenStreetMap (OSMDroid). Diseñada para la gestión, georreferenciación en terreno y seguimiento multimedia de transacciones y operaciones financieras en tiempo real, implementando control de acceso basado en roles (RBAC).

---

## 📌 1. Problema que Soluciona

En la gestión operativa tradicional de gastos y transacciones en terreno, las empresas enfrentan inconsistencias por falta de evidencias fotográficas, ausencia de trazabilidad geográfica de las compras o pagos, y demoras en la consolidación de información financiera.

**MisFinanzas** soluciona esta brecha permitiendo:
* Capturar evidencias fotográficas (comprobantes, facturas, recibos) en el momento exacto del registro.
* Georreferenciar automáticamente la ubicación geográfica de cada transacción mediante GPS y traducción a direcciones físicas legibles.
* Mantener a todos los integrantes sincronizados en tiempo real mediante notificaciones y actualizaciones reactivas en la nube.
* Delimitar responsabilidades operativas mediante roles diferenciados (Administrador vs. Operario).

---

## 🎯 2. Pilares Arquitectónicos y Técnicos Implementados

### A. Seguridad, Sesión y Control de Acceso (RBAC)
* **Firebase Authentication:** Registro e inicio de sesión funcional mediante correo electrónico y contraseña.
* **Persistencia de Sesión:** Integración de `SharedPreferences` (`SessionManager`) para auto-login al abrir la app si la sesión está activa.
* **Cierre de Sesión Seguro:** Botón de Logout que destruye el token en Firebase, limpia la caché local y redirige al Login protegiendo la pila de navegación.
* **Control de Roles (RBAC):**
  * **Administrador:** Visualización total, creación y privilegios exclusivos para eliminar registros.
  * **Operario:** Creación y visualización del historial en tiempo real, con restricción de eliminación.

### B. Datos y Multimedia en la Nube
* **Firestore en Tiempo Real:** CRUD asíncrono con sincronización reactiva en vivo mediante `addSnapshotListener`.
* **Captura Multimedia Moderna:** Uso de `ActivityResultLauncher` para captura de cámara o selección desde galería con gestión dinámica de permisos en tiempo de ejecución.
* **Firebase Storage:** Almacenamiento en la nube de imágenes comprimidas y vinculación del `downloadUrl` en cada documento.
* **Renderizado con Glide:** Carga eficiente en `RecyclerView` con estrategia de caché en disco, placeholders y prevención del bug de reciclaje.

### C. Geolocalización y Mapas Interactivos (GPS + OSMDroid)
* **Captura GPS:** Obtención de latitud y longitud mediante `FusedLocationProviderClient`.
* **Geocodificación Inversa:** Traducción de coordenadas a dirección física legible (calle/barrio/ciudad) con `Geocoder`.
* **Mapa Interactivo con OpenStreetMap (OSMDroid):**
  * Integración nativa sin requerir tarjetas de crédito ni claves de facturación (`MapView`).
  * Marcadores personalizados (`Marker`) dinámicos a partir de las coordenadas almacenadas en Firestore.
  * Ventanas de información contextual (`InfoWindow`) con título, monto y dirección de cada registro.
  * Alternancia fluida de capas de mapa (Estándar Mapnik vs. Satelital USGS) mediante un botón flotante interactivo.

### D. Notificaciones del Sistema (Locales y Push)
* **Canales de Notificación:** Registro de `NotificationChannel` para Android 8.0+ y solicitud en tiempo de ejecución del permiso `POST_NOTIFICATIONS` para Android 13+.
* **Notificación Local:** Emisión de alerta en la barra de estado con `PendingIntent` tras registrar transacciones con éxito.
* **Infraestructura FCM:** Servicio `MyFirebaseMessagingService` configurado para recibir alertas push remotas desde la consola de Firebase.

### E. Frontend y Buenas Prácticas
* **Diseño Responsivo:** Compatibilidad vertical y horizontal (Landscape) mediante contenedores `ScrollView` y componentes Material Design para evitar desbordes visuales.
* **Validaciones en Tiempo Real:** Prevención de campos vacíos o inconsistentes con `TextInputLayout.setError()`.

---

## 👥 3. Credenciales de Prueba

Para la verificación y calificación de las restricciones de rol (RBAC), utilice las siguientes cuentas de prueba:

| Rol | Correo Electrónico | Contraseña | Permisos Clave |
| :--- | :--- | :--- | :--- |
| **Administrador** | `admin@misfinanzas.com` | `Admin123*` | Control total, visualización en tiempo real y **botón de eliminación visible**. |
| **Operario** | `operario@misfinanzas.com` | `Operario123*` | Registro con foto/GPS, visualización de lista y mapas, **sin permiso de eliminación**. |

---

## 🛠️ 4. Tecnologías y Librerías Utilizadas

* **Lenguaje:** Java
* **IDE:** Android Studio
* **Arquitectura:** Componentes desacoplados (Model, View, Adapter, Service, SessionManager)
* **Backend:**
  * Firebase Auth
  * Cloud Firestore
  * Firebase Storage
  * Firebase Cloud Messaging (FCM)
* **Librerías Externas:**
  * `org.osmdroid:osmdroid-android:6.1.18` (Mapas interactivos y capas satelitales)
  * `com.github.bumptech.glide:glide:4.16.0` (Gestión eficiente de imágenes)
  * `com.google.android.gms:play-services-location:21.2.0` (Proveedor de ubicación GPS)
  * `com.google.android.material:material:1.11.0` (Componentes Material Design)

---

## 🚀 5. Instrucciones de Compilación y Ejecución

1. Clonar el repositorio:
   ```bash
   git clone [https://github.com/Erika2634/MisFinanzas2.git](https://github.com/Erika2634/MisFinanzas2.git)
