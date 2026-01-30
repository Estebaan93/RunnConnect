package com.example.runnconnect.ui.organizador.buscarInscripciones;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.runnconnect.data.repositorio.InscripcionRepositorio;
import com.example.runnconnect.data.response.BusquedaInscripcionResponse;

import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BuscarIncripcionesViewModel extends AndroidViewModel {
  private final InscripcionRepositorio repositorio;
  // LiveData
  private final MutableLiveData<List<BusquedaInscripcionResponse>> resultados = new MutableLiveData<>();
  private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
  private final MutableLiveData<String> mensajeExito = new MutableLiveData<>();
  private final MutableLiveData<String> mensajeError = new MutableLiveData<>();

  public BuscarIncripcionesViewModel(@NonNull Application application) {
    super(application);
    repositorio= new InscripcionRepositorio(application);
  }

  public LiveData<List<BusquedaInscripcionResponse>> getResultados() { return resultados; }
  public LiveData<Boolean> getIsLoading() { return isLoading; }
  public LiveData<String> getMensajeExito() { return mensajeExito; }
  public LiveData<String> getMensajeError() { return mensajeError; }

  // --- BUSCAR ---
  public void buscar(String termino) {
    if (termino == null || termino.trim().isEmpty()) return;

    isLoading.setValue(true);
    // Usa el metodo que agregamos al repositorio en el paso anterior
    repositorio.buscarIncripcion(termino, new Callback<List<BusquedaInscripcionResponse>>() {
      @Override
      public void onResponse(Call<List<BusquedaInscripcionResponse>> call, Response<List<BusquedaInscripcionResponse>> response) {
        isLoading.setValue(false);
        if (response.isSuccessful() && response.body() != null) {
          resultados.setValue(response.body());
        } else {
          // Si viene vacío o error, limpiamos la lista
          resultados.setValue(new ArrayList<>());
        }
      }

      @Override
      public void onFailure(Call<List<BusquedaInscripcionResponse>> call, Throwable t) {
        isLoading.setValue(false);
        mensajeError.setValue("Error de conexión");
      }
    });
  }

  // --- DAR DE BAJA ---
  public void darDeBaja(int idInscripcion, String motivo, String terminoActual) {
    isLoading.setValue(true);
    // Usa el endpoint "BajaOrganizador" del repositorio
    repositorio.darDeBajaRunner(idInscripcion, motivo, new Callback<ResponseBody>() {
      @Override
      public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
        isLoading.setValue(false);
        if (response.isSuccessful()) {
          mensajeExito.setValue("Inscripción cancelada");
          // Recargamos la búsqueda para ver el cambio de estado
          buscar(terminoActual);
        } else {
          mensajeError.setValue("No se pudo cancelar: " + response.code());
        }
      }
      @Override
      public void onFailure(Call<ResponseBody> call, Throwable t) {
        isLoading.setValue(false);
        mensajeError.setValue("Error de conexión");
      }
    });
  }


}
