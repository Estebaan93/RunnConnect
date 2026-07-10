package com.example.runnconnect.ui.eventosPublicos.mapa;

import android.app.Application;
import android.location.Location;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.runnconnect.R;
import com.example.runnconnect.data.repositorio.EventoRepositorio;
import com.example.runnconnect.data.repositorio.RutaRepositorio;
import com.example.runnconnect.data.response.MapaEventoResponse;
import com.example.runnconnect.data.response.PuntoInteresResponse;
import com.example.runnconnect.data.response.PuntosInteresEventoResponse;
import com.example.runnconnect.data.response.RutaPuntoResponse;
import android.graphics.Color;
import com.example.runnconnect.utils.MapUtils;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.SphericalUtil;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MapaPublicoViewModel extends AndroidViewModel {

  private final RutaRepositorio repositorio;
  private final EventoRepositorio eventoRepositorio;

  // estados de UI
  private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
  private final MutableLiveData<PolylineOptions> lineaRuta = new MutableLiveData<>(new PolylineOptions());
  private final MutableLiveData<List<MarkerOptions>> marcadoresInicioFin = new MutableLiveData<>(new ArrayList<>());
  private final MutableLiveData<List<MarkerOptions>> flechasGuias = new MutableLiveData<>(new ArrayList<>());
  private final MutableLiveData<List<MarkerOptions>> listaPuntosInteres = new MutableLiveData<>(new ArrayList<>());
  private final MutableLiveData<String> textoDistancia = new MutableLiveData<>("");
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
    eventoRepositorio = new EventoRepositorio(application);
  }

  // Getters
  public LiveData<Boolean> getIsLoading() { return isLoading; }
  public LiveData<PolylineOptions> getLineaRuta() { return lineaRuta; }
  public LiveData<List<MarkerOptions>> getMarcadoresInicioFin() { return marcadoresInicioFin; }
  public LiveData<List<MarkerOptions>> getFlechasGuias() { return flechasGuias; }
  public LiveData<List<MarkerOptions>> getListaPuntosInteres() { return listaPuntosInteres; }
  public LiveData<String> getTextoDistancia() { return textoDistancia; }
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
      return;
    }
    //primera emision del tipo de mapa (ahora que el mapa esta disponible)
    tipoMapa.setValue(GoogleMap.MAP_TYPE_NORMAL);
    cargarRuta(idEvento);
    cargarPuntosInteres(idEvento);
  }

  public void cargarPuntosInteres(int idEvento) {
    eventoRepositorio.obtenerPuntosInteres(idEvento, new Callback<PuntosInteresEventoResponse>() {
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
              uiPuntos.add(new MarkerOptions()
                .position(posicion)
                .title(p.getNombre())
                .icon(BitmapDescriptorFactory.fromResource(resourceId))
                .anchor(0.5f, 0.5f));
            }
          }
          listaPuntosInteres.setValue(uiPuntos);
        }
      }
      @Override
      public void onFailure(Call<PuntosInteresEventoResponse> call, Throwable t) {}
    });
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
            PolylineOptions poly = new PolylineOptions()
              .addAll(puntos).width(12).color(Color.BLUE).geodesic(true);
            lineaRuta.setValue(poly);

            List<MarkerOptions> extremos = new ArrayList<>();
            extremos.add(new MarkerOptions().position(puntos.get(0)).title("Largada")
              .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
            if (puntos.size() > 1) {
              extremos.add(new MarkerOptions().position(puntos.get(puntos.size() - 1)).title("Meta")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
            }
            marcadoresInicioFin.setValue(extremos);

            calcularDistancia(puntos);
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

  // Calculo matematico de distancia y flechas
  private void calcularDistancia(List<LatLng> puntos) {
    if (puntos == null || puntos.size() < 2) {
      textoDistancia.setValue("0.00 km");
      flechasGuias.setValue(new ArrayList<>());
      lineaRuta.setValue(new PolylineOptions());
      marcadoresInicioFin.setValue(new ArrayList<>());
      return;
    }

    double distanciaTotal = 0;
    double acumuladoFlechas = 0;
    double intervaloFlechas = 500; // Metros
    List<MarkerOptions> nuevasFlechas = new ArrayList<>();
    float[] res = new float[1];

    for (int i = 0; i < puntos.size() - 1; i++) {
      LatLng p1 = puntos.get(i);
      LatLng p2 = puntos.get(i + 1);

      Location.distanceBetween(p1.latitude, p1.longitude, p2.latitude, p2.longitude, res);
      double distSegmento = res[0];
      distanciaTotal += distSegmento;
      acumuladoFlechas += distSegmento;

      if (acumuladoFlechas >= intervaloFlechas) {
        float heading = (float) SphericalUtil.computeHeading(p1, p2);
        nuevasFlechas.add(new MarkerOptions()
          .position(p1)
          .icon(MapUtils.bitmapDescriptorFromVector(getApplication(), R.drawable.ic_flecha_sentido))
          .rotation(heading)
          .anchor(0.5f, 0.5f)
          .flat(true));
        acumuladoFlechas = 0;
      }
    }
    textoDistancia.setValue(String.format("%.2f km", distanciaTotal / 1000.0));
    flechasGuias.setValue(nuevasFlechas);
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