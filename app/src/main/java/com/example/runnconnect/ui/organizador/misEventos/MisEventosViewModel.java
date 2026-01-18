package com.example.runnconnect.ui.organizador.misEventos;

import android.app.Application;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.runnconnect.data.repositorio.EventoRepositorio;
import com.example.runnconnect.data.response.EventoResumenResponse;
import com.example.runnconnect.data.response.EventosPaginadosResponse;

import java.util.List;
import android.os.Handler;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MisEventosViewModel extends AndroidViewModel {
  private final MutableLiveData<List<EventoResumenResponse>> listaEventos = new MutableLiveData<>();
  private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
  private final MutableLiveData<String> errorMsg = new MutableLiveData<>();
  private final EventoRepositorio repositorio;

  private final MutableLiveData<String> mensajeExito= new MutableLiveData<>(null);

  //control de paginancion de los items card mis eventos
  private int paginaActual = 1;
  private boolean esUltimaPagina = false;
  private boolean isLoadingMore = false; // Evita multiples llamadas al scrollear rapido

  public MisEventosViewModel(@NonNull Application application) {
    super(application);
    repositorio = new EventoRepositorio(application);
  }

  public LiveData<List<EventoResumenResponse>> getListaEventos() { return listaEventos; }
  public LiveData<Boolean> getIsLoading() { return isLoading; }
  public LiveData<String> getErrorMsg() { return errorMsg; }

  // getters normales (sincronos)
  public int getPaginaActual() { return paginaActual; }
  public boolean isUltimaPagina() { return esUltimaPagina; }
  public boolean isLoadingMore() { return isLoadingMore; }
  public LiveData<String> getMensajeExito() { return mensajeExito; }


  // NUEVO: Metodo para recibir el mensaje y ocultarlo tras 4 segundos
  public void mostrarMensajeExito(String mensaje) {
    if (mensaje == null || mensaje.isEmpty()) return;

    mensajeExito.setValue(mensaje);

    // Timer para ocultar el mensaje automáticamente
    new Handler(Looper.getMainLooper()).postDelayed(() -> {
      mensajeExito.setValue(null); // Esto ocultará la vista en el Fragment
    }, 4000);

    // Opcional: Recargar la lista para ver el evento nuevo
    cargarEventos(true);
  }

  //recibe los metodos y decide si cargar
  public void verificarScroll(int itemsVisibles, int totalItems, int primerItemVisible) {
    // 1. Validaciones de estado (Protección)
    if (isLoadingMore || esUltimaPagina) {
      return;
    }

    // 2. Logica de Negocio (3 items)
    // Si (lo que veo + lo que ya pase) >= total - 3, entonces estoy al final
    if ((itemsVisibles + primerItemVisible) >= (totalItems - 3) && totalItems > 0) {
      cargarEventos(false); // false = Paginacion
    }
  }

  //recarga dinamicamente los datos a medida que scrollea
  public void cargarEventos(boolean reiniciar) {
    if (isLoadingMore) return; // Si ya está cargando, no hacemos nada

    if (reiniciar) {
      paginaActual = 1;
      esUltimaPagina = false;
      isLoading.setValue(true); // Spinner grande solo al inicio
    } else {
      if (esUltimaPagina) return; // Si ya no hay mas, salir
      paginaActual++;
      // No activamos isLoading global para no bloquear toda la pantalla,

    }
    Log.d("PAGINACION_TEST", "Pidiendo página: " + paginaActual + "...");
    isLoadingMore = true;

    // Llamamos al repositorio
    repositorio.obtenerMisEventos(paginaActual,new Callback<EventosPaginadosResponse>() {
      @Override
      public void onResponse(Call<EventosPaginadosResponse> call, Response<EventosPaginadosResponse> response) {
        isLoading.setValue(false);
        isLoadingMore = false;

        if (response.isSuccessful() && response.body() != null) {
          //listaEventos.setValue(response.body().getEventos());
          EventosPaginadosResponse data= response.body();

          //verifica si llega la final para pedir datos
          if(paginaActual>= data.getTotalPaginas()){
            esUltimaPagina= true;
          }

          //enviamos la lista al fragment
          listaEventos.setValue(data.getEventos());
        } else {
          // Si el servidor devuelve error (ej: 401, 500)
          errorMsg.setValue("Error del servidor: " + response.code());
          Log.d("ErrorServidor", "onResponse: " + response.code());
        }
      }

      @Override
      public void onFailure(Call<EventosPaginadosResponse> call, Throwable t) {
        isLoading.setValue(false);
        isLoadingMore= false;
        if(paginaActual>1) paginaActual--; //si falla retrocedemos

        errorMsg.setValue("Error de conexion: " + t.getMessage());
        Log.e("MisEventosVM", "Error API: " + t.getMessage());
      }
    });
  }

}