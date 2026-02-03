package com.example.runnconnect.ui.organizador.misEventos;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.runnconnect.data.repositorio.ResultadoRepositorio;
import com.example.runnconnect.data.response.ResultadosEventoResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ResultadosEventoViewModel extends AndroidViewModel {

  private final ResultadoRepositorio repositorio;
  private final MutableLiveData<List<ResultadosEventoResponse.ResultadoEventoItem>> listaResultados = new MutableLiveData<>();
  private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
  private final MutableLiveData<String> mensajeError = new MutableLiveData<>();

  public ResultadosEventoViewModel(@NonNull Application application) {
    super(application);
    repositorio = new ResultadoRepositorio(application);
  }

  public LiveData<List<ResultadosEventoResponse.ResultadoEventoItem>> getListaResultados() { return listaResultados; }
  public LiveData<Boolean> getIsLoading() { return isLoading; }
  public LiveData<String> getMensajeError() { return mensajeError; }

  public void cargarResultados(int idEvento) {
    isLoading.setValue(true);
    repositorio.obtenerResultadosEvento(idEvento, new Callback<ResultadosEventoResponse>() {
      @Override
      public void onResponse(Call<ResultadosEventoResponse> call, Response<ResultadosEventoResponse> response) {
        isLoading.setValue(false);
        if (response.isSuccessful() && response.body() != null) {
          listaResultados.setValue(response.body().getResultados());
        } else {
          mensajeError.setValue("Error al cargar resultados.");
        }
      }

      @Override
      public void onFailure(Call<ResultadosEventoResponse> call, Throwable t) {
        isLoading.setValue(false);
        mensajeError.setValue("Error de conexión.");
      }
    });
  }
}
