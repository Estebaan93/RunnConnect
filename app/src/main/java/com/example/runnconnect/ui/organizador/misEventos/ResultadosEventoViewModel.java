package com.example.runnconnect.ui.organizador.misEventos;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.runnconnect.data.conexion.ApiClient;
import com.example.runnconnect.data.conexion.ApiService;
import com.example.runnconnect.data.preferencias.SessionManager;
import com.example.runnconnect.data.response.ResultadosEventoResponse;

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

  private final ApiService apiService;
  private final SessionManager sessionManager;
  private final MutableLiveData<List<ResultadoUI>> listaResultados = new MutableLiveData<>();
  private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
  private final MutableLiveData<Integer> uiVisibilidadSinResultados = new MutableLiveData<>(android.view.View.GONE);
  private final MutableLiveData<Integer> uiVisibilidadRecycler = new MutableLiveData<>(android.view.View.GONE);
  private final MutableLiveData<Integer> uiVisibilidadError = new MutableLiveData<>(android.view.View.GONE);
  private final MutableLiveData<String> errorTexto = new MutableLiveData<>("");

  public ResultadosEventoViewModel(@NonNull Application application) {
    super(application);
    apiService = ApiClient.getApiService();
    sessionManager = new SessionManager(application);
  }

  public LiveData<List<ResultadoUI>> getListaResultados() { return listaResultados; }
  public LiveData<Boolean> getIsLoading() { return isLoading; }
  public LiveData<Integer> getUiVisibilidadSinResultados() { return uiVisibilidadSinResultados; }
  public LiveData<Integer> getUiVisibilidadRecycler() { return uiVisibilidadRecycler; }
  public LiveData<Integer> getUiVisibilidadError() { return uiVisibilidadError; }
  public LiveData<String> getErrorTexto() { return errorTexto; }

  public void cargarResultados(int idEvento) {
    if (idEvento <= 0) {
      errorTexto.setValue("ID de evento no válido.");
      uiVisibilidadError.setValue(android.view.View.VISIBLE);
      uiVisibilidadRecycler.setValue(android.view.View.GONE);
      uiVisibilidadSinResultados.setValue(android.view.View.GONE);
      return;
    }

    isLoading.setValue(true);
    uiVisibilidadError.setValue(android.view.View.GONE);

    apiService.obtenerResultadosEvento(idEvento).enqueue(new Callback<ResultadosEventoResponse>() {
      @Override
      public void onResponse(Call<ResultadosEventoResponse> call, Response<ResultadosEventoResponse> response) {
        isLoading.setValue(false);
        if (response.isSuccessful() && response.body() != null) {
          List<ResultadoUI> uiList = mapearAResultadoUI(response.body().getResultados());
          listaResultados.setValue(uiList);

          if (uiList.isEmpty()) {
            uiVisibilidadSinResultados.setValue(android.view.View.VISIBLE);
            uiVisibilidadRecycler.setValue(android.view.View.GONE);
          } else {
            uiVisibilidadSinResultados.setValue(android.view.View.GONE);
            uiVisibilidadRecycler.setValue(android.view.View.VISIBLE);
          }
        } else {
          errorTexto.setValue("Error al cargar resultados: " + response.code());
          uiVisibilidadError.setValue(android.view.View.VISIBLE);
          uiVisibilidadRecycler.setValue(android.view.View.GONE);
          uiVisibilidadSinResultados.setValue(android.view.View.GONE);
        }
      }

      @Override
      public void onFailure(Call<ResultadosEventoResponse> call, Throwable t) {
        isLoading.setValue(false);
        errorTexto.setValue("Error de conexión: " + t.getMessage());
        uiVisibilidadError.setValue(android.view.View.VISIBLE);
        uiVisibilidadRecycler.setValue(android.view.View.GONE);
        uiVisibilidadSinResultados.setValue(android.view.View.GONE);
      }
    });
  }

  private List<ResultadoUI> mapearAResultadoUI(List<ResultadosEventoResponse.ResultadoEventoItem> items) {
    List<ResultadoUI> uiList = new java.util.ArrayList<>();
    if (items != null) {
      for (ResultadosEventoResponse.ResultadoEventoItem item : items) {
        String pos = String.valueOf(item.getPosicionGeneral() != null ? item.getPosicionGeneral() : "-");
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
