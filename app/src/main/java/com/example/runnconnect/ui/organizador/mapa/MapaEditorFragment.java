package com.example.runnconnect.ui.organizador.mapa;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter; // Nuevo
import android.widget.EditText; // Nuevo
import android.widget.Spinner; // Nuevo
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog; // Nuevo
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
import com.google.maps.android.SphericalUtil; // Importante para flechas

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

  private void setupListeners() {
    binding.fabUndo.setOnClickListener(v -> viewModel.deshacer());
    binding.fabLayers.setOnClickListener(v -> viewModel.alternarCapas());
    binding.btnGuardarRuta.setOnClickListener(v -> viewModel.guardarRuta(idEvento));
  }

  private void setupObservers() {
    viewModel.getIsLoading().observe(getViewLifecycleOwner(), loading -> {
      binding.progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
      binding.btnGuardarRuta.setEnabled(!loading);
    });

    viewModel.getTextoDistancia().observe(getViewLifecycleOwner(), binding.tvDistanciaReal::setText);

    viewModel.getTipoMapa().observe(getViewLifecycleOwner(), tipo -> {
      if (mMap != null) mMap.setMapType(tipo);
    });

    // Dibujar en mapa (Ruta + Flechas)
    viewModel.getPuntosRuta().observe(getViewLifecycleOwner(), this::dibujarEnMapa);

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

    // NUEVO: Observer para abrir Diálogo de Puntos de Interés
    viewModel.getOrdenPedirDatosPI().observe(getViewLifecycleOwner(), latLng -> {
      if (latLng != null) {
        mostrarDialogoPuntoInteres(latLng);
        viewModel.resetOrdenDialogo();
      }
    });

    //observar lista de Punto interes
    viewModel.getListaPuntosInteres().observe(getViewLifecycleOwner(), puntos -> {
      if (puntos != null) {
        dibujarMarcadoresPOI(puntos);
      }
    });

  }
  //dibujar iconos de punto interes
  private void dibujarMarcadoresPOI(List<PuntoInteresResponse> puntos) {
    if (mMap == null) return;

    // OJO: No usamos mMap.clear() aquí porque borraría la ruta azul.
    // Solo limpiamos los marcadores de POI anteriores si es necesario (o dejamos que se acumulen si la lógica lo permite)
    // Para hacerlo limpio, podrías guardar los marcadores en una lista y borrarlos antes de redibujar,
    // pero por ahora, simplemente agreguemos los nuevos.

    for (PuntoInteresResponse p : puntos) {
      LatLng posicion = new LatLng(p.getLatitud().doubleValue(), p.getLongitud().doubleValue());

      // Obtener el icono según el tipo
      int resourceId = obtenerIconoPorTipo(p.getTipo());

      MarkerOptions markerOpt = new MarkerOptions()
        .position(posicion)
        .title(p.getNombre()) // Muestra "Puesto de Hidratación" al tocar
        .icon(BitmapDescriptorFactory.fromResource(resourceId)); // Carga el PNG
      // .anchor(0.5f, 0.5f) // Descomentar si el icono debe estar centrado (ej: círculo) o dejar default (pin)

      mMap.addMarker(markerOpt);
    }
  }

  // Helper para elegir el icono PNG
  private int obtenerIconoPorTipo(String tipo) {
    if (tipo == null) return R.drawable.ic_pin_help; // Default

    switch (tipo.toLowerCase().trim()) {
      case "hidratacion":
        return R.drawable.ic_pin_drop; //PNG de agua
      case "primeros_auxilios":
        return R.drawable.ic_pin_medical;      //PNG de cruz
      case "punto_energetico":
        return R.drawable.ic_pin_thunderbolt;     //PNG de rayo/comida
      default:
        return R.drawable.ic_pin_help;        // Default
    }
  }

  @Override
  public void onMapReady(@NonNull GoogleMap googleMap) {
    mMap = googleMap;
    mMap.getUiSettings().setZoomControlsEnabled(true);

    // MODIFICADO: Delegamos la decisión al ViewModel (Dibujar o Validar PI)
    mMap.setOnMapClickListener(latLng -> viewModel.procesarClickMapa(latLng));

    if (viewModel.getTipoMapa().getValue() != null) {
      mMap.setMapType(viewModel.getTipoMapa().getValue());
    }
    viewModel.onMapReady(idEvento);
  }

  private void dibujarEnMapa(List<LatLng> puntos) {
    if (mMap == null) return;
    mMap.clear();
    if (puntos == null || puntos.isEmpty()) return;

    // 1. Línea Azul
    PolylineOptions poly = new PolylineOptions()
      .addAll(puntos).width(12).color(Color.BLUE).geodesic(true);
    mMap.addPolyline(poly);

    // 2. NUEVO: Flechas de Sentido Dinámicas (cada 150m)
    dibujarFlechasDinamicas(puntos);

    // 3. Marcadores Inicio/Fin
    mMap.addMarker(new MarkerOptions().position(puntos.get(0))
      .title("Largada").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));

    if (puntos.size() > 1) {
      mMap.addMarker(new MarkerOptions().position(puntos.get(puntos.size() - 1))
        .title("Meta").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
    }
  }

  // Metodo Helper para las flechas
  private void dibujarFlechasDinamicas(List<LatLng> puntos) {
    if (getContext() == null) return; // Validación de seguridad

    double acumulado = 0;
    double intervalo = 150; // Metros entre flechas

    for (int i = 0; i < puntos.size() - 1; i++) {
      LatLng p1 = puntos.get(i);
      LatLng p2 = puntos.get(i + 1);

      float[] dist = new float[1];
      android.location.Location.distanceBetween(p1.latitude, p1.longitude, p2.latitude, p2.longitude, dist);
      acumulado += dist[0];

      if (acumulado >= intervalo) {
        float heading = (float) SphericalUtil.computeHeading(p1, p2);

        mMap.addMarker(new MarkerOptions()
          .position(p1)
          // Usamos el helper en lugar de fromResource directo
          .icon(bitmapDescriptorFromVector(requireContext(), R.drawable.ic_flecha_sentido))
          .rotation(heading)
          .anchor(0.5f, 0.5f)
          .flat(true));
        // -----------------------

        acumulado = 0;
      }
    }
  }

  private void mostrarDialogoPuntoInteres(LatLng latLng) {
    AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
    View v = getLayoutInflater().inflate(R.layout.dialog_crear_punto_interes, null);

    Spinner spTipo = v.findViewById(R.id.spTipoPunto);
    //EditText etNombre = v.findViewById(R.id.etNombrePunto);

    String[] opcionesVisuales = {"Hidratacion", "Primeros auxilios", "Punto energetico", "Otro"};
    String[] opcionesApi = {"hidratacion", "primeros_auxilios", "punto_energetico", "otro"};

    ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, opcionesVisuales);
    spTipo.setAdapter(adapter);

    builder.setView(v)
      .setTitle("Nuevo Punto de Interés")
      .setPositiveButton("Guardar", (d, w) -> {
        int position= spTipo.getSelectedItemPosition();
        String tipoEnviar = opcionesApi[position];
        String nombreAutomatico = opcionesVisuales[position];

        // Enviamos a la API
        viewModel.guardarPuntoInteresBackend(idEvento, tipoEnviar, latLng);
      })
      .setNegativeButton("Cancelar", null)
      .show();
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

  //transforma el vector en png para el sentido del circuito
  private com.google.android.gms.maps.model.BitmapDescriptor bitmapDescriptorFromVector(android.content.Context context, int vectorResId) {
    android.graphics.drawable.Drawable vectorDrawable = androidx.core.content.ContextCompat.getDrawable(context, vectorResId);
    if (vectorDrawable == null) return null;

    vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());
    android.graphics.Bitmap bitmap = android.graphics.Bitmap.createBitmap(
      vectorDrawable.getIntrinsicWidth(),
      vectorDrawable.getIntrinsicHeight(),
      android.graphics.Bitmap.Config.ARGB_8888);
    android.graphics.Canvas canvas = new android.graphics.Canvas(bitmap);
    vectorDrawable.draw(canvas);

    return com.google.android.gms.maps.model.BitmapDescriptorFactory.fromBitmap(bitmap);
  }


}