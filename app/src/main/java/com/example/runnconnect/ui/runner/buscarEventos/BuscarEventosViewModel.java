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
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BuscarEventosViewModel extends AndroidViewModel {

  private final ApiService apiService;

  private final MutableLiveData<List<EventoCardUI>> listaEventos = new MutableLiveData<>(new ArrayList<>());
  private final MutableLiveData<Integer> progressVisibility = new MutableLiveData<>(View.GONE);
  private final MutableLiveData<Integer> vacioVisibility = new MutableLiveData<>(View.GONE);
  private final MutableLiveData<Integer> errorVisibility = new MutableLiveData<>(View.GONE);
  private final MutableLiveData<String> errorText = new MutableLiveData<>("");

  public BuscarEventosViewModel(@NonNull Application application) {
    super(application);
    this.apiService = ApiClient.getApiService();
    cargarEventos();
  }

  public LiveData<List<EventoCardUI>> getListaEventos() {
    return listaEventos;
  }

  public LiveData<Integer> getProgressVisibility() {
    return progressVisibility;
  }

  public LiveData<Integer> getVacioVisibility() {
    return vacioVisibility;
  }

  public LiveData<Integer> getErrorVisibility() {
    return errorVisibility;
  }

  public LiveData<String> getErrorText() {
    return errorText;
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
          if (eventos == null || eventos.isEmpty()) {
            listaEventos.setValue(new ArrayList<>());
            vacioVisibility.setValue(View.VISIBLE);
          } else {
            List<EventoCardUI> itemsUI = new ArrayList<>();
            for (EventoResumenResponse ev : eventos) {
              itemsUI.add(mapearAEventoCardUI(ev));
            }
            listaEventos.setValue(itemsUI);
            vacioVisibility.setValue(View.GONE);
          }
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

  private EventoCardUI mapearAEventoCardUI(EventoResumenResponse ev) {
    int id = ev.getIdEvento();
    String nombre = ev.getNombre() != null ? ev.getNombre() : "Evento";
    String fecha = ev.getFechaHora() != null ? ev.getFechaHora().replace("T", " ") : "-";
    String lugar = ev.getLugar() != null ? ev.getLugar() : "-";
    String organizador = "Org: " + (ev.getNombreOrganizador() != null ? ev.getNombreOrganizador() : "-");

    List<String> nombresCategorias = new ArrayList<>();
    if (ev.getCategorias() != null) {
      for (CategoriaResponse cat : ev.getCategorias()) {
        if (cat.getNombre() != null) {
          nombresCategorias.add(cat.getNombre());
        }
      }
    }

    return new EventoCardUI(id, nombre, fecha, lugar, organizador, nombresCategorias);
  }
}
