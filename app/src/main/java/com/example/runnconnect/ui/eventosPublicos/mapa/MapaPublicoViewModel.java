package com.example.runnconnect.ui.eventosPublicos.mapa;

import android.app.Application;
import android.location.Location;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.runnconnect.data.repositorio.RutaRepositorio; // Usamos el Repo Correcto
import com.example.runnconnect.data.response.MapaEventoResponse;
import com.example.runnconnect.data.response.RutaPuntoResponse;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MapaPublicoViewModel extends AndroidViewModel {

  private final RutaRepositorio repositorio;

  // Estados de UI
  private final MutableLiveData<List<LatLng>> puntosRuta = new MutableLiveData<>();
  private final MutableLiveData<String> textoDistancia = new MutableLiveData<>("");
  private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
  private final MutableLiveData<String> mensajeError = new MutableLiveData<>();
  private final MutableLiveData<Integer> tipoMapa = new MutableLiveData<>(GoogleMap.MAP_TYPE_NORMAL);

  // Acciones de Camara (Zoom automatico)
  private final MutableLiveData<LatLngBounds> ordenHacerZoomRuta = new MutableLiveData<>();
  private final MutableLiveData<LatLng> ordenCentrarCamara = new MutableLiveData<>();
  public MapaPublicoViewModel(@NonNull Application application) {
    super(application);
    repositorio = new RutaRepositorio(application);
  }

  // Getters
  public LiveData<List<LatLng>> getPuntosRuta() { return puntosRuta; }
  public LiveData<String> getTextoDistancia() { return textoDistancia; }
  public LiveData<Boolean> getIsLoading() { return isLoading; }
  public LiveData<String> getMensajeError() { return mensajeError; }
  public LiveData<LatLngBounds> getOrdenHacerZoomRuta() { return ordenHacerZoomRuta; }
  public LiveData<LatLng> getOrdenCentrarCamara() { return ordenCentrarCamara; }
  public LiveData<Integer> getTipoMapa() { return tipoMapa; }
  public void cargarRuta(int idEvento) {
    isLoading.setValue(true);

    // Usamos el metodo PUBLICO del repositorio
    repositorio.obtenerRutaPublica(idEvento, new Callback<MapaEventoResponse>() {
      @Override
      public void onResponse(Call<MapaEventoResponse> call, Response<MapaEventoResponse> response) {
        isLoading.setValue(false);

        if (response.isSuccessful() && response.body() != null) {
          List<LatLng> puntos = new ArrayList<>();
          LatLngBounds.Builder builder = new LatLngBounds.Builder();
          boolean hayPuntos = false;

          // PARSEO: Convertir respuesta API a LatLng
          if (response.body().getRuta() != null) {
            for (RutaPuntoResponse p : response.body().getRuta()) {
              LatLng latLng = new LatLng(p.getLatitud(), p.getLongitud());
              puntos.add(latLng);
              builder.include(latLng); // Agregamos al calculador de Zoom
              hayPuntos = true;
            }
          }

          // Actualizar UI
          puntosRuta.setValue(puntos);
          calcularDistancia(puntos);

          // Zoom
          if (hayPuntos) {
            try {
              ordenHacerZoomRuta.setValue(builder.build());
            } catch (Exception e) {
              // Fallback si el builder falla (ej. 1 solo punto)
              if (!puntos.isEmpty()) ordenCentrarCamara.setValue(puntos.get(0));
            }
          } else {
            mensajeError.setValue("Este evento no tiene ruta cargada.");
          }

        } else {
          mensajeError.setValue("No se pudo cargar el mapa. Código: " + response.code());
        }
      }

      @Override
      public void onFailure(Call<MapaEventoResponse> call, Throwable t) {
        isLoading.setValue(false);
        mensajeError.setValue("Error de conexión.");
      }
    });
  }

  // Calculo matematico de distancia (igual al Organizador)
  private void calcularDistancia(List<LatLng> puntos) {
    if (puntos == null || puntos.size() < 2) return;

    double distancia = 0;
    float[] res = new float[1];
    for (int i = 0; i < puntos.size() - 1; i++) {
      Location.distanceBetween(
        puntos.get(i).latitude, puntos.get(i).longitude,
        puntos.get(i + 1).latitude, puntos.get(i + 1).longitude, res
      );
      distancia += res[0];
    }
    textoDistancia.setValue(String.format("%.2f km", distancia / 1000.0));
  }

  //Capa de mapa
  public void alternarTipoMapa() {
    Integer actual = tipoMapa.getValue();
    if (actual != null && actual == GoogleMap.MAP_TYPE_NORMAL) {
      tipoMapa.setValue(GoogleMap.MAP_TYPE_HYBRID); // Híbrido (Satelital + Calles)
    } else {
      tipoMapa.setValue(GoogleMap.MAP_TYPE_NORMAL); // Normal
    }
  }


}