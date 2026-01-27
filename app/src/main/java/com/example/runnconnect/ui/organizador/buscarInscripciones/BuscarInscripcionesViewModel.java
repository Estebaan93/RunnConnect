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

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BuscarInscripcionesViewModel extends AndroidViewModel {
    private final InscripcionRepositorio repositorio;
    
    private final MutableLiveData<List<BusquedaInscripcionResponse>> resultados = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> mensajeExito = new MutableLiveData<>();
    private final MutableLiveData<String> mensajeError = new MutableLiveData<>();

    public BuscarInscripcionesViewModel(@NonNull Application application) {
        super(application);
        repositorio = new InscripcionRepositorio(application);
    }

    public LiveData<List<BusquedaInscripcionResponse>> getResultados() { return resultados; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<String> getMensajeExito() { return mensajeExito; }
    public LiveData<String> getMensajeError() { return mensajeError; }

    public void buscar(String termino) {
        if (termino == null || termino.trim().isEmpty()) return;

        isLoading.setValue(true);
        repositorio.buscarIncripcion(termino, new Callback<List<BusquedaInscripcionResponse>>() {
            @Override
            public void onResponse(Call<List<BusquedaInscripcionResponse>> call, Response<List<BusquedaInscripcionResponse>> response) {
                isLoading.setValue(false);
                if (response.isSuccessful() && response.body() != null) {
                  //filtro
                  List<BusquedaInscripcionResponse> todos= response.body();
                  List<BusquedaInscripcionResponse> soloPagos= new ArrayList<>();
                  
                  for(BusquedaInscripcionResponse item : todos){
                    //agregamos los pagados
                    if("pagado".equalsIgnoreCase(item.getEstadoPago())){
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
    }

    public void darDeBaja(int idInscripcion, String motivo, String terminoActual) {
        isLoading.setValue(true);
        repositorio.darDeBajaRunner(idInscripcion, motivo, new Callback<okhttp3.ResponseBody>() {
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
    }
}