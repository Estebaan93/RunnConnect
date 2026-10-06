package com.example.runnconnect.ui.runner.buscarEventos;

import android.app.Application;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.runnconnect.data.conexion.ApiClient;
import com.example.runnconnect.data.conexion.ApiService;
import com.example.runnconnect.data.response.CategoriaResponse;
import com.example.runnconnect.data.response.EventoResumenResponse;
import com.example.runnconnect.data.response.EventosPaginadosResponse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BuscarEventosViewModel extends AndroidViewModel {

  private final ApiService apiService;

  private final List<EventoResumenResponse> listaEventosCompleta = new ArrayList<>();
  private String queryActual = "";
  private String filtroGeneroActual = "TODOS";
  private String filtroDistanciaActual = "TODAS";

  // Listas de opciones para los Spinners (proporcionadas por el ViewModel)
  private final MutableLiveData<List<String>> listaOpcionesGenero = new MutableLiveData<>(
      Arrays.asList("Todos", "Fem", "Mas", "X")
  );
  private final MutableLiveData<List<String>> listaOpcionesDistancia = new MutableLiveData<>(
      Arrays.asList("Todas", "10K", "20K", "+20K")
  );

  private final MutableLiveData<List<EventoResumenResponse>> listaEventos = new MutableLiveData<>(new ArrayList<>());
  private final MutableLiveData<Integer> progressVisibility = new MutableLiveData<>(View.GONE);
  private final MutableLiveData<Integer> vacioVisibility = new MutableLiveData<>(View.GONE);
  private final MutableLiveData<String> vacioText = new MutableLiveData<>("No hay eventos disponibles en este momento");
  private final MutableLiveData<Integer> errorVisibility = new MutableLiveData<>(View.GONE);
  private final MutableLiveData<String> errorText = new MutableLiveData<>("");

  public BuscarEventosViewModel(@NonNull Application application) {
    super(application);
    this.apiService = ApiClient.getApiService();
    cargarEventos();
  }

  public LiveData<List<String>> getListaOpcionesGenero() {
    return listaOpcionesGenero;
  }

  public LiveData<List<String>> getListaOpcionesDistancia() {
    return listaOpcionesDistancia;
  }

  public LiveData<List<EventoResumenResponse>> getListaEventos() {
    return listaEventos;
  }

  public LiveData<Integer> getProgressVisibility() {
    return progressVisibility;
  }

  public LiveData<Integer> getVacioVisibility() {
    return vacioVisibility;
  }

  public LiveData<String> getVacioText() {
    return vacioText;
  }

  public LiveData<Integer> getErrorVisibility() {
    return errorVisibility;
  }

  public LiveData<String> getErrorText() {
    return errorText;
  }

  public void onBusquedaTextoCambiado(String query) {
    this.queryActual = query != null ? query.trim().toLowerCase() : "";
    aplicarFiltros();
  }

  public void setFiltroGenero(String generoTexto) {
    if (generoTexto == null || generoTexto.equalsIgnoreCase("Todos")) {
      this.filtroGeneroActual = "TODOS";
    } else if (generoTexto.equalsIgnoreCase("Fem") || generoTexto.equalsIgnoreCase("Femenino")) {
      this.filtroGeneroActual = "FEM";
    } else if (generoTexto.equalsIgnoreCase("Mas") || generoTexto.equalsIgnoreCase("Masculino")) {
      this.filtroGeneroActual = "MASC";
    } else if (generoTexto.equalsIgnoreCase("X") || generoTexto.equalsIgnoreCase("Mixto")) {
      this.filtroGeneroActual = "MIXTO";
    } else {
      this.filtroGeneroActual = "TODOS";
    }
    aplicarFiltros();
  }

  public void setFiltroDistancia(String distanciaTexto) {
    if (distanciaTexto == null || distanciaTexto.equalsIgnoreCase("Todas")) {
      this.filtroDistanciaActual = "TODAS";
    } else if (distanciaTexto.contains("+20") || distanciaTexto.contains("20+")) {
      this.filtroDistanciaActual = "MAS_20K";
    } else if (distanciaTexto.contains("10")) {
      this.filtroDistanciaActual = "HASTA_10K";
    } else if (distanciaTexto.contains("20")) {
      this.filtroDistanciaActual = "HASTA_20K";
    } else {
      this.filtroDistanciaActual = "TODAS";
    }
    aplicarFiltros();
  }

  public void cargarEventos() {
    progressVisibility.setValue(View.VISIBLE);
    errorVisibility.setValue(View.GONE);
    vacioVisibility.setValue(View.GONE);

    apiService.obtenerEventosPublicados(1, 50).enqueue(new Callback<EventosPaginadosResponse>() {
      @Override
      public void onResponse(Call<EventosPaginadosResponse> call, Response<EventosPaginadosResponse> response) {
        progressVisibility.setValue(View.GONE);

        if (response.isSuccessful() && response.body() != null) {
          List<EventoResumenResponse> eventos = response.body().getEventos();
          listaEventosCompleta.clear();
          if (eventos != null) {
            listaEventosCompleta.addAll(eventos);
          }
          aplicarFiltros();
        } else {
          errorText.setValue("Error al cargar eventos (" + response.code() + ")");
          errorVisibility.setValue(View.VISIBLE);
        }
      }

      @Override
      public void onFailure(Call<EventosPaginadosResponse> call, Throwable t) {
        progressVisibility.setValue(View.GONE);
        errorText.setValue("Error de conexión. Intente nuevamente.");
        errorVisibility.setValue(View.VISIBLE);
      }
    });
  }

  private void aplicarFiltros() {
    List<EventoResumenResponse> filtrados = new ArrayList<>();

    for (EventoResumenResponse evento : listaEventosCompleta) {
      if (cumpleBusquedaTexto(evento) && cumpleFiltroGenero(evento) && cumpleFiltroDistancia(evento)) {
        filtrados.add(evento);
      }
    }

    listaEventos.setValue(filtrados);

    if (filtrados.isEmpty()) {
      vacioVisibility.setValue(View.VISIBLE);
      if (listaEventosCompleta.isEmpty()) {
        vacioText.setValue("No hay eventos disponibles en este momento");
      } else {
        vacioText.setValue("No se encontraron eventos con los filtros seleccionados");
      }
    } else {
      vacioVisibility.setValue(View.GONE);
    }
  }

  private boolean cumpleBusquedaTexto(EventoResumenResponse evento) {
    if (queryActual.isEmpty()) return true;

    boolean coincideNombre = evento.getNombre() != null &&
        evento.getNombre().toLowerCase().contains(queryActual);

    boolean coincideOrg = evento.getNombreOrganizador() != null &&
        evento.getNombreOrganizador().toLowerCase().contains(queryActual);

    return coincideNombre || coincideOrg;
  }

  private boolean cumpleFiltroGenero(EventoResumenResponse evento) {
    if ("TODOS".equals(filtroGeneroActual)) return true;

    List<CategoriaResponse> categorias = evento.getCategorias();
    for (CategoriaResponse cat : categorias) {
      String g = cat.getGenero() != null ? cat.getGenero().trim() : "X";
      if ("FEM".equals(filtroGeneroActual)) {
        if ("F".equalsIgnoreCase(g) || "X".equalsIgnoreCase(g)) return true;
      } else if ("MASC".equals(filtroGeneroActual)) {
        if ("M".equalsIgnoreCase(g) || "X".equalsIgnoreCase(g)) return true;
      } else if ("MIXTO".equals(filtroGeneroActual)) {
        if ("X".equalsIgnoreCase(g)) return true;
      }
    }
    return false;
  }

  private boolean cumpleFiltroDistancia(EventoResumenResponse evento) {
    if ("TODAS".equals(filtroDistanciaActual)) return true;

    List<CategoriaResponse> categorias = evento.getCategorias();
    for (CategoriaResponse cat : categorias) {
      int distKm = extraerDistanciaKm(cat.getNombre());
      if ("HASTA_10K".equals(filtroDistanciaActual)) {
        if (distKm > 0 && distKm <= 10) return true;
      } else if ("HASTA_20K".equals(filtroDistanciaActual)) {
        if (distKm > 0 && distKm <= 20) return true;
      } else if ("MAS_20K".equals(filtroDistanciaActual)) {
        if (distKm > 20) return true;
      }
    }
    return false;
  }

  private int extraerDistanciaKm(String nombreCat) {
    if (nombreCat == null || nombreCat.trim().isEmpty()) return 0;
    Matcher m = Pattern.compile("(\\d+)\\s*[kK]?").matcher(nombreCat);
    if (m.find()) {
      try {
        return Integer.parseInt(m.group(1));
      } catch (NumberFormatException ignored) {}
    }
    return 0;
  }
}
