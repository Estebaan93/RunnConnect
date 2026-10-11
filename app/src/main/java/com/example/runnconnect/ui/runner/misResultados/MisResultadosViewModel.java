package com.example.runnconnect.ui.runner.misResultados;

import android.app.Application;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.runnconnect.data.conexion.ApiClient;
import com.example.runnconnect.data.conexion.ApiService;
import com.example.runnconnect.data.preferencias.SessionManager;
import com.example.runnconnect.data.response.MisResultadosResponse;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MisResultadosViewModel extends AndroidViewModel {

  public static class ResultadoItemUI {
    public int idResultado;
    public int idEvento;
    public String nombreEvento;
    public String fechaLugar;
    public String categoria;
    public String tiempoOficial;
    public String posicionTexto;
  }

  public static class EstadisticasUI {
    public String carrerasCount = "0";
    public String mejorPuesto = "-";
  }

  private final ApiService apiService;
  private final SessionManager sessionManager;

  private final MutableLiveData<List<ResultadoItemUI>> listaResultados = new MutableLiveData<>(new ArrayList<>());
  private final MutableLiveData<EstadisticasUI> estadisticas = new MutableLiveData<>(new EstadisticasUI());
  private final MutableLiveData<Integer> progressBarVisibility = new MutableLiveData<>(View.GONE);
  private final MutableLiveData<Integer> vacioVisibility = new MutableLiveData<>(View.GONE);
  private final MutableLiveData<String> vacioTexto = new MutableLiveData<>("Aún no tienes resultados registrados");
  private final MutableLiveData<Integer> errorVisibility = new MutableLiveData<>(View.GONE);
  private final MutableLiveData<String> errorTexto = new MutableLiveData<>("");

  public MisResultadosViewModel(@NonNull Application application) {
    super(application);
    this.apiService = ApiClient.getApiService();
    this.sessionManager = new SessionManager(application);
    cargarMisResultados();
  }

  public LiveData<List<ResultadoItemUI>> getListaResultados() {
    return listaResultados;
  }

  public LiveData<EstadisticasUI> getEstadisticas() {
    return estadisticas;
  }

  public LiveData<Integer> getProgressBarVisibility() {
    return progressBarVisibility;
  }

  public LiveData<Integer> getVacioVisibility() {
    return vacioVisibility;
  }

  public LiveData<String> getVacioTexto() {
    return vacioTexto;
  }

  public LiveData<Integer> getErrorVisibility() {
    return errorVisibility;
  }

  public LiveData<String> getErrorTexto() {
    return errorTexto;
  }

  public void cargarMisResultados() {
    String token = sessionManager.leerToken();
    if (token == null || token.trim().isEmpty()) {
      errorTexto.setValue("No hay sesión activa");
      errorVisibility.setValue(View.VISIBLE);
      return;
    }

    progressBarVisibility.setValue(View.VISIBLE);
    errorVisibility.setValue(View.GONE);
    vacioVisibility.setValue(View.GONE);

    apiService.obtenerMisResultados("Bearer " + token).enqueue(new Callback<MisResultadosResponse>() {
      @Override
      public void onResponse(Call<MisResultadosResponse> call, Response<MisResultadosResponse> response) {
        progressBarVisibility.setValue(View.GONE);

        if (response.isSuccessful() && response.body() != null) {
          MisResultadosResponse data = response.body();

          // Mapeo de Estadísticas
          EstadisticasUI statsUI = new EstadisticasUI();
          if (data.getEstadisticas() != null) {
            statsUI.carrerasCount = String.valueOf(data.getEstadisticas().getCarrerasCompletadas());
            if (data.getEstadisticas().getPosicionCategoria() != null) {
              statsUI.mejorPuesto = "#" + data.getEstadisticas().getPosicionCategoria();
            } else {
              statsUI.mejorPuesto = "-";
            }
          } else {
            statsUI.carrerasCount = String.valueOf(data.getTotalCarreras());
            statsUI.mejorPuesto = "-";
          }
          estadisticas.setValue(statsUI);

          // Mapeo de Lista de Resultados
          List<ResultadoItemUI> itemsUI = new ArrayList<>();
          if (data.getResultados() != null) {
            for (MisResultadosResponse.MiResultadoItem item : data.getResultados()) {
              ResultadoItemUI ui = new ResultadoItemUI();
              ui.idResultado = item.getIdResultado();
              ui.idEvento = item.getIdEvento();
              ui.nombreEvento = item.getNombreEvento() != null ? item.getNombreEvento() : "Sin nombre";

              String fecha = (item.getFechaEvento() != null) ? item.getFechaEvento().replace("T", " ") : "";
              String lugar = (item.getLugarEvento() != null) ? item.getLugarEvento() : "";
              if (!fecha.isEmpty() && !lugar.isEmpty()) {
                ui.fechaLugar = fecha + " • " + lugar;
              } else if (!fecha.isEmpty()) {
                ui.fechaLugar = fecha;
              } else {
                ui.fechaLugar = lugar;
              }

              ui.categoria = "Categoría: " + (item.getNombreCategoria() != null ? item.getNombreCategoria() : "-");
              ui.tiempoOficial = item.getTiempoOficial() != null ? item.getTiempoOficial() : "--:--:--";

              if (item.getPosicionCategoria() != null) {
                if (item.getTotalParticipantesCategoria() != null && item.getTotalParticipantesCategoria() > 0) {
                  ui.posicionTexto = "Puesto #" + item.getPosicionCategoria() + " de " + item.getTotalParticipantesCategoria();
                } else {
                  ui.posicionTexto = "Puesto #" + item.getPosicionCategoria();
                }
              } else {
                ui.posicionTexto = "Sin posición registrada";
              }

              itemsUI.add(ui);
            }
          }

          listaResultados.setValue(itemsUI);

          if (itemsUI.isEmpty()) {
            vacioVisibility.setValue(View.VISIBLE);
            vacioTexto.setValue("Aún no tienes resultados registrados");
          } else {
            vacioVisibility.setValue(View.GONE);
          }
        } else {
          errorTexto.setValue("Error al cargar resultados (" + response.code() + ")");
          errorVisibility.setValue(View.VISIBLE);
        }
      }

      @Override
      public void onFailure(Call<MisResultadosResponse> call, Throwable t) {
        progressBarVisibility.setValue(View.GONE);
        errorTexto.setValue("Error de conexión. Intente nuevamente.");
        errorVisibility.setValue(View.VISIBLE);
      }
    });
  }
}
