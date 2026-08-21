# Mis Finanzas — Gestor de Gastos Personales (con Firebase Firestore)

**Nombre completo del estudiante:** Erika Shirley Arevalo Bernal

## 📱 Descripción de la aplicación

**Mis Finanzas** es una aplicación nativa para Android, desarrollada en Java, que permite
llevar un registro de las finanzas personales del usuario **en tiempo real y en la nube**.
Permite registrar ingresos y gastos, visualizarlos en una lista dinámica con tarjetas
(CardView) e íconos, editar o eliminar registros, y todos los cambios se sincronizan
automáticamente entre cualquier dispositivo conectado a la misma base de datos gracias a
**Firebase Firestore**.

### Funcionalidades principales

- **Sincronización en tiempo real** con Firestore mediante `SnapshotListener`: si un
  registro se crea, edita o elimina desde otro teléfono, todos los demás lo ven al instante,
  sin necesidad de refrescar la pantalla.
- **CRUD completo en NoSQL**: crear, leer, actualizar y eliminar documentos de la colección
  `transacciones`, cada uno identificado por su `Document ID` generado por Firestore.
- **Interfaz Material Design**: tarjetas con sombra y bordes redondeados, íconos vectoriales,
  y formularios con `TextInputLayout`.
- **Validaciones en tiempo real**: el formulario advierte con un `TextWatcher` si el concepto
  es muy corto o si el monto es $0, antes incluso de intentar guardar.
- **Prevención de doble envío**: el botón "Guardar" se deshabilita mientras la operación
  está en curso contra Firestore.
- **Liberación de memoria**: el `ListenerRegistration` del `SnapshotListener` se libera con
  `.remove()` en `onStop()`/`onDestroy()` de `MainActivity`.

## 🏗️ Arquitectura del proyecto

| Clase / Archivo | Responsabilidad |
|---|---|
| `Transaccion.java` | Modelo de datos (POJO) mapeado automáticamente por Firestore. |
| `FirestoreRepository.java` | Centraliza el CRUD y el `SnapshotListener` contra la colección `transacciones`. |
| `TransaccionAdapter.java` | Adapter del `RecyclerView`, con `ViewHolder` y tarjetas con ícono. |
| `MainActivity.java` | Pantalla principal: lista en tiempo real + saldo total + botón para agregar. |
| `FormActivity.java` | Formulario de creación / edición / eliminación, con validaciones y `TextInputLayout`. |

## 🗄️ Estructura de datos en Firestore

**Colección:** `transacciones`

Cada documento (identificado por un `Document ID` autogenerado) contiene:

| Campo | Tipo | Descripción |
|---|---|---|
| `concepto` | string | Descripción breve del movimiento |
| `monto` | number | Valor de la transacción |
| `tipo` | string | `"Ingreso"` o `"Gasto"` |
| `fechaCreacion` | timestamp | Se asigna automáticamente al crear el documento (`@ServerTimestamp`) |

## 🔥 Cómo configurar tu propio proyecto de Firebase (PASO OBLIGATORIO)

Este repositorio **no incluye** el archivo `google-services.json` porque contiene
credenciales específicas de cada proyecto de Firebase. Sin este archivo, la compilación
fallará. Sigue estos pasos:

1. Ve a [https://console.firebase.google.com](https://console.firebase.google.com) e inicia
   sesión con tu cuenta de Google.
2. Haz clic en **Agregar proyecto**, ponle un nombre (ej. `mis-finanzas`) y créalo.
3. Dentro del proyecto, ve a **Compilación → Firestore Database → Crear base de datos**.
   Elige **Iniciar en modo de prueba** (esto habilita lectura/escritura abierta por 30 días,
   suficiente para el desarrollo y la sustentación de esta actividad).
4. Ve a **Configuración del proyecto** (ícono de engranaje) → pestaña **Tus apps** →
   haz clic en el ícono de Android para agregar una app.
5. En **"Nombre del paquete de Android"** escribe exactamente:
   `com.example.misfinanzas`
6. Descarga el archivo `google-services.json` que te ofrece Firebase.
7. Copia ese archivo dentro de la carpeta `app/` de este proyecto (al mismo nivel que
   `build.gradle` del módulo app), reemplazando cualquier archivo existente.
8. En Android Studio, sincroniza el proyecto con Gradle (elefante con flecha 🔄, o
   "Sync Now" si aparece el aviso).
9. Ejecuta la app. Ya debería guardar y leer datos directamente desde tu proyecto de
   Firestore en la nube.

> ⚠️ **Nota sobre las reglas de seguridad:** el modo de prueba de Firestore expira a los
> 30 días. Para uso académico está bien, pero si necesitas reglas permanentes puedes
> configurar, en la pestaña **Reglas** de Firestore, algo como:
> ```
> rules_version = '2';
> service cloud.firestore {
>   match /databases/{database}/documents {
>     match /transacciones/{documentId} {
>       allow read, write: if true;
>     }
>   }
> }
> ```
> (Esto es solo para pruebas/entrega académica; en producción se debe usar Firebase
> Authentication y reglas más restrictivas).

## 🖼️ Capturas de pantalla

> Reemplaza las imágenes de la carpeta `/screenshots` por capturas reales de tu app en
> funcionamiento antes de entregar el repositorio.

### Lista de transacciones (sincronizada en tiempo real)
![Lista de transacciones](screenshots/lista_transacciones.png)

### Formulario de registro con validaciones
![Formulario de registro](screenshots/formulario_registro.png)

## ▶️ Cómo ejecutar el proyecto

1. Clona este repositorio.
2. Sigue la sección **"Cómo configurar tu propio proyecto de Firebase"** de arriba
   (agrega tu `google-services.json`).
3. Ábrelo con **Android Studio** y espera a que Gradle sincronice.
4. Ejecuta la app en un emulador o dispositivo físico con **Android 5.0 (API 21)** o superior
   y con conexión a internet.

## 🧰 Tecnologías utilizadas

- Java
- Android SDK nativo
- **Firebase Firestore** (base de datos NoSQL en tiempo real)
- `RecyclerView` + `ViewHolder`
- `CardView` con Material Design
- `TextInputLayout` / `TextInputEditText`
- `TextWatcher` para validaciones en tiempo real
