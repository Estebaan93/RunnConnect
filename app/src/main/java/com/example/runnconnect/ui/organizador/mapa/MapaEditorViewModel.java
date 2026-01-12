package com.example.runnconnect.ui.organizador.mapa;

import android.app.Application;
import android.location.Location;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.runnconnect.data.repositorio.RutaRepositorio;
import com.example.runnconnect.data.request.GuardarRutaRequest;
import com.example.runnconnect.data.request.RutaPuntoRequest;
import com.example.runnconnect.data.response.MapaEventoResponse;
import com.example.runnconnect.data.response.RutaPuntoResponse;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;


import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MapaEditorViewModel extends AndroidViewModel {

  private final RutaRepositorio repositorio;

  // Lista de puntos visuales (Google Maps)
  private final MutableLiveData<List<LatLng>> puntosRuta = new MutableLiveData<>(new ArrayList<>());

  // Estados UI
  private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
  private final MutableLiveData<String> mensaje = new MutableLiveData<>();
  private final MutableLiveData<Boolean> guardadoExitoso = new MutableLiveData<>(false);

  //distancia acumulada
  private final MutableLiveData<String> textoDistancia = new MutableLiveData<>("0.00 km");

  public MapaEditorViewModel(@NonNull Application application) {
    super(application);
    repositorio = new RutaRepositorio(application);
  }

  // Getters
  public LiveData<List<LatLng>> getPuntosRuta() {
    return puntosRuta;
  }

  public LiveData<Boolean> getIsLoading() {
    return isLoading;
  }

  public LiveData<String> getMensaje() {
    return mensaje;
  }

  public LiveData<Boolean> getGuardadoExitoso() {
    return guardadoExitoso;
  }

  public LiveData<String> getTextoDistancia() {
    return textoDistancia;
  }

  // Logica: Agregar punto al tocar el mapa
  public void agregarPunto(LatLng punto) {
    List<LatLng> actual = puntosRuta.getValue();
    if (actual != null) {
      actual.add(punto);
      puntosRuta.setValue(actual); // Notificar a la vista
      //recalcular al agregar
      calcularDistancia(actual);
    }
  }

  // Logica: Deshacer ultimo punto (boton "Undo")
  public void deshacerUltimo() {
    List<LatLng> actual = puntosRuta.getValue();
    if (actual != null && !actual.isEmpty()) {
      actual.remove(actual.size() - 1);
      puntosRuta.setValue(actual);

      //recalcular al borrar
      calcularDistancia(actual);
    }
  }

  //calcular distancia
  private void calcularDistancia(List<LatLng> puntos) {
    if (puntos == null || puntos.size() < 2) {
      textoDistancia.setValue("0.00 km");
      return;
    }
    double distanciaMetros = 0;
    float[] resultados = new float[1];

    for (int i = 0; i < puntos.size() - 1; i++) {
      LatLng p1 = puntos.get(i);
      LatLng p2 = puntos.get(i + 1);

      // Calculo preciso entre dos coordenadas
      Location.distanceBetween(
        p1.latitude, p1.longitude,
        p2.latitude, p2.longitude,
        resultados
      );
      distanciaMetros += resultados[0];
    }

    double km = distanciaMetros / 1000.0;
    textoDistancia.setValue(String.format("%.2f km", km));
  }


  // Logica: Guardar en Backend
  public void guardarRuta(int idEvento) {
    List<LatLng> ruta = puntosRuta.getValue();

    if (ruta == null || ruta.size() < 2) {
      mensaje.setValue("Debes marcar al menos 2 puntos (Inicio y Fin)");
      return;
    }

    // Convertir LatLng (Android) a DTO (Request API)
    List<RutaPuntoRequest> dtos = new ArrayList<>();
    for (int i = 0; i < ruta.size(); i++) {
      LatLng p = ruta.get(i);
      // Orden: i + 1, Lat, Lng
      dtos.add(new RutaPuntoRequest(i + 1, p.latitude, p.longitude));
    }

    GuardarRutaRequest request = new GuardarRutaRequest(dtos);

    isLoading.setValue(true);

    repositorio.guardarRuta(idEvento, request, new Callback<ResponseBody>() {
      @Override
      public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
        isLoading.setValue(false);
        if (response.isSuccessful()) {
          mensaje.setValue("¡Circuito guardado exitosamente!");
          guardadoExitoso.setValue(true);
        } else {
          mensaje.setValue("Error al guardar ruta");
        }
      }

      @Override
      public void onFailure(Call<ResponseBody> call, Throwable t) {
        isLoading.setValue(false);
        mensaje.setValue("Error de conexión: " + t.getMessage());
      }
    });
  }

  //recuperar ruta y puntos
  public void cargarRutaExistente(int idEvento) {
    isLoading.setValue(true);
    repositorio.obtenerRuta(idEvento, new Callback<MapaEventoResponse>() {
      @Override
      public void onResponse(Call<MapaEventoResponse> call, Response<MapaEventoResponse> response) {
        isLoading.setValue(false);
        if (response.isSuccessful() && response.body() != null) {
          List<LatLng> puntos = new ArrayList<>();
          // Asumiendo que response.body().getRuta() devuelve lista de puntos
          if (response.body().getRuta() != null) {
            for (RutaPuntoResponse p : response.body().getRuta()) {
              puntos.add(new LatLng(p.getLatitud(), p.getLongitud()));
            }
            // Actualizamos el LiveData, esto disparará el observer en el Fragment y dibujará
            puntosRuta.setValue(puntos);
            calcularDistancia(puntos);
          }
        }
      }

      @Override
      public void onFailure(Call<MapaEventoResponse> call, Throwable t) {
        isLoading.setValue(false);
        mensaje.setValue("Error al cargar ruta previa");
      }
    });
  }


}