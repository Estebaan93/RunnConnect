package com.example.runnconnect.ui.eventosPublicos.mapa;

import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

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
  //private int idEvento;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    binding = ActivityMapaPublicoBinding.inflate(getLayoutInflater());
    setContentView(binding.getRoot());

    //configurar Barra Superior
    if (getSupportActionBar() != null) {
      getSupportActionBar().setTitle("Recorrido del Evento");
      getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    //idEvento = getIntent().getIntExtra("idEvento", 0);
    viewModel = new ViewModelProvider(this).get(MapaPublicoViewModel.class);

    //iniciar Mapa
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
    // carga
    viewModel.getIsLoading().observe(this, loading ->
      binding.progressBar.setVisibility(loading ? View.VISIBLE : View.GONE));

    //error
    viewModel.getErrorText().observe(this, binding.tvError::setText);
    viewModel.getErrorVisibility().observe(this, binding.tvError::setVisibility);

    //tipo de mapa (siempre se recibe con el mapa listo)
    viewModel.getTipoMapa().observe(this, tipo ->mMap.setMapType(tipo));

    //ruta
    viewModel.getPuntosRuta().observe(this, this::dibujarRutaEnMapa);

    //distancia (se muestra en un TextView especifico)
    viewModel.getTextoDistancia().observe(this, binding.tvDistancia::setText);

    //meta (marcador adicional)
    viewModel.getPuntoMeta().observe(this, latLng -> {
      mMap.addMarker(new MarkerOptions()
        .position(latLng)
        .title("Meta")
        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
    });

    //zoom automatico
    viewModel.getOrdenHacerZoomRuta().observe(this, bounds ->
      mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100)));

    //centrar (fallback)
    viewModel.getOrdenCentrarCamara().observe(this, latLng ->
      mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15)));

    //flecha de volver (sin if)
    viewModel.getVolverAtras().observe(this, signal -> finish());

    //finalizacion
    viewModel.getFinalUser().observe(this, finish -> {
      if (Boolean.TRUE.equals(finish)) finish();
    });
  }

  @Override
  public void onMapReady(GoogleMap googleMap) {
    mMap = googleMap;
    mMap.getUiSettings().setZoomControlsEnabled(true);
    //mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL); // Mapa ligero no satelital
    int idEvento = getIntent().getIntExtra("idEvento", 0);
    viewModel.onMapaListo(idEvento);

  }

  private void dibujarRutaEnMapa(List<LatLng> puntos) {
    // Polyline
    PolylineOptions poly = new PolylineOptions()
      .addAll(puntos)
      .width(12)
      .color(Color.BLUE)
      .geodesic(true);
    mMap.addPolyline(poly);

    // Marcador inicio (siempre el primer punto)
    mMap.addMarker(new MarkerOptions()
      .position(puntos.get(0))
      .title("Largada")
      .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
    // El marcador de meta se añade por separado mediante puntoMeta

  }

  @Override
  public boolean onOptionsItemSelected(@NonNull MenuItem item) {
    if (item.getItemId() == android.R.id.home) {
      viewModel.flechaVolverAtras();
      return true;
    }
    return super.onOptionsItemSelected(item);
  }
}