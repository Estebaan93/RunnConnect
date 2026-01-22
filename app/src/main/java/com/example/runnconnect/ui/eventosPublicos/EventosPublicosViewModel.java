package com.example.runnconnect.ui.eventosPublicos;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.runnconnect.data.repositorio.EventoRepositorio;
import com.example.runnconnect.data.response.EventoResumenResponse;
import com.example.runnconnect.data.response.EventosPaginadosResponse;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EventosPublicosViewModel extends AndroidViewModel {

  private final EventoRepositorio repositorio;

  // Estados de UI
  private final MutableLiveData<List<EventoResumenResponse>> listaEventos = new MutableLiveData<>(new ArrayList<>());
  private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
  private final MutableLiveData<Boolean> isVacio = new MutableLiveData<>(false);

  // Eventos (Usamos MutableLiveData estándar)
  // Inicializamos sin valor para que sea null al principio
  private final MutableLiveData<String> mostrarToast = new MutableLiveData<>();
  private final MutableLiveData<Integer> navegarADetalle = new MutableLiveData<>();

  public EventosPublicosViewModel(@NonNull Application application) {
    super(application);
    repositorio = new EventoRepositorio(application);
  }

  // Getters
  public LiveData<List<EventoResumenResponse>> getListaEventos() { return listaEventos; }
  public LiveData<Boolean> getIsLoading() { return isLoading; }
  public LiveData<Boolean> getIsVacio() { return isVacio; }

  public LiveData<String> getMostrarToast() { return mostrarToast; }
  public LiveData<Integer> getNavegarADetalle() { return navegarADetalle; }

  // --- MÉTODOS DE RESET (Obligatorios con MutableLiveData estándar) ---
  public void resetToast() { mostrarToast.setValue(null); }
  public void resetNavegacion() { navegarADetalle.setValue(null); }

  // Acciones
  public void cargarEventos() {
    isLoading.setValue(true);

    repositorio.obtenerEventosPublicados(1, 50, new Callback<EventosPaginadosResponse>() {
      @Override
      public void onResponse(Call<EventosPaginadosResponse> call, Response<EventosPaginadosResponse> response) {
        isLoading.setValue(false);
        if (response.isSuccessful() && response.body() != null) {
          List<EventoResumenResponse> eventos = response.body().getEventos();
          listaEventos.setValue(eventos);
          isVacio.setValue(eventos == null || eventos.isEmpty());
        } else {
          mostrarToast.setValue("Error al cargar eventos: " + response.code());
          Log.d("ErrorEventoPublico", "ErrorObtener: "+ response.errorBody().toString());
        }
      }

      @Override
      public void onFailure(Call<EventosPaginadosResponse> call, Throwable t) {
        isLoading.setValue(false);
        mostrarToast.setValue("Error de conexión");
        isVacio.setValue(true);
        listaEventos.setValue(new ArrayList<>());
      }
    });
  }

  public void seleccionarEvento(int idEvento) {
    navegarADetalle.setValue(idEvento);
  }
}