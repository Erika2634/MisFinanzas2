package com.example.misfinanzas;

import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

/**
 * Modelo de datos (POJO) que representa una transacción financiera
 * (Ingreso o Gasto) tal como se guarda en Firestore.
 *
 * Requisitos de un POJO para Firestore:
 *  - Debe tener un constructor vacío (sin argumentos) público.
 *  - Cada campo debe tener su getter público (Firestore los usa para el mapeo automático).
 *  - El Document ID NO se guarda como campo dentro del documento, por eso
 *    se marca con @Exclude: se maneja aparte, fuera del propio documento.
 */
public class Transaccion {

    public static final String TIPO_INGRESO = "Ingreso";
    public static final String TIPO_GASTO = "Gasto";

    // Nombre de la colección en Firestore
    public static final String COLECCION = "transacciones";

    private String concepto;
    private double monto;
    private String tipo; // "Ingreso" o "Gasto"

    @ServerTimestamp
    private Date fechaCreacion; // Firestore la llena automáticamente al crear el documento

    // Document ID de Firestore. No se guarda dentro del documento (@Exclude),
    // se asigna manualmente al leer el documento desde el SnapshotListener.
    private String documentId;

    // Constructor vacío OBLIGATORIO para que Firestore pueda mapear documentos -> objetos Java
    public Transaccion() {
    }

    // Constructor útil para crear una transacción nueva (antes de subirla a Firestore)
    public Transaccion(String concepto, double monto, String tipo) {
        this.concepto = concepto;
        this.monto = monto;
        this.tipo = tipo;
    }

    public String getConcepto() {
        return concepto;
    }

    public void setConcepto(String concepto) {
        this.concepto = concepto;
    }

    public double getMonto() {
        return monto;
    }

    public void setMonto(double monto) {
        this.monto = monto;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public Date getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(Date fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    @Exclude
    public String getDocumentId() {
        return documentId;
    }

    @Exclude
    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    @Exclude
    public boolean esIngreso() {
        return TIPO_INGRESO.equals(tipo);
    }
}
