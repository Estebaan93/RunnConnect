package com.example.runnconnect.ui.notificaciones;

import android.app.Application;
import android.graphics.Color;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.runnconnect.R;
import com.example.runnconnect.data.conexion.ApiClient;
import com.example.runnconnect.data.conexion.ApiService;
import com.example.runnconnect.data.preferencias.SessionManager;
import com.example.runnconnect.data.response.MisNotificacionesResponse;

import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NotificacionesViewModel extends AndroidViewModel {

  public static class NotificacionUI {
    public int idNotificacion;
    public int idEvento;
    public String estadoEvento;
    public String titulo;
    public String mensaje;
    public String tiempoTranscurrido;
    public String eventoTexto;
    public int eventoVisibility;
    public int destinoNavId;
    public int cardBackgroundColor;
    public int badgeNuevaVisibility;
  }

  private final ApiService apiService;
  private final SessionManager sessionManager;

  private final MutableLiveData<List<NotificacionUI>> listaNotificaciones = new MutableLiveData<>(new ArrayList<>());
  private final MutableLiveData<Integer> progressBarVisibility = new MutableLiveData<>(View.GONE);
  private final MutableLiveData<Integer> vacioVisibility = new MutableLiveData<>(View.GONE);
  private final MutableLiveData<String> vacioTexto = new MutableLiveData<>("No tienes notificaciones en este momento");
  private final MutableLiveData<Integer> errorVisibility = new MutableLiveData<>(View.GONE);
  private final MutableLiveData<String> errorTexto = new MutableLiveData<>("");

  public NotificacionesViewModel(@NonNull Application application) {
    super(application);
    this.apiService = ApiClient.getApiService();
    this.sessionManager = new SessionManager(application);
    cargarNotificaciones();
  }

  public LiveData<List<NotificacionUI>> getListaNotificaciones() {
    return listaNotificaciones;
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

  public void cargarNotificaciones() {
    String token = sessionManager.leerToken();
    if (token == null || token.trim().isEmpty()) {
      errorTexto.setValue("No hay sesión activa");
      errorVisibility.setValue(View.VISIBLE);
      return;
    }

    progressBarVisibility.setValue(View.VISIBLE);
    errorVisibility.setValue(View.GONE);
    vacioVisibility.setValue(View.GONE);

    apiService.obtenerMisNotificaciones("Bearer " + token).enqueue(new Callback<MisNotificacionesResponse>() {
      @Override
      public void onResponse(Call<MisNotificacionesResponse> call, Response<MisNotificacionesResponse> response) {
        progressBarVisibility.setValue(View.GONE);

        if (response.isSuccessful() && response.body() != null) {
          List<NotificacionUI> itemsUI = new ArrayList<>();
          List<MisNotificacionesResponse.NotificacionItemResponse> lista = response.body().getNotificaciones();
          boolean esOrganizador = "organizador".equalsIgnoreCase(sessionManager.getTipoUsuario());

          if (lista != null) {
            for (MisNotificacionesResponse.NotificacionItemResponse item : lista) {
              // El organizador no debe recibir notificaciones de resultados (corresponden al runner)
              if (esOrganizador && item.getTitulo() != null && item.getTitulo().toLowerCase().contains("resultado")) {
                continue;
              }

              NotificacionUI ui = new NotificacionUI();
              ui.idNotificacion = item.getIdNotificacion();
              ui.idEvento = item.getIdEvento();
              ui.estadoEvento = (item.getEstadoEvento() != null) ? item.getEstadoEvento() : "";
              ui.titulo = (item.getTitulo() != null) ? item.getTitulo() : "Notificación";
              ui.mensaje = (item.getMensaje() != null) ? item.getMensaje() : "";
              ui.tiempoTranscurrido = (item.getTiempoTranscurrido() != null) ? item.getTiempoTranscurrido() : "";
              ui.destinoNavId = esOrganizador ? R.id.nav_gestion_inscriptos : R.id.nav_detalle_evento_runner;

              if (item.getNombreEvento() != null && !item.getNombreEvento().trim().isEmpty()) {
                ui.eventoTexto = "Evento: " + item.getNombreEvento();
                ui.eventoVisibility = View.VISIBLE;
              } else {
                ui.eventoTexto = "";
                ui.eventoVisibility = View.GONE;
              }

              ui.cardBackgroundColor = item.isEsNueva() ? Color.parseColor("#EBF5FB") : Color.WHITE;
              ui.badgeNuevaVisibility = item.isEsNueva() ? View.VISIBLE : View.GONE;

              itemsUI.add(ui);
            }
          }

          listaNotificaciones.setValue(itemsUI);

          if (itemsUI.isEmpty()) {
            vacioVisibility.setValue(View.VISIBLE);
            vacioTexto.setValue("No tienes notificaciones en este momento");
          } else {
            vacioVisibility.setValue(View.GONE);
          }

          marcarComoLeidas(token);
        } else {
          errorTexto.setValue("Error al cargar notificaciones (" + response.code() + ")");
          errorVisibility.setValue(View.VISIBLE);
        }
      }

      @Override
      public void onFailure(Call<MisNotificacionesResponse> call, Throwable t) {
        progressBarVisibility.setValue(View.GONE);
        errorTexto.setValue("Error de conexión. Intente nuevamente.");
        errorVisibility.setValue(View.VISIBLE);
      }
    });
  }

  private void marcarComoLeidas(String token) {
    apiService.marcarNotificacionesComoLeidas("Bearer " + token).enqueue(new Callback<ResponseBody>() {
      @Override
      public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {}

      @Override
      public void onFailure(Call<ResponseBody> call, Throwable t) {}
    });
  }
}
