package com.example.misfinanzas;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TransaccionAdapter adapter;
    private final List<Transaccion> transacciones = new ArrayList<>();

    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private SessionManager sessionManager;
    private ListenerRegistration firestoreListener;

    private final ActivityResultLauncher<String> requestNotifPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {});

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sessionManager = new SessionManager(this);
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        if (!sessionManager.isLoggedIn() || auth.getCurrentUser() == null) {
            cerrarSesionSegura();
            return;
        }

        setContentView(R.layout.activity_main);

        NotificationHelper.createNotificationChannel(this);
        pedirPermisoNotificacionesAndroid13();

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        boolean esAdmin = sessionManager.isAdmin();
        adapter = new TransaccionAdapter(transacciones, esAdmin, this::confirmarEliminacion);
        recyclerView.setAdapter(adapter);

        FloatingActionButton fabAdd = findViewById(R.id.fabAdd);
        FloatingActionButton fabMap = findViewById(R.id.fabMap);

        fabAdd.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, FormActivity.class)));
        fabMap.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, MapActivity.class)));

        escucharTransaccionesEnTiempoReal();
    }

    private void pedirPermisoNotificacionesAndroid13() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestNotifPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }
    }

    private void escucharTransaccionesEnTiempoReal() {
        // addSnapshotListener garantiza sincronización en vivo
        firestoreListener = db.collection("transacciones")
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        Toast.makeText(this, "Error al sincronizar: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (snapshots != null) {
                        transacciones.clear();
                        for (QueryDocumentSnapshot doc : snapshots) {
                            Transaccion t = doc.toObject(Transaccion.class);
                            transacciones.add(t);
                        }
                        adapter.notifyDataSetChanged();
                    }
                });
    }

    private void confirmarEliminacion(Transaccion t) {
        new AlertDialog.Builder(this)
                .setTitle("Eliminar Registro")
                .setMessage("¿Deseas eliminar la transacción \"" + t.getTitulo() + "\"?")
                .setPositiveButton("Eliminar", (dialog, which) -> {
                    db.collection("transacciones").document(t.getId()).delete()
                            .addOnSuccessListener(aVoid ->
                                    Toast.makeText(this, "Registro eliminado", Toast.LENGTH_SHORT).show())
                            .addOnFailureListener(err ->
                                    Toast.makeText(this, "Error al eliminar: " + err.getMessage(), Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void cerrarSesionSegura() {
        sessionManager.clearSession();
        auth.signOut();
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, 1, 0, "Cerrar Sesión").setIcon(android.R.drawable.ic_lock_power_off).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == 1) {
            cerrarSesionSegura();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (firestoreListener != null) {
            firestoreListener.remove();
        }
    }


}