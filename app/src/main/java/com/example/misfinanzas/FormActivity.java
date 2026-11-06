package com.example.misfinanzas;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class FormActivity extends AppCompatActivity {

    private ImageView ivPreview;
    private TextInputLayout tilTitulo, tilMonto, tilDescripcion;
    private TextInputEditText etTitulo, etMonto, etDescripcion;
    private TextView tvDireccion;
    private ProgressBar progressBar;
    private Button btnCamara, btnGaleria, btnObtenerUbicacion, btnGuardar;

    private Uri imageUri = null;
    private Bitmap imageBitmap = null;
    private double currentLat = 0.0;
    private double currentLng = 0.0;
    private String currentAddress = "";

    private FusedLocationProviderClient fusedLocationClient;
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private FirebaseAuth auth;

    // Launchers de imágenes
    private final ActivityResultLauncher<String> galleryLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    imageUri = uri;
                    imageBitmap = null;
                    ivPreview.setImageURI(uri);
                }
            });

    private final ActivityResultLauncher<Void> cameraLauncher =
            registerForActivityResult(new ActivityResultContracts.TakePicturePreview(), bitmap -> {
                if (bitmap != null) {
                    imageBitmap = bitmap;
                    imageUri = null;
                    ivPreview.setImageBitmap(bitmap);
                }
            });

    // Launcher de permisos de ubicación
    private final ActivityResultLauncher<String[]> locationPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                Boolean fineGranted = result.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false);
                Boolean coarseGranted = result.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false);
                if (fineGranted || coarseGranted) {
                    obtenerCoordenadas();
                } else {
                    Toast.makeText(this, "Permiso de ubicación denegado", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_form);

        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        auth = FirebaseAuth.getInstance();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        ivPreview = findViewById(R.id.ivPreview);
        tilTitulo = findViewById(R.id.tilTitulo);
        tilMonto = findViewById(R.id.tilMonto);
        tilDescripcion = findViewById(R.id.tilDescripcion);
        etTitulo = findViewById(R.id.etTitulo);
        etMonto = findViewById(R.id.etMonto);
        etDescripcion = findViewById(R.id.etDescripcion);
        tvDireccion = findViewById(R.id.tvDireccion);
        progressBar = findViewById(R.id.progressBar);
        btnCamara = findViewById(R.id.btnCamara);
        btnGaleria = findViewById(R.id.btnGaleria);
        btnObtenerUbicacion = findViewById(R.id.btnObtenerUbicacion);
        btnGuardar = findViewById(R.id.btnGuardar);

        btnGaleria.setOnClickListener(v -> galleryLauncher.launch("image/*"));
        btnCamara.setOnClickListener(v -> cameraLauncher.launch(null));
        btnObtenerUbicacion.setOnClickListener(v -> verificarPermisosUbicacion());
        btnGuardar.setOnClickListener(v -> validarYGuardar());
    }

    private void verificarPermisosUbicacion() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            obtenerCoordenadas();
        } else {
            locationPermissionLauncher.launch(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            });
        }
    }

    private void obtenerCoordenadas() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null) {
                currentLat = location.getLatitude();
                currentLng = location.getLongitude();
                geocodificarInversa(location);
            } else {
                Toast.makeText(this, "Asegúrate de tener el GPS activado", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void geocodificarInversa(Location location) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                currentAddress = address.getAddressLine(0);
            } else {
                currentAddress = "Lat: " + currentLat + ", Lng: " + currentLng;
            }
        } catch (IOException e) {
            currentAddress = "Lat: " + currentLat + ", Lng: " + currentLng;
        }
        tvDireccion.setText("Ubicación: " + currentAddress);
    }

    private void validarYGuardar() {
        String titulo = etTitulo.getText() != null ? etTitulo.getText().toString().trim() : "";
        String montoStr = etMonto.getText() != null ? etMonto.getText().toString().trim() : "";
        String descripcion = etDescripcion.getText() != null ? etDescripcion.getText().toString().trim() : "";

        tilTitulo.setError(null);
        tilMonto.setError(null);

        if (TextUtils.isEmpty(titulo)) {
            tilTitulo.setError("El título es obligatorio");
            return;
        }
        if (TextUtils.isEmpty(montoStr)) {
            tilMonto.setError("El monto es obligatorio");
            return;
        }

        double monto = Double.parseDouble(montoStr);
        setLoading(true);

        // Si hay imagen, se sube a Storage primero
        if (imageUri != null || imageBitmap != null) {
            subirImagenYGuardar(titulo, descripcion, monto);
        } else {
            guardarEnFirestore(titulo, descripcion, monto, "");
        }
    }

    private void subirImagenYGuardar(String titulo, String descripcion, double monto) {
        String filename = "evidencias/" + UUID.randomUUID().toString() + ".jpg";
        StorageReference ref = storage.getReference().child(filename);

        byte[] data;
        if (imageBitmap != null) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            imageBitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
            data = baos.toByteArray();
        } else {
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
                data = baos.toByteArray();
            } catch (IOException e) {
                setLoading(false);
                Toast.makeText(this, "Error procesando imagen", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        ref.putBytes(data)
                .addOnSuccessListener(taskSnapshot -> ref.getDownloadUrl().addOnSuccessListener(uri -> {
                    guardarEnFirestore(titulo, descripcion, monto, uri.toString());
                }))
                .addOnFailureListener(e -> {
                    setLoading(false);
                    Toast.makeText(this, "Fallo al subir imagen: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void guardarEnFirestore(String titulo, String descripcion, double monto, String downloadUrl) {
        String docId = db.collection("transacciones").document().getId();
        String uid = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : "anonimo";

        Transaccion t = new Transaccion(docId, titulo, descripcion, monto, "GASTO", downloadUrl, currentLat, currentLng, currentAddress, uid);

        db.collection("transacciones").document(docId).set(t)
                .addOnSuccessListener(aVoid -> {
                    setLoading(false);
                    Toast.makeText(this, "Registro guardado exitosamente", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    setLoading(false);
                    Toast.makeText(this, "Error al guardar: " + e.getMessage(), Toast.LENGTH_SHORT).show();

                    NotificationHelper.sendLocalNotification(
                            FormActivity.this,
                            "Registro Guardado",
                            "La transacción \"" + titulo + "\" por $" + monto + " se guardó correctamente."
                    );
                });
    }

    private void setLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        btnGuardar.setEnabled(!isLoading);
    }
}