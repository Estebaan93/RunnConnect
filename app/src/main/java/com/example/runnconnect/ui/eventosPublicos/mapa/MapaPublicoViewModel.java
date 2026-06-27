package com.example.runnconnect.ui.eventosPublicos.mapa;

import android.app.Application;
import android.content.Intent;
import android.location.Location;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.runnconnect.data.repositorio.RutaRepositorio; // Usamos el Repo Correcto
import com.example.runnconnect.data.response.MapaEventoResponse;
import com.example.runnconnect.data.response.RutaPuntoResponse;
import com.example.runnconnect.ui.login.LoginActivity;
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

  // estados de UI
  private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
  private final MutableLiveData<List<LatLng>> puntosRuta = new MutableLiveData<>();
  private final MutableLiveData<String> textoDistancia = new MutableLiveData<>("");
  private final MutableLiveData<LatLng> puntoMeta = new MutableLiveData<>(); // null si no hay meta
  // Error handling
  private final MutableLiveData<String> errorText = new MutableLiveData<>();
  private final MutableLiveData<Integer> errorVisibility = new MutableLiveData<>(View.GONE);
  private final MutableLiveData<Integer> tipoMapa = new MutableLiveData<>();
  // Acciones de Camara (zoom automatico)
  private final MutableLiveData<LatLngBounds> ordenHacerZoomRuta = new MutableLiveData<>();
  private final MutableLiveData<LatLng> ordenCentrarCamara = new MutableLiveData<>();
  private final MutableLiveData<Boolean> finalUser = new MutableLiveData<>();
  private final MutableLiveData<Boolean> volverAtras = new MutableLiveData<>();
  public MapaPublicoViewModel(@NonNull Application application) {
    super(application);
    repositorio = new RutaRepositorio(application);
  }

  // Getters
  public LiveData<Boolean> getIsLoading() { return isLoading; }
  public LiveData<List<LatLng>> getPuntosRuta() { return puntosRuta; }
  public LiveData<String> getTextoDistancia() { return textoDistancia; }
  public LiveData<LatLng> getPuntoMeta() { return puntoMeta; }
  public LiveData<String> getErrorText() { return errorText; }
  public LiveData<Integer> getErrorVisibility() { return errorVisibility; }
  public LiveData<Integer> getTipoMapa() { return tipoMapa; }
  public LiveData<LatLngBounds> getOrdenHacerZoomRuta() { return ordenHacerZoomRuta; }
  public LiveData<LatLng> getOrdenCentrarCamara() { return ordenCentrarCamara; }
  public LiveData<Boolean> getFinalUser() { return finalUser; }
  public LiveData<Boolean> getVolverAtras() { return volverAtras; }


  /*se llama cuando el mapa ya esta listo.
  el ViewModel valida el id, carga la ruta y emite el tipo de mapa inicial.*/
  public void onMapaListo(int idEvento) {
    if (idEvento == 0) {
      mostrarError("Error: Evento no identificado");
      finalUser.setValue(true);
      finalUser.postValue(false);
      return;
    }
    //primera emision del tipo de mapa (ahora que el mapa esta disponible)
    tipoMapa.setValue(GoogleMap.MAP_TYPE_NORMAL);
    cargarRuta(idEvento);
  }

  public void cargarRuta(int idEvento) {
    isLoading.setValue(true);

    ocultarError();

    //usamos el metodo publico del repositorio
    repositorio.obtenerRutaPublica(idEvento, new Callback<MapaEventoResponse>() {
      @Override
      public void onResponse(Call<MapaEventoResponse> call, Response<MapaEventoResponse> response) {
        isLoading.setValue(false);

        if (response.isSuccessful() && response.body() != null) {
          List<LatLng> puntos = new ArrayList<>();
          LatLngBounds.Builder builder = new LatLngBounds.Builder();
          boolean hayPuntos = false;

          // PARSEO: convertir respuesta API a LatLng
          if (response.body().getRuta() != null) {
            for (RutaPuntoResponse p : response.body().getRuta()) {
              LatLng latLng = new LatLng(p.getLatitud(), p.getLongitud());
              puntos.add(latLng);
              builder.include(latLng); // Agregamos al calculador de Zoom
              hayPuntos = true;
            }
          }

          // Actualizar UI
         /* puntosRuta.setValue(puntos);
          calcularDistancia(puntos);*/

          // Zoom
          if (hayPuntos) {
            puntosRuta.setValue(puntos);
            calcularDistancia(puntos);
            // Marcador de meta solo si hay mas de un punto
            puntoMeta.setValue(puntos.size() > 1 ? puntos.get(puntos.size() - 1) : null);
            try {
              ordenHacerZoomRuta.setValue(builder.build());
            } catch (Exception e) {
              // Fallback si el builder falla (ej. 1 solo punto)
              ordenCentrarCamara.setValue(puntos.get(0));
            }
          } else {
            mostrarError("Este evento no tiene ruta cargada.");
          }

        } else {
          mostrarError("No se pudo cargar el mapa. Código: " + response.code());
        }
      }

      @Override
      public void onFailure(Call<MapaEventoResponse> call, Throwable t) {
        isLoading.setValue(false);
        mostrarError("Error de conexión.");
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
      tipoMapa.setValue(GoogleMap.MAP_TYPE_HYBRID); // Hibrido (Satelital + Calles)
    } else {
      tipoMapa.setValue(GoogleMap.MAP_TYPE_NORMAL); // Normal
    }
  }


  private void mostrarError(String mensaje) {
    errorText.setValue(mensaje);
    errorVisibility.setValue(View.VISIBLE);
  }

  private void ocultarError() {
    errorVisibility.setValue(View.GONE);
  }

  public void flechaVolverAtras() {
    volverAtras.setValue(true);
    volverAtras.postValue(false);   // reseteo automatico
  }

}