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
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.maps.android.PolyUtil;

import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MapaEditorViewModel extends AndroidViewModel {

  private final RutaRepositorio repositorio;

  // --- ESTADOS DE LA VISTA (Datos que se muestran) ---
  private final MutableLiveData<List<LatLng>> puntosRuta = new MutableLiveData<>(new ArrayList<>());
  private final MutableLiveData<String> textoDistancia = new MutableLiveData<>("0.00 km");
  private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
  private final MutableLiveData<Integer> tipoMapa = new MutableLiveData<>(GoogleMap.MAP_TYPE_NORMAL);

  // --- ORDENES PARA LA VISTA (Acciones puntuales) ---
  // Usamos null para indicar "sin orden pendiente"
  private final MutableLiveData<String> ordenNavegarSalida = new MutableLiveData<>(null);
  private final MutableLiveData<String> ordenMostrarError = new MutableLiveData<>(null);
  private final MutableLiveData<LatLngBounds> ordenHacerZoomRuta = new MutableLiveData<>(null);
  private final MutableLiveData<LatLng> ordenCentrarCamara = new MutableLiveData<>(null);
  private final MutableLiveData<LatLng> ordenPedirDatosPI = new MutableLiveData<>(null); //puntos interes

  // Control interno para no recargar si ya se cargo
  private boolean datosCargados = false;

  public MapaEditorViewModel(@NonNull Application application) {
    super(application);
    repositorio = new RutaRepositorio(application);
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
  public LiveData<LatLng> getOrdenPedirDatosPI() { return ordenPedirDatosPI; }

  // --- METODOS DE RESET (Para que las ordenes no se repitan) ---
  public void resetOrdenNavegacion() { ordenNavegarSalida.setValue(null); }
  public void resetOrdenError() { ordenMostrarError.setValue(null); }
  public void resetOrdenCamara() {
    ordenHacerZoomRuta.setValue(null);
    ordenCentrarCamara.setValue(null);
  }

  //puntos interes
  public void validarPuntoInteres(LatLng puntoClickeado) {
    List<LatLng> ruta = puntosRuta.getValue();
    if (ruta == null || ruta.size() < 2) {
      ordenMostrarError.setValue("Primero debés guardar el circuito");
      return;
    }

    // VALIDACION: esta el clic a menos de 15 metros de la ruta?
    boolean estaEnRuta = PolyUtil.isLocationOnPath(puntoClickeado, ruta, true, 15);

    if (estaEnRuta) {
      ordenPedirDatosPI.setValue(puntoClickeado); // Avisamos al Fragment para abrir el Spinner
    } else {
      ordenMostrarError.setValue("Los puntos de interés deben marcarse sobre el circuito");
    }
  }

  public void resetOrdenDialogo() { ordenPedirDatosPI.setValue(null); }

  // --- ENTRADAS (Eventos desde el Fragment) ---

  // 1. Cuando el mapa esta listo
  public void onMapReady(int idEvento) {
    if (datosCargados) return;
    datosCargados = true;

    if (idEvento != 0) {
      cargarRutaBackend(idEvento);
    } else {
      // Logica: Si es nuevo, centrar en San Luis
      ordenCentrarCamara.setValue(new LatLng(-33.29501, -66.33563));
    }
  }

  // 2. Click en el mapa
  public void agregarPunto(LatLng punto) {
    List<LatLng> lista = puntosRuta.getValue();
    if (lista != null) {
      lista.add(punto);
      puntosRuta.setValue(lista);
      calcularDistancia(lista);
    }
  }

  // 3. Boton Guardar
  public void guardarRuta(int idEvento) {
    if (idEvento == 0) {
      ordenMostrarError.setValue("Error: No hay evento asociado");
      return;
    }

    List<LatLng> ruta = puntosRuta.getValue();
    if (ruta == null || ruta.size() < 2) {
      ordenMostrarError.setValue("Marca Inicio y Fin en el mapa");
      return;
    }

    // Mapeo a Request
    List<RutaPuntoRequest> dtos = new ArrayList<>();
    for (int i = 0; i < ruta.size(); i++) {
      LatLng p = ruta.get(i);
      dtos.add(new RutaPuntoRequest(i + 1, p.latitude, p.longitude));
    }

    isLoading.setValue(true);
    repositorio.guardarRuta(idEvento, new GuardarRutaRequest(dtos), new Callback<ResponseBody>() {
      @Override
      public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
        isLoading.setValue(false);
        if (response.isSuccessful()) {
          // Logica: Exito -> Ordenar salir con mensaje
          ordenNavegarSalida.setValue("¡Circuito guardado exitosamente!");
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

  // --- ACCIONES DE UI SIMPLES ---
  public void deshacer() {
    List<LatLng> lista = puntosRuta.getValue();
    if (lista != null && !lista.isEmpty()) {
      lista.remove(lista.size() - 1);
      puntosRuta.setValue(lista);
      calcularDistancia(lista);
    }
  }

  public void alternarCapas() {
    Integer actual = tipoMapa.getValue();
    if (actual != null && actual == GoogleMap.MAP_TYPE_NORMAL) {
      tipoMapa.setValue(GoogleMap.MAP_TYPE_HYBRID);
    } else {
      tipoMapa.setValue(GoogleMap.MAP_TYPE_NORMAL);
    }
  }

  // --- LOGICA PRIVADA (Calculos) ---

  private void cargarRutaBackend(int idEvento) {
    isLoading.setValue(true);
    repositorio.obtenerRuta(idEvento, new Callback<MapaEventoResponse>() {
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
              builder.include(latLng); // Logica matematica aqui
            }
          }
          puntosRuta.setValue(puntos);
          calcularDistancia(puntos);

          // Decidir encuadre
          if (!puntos.isEmpty()) {
            try {
              // Si hay puntos, mandamos orden de Zoom (Bounds)
              ordenHacerZoomRuta.setValue(builder.build());
            } catch (Exception e) {
              ordenCentrarCamara.setValue(puntos.get(0));
            }
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
    if (puntos == null || puntos.size() < 2) {
      textoDistancia.setValue("0.00 km");
      return;
    }
    double distancia = 0;
    float[] res = new float[1];
    for (int i = 0; i < puntos.size() - 1; i++) {
      Location.distanceBetween(
        puntos.get(i).latitude, puntos.get(i).longitude,
        puntos.get(i+1).latitude, puntos.get(i+1).longitude, res
      );
      distancia += res[0];
    }
    textoDistancia.setValue(String.format("%.2f km", distancia / 1000.0));
  }
}