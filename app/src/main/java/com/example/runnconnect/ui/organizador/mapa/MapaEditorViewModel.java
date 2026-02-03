package com.example.runnconnect.ui.organizador.mapa;

import android.app.Application;
import android.location.Location;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.runnconnect.data.repositorio.EventoRepositorio;
import com.example.runnconnect.data.repositorio.RutaRepositorio;
import com.example.runnconnect.data.request.CrearPuntoInteresRequest;
import com.example.runnconnect.data.request.GuardarRutaRequest;
import com.example.runnconnect.data.request.RutaPuntoRequest;
import com.example.runnconnect.data.response.MapaEventoResponse;
import com.example.runnconnect.data.response.PuntoInteresResponse;
import com.example.runnconnect.data.response.PuntosInteresEventoResponse;
import com.example.runnconnect.data.response.RutaPuntoResponse;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.maps.android.PolyUtil;
import com.google.maps.android.SphericalUtil;

import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MapaEditorViewModel extends AndroidViewModel {

  private final RutaRepositorio rutaRepositorio;
  private final EventoRepositorio eventoRepositorio;

  // --- ESTADOS DE LA VISTA ---
  private final MutableLiveData<List<LatLng>> puntosRuta = new MutableLiveData<>(new ArrayList<>());

  // lista de flechas calculadas (Posicion + Rotacion)
  private final MutableLiveData<List<FlechaMapa>> flechasGuias = new MutableLiveData<>(new ArrayList<>());

  private final MutableLiveData<String> textoDistancia = new MutableLiveData<>("0.00 km");
  private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
  private final MutableLiveData<Integer> tipoMapa = new MutableLiveData<>(GoogleMap.MAP_TYPE_NORMAL);

  // --- ORDENES ---
  private final MutableLiveData<String> ordenNavegarSalida = new MutableLiveData<>(null);
  private final MutableLiveData<String> ordenMostrarError = new MutableLiveData<>(null);
  private final MutableLiveData<LatLngBounds> ordenHacerZoomRuta = new MutableLiveData<>(null);
  private final MutableLiveData<LatLng> ordenCentrarCamara = new MutableLiveData<>(null);
  private final MutableLiveData<LatLng> ordenPedirDatosPI = new MutableLiveData<>(null);

  private final MutableLiveData<List<PuntoInteresResponse>> listaPuntosInteres = new MutableLiveData<>();

  private boolean datosCargados = false;
  private boolean modoPuntosInteres = false;

  // Mapeo interno: Indices del Spinner -> Strings de la API
  private final String[] TIPOS_PUNTO_API = {"hidratacion", "primeros_auxilios", "punto_energetico", "otro"};
  private final String[] NOMBRES_PUNTO_UI = {"Hidratación", "Primeros Auxilios", "Punto Energético", "Otro"};
  public MapaEditorViewModel(@NonNull Application application) {
    super(application);
    rutaRepositorio = new RutaRepositorio(application);
    eventoRepositorio = new EventoRepositorio(application);
  }

  // --- GETTERS ---
  public String[] getNombresPuntoUi(){ return NOMBRES_PUNTO_UI; }
  public LiveData<List<LatLng>> getPuntosRuta() { return puntosRuta; }
  public LiveData<List<FlechaMapa>> getFlechasGuias() { return flechasGuias; } // Nuevo
  public LiveData<String> getTextoDistancia() { return textoDistancia; }
  public LiveData<Boolean> getIsLoading() { return isLoading; }
  public LiveData<Integer> getTipoMapa() { return tipoMapa; }
  public LiveData<String> getOrdenNavegarSalida() { return ordenNavegarSalida; }
  public LiveData<String> getOrdenMostrarError() { return ordenMostrarError; }
  public LiveData<LatLngBounds> getOrdenHacerZoomRuta() { return ordenHacerZoomRuta; }
  public LiveData<LatLng> getOrdenCentrarCamara() { return ordenCentrarCamara; }
  public LiveData<LatLng> getOrdenPedirDatosPI() { return ordenPedirDatosPI; }
  public LiveData<List<PuntoInteresResponse>> getListaPuntosInteres() { return listaPuntosInteres; }

  // --- RESETS ---
  public void resetOrdenNavegacion() { ordenNavegarSalida.setValue(null); }
  public void resetOrdenError() { ordenMostrarError.setValue(null); }
  public void resetOrdenCamara() { ordenHacerZoomRuta.setValue(null); ordenCentrarCamara.setValue(null); }
  public void resetOrdenDialogo() { ordenPedirDatosPI.setValue(null); }

  // --- LOGICA DE NEGOCIO ---

  public void onMapReady(int idEvento) {
    if (datosCargados) return;
    datosCargados = true;
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
      ordenMostrarError.setValue("No se puede deshacer una ruta ya guardada");
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
      return;
    }

    double distanciaTotal = 0;
    double acumuladoFlechas = 0;
    double intervaloFlechas = 150; // Metros
    List<FlechaMapa> nuevasFlechas = new ArrayList<>();
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
        nuevasFlechas.add(new FlechaMapa(p1, heading));
        acumuladoFlechas = 0;
      }
    }

    textoDistancia.setValue(String.format("%.2f km", distanciaTotal / 1000.0));
    flechasGuias.setValue(nuevasFlechas);
  }

  private void validarPuntoInteres(LatLng puntoClickeado) {
    List<LatLng> ruta = puntosRuta.getValue();
    if (ruta == null || ruta.size() < 2) {
      ordenMostrarError.setValue("Primero debés dibujar y guardar el circuito");
      return;
    }
    boolean estaEnRuta = PolyUtil.isLocationOnPath(puntoClickeado, ruta, true, 20);

    if (estaEnRuta) {
      ordenPedirDatosPI.setValue(puntoClickeado);
    } else {
      ordenMostrarError.setValue("El punto debe estar sobre el circuito (línea azul)");
    }
  }

  // ahora recibe el indice del spinner
  public void guardarPuntoInteresPorIndice(int idEvento, int indiceSpinner, LatLng latLng) {
    if (indiceSpinner < 0 || indiceSpinner >= TIPOS_PUNTO_API.length) {
      ordenMostrarError.setValue("Tipo de punto no válido");
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

    eventoRepositorio.crearPuntoInteres(idEvento, request, new Callback<ResponseBody>() {
      @Override
      public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
        isLoading.setValue(false);
        if (response.isSuccessful()) {
          ordenMostrarError.setValue("Punto de interes agregado!");
          cargarPuntosInteres(idEvento);
        } else {
          String errorMsg = "Error desconocido";
          try {
            // Leemos el stream del error UNA sola vez
            if (response.errorBody() != null) {
              errorMsg = response.errorBody().string();
            }
          } catch (Exception e) {
            e.printStackTrace();
          }
          String logMsg = "Código: " + response.code() + " | Mensaje: " + errorMsg;
          Log.e("ERROR_PUNTO", logMsg);
          ordenMostrarError.setValue("Error al guardar: " + response.code());
        }
      }
      @Override
      public void onFailure(Call<ResponseBody> call, Throwable t) {
        isLoading.setValue(false);
        ordenMostrarError.setValue("Error de conexión");
      }
    });
  }

  public void cargarPuntosInteres(int idEvento) {
    eventoRepositorio.obtenerPuntosInteres(idEvento, new Callback<PuntosInteresEventoResponse>() {
      @Override
      public void onResponse(Call<PuntosInteresEventoResponse> call, Response<PuntosInteresEventoResponse> response) {
        if (response.isSuccessful() && response.body() != null) {
          listaPuntosInteres.setValue(response.body().getPuntosInteres());
        }
      }
      @Override
      public void onFailure(Call<PuntosInteresEventoResponse> call, Throwable t) {}
    });
  }

  public void guardarRuta(int idEvento) {
    if (idEvento == 0) { ordenMostrarError.setValue("Error: No hay evento asociado"); return; }
    List<LatLng> ruta = puntosRuta.getValue();
    if (ruta == null || ruta.size() < 2) { ordenMostrarError.setValue("Marca Inicio y Fin en el mapa"); return; }

    List<RutaPuntoRequest> dtos = new ArrayList<>();
    for (int i = 0; i < ruta.size(); i++) {
      LatLng p = ruta.get(i);
      dtos.add(new RutaPuntoRequest(i + 1, p.latitude, p.longitude));
    }

    isLoading.setValue(true);
    rutaRepositorio.guardarRuta(idEvento, new GuardarRutaRequest(dtos), new Callback<ResponseBody>() {
      @Override
      public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
        isLoading.setValue(false);
        if (response.isSuccessful()) {
          ordenNavegarSalida.setValue("¡Circuito guardado exitosamente!");
          modoPuntosInteres = true;
        } else {
          ordenMostrarError.setValue("Error al guardar en servidor");
        }
      }
      @Override
      public void onFailure(Call<ResponseBody> call, Throwable t) {
        isLoading.setValue(false);
        ordenMostrarError.setValue("Error de conexión");
      }
    });
  }

  public void alternarCapas() {
    Integer actual = tipoMapa.getValue();
    tipoMapa.setValue(actual != null && actual == GoogleMap.MAP_TYPE_NORMAL ? GoogleMap.MAP_TYPE_HYBRID : GoogleMap.MAP_TYPE_NORMAL);
  }

  private void cargarRutaBackend(int idEvento) {
    isLoading.setValue(true);
    rutaRepositorio.obtenerRuta(idEvento, new Callback<MapaEventoResponse>() {
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
        ordenMostrarError.setValue("No se pudo recuperar la ruta");
      }
    });
  }

  // CLASE INTERNA PARA TRANSPORTAR DATOS DE FLECHAS A LA VISTA
  public static class FlechaMapa {
    public final LatLng posicion;
    public final float rotacion;

    public FlechaMapa(LatLng posicion, float rotacion) {
      this.posicion = posicion;
      this.rotacion = rotacion;
    }
  }
}