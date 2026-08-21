package com.example.misfinanzas;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.List;
import java.util.Locale;

/**
 * Pantalla principal: muestra el listado de todas las transacciones
 * (ingresos y gastos) en TIEMPO REAL, conectado directamente a Firebase
 * Firestore mediante un SnapshotListener. Ya no lee de una base de datos
 * local: cualquier cambio hecho desde otro teléfono se refleja aquí
 * automáticamente, sin necesidad de refrescar la pantalla.
 */
public class MainActivity extends AppCompatActivity implements TransaccionAdapter.OnItemClickListener {

    public static final String EXTRA_DOCUMENT_ID = "extra_document_id";

    private RecyclerView rvTransacciones;
    private TextView tvSaldoTotal;
    private TextView tvVacio;
    private FloatingActionButton fabAgregar;

    private FirestoreRepository repositorio;
    private TransaccionAdapter adapter;

    // Referencia a la suscripción activa de Firestore. Debe liberarse en onDestroy()
    // para no dejar el listener escuchando en segundo plano y evitar fugas de memoria.
    private ListenerRegistration listenerRegistration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        repositorio = new FirestoreRepository();

        rvTransacciones = findViewById(R.id.rvTransacciones);
        tvSaldoTotal = findViewById(R.id.tvSaldoTotal);
        tvVacio = findViewById(R.id.tvVacio);
        fabAgregar = findViewById(R.id.fabAgregar);

        rvTransacciones.setLayoutManager(new LinearLayoutManager(this));

        adapter = new TransaccionAdapter(new java.util.ArrayList<>(), this);
        rvTransacciones.setAdapter(adapter);

        fabAgregar.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, FormActivity.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Nos suscribimos al SnapshotListener cuando la pantalla se vuelve visible.
        suscribirseATransacciones();
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Cancelamos la suscripción cuando la pantalla deja de estar visible,
        // para no seguir recibiendo actualizaciones ni consumiendo memoria/red.
        if (listenerRegistration != null) {
            listenerRegistration.remove();
            listenerRegistration = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Segunda liberación de seguridad: garantiza que el ListenerRegistration
        // quede liberado sí o sí al destruirse la Activity.
        if (listenerRegistration != null) {
            listenerRegistration.remove();
            listenerRegistration = null;
        }
    }

    /**
     * Se conecta a la colección "transacciones" de Firestore. Cada vez que
     * hay un cambio (crear, editar, eliminar, desde cualquier dispositivo),
     * este callback se dispara de nuevo automáticamente con la lista actualizada.
     */
    private void suscribirseATransacciones() {
        listenerRegistration = repositorio.escucharTransacciones(new FirestoreRepository.ListaTransaccionesListener() {
            @Override
            public void onListaActualizada(List<Transaccion> lista) {
                actualizarUI(lista);
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(MainActivity.this,
                        "Error al sincronizar con Firestore: " + e.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    /**
     * Actualiza el RecyclerView, el saldo total y el mensaje de "vacío"
     * cada vez que llega una nueva instantánea (snapshot) desde Firestore.
     */
    private void actualizarUI(List<Transaccion> lista) {
        adapter.actualizarLista(lista);

        double saldo = 0.0;
        for (Transaccion t : lista) {
            saldo += t.esIngreso() ? t.getMonto() : -t.getMonto();
        }
        tvSaldoTotal.setText(String.format(Locale.getDefault(), "$ %,.2f", saldo));

        if (lista.isEmpty()) {
            tvVacio.setVisibility(View.VISIBLE);
            rvTransacciones.setVisibility(View.GONE);
        } else {
            tvVacio.setVisibility(View.GONE);
            rvTransacciones.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Al tocar una tarjeta de la lista, abrimos el formulario en modo edición,
     * enviando el Document ID de Firestore de la transacción seleccionada.
     */
    @Override
    public void onItemClick(Transaccion transaccion) {
        Intent intent = new Intent(MainActivity.this, FormActivity.class);
        intent.putExtra(EXTRA_DOCUMENT_ID, transaccion.getDocumentId());
        startActivity(intent);
    }
}
