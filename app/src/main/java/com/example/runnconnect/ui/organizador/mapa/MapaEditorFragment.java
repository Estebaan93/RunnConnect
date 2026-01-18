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
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
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
  private int idEvento = 0;

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    if (getArguments() != null) {
      idEvento = getArguments().getInt("idEvento", 0);
    }
  }

  public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    binding = FragmentEditorMapaBinding.inflate(inflater, container, false);
    viewModel = new ViewModelProvider(this).get(MapaEditorViewModel.class);

    SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
    if (mapFragment != null) {
      mapFragment.getMapAsync(this);
    }

    setupListeners();
    setupObservers();

    return binding.getRoot();
  }

  // --- ENTRADAS (Usuario -> ViewModel) ---
  private void setupListeners() {
    binding.fabUndo.setOnClickListener(v -> viewModel.deshacer());
    binding.fabLayers.setOnClickListener(v -> viewModel.alternarCapas());
    binding.btnGuardarRuta.setOnClickListener(v -> viewModel.guardarRuta(idEvento));
  }

  // --- SALIDAS (ViewModel -> UI) ---
  private void setupObservers() {

    // 1. Mostrar carga
    viewModel.getIsLoading().observe(getViewLifecycleOwner(), loading -> {
      binding.progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
      binding.btnGuardarRuta.setEnabled(!loading);
    });

    // 2. Actualizar texto distancia
    viewModel.getTextoDistancia().observe(getViewLifecycleOwner(), binding.tvDistanciaReal::setText);

    // 3. Configurar tipo de mapa
    viewModel.getTipoMapa().observe(getViewLifecycleOwner(), tipo -> {
      if (mMap != null) mMap.setMapType(tipo);
    });

    // 4. Dibujar ruta
    viewModel.getPuntosRuta().observe(getViewLifecycleOwner(), this::dibujarEnMapa);

    // 5. Mensajes de Error
    viewModel.getOrdenMostrarError().observe(getViewLifecycleOwner(), msg -> {
      if (msg != null) {
        Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
        viewModel.resetOrdenError();
      }
    });

    // 6. Orden de Zoom (Ruta cargada)
    viewModel.getOrdenHacerZoomRuta().observe(getViewLifecycleOwner(), bounds -> {
      if (bounds != null && mMap != null) {
        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100));
        viewModel.resetOrdenCamara();
      }
    });

    // 7. Orden de Centro (Evento nuevo o fallback)
    viewModel.getOrdenCentrarCamara().observe(getViewLifecycleOwner(), centro -> {
      if (centro != null && mMap != null) {
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(centro, 13));
        viewModel.resetOrdenCamara();
      }
    });

    // 8. ORDEN DE NAVEGACION
    viewModel.getOrdenNavegarSalida().observe(getViewLifecycleOwner(), mensaje -> {
      if (mensaje != null) {
        navegarAlListado(mensaje);
        viewModel.resetOrdenNavegacion();
      }
    });
  }

  @Override
  public void onMapReady(@NonNull GoogleMap googleMap) {
    mMap = googleMap;
    mMap.getUiSettings().setZoomControlsEnabled(true);
    mMap.setOnMapClickListener(latLng -> viewModel.agregarPunto(latLng));

    // Sincronizar estado inicial
    if (viewModel.getTipoMapa().getValue() != null) {
      mMap.setMapType(viewModel.getTipoMapa().getValue());
    }

    // Avisar al VM que estamos listos
    viewModel.onMapReady(idEvento);
  }

  // PINTAR (Solo visual, no calcula nada)
  private void dibujarEnMapa(List<LatLng> puntos) {
    if (mMap == null) return;
    mMap.clear();

    if (puntos == null || puntos.isEmpty()) return;

    PolylineOptions poly = new PolylineOptions()
      .addAll(puntos).width(12).color(Color.BLUE).geodesic(true);
    mMap.addPolyline(poly);

    mMap.addMarker(new MarkerOptions().position(puntos.get(0))
      .title("Largada").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));

    if (puntos.size() > 1) {
      mMap.addMarker(new MarkerOptions().position(puntos.get(puntos.size() - 1))
        .title("Meta").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
    }
  }

  // NAVEGACION
  private void navegarAlListado(String mensajeExito) {
    NavController navController = Navigation.findNavController(requireView());

    // 1. Intentar pasar mensaje al stack anterior
    try {
      if (navController.getPreviousBackStackEntry() != null) {
        navController.getPreviousBackStackEntry()
          .getSavedStateHandle()
          .set("mensaje_exito", mensajeExito);
      }
    } catch (Exception e) {}

    // 2. Intentar volver atras (Pop)
    boolean sePudoVolver = navController.popBackStack(R.id.nav_mis_eventos, false);

    // 3. Si no se pudo (vienes del menu), navegar explícitamente
    if (!sePudoVolver) {
      NavOptions options = new NavOptions.Builder()
        .setPopUpTo(R.id.nav_crear_evento, true) // Limpiar historial
        .setLaunchSingleTop(true)
        .build();

      Bundle args = new Bundle();
      args.putString("mensaje_arg", mensajeExito);

      navController.navigate(R.id.nav_mis_eventos, args, options);
    }
  }
}