package com.example.runnconnect.ui.organizador.mapa;

import android.app.Application;
import android.location.Location;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.runnconnect.data.repositorio.EventoRepositorio; // Nuevo
import com.example.runnconnect.data.repositorio.RutaRepositorio;
import com.example.runnconnect.data.request.CrearPuntoInteresRequest; // Nuevo
import com.example.runnconnect.data.request.GuardarRutaRequest;
import com.example.runnconnect.data.request.RutaPuntoRequest;
import com.example.runnconnect.data.response.MapaEventoResponse;
import com.example.runnconnect.data.response.PuntoInteresResponse;
import com.example.runnconnect.data.response.PuntosInteresEventoResponse;
import com.example.runnconnect.data.response.RutaPuntoResponse;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.maps.android.PolyUtil; // Necesario para el imán

import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MapaEditorViewModel extends AndroidViewModel {

  private final RutaRepositorio rutaRepositorio;
  private final EventoRepositorio eventoRepositorio; // Nuevo Repo para PIs

  // --- ESTADOS DE LA VISTA ---
  private final MutableLiveData<List<LatLng>> puntosRuta = new MutableLiveData<>(new ArrayList<>());
  private final MutableLiveData<String> textoDistancia = new MutableLiveData<>("0.00 km");
  private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
  private final MutableLiveData<Integer> tipoMapa = new MutableLiveData<>(GoogleMap.MAP_TYPE_NORMAL);

  // --- ORDENES PARA LA VISTA ---
  private final MutableLiveData<String> ordenNavegarSalida = new MutableLiveData<>(null);
  private final MutableLiveData<String> ordenMostrarError = new MutableLiveData<>(null);
  private final MutableLiveData<LatLngBounds> ordenHacerZoomRuta = new MutableLiveData<>(null);
  private final MutableLiveData<LatLng> ordenCentrarCamara = new MutableLiveData<>(null);

  // NUEVO: Orden para abrir el diálogo de Puntos de Interés
  private final MutableLiveData<LatLng> ordenPedirDatosPI = new MutableLiveData<>(null);
  private final MutableLiveData<List<PuntoInteresResponse>> listaPuntosInteres = new MutableLiveData<>();
  private boolean datosCargados = false;
  private boolean modoPuntosInteres = false; // Flag para controlar el click

  public MapaEditorViewModel(@NonNull Application application) {
    super(application);
    rutaRepositorio = new RutaRepositorio(application);
    eventoRepositorio = new EventoRepositorio(application);
  }

  // --- GETTERS ---
  public LiveData<List<LatLng>> getPuntosRuta() { return puntosRuta; }
  public LiveData<String> getTextoDistancia() { return textoDistancia; }
  public LiveData<Boolean> getIsLoading() { return isLoading; }
  public LiveData<Integer> getTipoMapa() { return tipoMapa; }
  public LiveData<String> getOrdenNavegarSalida() { return ordenNavegarSalida; }
  public LiveData<String> getOrdenMostrarError() { return ordenMostrarError; }
  public LiveData<LatLngBounds> getOrdenHacerZoomRuta() { return ordenHacerZoomRuta; }
  public LiveData<LatLng> getOrdenCentrarCamara() { return ordenCentrarCamara; }
  public LiveData<LatLng> getOrdenPedirDatosPI() { return ordenPedirDatosPI; } // Nuevo Getter
  public LiveData<List<PuntoInteresResponse>> getListaPuntosInteres() { return listaPuntosInteres; }
  // --- RESETS ---
  public void resetOrdenNavegacion() { ordenNavegarSalida.setValue(null); }
  public void resetOrdenError() { ordenMostrarError.setValue(null); }
  public void resetOrdenCamara() { ordenHacerZoomRuta.setValue(null); ordenCentrarCamara.setValue(null); }
  public void resetOrdenDialogo() { ordenPedirDatosPI.setValue(null); } // Nuevo Reset

  // --- ENTRADAS (Eventos) ---

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

  // LÓGICA PRINCIPAL DEL CLICK (Fusionada)
  public void procesarClickMapa(LatLng punto) {
    if (modoPuntosInteres) {
      // Si la ruta ya está cargada/guardada, intentamos validar un PI
      validarPuntoInteres(punto);
    } else {
      // Si estamos dibujando, agregamos puntos al trazado
      agregarPuntoRuta(punto);
    }
  }

  // Lógica Original de Dibujo
  private void agregarPuntoRuta(LatLng punto) {
    List<LatLng> lista = puntosRuta.getValue();
    if (lista != null) {
      lista.add(punto);
      puntosRuta.setValue(lista);
      calcularDistancia(lista);
    }
  }

  // NUEVA: Lógica del Imán para PIs
  private void validarPuntoInteres(LatLng puntoClickeado) {
    List<LatLng> ruta = puntosRuta.getValue();
    if (ruta == null || ruta.size() < 2) {
      ordenMostrarError.setValue("Primero debés dibujar y guardar el circuito");
      return;
    }
    // Tolerancia 20 metros
    boolean estaEnRuta = PolyUtil.isLocationOnPath(puntoClickeado, ruta, true, 20);

    if (estaEnRuta) {
      ordenPedirDatosPI.setValue(puntoClickeado);
    } else {
      ordenMostrarError.setValue("El punto debe estar sobre el circuito (línea azul)");
    }
  }

  // NUEVA: Guardar PI en Backend
  public void guardarPuntoInteresBackend(int idEvento, String tipo, LatLng latLng) {
    isLoading.setValue(true);
    //CrearPuntoInteresRequest request = new CrearPuntoInteresRequest(tipo, nombre, latLng.latitude, latLng.longitude);
    String tipoApi= tipo.toLowerCase().trim();
    //String nombreApi= nombre.trim();

    CrearPuntoInteresRequest request= new CrearPuntoInteresRequest(tipoApi, latLng.latitude, latLng.longitude);

    eventoRepositorio.crearPuntoInteres(idEvento, request, new Callback<ResponseBody>() {
      @Override
      public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
        isLoading.setValue(false);
        if (response.isSuccessful()) {
          ordenMostrarError.setValue("¡Punto de interes agregado!"); // Usamos error para mostrar Toast rápido o creamos un LiveData de éxito
          cargarPuntosInteres(idEvento);
        } else {
          try{
            String errorBody = response.errorBody().string();
            android.util.Log.e("API_ERROR", "Error 400 Detalle: " + errorBody);
            ordenMostrarError.setValue("Error de validación: " + errorBody);
          }catch (Exception e){
            ordenMostrarError.setValue("Error " + response.code() + " sin detalle.");
          }
          ordenMostrarError.setValue("Error al guardar punto");
          Log.d("ErrorPuntoInteres", "ERROR: " + response.code() + response.toString());
        }
      }
      @Override
      public void onFailure(Call<ResponseBody> call, Throwable t) {
        isLoading.setValue(false);
        ordenMostrarError.setValue("Error de conexión");
      }
    });
  }
  //cargar punto interes
  public void cargarPuntosInteres(int idEvento) {
    // Usamos el endpoint GET PuntosInteres que ya tienes en tu API
    // Nota: Asegurate de tener este metodo en tu EventoRepositorio o RutaRepositorio
    eventoRepositorio.obtenerPuntosInteres(idEvento, new Callback<PuntosInteresEventoResponse>() {
      @Override
      public void onResponse(Call<PuntosInteresEventoResponse> call, Response<PuntosInteresEventoResponse> response) {
        if (response.isSuccessful() && response.body() != null) {
          // Actualizamos la lista, esto disparará el observer en el Fragment
          listaPuntosInteres.setValue(response.body().getPuntosInteres());
        }
      }
      @Override
      public void onFailure(Call<PuntosInteresEventoResponse> call, Throwable t) {
        // Manejo silencioso o log
      }
    });
  }

  // Lógica Original: Guardar Ruta
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
          modoPuntosInteres = true; // Al guardar, habilitamos modo PIs
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

  // Lógica Original: Deshacer
  public void deshacer() {
    if (modoPuntosInteres) {
      ordenMostrarError.setValue("No se puede deshacer una ruta ya guardada");
      return;
    }
    List<LatLng> lista = puntosRuta.getValue();
    if (lista != null && !lista.isEmpty()) {
      lista.remove(lista.size() - 1);
      puntosRuta.setValue(lista);
      calcularDistancia(lista);
    }
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
          calcularDistancia(puntos);

          // Si cargamos del backend, asumimos que ya existe ruta -> Modo PIs activado
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

  private void calcularDistancia(List<LatLng> puntos) {
    if (puntos == null || puntos.size() < 2) { textoDistancia.setValue("0.00 km"); return; }
    double distancia = 0;
    float[] res = new float[1];
    for (int i = 0; i < puntos.size() - 1; i++) {
      Location.distanceBetween(puntos.get(i).latitude, puntos.get(i).longitude, puntos.get(i+1).latitude, puntos.get(i+1).longitude, res);
      distancia += res[0];
    }
    textoDistancia.setValue(String.format("%.2f km", distancia / 1000.0));
  }
}