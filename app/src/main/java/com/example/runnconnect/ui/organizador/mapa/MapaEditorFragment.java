package com.example.runnconnect.ui.organizador.mapa;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.example.runnconnect.R;
import com.example.runnconnect.databinding.FragmentEditorMapaBinding;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.List;

public class MapaEditorFragment extends Fragment implements OnMapReadyCallback {

  private FragmentEditorMapaBinding binding;
  private MapaEditorViewModel viewModel;
  private GoogleMap mMap;

  // El ID del evento que viene del paso anterior
  private int idEvento = 0;

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    // Recibir argumentos (ID Evento)
    if (getArguments() != null) {
      idEvento = getArguments().getInt("idEvento", 0);
    }
  }

  public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    binding = FragmentEditorMapaBinding.inflate(inflater, container, false);
    viewModel = new ViewModelProvider(this).get(MapaEditorViewModel.class);

    // Inicializar el mapa
    SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
    if (mapFragment != null) {
      mapFragment.getMapAsync(this);
    }

    setupListeners();
    setupObservers();

    return binding.getRoot();
  }

  @Override
  public void onMapReady(@NonNull GoogleMap googleMap) {
    mMap = googleMap;

    // Configuración inicial: Zoom en San Luis (o ubicación del usuario si tuviéramos permiso GPS)
    LatLng sanLuisCentro = new LatLng(-33.29501, -66.33563);
    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sanLuisCentro, 13));

    // Habilitar controles de zoom
    mMap.getUiSettings().setZoomControlsEnabled(true);

    // Listener: Al tocar el mapa, agregamos un punto
    mMap.setOnMapClickListener(latLng -> {
      viewModel.agregarPunto(latLng);
    });
  }

  private void setupListeners() {
    // Botón Deshacer
    binding.fabUndo.setOnClickListener(v -> viewModel.deshacerUltimo());

    // Botón Guardar
    binding.btnGuardarRuta.setOnClickListener(v -> {
      if (idEvento == 0) {
        Toast.makeText(getContext(), "Error: No hay evento seleccionado", Toast.LENGTH_SHORT).show();
        return;
      }
      viewModel.guardarRuta(idEvento);
    });
  }

  private void setupObservers() {
    // Cada vez que cambia la lista de puntos, redibujamos
    viewModel.getPuntosRuta().observe(getViewLifecycleOwner(), this::dibujarRuta);

    // Mensajes
    viewModel.getMensaje().observe(getViewLifecycleOwner(), msg -> {
      if (msg != null) Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
    });

    // Loading
    viewModel.getIsLoading().observe(getViewLifecycleOwner(), loading -> {
      binding.progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
      binding.btnGuardarRuta.setEnabled(!loading);
    });

    // Navegación al terminar
    viewModel.getGuardadoExitoso().observe(getViewLifecycleOwner(), exito -> {
      if (exito) {
        // Volvemos a "Mis Eventos" o "Inicio" eliminando la pila de "Crear"
        Navigation.findNavController(requireView()).popBackStack(R.id.nav_mis_eventos, false);
      }
    });
  }

  // Método "tonto" de pintado: Borra todo y dibuja de nuevo
  private void dibujarRuta(List<LatLng> puntos) {
    if (mMap == null) return;
    mMap.clear(); // Limpiamos marcadores y líneas viejas

    if (puntos.isEmpty()) return;

    // 1. Dibujar la línea (Polyline)
    PolylineOptions polyline = new PolylineOptions()
      .addAll(puntos)
      .width(12)
      .color(Color.BLUE) // Puedes usar Color.parseColor("#1976D2")
      .geodesic(true);
    mMap.addPolyline(polyline);

    // 2. Marcador de Inicio (Verde)
    mMap.addMarker(new MarkerOptions()
      .position(puntos.get(0))
      .title("Largada")
      .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));

    // 3. Marcador de Fin (Rojo) - Solo si hay más de 1 punto
    if (puntos.size() > 1) {
      mMap.addMarker(new MarkerOptions()
        .position(puntos.get(puntos.size() - 1))
        .title("Meta")
        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
    }

    // Puntos intermedios (Puntos criticos) se verán después...
  }
}
