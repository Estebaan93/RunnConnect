package com.example.runnconnect.ui.organizador.mapa;

import android.app.Application;
import android.location.Location;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.runnconnect.R;
import com.example.runnconnect.data.conexion.ApiClient;
import com.example.runnconnect.data.conexion.ApiService;
import com.example.runnconnect.data.preferencias.SessionManager;
import com.example.runnconnect.data.request.CrearPuntoInteresRequest;
import com.example.runnconnect.data.request.GuardarRutaRequest;
import com.example.runnconnect.data.request.RutaPuntoRequest;
import com.example.runnconnect.data.response.MapaEventoResponse;
import com.example.runnconnect.data.response.PuntoInteresResponse;
import com.example.runnconnect.data.response.PuntosInteresEventoResponse;
import android.graphics.Color;
import com.example.runnconnect.data.response.RutaPuntoResponse;
import com.example.runnconnect.utils.MapUtils;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.PolyUtil;
import com.google.maps.android.SphericalUtil;

import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MapaEditorViewModel extends AndroidViewModel {

  private final ApiService apiService;
  private final SessionManager sessionManager;

  // --- ESTADOS DE LA VISTA ---
  private final MutableLiveData<List<LatLng>> puntosRuta = new MutableLiveData<>(new ArrayList<>());
  private final MutableLiveData<PolylineOptions> lineaRuta = new MutableLiveData<>(new PolylineOptions());
  private final MutableLiveData<List<MarkerOptions>> marcadoresInicioFin = new MutableLiveData<>(new ArrayList<>());

  // lista de flechas calculadas (Opciones de Marcadores pre-procesadas)
  private final MutableLiveData<List<MarkerOptions>> flechasGuias = new MutableLiveData<>(new ArrayList<>());

  private final MutableLiveData<String> textoDistancia = new MutableLiveData<>("0.00 km");
  private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
  private final MutableLiveData<Integer> tipoMapa = new MutableLiveData<>(GoogleMap.MAP_TYPE_NORMAL);

  // --- ORDENES ---
  private final MutableLiveData<String> ordenNavegarSalida = new MutableLiveData<>();
  
  private final MutableLiveData<String> errorText = new MutableLiveData<>("");
  private final MutableLiveData<Integer> errorVisibility = new MutableLiveData<>(View.GONE);

  private final MutableLiveData<LatLngBounds> ordenHacerZoomRuta = new MutableLiveData<>();
  private final MutableLiveData<LatLng> ordenCentrarCamara = new MutableLiveData<>();
  private final MutableLiveData<LatLng> ordenPedirDatosPI = new MutableLiveData<>();

  private final MutableLiveData<Boolean> isSoloLectura = new MutableLiveData<>(false);

  private final MutableLiveData<List<MarkerOptions>> listaPuntosInteres = new MutableLiveData<>(new ArrayList<>());

  private boolean datosCargados = false;
  private boolean modoPuntosInteres = false;

  // Mapeo interno: Indices del Spinner -> Strings de la API
  private final String[] TIPOS_PUNTO_API = {"hidratacion", "primeros_auxilios", "punto_energetico", "otro"};
  private final String[] NOMBRES_PUNTO_UI = {"Hidratación", "Primeros Auxilios", "Punto Energético", "Otro"};
  public MapaEditorViewModel(@NonNull Application application) {
    super(application);
    apiService = ApiClient.getApiService();
    sessionManager = new SessionManager(application);
  }

  // --- GETTERS ---
  public String[] getNombresPuntoUi(){ return NOMBRES_PUNTO_UI; }
  public LiveData<List<LatLng>> getPuntosRuta() { return puntosRuta; }
  public LiveData<PolylineOptions> getLineaRuta() { return lineaRuta; }
  public LiveData<List<MarkerOptions>> getMarcadoresInicioFin() { return marcadoresInicioFin; }
  public LiveData<List<MarkerOptions>> getFlechasGuias() { return flechasGuias; }
  public LiveData<String> getTextoDistancia() { return textoDistancia; }
  public LiveData<Boolean> getIsLoading() { return isLoading; }
  public LiveData<Integer> getTipoMapa() { return tipoMapa; }
  public LiveData<String> getOrdenNavegarSalida() { return ordenNavegarSalida; }
  
  public LiveData<String> getErrorText() { return errorText; }
  public LiveData<Integer> getErrorVisibility() { return errorVisibility; }
  public LiveData<LatLngBounds> getOrdenHacerZoomRuta() { return ordenHacerZoomRuta; }
  public LiveData<LatLng> getOrdenCentrarCamara() { return ordenCentrarCamara; }
  public LiveData<LatLng> getOrdenPedirDatosPI() { return ordenPedirDatosPI; }
  public LiveData<Boolean> getIsSoloLectura() { return isSoloLectura; }
  public LiveData<List<MarkerOptions>> getListaPuntosInteres() { return listaPuntosInteres; }

  // --- RESETS ---

  public void ocultarError() {
    errorVisibility.setValue(View.GONE);
  }

  private void mostrarError(String mensaje) {
    errorText.setValue(mensaje);
    errorVisibility.setValue(View.VISIBLE);
  }

  // --- LOGICA DE NEGOCIO ---
  public void onMapReady(int idEvento, String estadoEvento) {
    if (datosCargados) return;
    datosCargados = true;
    
    boolean readOnly = "finalizado".equalsIgnoreCase(estadoEvento) || "cancelado".equalsIgnoreCase(estadoEvento);
    isSoloLectura.setValue(readOnly);

    if (idEvento != 0) {
      cargarRutaBackend(idEvento);
      cargarPuntosInteres(idEvento);
    } else {
      ordenCentrarCamara.setValue(new LatLng(-33.29501, -66.33563));
    }
  }

  public void procesarClickMapa(LatLng punto) {
    if (modoPuntosInteres) {
      validarPuntoInteres(punto);
    } else {
      agregarPuntoRuta(punto);
    }
  }

  private void agregarPuntoRuta(LatLng punto) {
    List<LatLng> lista = puntosRuta.getValue();
    if (lista != null) {
      lista.add(punto);
      puntosRuta.setValue(lista);
      actualizarCalculosRuta(lista); // Centralizamos calculos
    }
  }

  public void deshacer() {
    if (modoPuntosInteres) {
      mostrarError("No se puede deshacer una ruta ya guardada");
      return;
    }
    List<LatLng> lista = puntosRuta.getValue();
    if (lista != null && !lista.isEmpty()) {
      lista.remove(lista.size() - 1);
      puntosRuta.setValue(lista);
      actualizarCalculosRuta(lista);
    }
  }

  // Metodo centralizado para recalcular distancia y flechas cuando la ruta cambia
  private void actualizarCalculosRuta(List<LatLng> puntos) {
    // 1. Calcular Distancia Texto
    if (puntos == null || puntos.size() < 2) {
      textoDistancia.setValue("0.00 km");
      flechasGuias.setValue(new ArrayList<>()); // Limpiar flechas
      lineaRuta.setValue(new PolylineOptions());
      marcadoresInicioFin.setValue(new ArrayList<>());
      return;
    }

    double distanciaTotal = 0;
    double acumuladoFlechas = 0;
    double intervaloFlechas = 200; // Metros
    List<MarkerOptions> nuevasFlechas = new ArrayList<>();
    float[] res = new float[1];

    for (int i = 0; i < puntos.size() - 1; i++) {
      LatLng p1 = puntos.get(i);
      LatLng p2 = puntos.get(i + 1);

      Location.distanceBetween(p1.latitude, p1.longitude, p2.latitude, p2.longitude, res);
      double distSegmento = res[0];
      distanciaTotal += distSegmento;
      acumuladoFlechas += distSegmento;

      // logica de calculo de flechas
      if (acumuladoFlechas >= intervaloFlechas) {
        float heading = (float) SphericalUtil.computeHeading(p1, p2);
        MarkerOptions flechaMarker = new MarkerOptions()
                .position(p1)
                .icon(MapUtils.bitmapDescriptorFromVector(getApplication(), R.drawable.ic_flecha_sentido))
                .rotation(heading)
                .anchor(0.5f, 0.5f)
                .flat(true);
        nuevasFlechas.add(flechaMarker);
        acumuladoFlechas = 0;
      }
    }

    PolylineOptions poly = new PolylineOptions()
        .addAll(puntos).width(12).color(Color.BLUE).geodesic(true);
    lineaRuta.setValue(poly);

    List<MarkerOptions> extremos = new ArrayList<>();
    extremos.add(new MarkerOptions().position(puntos.get(0))
        .title("Largada").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
    extremos.add(new MarkerOptions().position(puntos.get(puntos.size() - 1))
        .title("Meta").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
    marcadoresInicioFin.setValue(extremos);

    textoDistancia.setValue(String.format("%.2f km", distanciaTotal / 1000.0));
    flechasGuias.setValue(nuevasFlechas);
  }

  private void validarPuntoInteres(LatLng puntoClickeado) {
    List<LatLng> ruta = puntosRuta.getValue();
    if (ruta == null || ruta.size() < 2) {
      mostrarError("Primero debés dibujar y guardar el circuito");
      return;
    }
    boolean estaEnRuta = PolyUtil.isLocationOnPath(puntoClickeado, ruta, true, 20);

    if (estaEnRuta) {
      ordenPedirDatosPI.setValue(puntoClickeado);
    } else {
      mostrarError("El punto debe estar sobre el circuito (línea azul)");
    }
  }

  // ahora recibe el indice del spinner
  public void guardarPuntoInteresPorIndice(int idEvento, int indiceSpinner, LatLng latLng) {
    if (indiceSpinner < 0 || indiceSpinner >= TIPOS_PUNTO_API.length) {
      mostrarError("Error: Evento no identificado");
      return;
    }
    String tipoApi = TIPOS_PUNTO_API[indiceSpinner];
    String nombreUi= NOMBRES_PUNTO_UI[indiceSpinner];
    guardarPuntoInteresBackend(idEvento, tipoApi, nombreUi, latLng);
  }

  private void guardarPuntoInteresBackend(int idEvento, String tipo, String nombre, LatLng latLng) {
    isLoading.setValue(true);
    String tipoApi = tipo.toLowerCase().trim();
    CrearPuntoInteresRequest request = new CrearPuntoInteresRequest(tipoApi, nombre,latLng.latitude, latLng.longitude);

    String token = sessionManager.leerToken();
    if (token != null && !token.isEmpty()) {
      apiService.crearPuntoInteres("Bearer " + token, idEvento, request).enqueue(new Callback<ResponseBody>() {
        @Override
        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
          isLoading.setValue(false);
          if (response.isSuccessful()) {
            mostrarError("Punto de interes agregado!");
            cargarPuntosInteres(idEvento);
          } else {
            String errorMsg = "Error desconocido";
            try {
              if (response.errorBody() != null) {
                errorMsg = response.errorBody().string();
              }
            } catch (Exception e) {
              e.printStackTrace();
            }
            String logMsg = "Código: " + response.code() + " | Mensaje: " + errorMsg;
            Log.e("ERROR_PUNTO", logMsg);
            mostrarError("Error al guardar: " + response.code());
          }
        }
        @Override
        public void onFailure(Call<ResponseBody> call, Throwable t) {
          isLoading.setValue(false);
          mostrarError("Error de conexión");
        }
      });
    } else {
      isLoading.setValue(false);
      mostrarError("Sesion expirada.");
    }
  }

  public void cargarPuntosInteres(int idEvento) {
    apiService.obtenerPuntosInteres(idEvento).enqueue(new Callback<PuntosInteresEventoResponse>() {
      @Override
      public void onResponse(Call<PuntosInteresEventoResponse> call, Response<PuntosInteresEventoResponse> response) {
        if (response.isSuccessful() && response.body() != null) {
          List<PuntoInteresResponse> puntos = response.body().getPuntosInteres();
          List<MarkerOptions> uiPuntos = new ArrayList<>();
          if (puntos != null) {
            for (PuntoInteresResponse p : puntos) {
              if (p.getLatitud() == null || p.getLongitud() == null) continue;
              LatLng posicion = new LatLng(p.getLatitud().doubleValue(), p.getLongitud().doubleValue());
              int resourceId = obtenerIconoPorTipo(p.getTipo());
              
              MarkerOptions poiMarker = new MarkerOptions()
                      .position(posicion)
                      .title(p.getNombre())
                      .icon(BitmapDescriptorFactory.fromResource(resourceId))
                      .anchor(0.5f, 0.5f);
              uiPuntos.add(poiMarker);
            }
          }
          listaPuntosInteres.setValue(uiPuntos);
        }
      }
      @Override
      public void onFailure(Call<PuntosInteresEventoResponse> call, Throwable t) {}
    });
  }

  public void guardarRuta(int idEvento) {
    if (idEvento == 0) { mostrarError("Error: No hay evento asociado"); return; }
    List<LatLng> ruta = puntosRuta.getValue();
    if (ruta == null || ruta.size() < 2) { mostrarError("Marca Inicio y Fin en el mapa"); return; }

    List<RutaPuntoRequest> dtos = new ArrayList<>();
    for (int i = 0; i < ruta.size(); i++) {
      LatLng p = ruta.get(i);
      dtos.add(new RutaPuntoRequest(i + 1, p.latitude, p.longitude));
    }

    isLoading.setValue(true);
    String token = sessionManager.leerToken();
    apiService.guardarRuta("Bearer " + token, idEvento, new GuardarRutaRequest(dtos)).enqueue(new Callback<ResponseBody>() {
      @Override
      public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
        isLoading.setValue(false);
        if (response.isSuccessful()) {
          ordenNavegarSalida.setValue("¡Circuito guardado exitosamente!");
          modoPuntosInteres = true;
        } else {
          mostrarError("Error al guardar en servidor");
        }
      }
      @Override
      public void onFailure(Call<ResponseBody> call, Throwable t) {
        isLoading.setValue(false);
        mostrarError("Error de conexión");
      }
    });
  }

  public void alternarCapas() {
    Integer actual = tipoMapa.getValue();
    tipoMapa.setValue(actual != null && actual == GoogleMap.MAP_TYPE_NORMAL ? GoogleMap.MAP_TYPE_HYBRID : GoogleMap.MAP_TYPE_NORMAL);
  }

  private void cargarRutaBackend(int idEvento) {
    isLoading.setValue(true);
    String token = sessionManager.leerToken();
    if(token != null) {
      apiService.obtenerMapaCompleto("Bearer "+token, idEvento).enqueue(new Callback<MapaEventoResponse>() {
        @Override
        public void onResponse(Call<MapaEventoResponse> call, Response<MapaEventoResponse> response) {
          isLoading.setValue(false);
          if (response.isSuccessful() && response.body() != null) {
            List<LatLng> puntos = new ArrayList<>();
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            if (response.body().getRuta() != null) {
              for (RutaPuntoResponse p : response.body().getRuta()) {
                LatLng latLng = new LatLng(p.getLatitud(), p.getLongitud());
                puntos.add(latLng);
                builder.include(latLng);
              }
            }
            puntosRuta.setValue(puntos);
            actualizarCalculosRuta(puntos);

            if (!puntos.isEmpty()) {
              modoPuntosInteres = true;
              try { ordenHacerZoomRuta.setValue(builder.build()); }
              catch (Exception e) { ordenCentrarCamara.setValue(puntos.get(0)); }
            } else {
              ordenCentrarCamara.setValue(new LatLng(-33.29501, -66.33563));
            }
          }
        }
        @Override
        public void onFailure(Call<MapaEventoResponse> call, Throwable t) {
          isLoading.setValue(false);
          mostrarError("No se pudo recuperar la ruta");
        }
      });
    } else {
      isLoading.setValue(false);
      mostrarError("No se pudo recuperar la ruta (Sin sesión)");
    }
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
}