package com.example.runnconnect.ui.eventosPublicos.mapa;


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

import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;

public class MapaPublicoActivity extends AppCompatActivity implements OnMapReadyCallback {

  private ActivityMapaPublicoBinding binding;
  private MapaPublicoViewModel viewModel;
  private GoogleMap mMap;

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

    viewModel = new ViewModelProvider(this).get(MapaPublicoViewModel.class);

    //iniciar Mapa
    SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
      .findFragmentById(R.id.map);
    if (mapFragment != null) {
      mapFragment.getMapAsync(this);
    }

    binding.btnVolver.setOnClickListener(v -> finish());

    binding.fabLayers.setOnClickListener(v -> viewModel.alternarTipoMapa());
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

    viewModel.getLineaRuta().observe(this, poly -> {
      mMap.clear();
      mMap.addPolyline(poly);
      dibujarMarcadoresPOI(viewModel.getListaPuntosInteres().getValue());
    });

    viewModel.getMarcadoresInicioFin().observe(this, extremos -> {
      extremos.forEach(mMap::addMarker);
    });

    viewModel.getFlechasGuias().observe(this, this::dibujarFlechasVisuales);
    viewModel.getListaPuntosInteres().observe(this, this::dibujarMarcadoresPOI);
    viewModel.getTextoDistancia().observe(this, binding.tvDistancia::setText);

    //zoom automatico
    viewModel.getOrdenHacerZoomRuta().observe(this, bounds ->
      mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100)));

    //centrar (fallback)
    viewModel.getOrdenCentrarCamara().observe(this, latLng ->
      mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15)));

    //flecha de volver (sin if)
    viewModel.getVolverAtras().observe(this, signal -> finish());

    //finalizacion
    viewModel.getFinalUser().observe(this, signal -> finish());
  }

  @Override
  public void onMapReady(GoogleMap googleMap) {
    mMap = googleMap;
    mMap.getUiSettings().setZoomControlsEnabled(true);
    //mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL); // Mapa ligero no satelital
    setupObservers();
    int idEvento = getIntent().getIntExtra("idEvento", 0);
    viewModel.onMapaListo(idEvento);

  }

  private void dibujarFlechasVisuales(List<MarkerOptions> opciones) {
    opciones.forEach(mMap::addMarker);
  }

  private void dibujarMarcadoresPOI(List<MarkerOptions> opciones) {
    opciones.forEach(mMap::addMarker);
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