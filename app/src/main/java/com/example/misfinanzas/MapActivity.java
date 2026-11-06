package com.example.misfinanzas;

import android.content.Context;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

public class MapActivity extends AppCompatActivity {

    private MapView mapView;
    private FirebaseFirestore db;
    private boolean isSatellite = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Configuración obligatoria del agente de usuario para OSM
        Context ctx = getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));
        Configuration.getInstance().setUserAgentValue(getPackageName());

        setContentView(R.layout.activity_map);

        db = FirebaseFirestore.getInstance();
        mapView = findViewById(R.id.mapView);
        mapView.setMultiTouchControls(true);
        mapView.getController().setZoom(14.0);

        FloatingActionButton fabToggleLayer = findViewById(R.id.fabToggleLayer);
        fabToggleLayer.setOnClickListener(v -> alternarCapa());

        cargarPuntosFirestore();
    }

    private void alternarCapa() {
        if (isSatellite) {
            mapView.setTileSource(TileSourceFactory.MAPNIK); // Modo normal
            Toast.makeText(this, "Modo Normal (Mapnik)", Toast.LENGTH_SHORT).show();
            isSatellite = false;
        } else {
            mapView.setTileSource(TileSourceFactory.USGS_SAT); // Capa satelital pública
            Toast.makeText(this, "Modo Satélite", Toast.LENGTH_SHORT).show();
            isSatellite = true;
        }
    }

    private void cargarPuntosFirestore() {
        db.collection("transacciones").get().addOnSuccessListener(queryDocumentSnapshots -> {
            if (queryDocumentSnapshots.isEmpty()) return;

            GeoPoint lastPoint = null;
            for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                Transaccion t = doc.toObject(Transaccion.class);
                if (t.getLatitud() != 0.0 && t.getLongitud() != 0.0) {
                    GeoPoint point = new GeoPoint(t.getLatitud(), t.getLongitud());
                    lastPoint = point;

                    Marker marker = new Marker(mapView);
                    marker.setPosition(point);
                    marker.setTitle(t.getTitulo());
                    marker.setSnippet("Monto: $" + t.getMonto() + "\n" + (t.getDireccion() != null ? t.getDireccion() : ""));
                    mapView.getOverlays().add(marker);
                }
            }

            if (lastPoint != null) {
                mapView.getController().setCenter(lastPoint);
            }
            mapView.invalidate(); // Refrescar el mapa
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }
}