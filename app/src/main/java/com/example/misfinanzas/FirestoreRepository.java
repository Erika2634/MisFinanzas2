package com.example.misfinanzas;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * Repositorio centralizado para todas las operaciones CRUD contra la
 * colección "transacciones" de Firebase Firestore.
 *
 * Aísla el resto de la app (Activities, Adapter) del SDK de Firestore:
 * si el día de mañana se cambia de backend, solo se toca esta clase.
 */
public class FirestoreRepository {

    private final CollectionReference coleccionTransacciones;

    public FirestoreRepository() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        coleccionTransacciones = db.collection(Transaccion.COLECCION);
    }

    /**
     * Interfaz genérica de callback para operaciones de escritura (crear, actualizar, eliminar).
     */
    public interface OperacionCallback {
        void onExito();
        void onError(Exception e);
    }

    // ------------------ CREATE ------------------

    /**
     * Inserta una nueva transacción como un nuevo documento en la colección.
     * Firestore genera automáticamente el Document ID.
     */
    public void insertar(Transaccion transaccion, OperacionCallback callback) {
        coleccionTransacciones
                .add(transaccion)
                .addOnSuccessListener(new OnSuccessListener<com.google.firebase.firestore.DocumentReference>() {
                    @Override
                    public void onSuccess(com.google.firebase.firestore.DocumentReference documentReference) {
                        callback.onExito();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        callback.onError(e);
                    }
                });
    }

    // ------------------ UPDATE ------------------

    /**
     * Actualiza un documento existente, ubicándolo por su Document ID.
     */
    public void actualizar(String documentId, Transaccion transaccion, OperacionCallback callback) {
        coleccionTransacciones.document(documentId)
                .set(transaccion)
                .addOnSuccessListener(unused -> callback.onExito())
                .addOnFailureListener(callback::onError);
    }

    // ------------------ DELETE ------------------

    /**
     * Elimina un documento de la colección, ubicándolo por su Document ID.
     */
    public void eliminar(String documentId, OperacionCallback callback) {
        coleccionTransacciones.document(documentId)
                .delete()
                .addOnSuccessListener(unused -> callback.onExito())
                .addOnFailureListener(callback::onError);
    }

    // ------------------ READ (tiempo real) ------------------

    /**
     * Interfaz de callback para recibir la lista actualizada de transacciones
     * cada vez que hay un cambio en la colección (tiempo real).
     */
    public interface ListaTransaccionesListener {
        void onListaActualizada(List<Transaccion> lista);
        void onError(Exception e);
    }

    /**
     * Se suscribe a la colección "transacciones" con un SnapshotListener: cada vez
     * que se crea, edita o elimina un documento (desde cualquier dispositivo),
     * este callback se dispara de nuevo automáticamente con la lista actualizada.
     *
     * IMPORTANTE: guarda el ListenerRegistration devuelto y llama a .remove()
     * en onDestroy() de la Activity para liberar memoria y evitar fugas.
     */
    public ListenerRegistration escucharTransacciones(ListaTransaccionesListener listener) {
        return coleccionTransacciones
                .orderBy("fechaCreacion", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(QuerySnapshot querySnapshot, FirebaseFirestoreException error) {
                        if (error != null) {
                            listener.onError(error);
                            return;
                        }

                        List<Transaccion> lista = new ArrayList<>();
                        if (querySnapshot != null) {
                            for (QueryDocumentSnapshot doc : querySnapshot) {
                                // Mapeo automático documento -> objeto Java (POJO)
                                Transaccion t = doc.toObject(Transaccion.class);
                                t.setDocumentId(doc.getId());
                                lista.add(t);
                            }
                        }
                        listener.onListaActualizada(lista);
                    }
                });
    }
}
