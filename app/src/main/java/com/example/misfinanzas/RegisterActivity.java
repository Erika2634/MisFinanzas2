package com.example.misfinanzas;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class RegisterActivity extends AppCompatActivity {

    private TextInputLayout tilNombre, tilEmail, tilPassword;
    private TextInputEditText etNombre, etEmail, etPassword;
    private RadioButton rbAdmin;
    private Button btnRegistrar;
    private TextView tvVolverLogin;
    private ProgressBar progressBar;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        sessionManager = new SessionManager(this);

        tilNombre = findViewById(R.id.tilRegNombre);
        tilEmail = findViewById(R.id.tilRegEmail);
        tilPassword = findViewById(R.id.tilRegPassword);
        etNombre = findViewById(R.id.etRegNombre);
        etEmail = findViewById(R.id.etRegEmail);
        etPassword = findViewById(R.id.etRegPassword);
        rbAdmin = findViewById(R.id.rbAdmin);
        btnRegistrar = findViewById(R.id.btnRegistrar);
        tvVolverLogin = findViewById(R.id.tvVolverLogin);
        progressBar = findViewById(R.id.pbRegistro);

        btnRegistrar.setOnClickListener(v -> registrarUsuario());
        tvVolverLogin.setOnClickListener(v -> finish());
    }

    private void registrarUsuario() {
        String nombre = etNombre.getText() != null ? etNombre.getText().toString().trim() : "";
        String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
        String password = etPassword.getText() != null ? etPassword.getText().toString().trim() : "";
        String rol = rbAdmin.isChecked() ? "ADMIN" : "OPERARIO";

        tilNombre.setError(null);
        tilEmail.setError(null);
        tilPassword.setError(null);

        if (TextUtils.isEmpty(nombre)) {
            tilNombre.setError("Ingresa tu nombre");
            return;
        }
        if (TextUtils.isEmpty(email)) {
            tilEmail.setError("Ingresa tu correo");
            return;
        }
        if (TextUtils.isEmpty(password) || password.length() < 6) {
            tilPassword.setError("La contraseña debe tener al menos 6 caracteres");
            return;
        }

        setLoading(true);

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser firebaseUser = authResult.getUser();
                    if (firebaseUser != null) {
                        guardarDatosUsuario(firebaseUser.getUid(), nombre, email, rol);
                    }
                })
                .addOnFailureListener(e -> {
                    setLoading(false);
                    Toast.makeText(RegisterActivity.this, "Error en registro: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void guardarDatosUsuario(String uid, String nombre, String email, String rol) {
        Usuario usuario = new Usuario(uid, email, nombre, rol);

        db.collection("usuarios").document(uid).set(usuario)
                .addOnSuccessListener(aVoid -> {
                    setLoading(false);
                    // Guardar sesión local en SharedPreferences
                    sessionManager.saveSession(uid, email, rol);

                    Toast.makeText(this, "Usuario registrado como: " + rol, Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    setLoading(false);
                    Toast.makeText(this, "Error guardando rol: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void setLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        btnRegistrar.setEnabled(!isLoading);
    }
}