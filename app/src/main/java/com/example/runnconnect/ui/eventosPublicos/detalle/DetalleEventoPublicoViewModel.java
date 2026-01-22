package com.example.runnconnect.ui.eventosPublicos.detalle;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.runnconnect.data.repositorio.EventoRepositorio;
import com.example.runnconnect.data.response.EventoDetalleResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DetalleEventoPublicoViewModel extends AndroidViewModel {

  private final EventoRepositorio repositorio;

  // Estados de UI
  private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
  private final MutableLiveData<EventoDetalleResponse> evento = new MutableLiveData<>();

  // Eventos de un solo uso (Mensajes)
  private final MutableLiveData<String> mensajeError = new MutableLiveData<>();

  public DetalleEventoPublicoViewModel(@NonNull Application application) {
    super(application);
    repositorio = new EventoRepositorio(application);
  }

  // Getters
  public LiveData<Boolean> getIsLoading() { return isLoading; }
  public LiveData<EventoDetalleResponse> getEvento() { return evento; }
  public LiveData<String> getMensajeError() { return mensajeError; }

  // Reset para eventos manuales
  public void resetMensajeError() { mensajeError.setValue(null); }

  public void cargarDetalle(int idEvento) {
    isLoading.setValue(true);

    repositorio.obtenerDetalleEventoPublico(idEvento, new Callback<EventoDetalleResponse>() {
      @Override
      public void onResponse(Call<EventoDetalleResponse> call, Response<EventoDetalleResponse> response) {
        isLoading.setValue(false);
        if (response.isSuccessful() && response.body() != null) {
          evento.setValue(response.body());
        } else {
          mensajeError.setValue("No se pudo cargar la información del evento.");
        }
      }

      @Override
      public void onFailure(Call<EventoDetalleResponse> call, Throwable t) {
        isLoading.setValue(false);
        mensajeError.setValue("Error de conexión con el servidor.");
      }
    });
  }
}