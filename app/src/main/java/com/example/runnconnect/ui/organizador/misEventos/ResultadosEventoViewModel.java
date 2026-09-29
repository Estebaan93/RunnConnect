package com.example.runnconnect.ui.organizador.misEventos;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.runnconnect.data.conexion.ApiClient;
import com.example.runnconnect.data.conexion.ApiService;
import com.example.runnconnect.data.preferencias.SessionManager;
import com.example.runnconnect.data.response.CategoriaResponse;
import com.example.runnconnect.data.response.EventoDetalleResponse;
import com.example.runnconnect.data.response.InscriptoEventoResponse;
import com.example.runnconnect.data.response.ResultadosEventoResponse;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ResultadosEventoViewModel extends AndroidViewModel {

  public static class ResultadoUI {
    public final String posicion;
    public final String nombreRunner;
    public final String categoriaYGenero;
    public final String tiempoOficial;

    public ResultadoUI(String posicion, String nombreRunner, String categoriaYGenero, String tiempoOficial) {
      this.posicion = posicion;
      this.nombreRunner = nombreRunner;
      this.categoriaYGenero = categoriaYGenero;
      this.tiempoOficial = tiempoOficial;
    }
  }

  public static class FichaTecnicaUI {
    public final String nombreCompleto;
    public final String dniSexoEdad;
    public final String localidad;
    public final String categoriaTalle;
    public final String email;
    public final String telefono;
    public final String contactoEmergencia;
    public final String telEmergencia;

    public FichaTecnicaUI(String nombreCompleto, String dniSexoEdad, String localidad,
                          String categoriaTalle, String email, String telefono,
                          String contactoEmergencia, String telEmergencia) {
      this.nombreCompleto = nombreCompleto;
      this.dniSexoEdad = dniSexoEdad;
      this.localidad = localidad;
      this.categoriaTalle = categoriaTalle;
      this.email = email;
      this.telefono = telefono;
      this.contactoEmergencia = contactoEmergencia;
      this.telEmergencia = telEmergencia;
    }
  }

  private final ApiService apiService;
  private final SessionManager sessionManager;

  private final List<CategoriaResponse> listaCategorias = new ArrayList<>();
  private final List<ResultadosEventoResponse.ResultadoEventoItem> listaActualItems = new ArrayList<>();
  private int idCategoriaActual = -1;

  private final MutableLiveData<List<String>> uiCategoriasNombres = new MutableLiveData<>(new ArrayList<>());
  private final MutableLiveData<Integer> uiVisibilidadSpinner = new MutableLiveData<>(android.view.View.GONE);
  private final MutableLiveData<List<ResultadoUI>> listaResultados = new MutableLiveData<>();
  private final MutableLiveData<Integer> uiVisibilidadLoading = new MutableLiveData<>(android.view.View.GONE);
  private final MutableLiveData<Integer> uiVisibilidadSinResultados = new MutableLiveData<>(android.view.View.GONE);
  private final MutableLiveData<Integer> uiVisibilidadRecycler = new MutableLiveData<>(android.view.View.GONE);
  private final MutableLiveData<Integer> uiVisibilidadError = new MutableLiveData<>(android.view.View.GONE);
  private final MutableLiveData<String> errorTexto = new MutableLiveData<>("");
  private final MutableLiveData<FichaTecnicaUI> uiFichaTecnica = new MutableLiveData<>(null);

  public ResultadosEventoViewModel(@NonNull Application application) {
    super(application);
    apiService = ApiClient.getApiService();
    sessionManager = new SessionManager(application);
  }

  public LiveData<List<String>> getUiCategoriasNombres() { return uiCategoriasNombres; }
  public LiveData<Integer> getUiVisibilidadSpinner() { return uiVisibilidadSpinner; }
  public LiveData<List<ResultadoUI>> getListaResultados() { return listaResultados; }
  public LiveData<Integer> getUiVisibilidadLoading() { return uiVisibilidadLoading; }
  public LiveData<Integer> getUiVisibilidadSinResultados() { return uiVisibilidadSinResultados; }
  public LiveData<Integer> getUiVisibilidadRecycler() { return uiVisibilidadRecycler; }
  public LiveData<Integer> getUiVisibilidadError() { return uiVisibilidadError; }
  public LiveData<String> getErrorTexto() { return errorTexto; }
  public LiveData<FichaTecnicaUI> getUiFichaTecnica() { return uiFichaTecnica; }

  public void limpiarFichaTecnica() {
    uiFichaTecnica.setValue(null);
  }

  public void cargarResultados(int idEvento) {
    if (idEvento <= 0) {
      errorTexto.setValue("ID de evento no válido.");
      uiVisibilidadError.setValue(android.view.View.VISIBLE);
      uiVisibilidadRecycler.setValue(android.view.View.GONE);
      uiVisibilidadSinResultados.setValue(android.view.View.GONE);
      uiVisibilidadSpinner.setValue(android.view.View.GONE);
      return;
    }

    uiVisibilidadLoading.setValue(android.view.View.VISIBLE);
    uiVisibilidadError.setValue(android.view.View.GONE);

    String token = sessionManager.leerToken();
    Call<EventoDetalleResponse> call = (token != null)
        ? apiService.obtenerEventoPorId("Bearer " + token, idEvento)
        : apiService.obtenerEventoPorIdPublico(idEvento);

    call.enqueue(new Callback<EventoDetalleResponse>() {
      @Override
      public void onResponse(Call<EventoDetalleResponse> call, Response<EventoDetalleResponse> response) {
        if (response.isSuccessful() && response.body() != null) {
          EventoDetalleResponse evento = response.body();
          listaCategorias.clear();
          List<String> nombres = new ArrayList<>();

          if (evento.getCategorias() != null && !evento.getCategorias().isEmpty()) {
            listaCategorias.addAll(evento.getCategorias());
            for (CategoriaResponse cat : listaCategorias) {
              String nombre = cat.getNombre() != null ? cat.getNombre() : "Categoría";
              if (cat.getGenero() != null && !cat.getGenero().isEmpty()) {
                nombre += " (" + cat.getGenero() + ")";
              }
              nombres.add(nombre);
            }
            uiCategoriasNombres.setValue(nombres);
            uiVisibilidadSpinner.setValue(android.view.View.VISIBLE);

            // Cargar automáticamente los resultados de la primera categoría
            cargarResultadosPorCategoriaId(listaCategorias.get(0).getIdCategoria());
          } else {
            uiVisibilidadLoading.setValue(android.view.View.GONE);
            uiCategoriasNombres.setValue(nombres);
            uiVisibilidadSpinner.setValue(android.view.View.GONE);
            uiVisibilidadSinResultados.setValue(android.view.View.VISIBLE);
            uiVisibilidadRecycler.setValue(android.view.View.GONE);
          }
        } else {
          uiVisibilidadLoading.setValue(android.view.View.GONE);
          errorTexto.setValue("Error al cargar categorías del evento: " + response.code());
          uiVisibilidadError.setValue(android.view.View.VISIBLE);
          uiVisibilidadRecycler.setValue(android.view.View.GONE);
          uiVisibilidadSinResultados.setValue(android.view.View.GONE);
          uiVisibilidadSpinner.setValue(android.view.View.GONE);
        }
      }

      @Override
      public void onFailure(Call<EventoDetalleResponse> call, Throwable t) {
        uiVisibilidadLoading.setValue(android.view.View.GONE);
        errorTexto.setValue("Error de conexión: " + t.getMessage());
        uiVisibilidadError.setValue(android.view.View.VISIBLE);
        uiVisibilidadRecycler.setValue(android.view.View.GONE);
        uiVisibilidadSinResultados.setValue(android.view.View.GONE);
        uiVisibilidadSpinner.setValue(android.view.View.GONE);
      }
    });
  }

  public void seleccionarCategoriaPorIndice(int index) {
    if (index >= 0 && index < listaCategorias.size()) {
      int idCat = listaCategorias.get(index).getIdCategoria();
      if (idCat != idCategoriaActual) {
        cargarResultadosPorCategoriaId(idCat);
      }
    }
  }

  private void cargarResultadosPorCategoriaId(int idCategoria) {
    this.idCategoriaActual = idCategoria;
    uiVisibilidadLoading.setValue(android.view.View.VISIBLE);
    uiVisibilidadError.setValue(android.view.View.GONE);

    apiService.obtenerResultadosCategoria(idCategoria).enqueue(new Callback<List<ResultadosEventoResponse.ResultadoEventoItem>>() {
      @Override
      public void onResponse(Call<List<ResultadosEventoResponse.ResultadoEventoItem>> call,
                             Response<List<ResultadosEventoResponse.ResultadoEventoItem>> response) {
        uiVisibilidadLoading.setValue(android.view.View.GONE);
        if (response.isSuccessful() && response.body() != null) {
          listaActualItems.clear();
          listaActualItems.addAll(response.body());

          List<ResultadoUI> uiList = mapearAResultadoUI(response.body());
          listaResultados.setValue(uiList);

          if (uiList.isEmpty()) {
            uiVisibilidadSinResultados.setValue(android.view.View.VISIBLE);
            uiVisibilidadRecycler.setValue(android.view.View.GONE);
          } else {
            uiVisibilidadSinResultados.setValue(android.view.View.GONE);
            uiVisibilidadRecycler.setValue(android.view.View.VISIBLE);
          }
        } else {
          listaActualItems.clear();
          errorTexto.setValue("Error al cargar resultados de categoría: " + response.code());
          uiVisibilidadError.setValue(android.view.View.VISIBLE);
          uiVisibilidadRecycler.setValue(android.view.View.GONE);
          uiVisibilidadSinResultados.setValue(android.view.View.GONE);
        }
      }

      @Override
      public void onFailure(Call<List<ResultadosEventoResponse.ResultadoEventoItem>> call, Throwable t) {
        listaActualItems.clear();
        uiVisibilidadLoading.setValue(android.view.View.GONE);
        errorTexto.setValue("Error de conexión: " + t.getMessage());
        uiVisibilidadError.setValue(android.view.View.VISIBLE);
        uiVisibilidadRecycler.setValue(android.view.View.GONE);
        uiVisibilidadSinResultados.setValue(android.view.View.GONE);
      }
    });
  }

  public void solicitarFichaTecnica(int posicion) {
    if (posicion < 0 || posicion >= listaActualItems.size()) return;
    int idInscripcion = listaActualItems.get(posicion).getIdInscripcion();
    cargarFichaRunner(idInscripcion);
  }

  private void cargarFichaRunner(int idInscripcion) {
    String token = sessionManager.leerToken();
    if (token == null) {
      errorTexto.setValue("No hay sesión activa.");
      uiVisibilidadError.setValue(android.view.View.VISIBLE);
      return;
    }

    uiVisibilidadLoading.setValue(android.view.View.VISIBLE);
    uiVisibilidadError.setValue(android.view.View.GONE);

    apiService.obtenerFichaInscripcion("Bearer " + token, idInscripcion).enqueue(new Callback<InscriptoEventoResponse>() {
      @Override
      public void onResponse(Call<InscriptoEventoResponse> call, Response<InscriptoEventoResponse> response) {
        uiVisibilidadLoading.setValue(android.view.View.GONE);
        if (response.isSuccessful() && response.body() != null) {
          InscriptoEventoResponse data = response.body();
          InscriptoEventoResponse.RunnerInscriptoInfo r = data.getRunner();

          String nombre = (r != null && r.getNombreCompleto() != null) ? r.getNombreCompleto().trim() : "";
          String dni = (r != null && r.getDni() != null) ? r.getDni() : "-";
          String sexo = (r != null && r.getGenero() != null) ? r.getGenero() : "-";
          String edad = (r != null && r.getEdad() != null && r.getEdad() > 0) ? " | " + r.getEdad() + " Años" : "";
          String dniSexoEdad = "DNI: " + dni + " | " + sexo + edad;

          String localidad = (r != null && r.getLocalidad() != null) ? r.getLocalidad() : "No especificada";
          String categoria = data.getNombreCategoria() != null ? data.getNombreCategoria() : "-";
          String talle = data.getTalleRemera() != null ? data.getTalleRemera() : "-";
          String categoriaTalle = "Categoría: " + categoria + " | Talle: " + talle;

          String email = (r != null && r.getEmail() != null) ? r.getEmail() : "-";
          String telefono = (r != null && r.getTelefono() != null) ? r.getTelefono() : "-";
          String contactoEmergencia = (r != null && r.getContactoEmergenciaFormateado() != null)
              ? r.getContactoEmergenciaFormateado() : "Contacto: No especificado";
          String telEmergencia = (r != null && r.getTelEmergenciaFormateado() != null)
              ? r.getTelEmergenciaFormateado() : "Tel: No especificado";

          uiFichaTecnica.setValue(new FichaTecnicaUI(nombre, dniSexoEdad, localidad, categoriaTalle, email, telefono, contactoEmergencia, telEmergencia));
        } else {
          errorTexto.setValue("Error al obtener ficha del corredor: " + response.code());
          uiVisibilidadError.setValue(android.view.View.VISIBLE);
        }
      }

      @Override
      public void onFailure(Call<InscriptoEventoResponse> call, Throwable t) {
        uiVisibilidadLoading.setValue(android.view.View.GONE);
        errorTexto.setValue("Error de conexión al obtener ficha: " + t.getMessage());
        uiVisibilidadError.setValue(android.view.View.VISIBLE);
      }
    });
  }

  private List<ResultadoUI> mapearAResultadoUI(List<ResultadosEventoResponse.ResultadoEventoItem> items) {
    List<ResultadoUI> uiList = new ArrayList<>();
    if (items != null) {
      for (ResultadosEventoResponse.ResultadoEventoItem item : items) {
        String pos = String.valueOf(item.getPosicionCategoria() != null ? item.getPosicionCategoria()
            : (item.getPosicionGeneral() != null ? item.getPosicionGeneral() : "-"));
        String nombre = item.getNombreRunner() != null ? item.getNombreRunner() : "";
        String cat = item.getNombreCategoria() != null ? item.getNombreCategoria() : "";
        if (item.getGenero() != null && !item.getGenero().isEmpty()) {
          cat += " (" + item.getGenero() + ")";
        }
        String tiempo = item.getTiempoOficial() != null ? item.getTiempoOficial() : "";
        uiList.add(new ResultadoUI(pos, nombre, cat, tiempo));
      }
    }
    return uiList;
  }
}
