# Mis Finanzas — Gestor de Gastos Personales (con Firebase Firestore)

**Nombre completo del estudiante:** Erika Shirley Arevalo Bernal

## Descripción de la aplicación

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

## Arquitectura del proyecto

| Clase / Archivo | Responsabilidad |
|---|---|
| `Transaccion.java` | Modelo de datos (POJO) mapeado automáticamente por Firestore. |
| `FirestoreRepository.java` | Centraliza el CRUD y el `SnapshotListener` contra la colección `transacciones`. |
| `TransaccionAdapter.java` | Adapter del `RecyclerView`, con `ViewHolder` y tarjetas con ícono. |
| `MainActivity.java` | Pantalla principal: lista en tiempo real + saldo total + botón para agregar. |
| `FormActivity.java` | Formulario de creación / edición / eliminación, con validaciones y `TextInputLayout`. |

## Estructura de datos en Firestore

**Colección:** `transacciones`

Cada documento (identificado por un `Document ID` autogenerado) contiene:

| Campo | Tipo | Descripción |
|---|---|---|
| `concepto` | string | Descripción breve del movimiento |
| `monto` | number | Valor de la transacción |
| `tipo` | string | `"Ingreso"` o `"Gasto"` |
| `fechaCreacion` | timestamp | Se asigna automáticamente al crear el documento (`@ServerTimestamp`) |

## Capturas de pantalla

### Lista de transacciones (sincronizada en tiempo real)
![Lista de transacciones](screenshots/lista_transacciones.png)

### Formulario de registro con validaciones
![Formulario de registro](screenshots/formulario_registro.png)

## Tecnologías utilizadas

- Java
- Android SDK nativo
- **Firebase Firestore** (base de datos NoSQL en tiempo real)
- `RecyclerView` + `ViewHolder`
- `CardView` con Material Design
- `TextInputLayout` / `TextInputEditText`
- `TextWatcher` para validaciones en tiempo real
