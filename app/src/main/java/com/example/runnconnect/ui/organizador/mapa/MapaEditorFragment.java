package com.example.runnconnect.ui.organizador.mapa;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.Navigation;

import com.example.runnconnect.R;
import com.example.runnconnect.data.response.PuntoInteresResponse;
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

  private String estadoEvento="";

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    if (getArguments() != null) {
      idEvento = getArguments().getInt("idEvento", 0);

      //recuperamos el estado del bundle
      estadoEvento= getArguments().getString("estadoEvento");
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

  private void setupListeners() {
    boolean esSoloLectura= "finalizado".equalsIgnoreCase(estadoEvento) || "cancelado".equalsIgnoreCase(estadoEvento);

    /*binding.fabUndo.setOnClickListener(v -> viewModel.deshacer());
    binding.fabLayers.setOnClickListener(v -> viewModel.alternarCapas());
    binding.btnGuardarRuta.setOnClickListener(v -> viewModel.guardarRuta(idEvento));
    */
    if (esSoloLectura) {
      // Si es solo lectura, los botones no hacen nada (o muestran un aviso)
      binding.fabUndo.setOnClickListener(null);
      binding.btnGuardarRuta.setOnClickListener(null);
    } else {
      // Lógica normal
      binding.fabUndo.setOnClickListener(v -> viewModel.deshacer());
      binding.btnGuardarRuta.setOnClickListener(v -> viewModel.guardarRuta(idEvento));
    }
    // El cambio de capas siempre se permite
    binding.fabLayers.setOnClickListener(v -> viewModel.alternarCapas());
 
  }

  private void setupObservers() {
    // ... Observers visuales basicos ...
    viewModel.getIsLoading().observe(getViewLifecycleOwner(), loading -> {
      binding.progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
      
     // Solo habilitamos el boton si NO esta cargando Y si NO es solo lectura
     if (!esSoloLectura()) {
      binding.btnGuardarRuta.setEnabled(!loading);
  }
    });

    viewModel.getTextoDistancia().observe(getViewLifecycleOwner(), binding.tvDistanciaReal::setText);

    viewModel.getTipoMapa().observe(getViewLifecycleOwner(), tipo -> {
      if (mMap != null) mMap.setMapType(tipo);
    });

    // 1. DIBUJAR RUTA (LINEA AZUL)
    viewModel.getPuntosRuta().observe(getViewLifecycleOwner(), this::dibujarLineaRuta);

    // 2. DIBUJAR FLECHAS DE SENTIDO (Calculadas en VM)
    viewModel.getFlechasGuias().observe(getViewLifecycleOwner(), this::dibujarFlechasVisuales);

    // 3. DIBUJAR PUNTOS DE INTERES (Iconos)
    viewModel.getListaPuntosInteres().observe(getViewLifecycleOwner(), this::dibujarMarcadoresPOI);

    // ... observers de navegacion y errores ...
    viewModel.getOrdenMostrarError().observe(getViewLifecycleOwner(), msg -> {
      if (msg != null) {
        Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
        viewModel.resetOrdenError();
      }
    });

    viewModel.getOrdenHacerZoomRuta().observe(getViewLifecycleOwner(), bounds -> {
      if (bounds != null && mMap != null) {
        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100));
        viewModel.resetOrdenCamara();
      }
    });

    viewModel.getOrdenCentrarCamara().observe(getViewLifecycleOwner(), centro -> {
      if (centro != null && mMap != null) {
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(centro, 13));
        viewModel.resetOrdenCamara();
      }
    });

    viewModel.getOrdenNavegarSalida().observe(getViewLifecycleOwner(), mensaje -> {
      if (mensaje != null) {
        navegarAlListado(mensaje);
        viewModel.resetOrdenNavegacion();
      }
    });

    viewModel.getOrdenPedirDatosPI().observe(getViewLifecycleOwner(), latLng -> {
      if (latLng != null) {
        mostrarDialogoPuntoInteres(latLng);
        viewModel.resetOrdenDialogo();
      }
    });
  }

  @Override
  public void onMapReady(@NonNull GoogleMap googleMap) {
    mMap = googleMap;
    mMap.getUiSettings().setZoomControlsEnabled(true);

    if (viewModel.getTipoMapa().getValue() != null) {
      mMap.setMapType(viewModel.getTipoMapa().getValue());
    }
    
    // Carga inicial de datos
    viewModel.onMapReady(idEvento);

    // LOGICA DE PROTECCION (AL FINAL PARA SOBRESCRIBIR)
    boolean esSoloLectura = "finalizado".equalsIgnoreCase(estadoEvento) || "cancelado".equalsIgnoreCase(estadoEvento);

    if (esSoloLectura) {
        // 1. Bloquear interaccion con el mapa (Click para crear puntos)
        mMap.setOnMapClickListener(null); 
        
        // 2. Ocultar botones visualmente
        binding.btnGuardarRuta.setVisibility(View.GONE);
        binding.fabUndo.setVisibility(View.GONE);
        
        // 3. Feedback
        // Toast.makeText(getContext(), "Modo Solo Lectura", Toast.LENGTH_SHORT).show();
    } else {
        // Modo Edicion: Asignar listener
        mMap.setOnMapClickListener(latLng -> viewModel.procesarClickMapa(latLng));
        
        binding.btnGuardarRuta.setVisibility(View.VISIBLE);
        binding.fabUndo.setVisibility(View.VISIBLE);
    }

  }
  //helper verificar estado
  private boolean esSoloLectura() {
    return "finalizdo".equalsIgnoreCase(estadoEvento) || "cancelado".equalsIgnoreCase(estadoEvento);
  }

  //  METODOS DE DIBUJADO PURO (Reciben datos listos)
  private void dibujarLineaRuta(List<LatLng> puntos) {
    if (mMap == null) return;
    mMap.clear(); // Limpia tod para redibujar

    if (puntos != null && !puntos.isEmpty()) {
      PolylineOptions poly = new PolylineOptions()
        .addAll(puntos).width(12).color(Color.BLUE).geodesic(true);
      mMap.addPolyline(poly);

      // Marcadores Inicio/Fin
      mMap.addMarker(new MarkerOptions().position(puntos.get(0))
        .title("Largada").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
      if (puntos.size() > 1) {
        mMap.addMarker(new MarkerOptions().position(puntos.get(puntos.size() - 1))
          .title("Meta").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
      }
    }

    // Al hacer clear(), debemos redibujar los PI si ya existen en el VM
    if (viewModel.getListaPuntosInteres().getValue() != null) {
      dibujarMarcadoresPOI(viewModel.getListaPuntosInteres().getValue());
    }
  }

  private void dibujarFlechasVisuales(List<MapaEditorViewModel.FlechaMapa> flechas) {
    if (mMap == null || flechas == null || getContext() == null) return;

    for (MapaEditorViewModel.FlechaMapa flecha : flechas) {
      mMap.addMarker(new MarkerOptions()
        .position(flecha.posicion)
        .icon(bitmapDescriptorFromVector(requireContext(), R.drawable.ic_flecha_sentido))
        .rotation(flecha.rotacion)
        .anchor(0.5f, 0.5f)
        .flat(true));
    }
  }

  private void dibujarMarcadoresPOI(List<PuntoInteresResponse> puntos) {
    if (mMap == null || puntos == null) return;

    for (PuntoInteresResponse p : puntos) {
      if (p.getLatitud() == null || p.getLongitud() == null) continue;
      LatLng posicion = new LatLng(p.getLatitud().doubleValue(), p.getLongitud().doubleValue());
      int resourceId = obtenerIconoPorTipo(p.getTipo());

      mMap.addMarker(new MarkerOptions()
        .position(posicion)
        .title(p.getNombre())
        .icon(BitmapDescriptorFactory.fromResource(resourceId))
        .anchor(0.5f, 0.5f));
    }
  }

  //helper
  private void mostrarDialogoPuntoInteres(LatLng latLng) {
    AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
    View v = getLayoutInflater().inflate(R.layout.dialog_crear_punto_interes, null);
    Spinner spTipo = v.findViewById(R.id.spTipoPunto);

    ArrayAdapter<String> adapter = new ArrayAdapter<>(
      requireContext(),
      android.R.layout.simple_spinner_dropdown_item,
      viewModel.getNombresPuntoUi());

    spTipo.setAdapter(adapter);

    builder.setView(v)
      .setTitle("Nuevo Punto de Interés")
      .setPositiveButton("Guardar", (d, w) -> {
        // Solo pasamos el índice y la posición. El VM decide qué string usar.
        int indiceSeleccionado = spTipo.getSelectedItemPosition();
        viewModel.guardarPuntoInteresPorIndice(idEvento, indiceSeleccionado, latLng);
      })
      .setNegativeButton("Cancelar", null)
      .show();
  }

  private int obtenerIconoPorTipo(String tipo) {
    if (tipo == null) return R.drawable.ic_pin_help;
    switch (tipo.toLowerCase().trim()) {
      case "hidratacion": return R.drawable.ic_pin_drop;
      case "primeros_auxilios": return R.drawable.ic_pin_medical;
      case "punto_energetico":
      case "punto energetico": return R.drawable.ic_pin_thunderbolt;
      default: return R.drawable.ic_pin_help;
    }
  }

  private void navegarAlListado(String mensajeExito) {
    NavController navController = Navigation.findNavController(requireView());
    try {
      androidx.navigation.NavBackStackEntry entry = navController.getBackStackEntry(R.id.nav_mis_eventos);
      entry.getSavedStateHandle().set("mensaje_exito", mensajeExito);
      navController.popBackStack(R.id.nav_mis_eventos, false);
    } catch (IllegalArgumentException e) {
      NavOptions options = new NavOptions.Builder()
        .setPopUpTo(R.id.nav_crear_evento, true)
        .setLaunchSingleTop(true).build();
      Bundle args = new Bundle();
      args.putString("mensaje_arg", mensajeExito);
      navController.navigate(R.id.nav_mis_eventos, args, options);
    }
  }

  private com.google.android.gms.maps.model.BitmapDescriptor bitmapDescriptorFromVector(android.content.Context context, int vectorResId) {
    try {
      android.graphics.drawable.Drawable vectorDrawable = androidx.core.content.ContextCompat.getDrawable(context, vectorResId);
      if (vectorDrawable == null) return null;
      vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());
      android.graphics.Bitmap bitmap = android.graphics.Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), android.graphics.Bitmap.Config.ARGB_8888);
      android.graphics.Canvas canvas = new android.graphics.Canvas(bitmap);
      vectorDrawable.draw(canvas);
      return com.google.android.gms.maps.model.BitmapDescriptorFactory.fromBitmap(bitmap);
    } catch (Exception e) {
      return com.google.android.gms.maps.model.BitmapDescriptorFactory.defaultMarker();
    }
  }
}