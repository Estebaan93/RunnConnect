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
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.List;

public class MapaEditorFragment extends Fragment implements OnMapReadyCallback {

  private FragmentEditorMapaBinding binding;
  private MapaEditorViewModel viewModel;
  private GoogleMap mMap;

  // El ID del evento que viene del paso anterior
  private int idEvento = 0;

  //bandera para centrar la camara
  private boolean camaraCentradaInicialmente= false;

  //variable para alternar tipo de mapa
  private int currentMapType= GoogleMap.MAP_TYPE_HYBRID;

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

    // INICIO: Modo Hhbrido (Mejor para Trail/Montaña por defecto)
    mMap.setMapType(currentMapType);

    // Habilitar controles de zoom
    mMap.getUiSettings().setZoomControlsEnabled(true);

    /*si el evento es nuevo se muestra el centro de san luis -
    * si el evnto existe cargamos el punto 1 o largada para enfocar la ruta*/

    if (idEvento == 0) {
      LatLng sanLuisCentro = new LatLng(-33.29501, -66.33563);
      mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sanLuisCentro, 13));
    } else {
      // Si hay ID, cargamos la ruta. La cámara se moverá en 'dibujarRuta'
      viewModel.cargarRutaExistente(idEvento);
    }
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
    // NUEVO: Botón flotante para cambiar capas
    binding.fabLayers.setOnClickListener(v -> {
      if (mMap == null) return;

      if (currentMapType == GoogleMap.MAP_TYPE_NORMAL) {
        currentMapType = GoogleMap.MAP_TYPE_HYBRID; // Satélite
        Toast.makeText(getContext(), "Vista Satelital (Montaña)", Toast.LENGTH_SHORT).show();
      } else {
        currentMapType = GoogleMap.MAP_TYPE_NORMAL; // Calles
        Toast.makeText(getContext(), "Vista Normal (Ciudad)", Toast.LENGTH_SHORT).show();
      }
      mMap.setMapType(currentMapType);
    });

  }

  private void setupObservers() {
    // Cada vez que cambia la lista de puntos, redibujamos
    viewModel.getPuntosRuta().observe(getViewLifecycleOwner(), this::dibujarRuta);

    // NUEVO: Actualizar texto de kilómetros
    viewModel.getTextoDistancia().observe(getViewLifecycleOwner(), txt -> {
      binding.tvDistanciaReal.setText(txt);
    });

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

  // metodo de pintado: Borra y dibuja de nuevo
  private void dibujarRuta(List<LatLng> puntos) {
    if (mMap == null) return;
    mMap.clear(); // Limpiamos marcadores y líneas viejas

    if (puntos.isEmpty()) return;

    // 1. Dibujar la línea (Polyline)
    PolylineOptions polyline = new PolylineOptions()
      .addAll(puntos)
      .width(12)
      .color(Color.BLUE) // Puedes usar Color.parseColor("#1976D2")
      .geodesic(true); // Geodesic ayuda a la precisión visual en distancias largas
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
// --- CORRECCIÓN CLAVE: MOVER CÁMARA AUTOMÁTICAMENTE ---
    // Si estamos editando un evento existente y es la PRIMERA VEZ que dibujamos la ruta cargada
    // y NO hemos centrado la cámara todavía:
    if (idEvento != 0 && !camaraCentradaInicialmente && !puntos.isEmpty()) {

      // Opción A: Centrar solo en la Largada con Zoom Alto
      // mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(puntos.get(0), 16));

      // Opción B (RECOMENDADA): Encuadrar toda la ruta para verla completa
      try {
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (LatLng p : puntos) {
          builder.include(p);
        }
        LatLngBounds bounds = builder.build();
        // moveCamera con padding de 100 pixels para que no quede pegado al borde
        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100));
      } catch (Exception e) {
        // Fallback si la ruta es un solo punto o error de bounds: ir a la largada
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(puntos.get(0), 15));
      }

      // Marcamos como centrado para que al seguir editando no te mueva la cámara a cada rato
      camaraCentradaInicialmente = true;
    }

  }

}
