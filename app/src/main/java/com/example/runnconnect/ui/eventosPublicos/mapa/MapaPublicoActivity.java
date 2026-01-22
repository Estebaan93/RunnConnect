package com.example.runnconnect.ui.eventosPublicos.mapa;

import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.runnconnect.R;
import com.example.runnconnect.databinding.ActivityMapaPublicoBinding;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.List;

public class MapaPublicoActivity extends AppCompatActivity implements OnMapReadyCallback {

  private ActivityMapaPublicoBinding binding;
  private MapaPublicoViewModel viewModel;
  private GoogleMap mMap;
  private int idEvento;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    binding = ActivityMapaPublicoBinding.inflate(getLayoutInflater());
    setContentView(binding.getRoot());

    // Configurar Barra Superior
    if (getSupportActionBar() != null) {
      getSupportActionBar().setTitle("Recorrido del Evento");
      getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    idEvento = getIntent().getIntExtra("idEvento", 0);
    viewModel = new ViewModelProvider(this).get(MapaPublicoViewModel.class);

    // Iniciar Mapa
    SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
      .findFragmentById(R.id.map);
    if (mapFragment != null) {
      mapFragment.getMapAsync(this);
    }

    binding.btnVolver.setOnClickListener(v -> finish());

    binding.fabLayers.setOnClickListener(v -> viewModel.alternarTipoMapa());

    setupObservers();
  }

  private void setupObservers() {
    // 1. Dibujar Ruta
    viewModel.getPuntosRuta().observe(this, this::dibujarRutaEnMapa);

    viewModel.getTipoMapa().observe(this, tipo -> {
      if (mMap != null) {
        mMap.setMapType(tipo);
      }
    });

    // 2. Hacer Zoom Automático
    viewModel.getOrdenHacerZoomRuta().observe(this, bounds -> {
      if (mMap != null && bounds != null) {
        try {
          // Padding de 100px para que la ruta no toque los bordes
          mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100));
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    });

    // 3. Fallback Centrar
    viewModel.getOrdenCentrarCamara().observe(this, latLng -> {
      if (mMap != null && latLng != null) {
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
      }
    });

    // 4. Mostrar Distancia en el subtítulo
    viewModel.getTextoDistancia().observe(this, dist -> {
      if (!dist.isEmpty() && getSupportActionBar() != null) {
        getSupportActionBar().setSubtitle("Distancia total: " + dist);
      }
    });

    // 5. Errores
    viewModel.getMensajeError().observe(this, msg -> {
      if (msg != null) Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    });
  }

  @Override
  public void onMapReady(GoogleMap googleMap) {
    mMap = googleMap;
    mMap.getUiSettings().setZoomControlsEnabled(true);
    mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL); // Mapa ligero

    if (idEvento != 0) {
      viewModel.cargarRuta(idEvento);
    } else {
      Toast.makeText(this, "Error: Evento no identificado", Toast.LENGTH_SHORT).show();
    }

    if (viewModel.getTipoMapa().getValue() != null) {
      mMap.setMapType(viewModel.getTipoMapa().getValue());
    }
  }

  private void dibujarRutaEnMapa(List<LatLng> puntos) {
    if (mMap == null || puntos == null || puntos.isEmpty()) return;

    mMap.clear();

    // Línea Poligonal
    PolylineOptions poly = new PolylineOptions()
      .addAll(puntos)
      .width(12)
      .color(Color.BLUE) // Azul clásico de rutas
      .geodesic(true);
    mMap.addPolyline(poly);

    // Marcador Inicio
    mMap.addMarker(new MarkerOptions()
      .position(puntos.get(0))
      .title("Largada")
      .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));

    // Marcador Fin (Solo si hay más de 1 punto)
    if (puntos.size() > 1) {
      mMap.addMarker(new MarkerOptions()
        .position(puntos.get(puntos.size() - 1))
        .title("Meta")
        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
    }
  }

  @Override
  public boolean onOptionsItemSelected(@NonNull MenuItem item) {
    if (item.getItemId() == android.R.id.home) {
      finish();
      return true;
    }
    return super.onOptionsItemSelected(item);
  }
}