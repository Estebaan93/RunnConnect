package com.example.runnconnect.ui.organizador.misEventos;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.runnconnect.data.repositorio.EventoRepositorio;
import com.example.runnconnect.data.request.CambiarEstadoRequest;
import com.example.runnconnect.data.response.EventoDetalleResponse;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DetalleEventoViewModel extends AndroidViewModel {
  private final EventoRepositorio repositorio;
  private final MutableLiveData<EventoDetalleResponse> evento = new MutableLiveData<>();
  private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
  private final MutableLiveData<String> errorMsg = new MutableLiveData<>();

  public DetalleEventoViewModel(@NonNull Application application) {
    super(application);
    repositorio = new EventoRepositorio(application);
  }

  public LiveData<EventoDetalleResponse> getEvento() { return evento; }
  public LiveData<Boolean> getIsLoading() { return isLoading; }
  public LiveData<String> getErrorMsg() { return errorMsg; }

  public void cargarDetalle(int idEvento) {
    isLoading.setValue(true);
    repositorio.obtenerDetalleEvento(idEvento, new Callback<EventoDetalleResponse>() {
      @Override
      public void onResponse(Call<EventoDetalleResponse> call, Response<EventoDetalleResponse> response) {
        isLoading.setValue(false);
        if (response.isSuccessful() && response.body() != null) {
          evento.setValue(response.body());
        } else {
          errorMsg.setValue("Error al cargar detalle");
        }
      }

      @Override
      public void onFailure(Call<EventoDetalleResponse> call, Throwable t) {
        isLoading.setValue(false);
        errorMsg.setValue("Error de conexión");
      }
    });
  }

  public void cambiarEstado(int idEvento, String nuevoEstado, String motivo) {
    isLoading.setValue(true); // Se activa el loading

    try {
      CambiarEstadoRequest request = new CambiarEstadoRequest(nuevoEstado, motivo);

      repositorio.cambiarEstado(idEvento, request, new Callback<ResponseBody>() {
        @Override
        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
          isLoading.setValue(false); // Apagamos loading

          if (response.isSuccessful()) {
            errorMsg.setValue("Estado actualizado correctamente");

            // Actualización optimista en local
            EventoDetalleResponse actual = evento.getValue();
            if (actual != null) {
              actual.setEstado(nuevoEstado);
              evento.setValue(actual);
            } else {
              cargarDetalle(idEvento);
            }
          } else {
            errorMsg.setValue("Error al actualizar: " + response.code());
          }
        }

        @Override
        public void onFailure(Call<ResponseBody> call, Throwable t) {
          isLoading.setValue(false); // Apagamos loading
          errorMsg.setValue("Fallo de conexión: " + t.getMessage());
        }
      });
    } catch (Exception e) {
      // Si falla la creación del request o el repositorio explota antes de llamar
      isLoading.setValue(false);
      errorMsg.setValue("Error interno: " + e.getMessage());
    }
  }

  public void limpiarMensaje() {
    errorMsg.setValue(null);
  }
}