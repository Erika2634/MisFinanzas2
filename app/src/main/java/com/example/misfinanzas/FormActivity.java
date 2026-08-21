package com.example.misfinanzas;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

/**
 * Pantalla de formulario para registrar una nueva transacción en Firestore,
 * o editar/eliminar una ya existente (ubicada por su Document ID).
 *
 * Incluye validaciones de negocio en tiempo real con TextWatcher y bloquea
 * el botón de guardar mientras la operación está en curso, para evitar
 * el doble envío de datos.
 */
public class FormActivity extends AppCompatActivity {

    private static final int LONGITUD_MINIMA_CONCEPTO = 3;

    private TextInputLayout tilConcepto;
    private TextInputLayout tilMonto;
    private TextInputEditText etConcepto;
    private TextInputEditText etMonto;
    private RadioGroup rgTipo;
    private RadioButton rbIngreso;
    private MaterialButton btnGuardar;
    private MaterialButton btnEliminar;
    private ProgressBar progressBar;

    private FirestoreRepository repositorio;

    // Si es null, estamos creando una transacción nueva. Si tiene valor, estamos editando
    // el documento de Firestore con ese Document ID.
    private String documentIdActual = null;

    // Evita que el usuario dispare varias operaciones de guardado en simultáneo
    // (por ejemplo, tocando "Guardar" varias veces muy rápido).
    private boolean guardando = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_form);

        repositorio = new FirestoreRepository();

        tilConcepto = findViewById(R.id.tilConcepto);
        tilMonto = findViewById(R.id.tilMonto);
        etConcepto = findViewById(R.id.etConcepto);
        etMonto = findViewById(R.id.etMonto);
        rgTipo = findViewById(R.id.rgTipo);
        rbIngreso = findViewById(R.id.rbIngreso);
        btnGuardar = findViewById(R.id.btnGuardar);
        btnEliminar = findViewById(R.id.btnEliminar);
        progressBar = findViewById(R.id.progressBar);

        documentIdActual = getIntent().getStringExtra(MainActivity.EXTRA_DOCUMENT_ID);

        if (documentIdActual != null) {
            modoEdicion();
        }

        configurarValidacionesEnTiempoReal();

        btnGuardar.setOnClickListener(v -> guardarTransaccion());
        btnEliminar.setOnClickListener(v -> eliminarTransaccion());
    }

    /**
     * Configura la pantalla para editar una transacción existente: la busca
     * en Firestore por su Document ID y precarga los campos del formulario.
     */
    private void modoEdicion() {
        setTitle("Editar transacción");
        btnEliminar.setVisibility(View.VISIBLE);
        btnGuardar.setText("Actualizar");

        com.google.firebase.firestore.FirebaseFirestore.getInstance()
                .collection(Transaccion.COLECCION)
                .document(documentIdActual)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    Transaccion t = documentSnapshot.toObject(Transaccion.class);
                    if (t != null) {
                        etConcepto.setText(t.getConcepto());
                        etMonto.setText(String.valueOf(t.getMonto()));
                        if (t.esIngreso()) {
                            rbIngreso.setChecked(true);
                        } else {
                            findViewById(R.id.rbGasto).performClick();
                        }
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this,
                        "No se pudo cargar la transacción: " + e.getMessage(),
                        Toast.LENGTH_LONG).show());
    }

    /**
     * Validaciones de negocio en tiempo real, mientras el usuario escribe:
     *  - Concepto: advierte si el texto es demasiado corto.
     *  - Monto: advierte si es $0 o un número inválido.
     * Estas validaciones NO bloquean la escritura; solo muestran un mensaje
     * de ayuda bajo el campo (error) hasta que el valor sea válido.
     */
    private void configurarValidacionesEnTiempoReal() {
        etConcepto.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0 && s.length() < LONGITUD_MINIMA_CONCEPTO) {
                    tilConcepto.setError("El concepto es muy corto (mínimo " + LONGITUD_MINIMA_CONCEPTO + " caracteres)");
                } else {
                    tilConcepto.setError(null);
                }
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });

        etMonto.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() == 0) {
                    tilMonto.setError(null);
                    return;
                }
                try {
                    double valor = Double.parseDouble(s.toString());
                    if (valor <= 0) {
                        tilMonto.setError("El monto no puede ser $0");
                    } else {
                        tilMonto.setError(null);
                    }
                } catch (NumberFormatException e) {
                    tilMonto.setError("Ingresa un número válido");
                }
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });
    }

    /**
     * Valida el formulario por completo y, si todo es correcto, guarda la
     * transacción en Firestore (create si es nueva, update si se está editando).
     */
    private void guardarTransaccion() {
        if (guardando) {
            // Ya hay una operación de guardado en curso: ignoramos el toque
            // para evitar el doble envío del mismo dato.
            return;
        }

        String concepto = etConcepto.getText() != null ? etConcepto.getText().toString().trim() : "";
        String montoTexto = etMonto.getText() != null ? etMonto.getText().toString().trim() : "";

        // Validación obligatoria: ningún campo crítico puede llegar vacío a la base de datos
        if (TextUtils.isEmpty(concepto)) {
            tilConcepto.setError("El concepto es obligatorio");
            etConcepto.requestFocus();
            return;
        }
        if (concepto.length() < LONGITUD_MINIMA_CONCEPTO) {
            tilConcepto.setError("El concepto es muy corto (mínimo " + LONGITUD_MINIMA_CONCEPTO + " caracteres)");
            etConcepto.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(montoTexto)) {
            tilMonto.setError("El monto es obligatorio");
            etMonto.requestFocus();
            return;
        }

        double monto;
        try {
            monto = Double.parseDouble(montoTexto);
        } catch (NumberFormatException e) {
            tilMonto.setError("Monto inválido");
            etMonto.requestFocus();
            return;
        }

        if (monto <= 0) {
            tilMonto.setError("El monto debe ser mayor a $0");
            etMonto.requestFocus();
            return;
        }

        int idSeleccionado = rgTipo.getCheckedRadioButtonId();
        String tipo = (idSeleccionado == R.id.rbIngreso)
                ? Transaccion.TIPO_INGRESO
                : Transaccion.TIPO_GASTO;

        Transaccion transaccion = new Transaccion(concepto, monto, tipo);

        mostrarCargando(true);

        FirestoreRepository.OperacionCallback callback = new FirestoreRepository.OperacionCallback() {
            @Override
            public void onExito() {
                mostrarCargando(false);
                Toast.makeText(FormActivity.this,
                        documentIdActual == null ? "Transacción guardada" : "Transacción actualizada",
                        Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onError(Exception e) {
                mostrarCargando(false);
                Toast.makeText(FormActivity.this,
                        "Ocurrió un error: " + e.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        };

        if (documentIdActual == null) {
            repositorio.insertar(transaccion, callback);
        } else {
            repositorio.actualizar(documentIdActual, transaccion, callback);
        }
    }

    /**
     * Elimina el documento actual de Firestore, ubicándolo por su Document ID.
     */
    private void eliminarTransaccion() {
        if (guardando || documentIdActual == null) {
            return;
        }

        mostrarCargando(true);
        repositorio.eliminar(documentIdActual, new FirestoreRepository.OperacionCallback() {
            @Override
            public void onExito() {
                mostrarCargando(false);
                Toast.makeText(FormActivity.this, "Transacción eliminada", Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onError(Exception e) {
                mostrarCargando(false);
                Toast.makeText(FormActivity.this,
                        "Ocurrió un error al eliminar: " + e.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    /**
     * Muestra u oculta el indicador de carga y deshabilita los botones
     * mientras una operación contra Firestore está en curso, para
     * prevenir que el usuario dispare un doble envío de datos.
     */
    private void mostrarCargando(boolean cargando) {
        guardando = cargando;
        progressBar.setVisibility(cargando ? View.VISIBLE : View.GONE);
        btnGuardar.setEnabled(!cargando);
        btnEliminar.setEnabled(!cargando);
    }
}
