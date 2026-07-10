package com.example.runnconnect.ui.organizador.buscarInscripciones;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.runnconnect.data.conexion.ApiClient;
import com.example.runnconnect.data.conexion.ApiService;
import com.example.runnconnect.data.preferencias.SessionManager;
import com.example.runnconnect.data.request.MotivoBajaRequest;
import com.example.runnconnect.data.response.BusquedaInscripcionResponse;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BuscarInscripcionesViewModel extends AndroidViewModel {
  private final ApiService apiService;
  private final SessionManager sessionManager;

  private final MutableLiveData<List<BusquedaInscripcionResponse>> resultados = new MutableLiveData<>();
  private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
  private final MutableLiveData<String> mensajeExito = new MutableLiveData<>();
  private final MutableLiveData<String> mensajeError = new MutableLiveData<>();

  public BuscarInscripcionesViewModel(@NonNull Application application) {
    super(application);
    this.sessionManager = new SessionManager(application);
    this.apiService = ApiClient.getApiService();
  }

  public LiveData<List<BusquedaInscripcionResponse>> getResultados() {
    return resultados;
  }

  public LiveData<Boolean> getIsLoading() {
    return isLoading;
  }

  public LiveData<String> getMensajeExito() {
    return mensajeExito;
  }

  public LiveData<String> getMensajeError() {
    return mensajeError;
  }

  public void buscar(String termino) {
    if (termino == null || termino.trim().isEmpty())
      return;

    isLoading.setValue(true);
    String token = sessionManager.leerToken();
    if (token != null) {
      apiService.buscarInscriptos("Bearer " + token, termino).enqueue(new Callback<List<BusquedaInscripcionResponse>>() {
        @Override
        public void onResponse(Call<List<BusquedaInscripcionResponse>> call,
            Response<List<BusquedaInscripcionResponse>> response) {
          isLoading.setValue(false);
          if (response.isSuccessful() && response.body() != null) {
            // filtro
            List<BusquedaInscripcionResponse> todos = response.body();
            List<BusquedaInscripcionResponse> soloPagos = new ArrayList<>();

            for (BusquedaInscripcionResponse item : todos) {
              // agregamos los pagados
              if ("pagado".equalsIgnoreCase(item.getEstadoPago())) {
                soloPagos.add(item);
              }
            }

            resultados.setValue(soloPagos);
          } else {
            resultados.setValue(new ArrayList<>());
          }
        }

        @Override
        public void onFailure(Call<List<BusquedaInscripcionResponse>> call, Throwable t) {
          isLoading.setValue(false);
          mensajeError.setValue("Error de conexión");
        }
      });
    } else {
      isLoading.setValue(false);
      mensajeError.setValue("Sin sesión");
    }
  }

  public void darDeBaja(int idInscripcion, String motivo, String terminoActual) {
    isLoading.setValue(true);
    String token= sessionManager.leerToken();
    if(token != null){
      MotivoBajaRequest request = new MotivoBajaRequest(motivo);
      apiService.darDeBajaRunner("Bearer "+token, idInscripcion, request).enqueue(new Callback<okhttp3.ResponseBody>() {
        @Override
        public void onResponse(Call<okhttp3.ResponseBody> call, Response<okhttp3.ResponseBody> response) {
          isLoading.setValue(false);
          if (response.isSuccessful()) {
            mensajeExito.setValue("Inscripción cancelada");
            buscar(terminoActual);
          } else {
            mensajeError.setValue("Error: " + response.code());
          }
        }

        @Override
        public void onFailure(Call<okhttp3.ResponseBody> call, Throwable t) {
          isLoading.setValue(false);
          mensajeError.setValue("Error de conexión");
        }
      });
    }else{
      isLoading.setValue(false);
      mensajeError.setValue("No hay sesion activa");
    }
  }

  //limpiar caja
  public void limpiarBusqueda(){
    resultados.setValue(new ArrayList<>());
    mensajeExito.setValue("");
    mensajeError.setValue("");
  }



}