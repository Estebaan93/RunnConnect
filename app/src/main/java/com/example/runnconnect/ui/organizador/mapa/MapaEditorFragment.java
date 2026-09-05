package com.example.runnconnect.ui.organizador.mapa;

import android.os.Bundle;
import android.util.Log;
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

import java.util.ArrayList;
import java.util.List;

public class MapaEditorFragment extends Fragment implements OnMapReadyCallback {

  private FragmentEditorMapaBinding binding;
  private MapaEditorViewModel viewModel;
  private GoogleMap mMap;
  private int idEvento = 0;
  private List<com.google.android.gms.maps.model.Marker> marcadoresPoiActivos = new ArrayList<>();

  private String estadoEvento="";

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    idEvento = getArguments().getInt("idEvento", 0);
    estadoEvento = getArguments().getString("estadoEvento");
  }

  public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    binding = FragmentEditorMapaBinding.inflate(inflater, container, false);
    viewModel = new ViewModelProvider(this).get(MapaEditorViewModel.class);

    SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
    //if (mapFragment != null) {
      mapFragment.getMapAsync(this);
    //}

    setupListeners();

    return binding.getRoot();
  }

  private void setupListeners() {
    binding.fabUndo.setOnClickListener(v -> viewModel.deshacer());
    binding.btnGuardarRuta.setOnClickListener(v -> viewModel.guardarRuta(idEvento));
    binding.fabLayers.setOnClickListener(v -> viewModel.alternarCapas());
    binding.btnCloseError.setOnClickListener(v -> viewModel.ocultarError());
  }

  private void setupObservers() {
    viewModel.getIsLoading().observe(getViewLifecycleOwner(), loading -> {
      binding.progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
      binding.btnGuardarRuta.setEnabled(!loading);
    });

    viewModel.getVisibilidadEdicion().observe(getViewLifecycleOwner(), visibilidad -> {
      binding.btnGuardarRuta.setVisibility(visibilidad);
      binding.fabUndo.setVisibility(visibilidad);
    });

    viewModel.getTextoDistancia().observe(getViewLifecycleOwner(), binding.tvDistanciaReal::setText);

    viewModel.getTipoMapa().observe(getViewLifecycleOwner(), mMap::setMapType);

    viewModel.getLineaRuta().observe(getViewLifecycleOwner(), poly -> {
      mMap.clear();
      mMap.addPolyline(poly);
      dibujarMarcadoresPOI(viewModel.getListaPuntosInteres().getValue());
    });

    viewModel.getMarcadoresInicioFin().observe(getViewLifecycleOwner(), extremos -> {
      extremos.forEach(mMap::addMarker);
    });

    viewModel.getFlechasGuias().observe(getViewLifecycleOwner(), this::dibujarFlechasVisuales);

    viewModel.getListaPuntosInteres().observe(getViewLifecycleOwner(), this::dibujarMarcadoresPOI);

    viewModel.getErrorText().observe(getViewLifecycleOwner(), binding.tvErrorText::setText);
    viewModel.getErrorVisibility().observe(getViewLifecycleOwner(), visibility -> {
      binding.errorContainer.setVisibility(visibility);
    });

    viewModel.getOrdenHacerZoomRuta().observe(getViewLifecycleOwner(), bounds -> 
      mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100))
    );

    viewModel.getOrdenCentrarCamara().observe(getViewLifecycleOwner(), centro -> 
      mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(centro, 13))
    );

    viewModel.getOrdenNavegarSalida().observe(getViewLifecycleOwner(), this::navegarAlListado);

    viewModel.getOrdenPedirDatosPI().observe(getViewLifecycleOwner(), this::mostrarDialogoPuntoInteres);

    viewModel.getMostrarConfirmacionEliminarSignal().observe(getViewLifecycleOwner(), signal -> {
      Integer idPunto = viewModel.getIdPuntoAEliminar().getValue();
      new AlertDialog.Builder(requireContext())
        .setTitle("Eliminar Punto de Interés")
        .setMessage("¿Deseas eliminar este punto?")
        .setPositiveButton("Sí", (d, w) -> viewModel.eliminarPuntoInteres(idEvento, idPunto))
        .setNegativeButton("No", null)
        .show();
    });
  }

  @Override
  public void onMapReady(@NonNull GoogleMap googleMap) {
    mMap = googleMap;
    mMap.getUiSettings().setZoomControlsEnabled(true);
    mMap.setOnMapClickListener(latLng -> viewModel.procesarClickMapa(latLng));
    mMap.setOnInfoWindowClickListener(marker -> viewModel.onMarkerClick(marker.getTag()));

    setupObservers();

    viewModel.onMapReady(idEvento, estadoEvento);
  }

  private void dibujarFlechasVisuales(List<MarkerOptions> opciones) {
    opciones.forEach(mMap::addMarker);
  }

  private void dibujarMarcadoresPOI(List<MapaEditorViewModel.PoiMarkerUIState> opciones) {
    marcadoresPoiActivos.forEach(com.google.android.gms.maps.model.Marker::remove);
    marcadoresPoiActivos.clear();
    
    opciones.forEach(poi -> {
      com.google.android.gms.maps.model.Marker m = mMap.addMarker(poi.options);
      m.setTag(poi.id);
      marcadoresPoiActivos.add(m);
    });
  }

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
        int indiceSeleccionado = spTipo.getSelectedItemPosition();
        viewModel.guardarPuntoInteresPorIndice(idEvento, indiceSeleccionado, latLng);
      })
      .setNegativeButton("Cancelar", null)
      .show();
  }

  private void navegarAlListado(String mensajeExito) {
    Bundle args = new Bundle();
    args.putString("nuevoEvento_misEventos", mensajeExito);

    Log.d("msjExitoNuevoEvento", "navegarAlListado: ok " + args.getString("nuevoEvento_misEventos"));
    //
    getParentFragmentManager().setFragmentResult("requestKeyMapa", args);

    // Volvemos hacia atras destruyendo tod hasta encontrar "nav_mis_eventos"
    Navigation.findNavController(requireView()).popBackStack(R.id.nav_mis_eventos, false);
  }
}