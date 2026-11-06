package com.example.misfinanzas;

public class Transaccion {
    // Constante que busca FirestoreRepository
    public static final String COLECCION = "transacciones";

    private String id;
    private String documentId; // Requerido por FirestoreRepository
    private String titulo;
    private String descripcion;
    private double monto;
    private String tipo; // "INGRESO" o "GASTO"
    private String imageUrl;
    private double latitud;
    private double longitud;
    private String direccion;
    private String usuarioUid;

    public Transaccion() {} // Constructor vacío para Firestore

    public Transaccion(String id, String titulo, String descripcion, double monto,
                       String tipo, String imageUrl, double latitud, double longitud,
                       String direccion, String usuarioUid) {
        this.id = id;
        this.documentId = id;
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.monto = monto;
        this.tipo = tipo;
        this.imageUrl = imageUrl;
        this.latitud = latitud;
        this.longitud = longitud;
        this.direccion = direccion;
        this.usuarioUid = usuarioUid;
    }

    public String getId() { return id != null ? id : documentId; }
    public void setId(String id) {
        this.id = id;
        this.documentId = id;
    }

    public String getDocumentId() { return documentId != null ? documentId : id; }
    public void setDocumentId(String documentId) {
        this.documentId = documentId;
        this.id = documentId;
    }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public double getMonto() { return monto; }
    public void setMonto(double monto) { this.monto = monto; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public double getLatitud() { return latitud; }
    public void setLatitud(double latitud) { this.latitud = latitud; }

    public double getLongitud() { return longitud; }
    public void setLongitud(double longitud) { this.longitud = longitud; }

    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }

    public String getUsuarioUid() { return usuarioUid; }
    public void setUsuarioUid(String usuarioUid) { this.usuarioUid = usuarioUid; }
}