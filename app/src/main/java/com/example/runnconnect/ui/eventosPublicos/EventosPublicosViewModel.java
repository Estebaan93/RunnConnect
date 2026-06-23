package com.example.runnconnect.ui.eventosPublicos;

import android.app.Application;
import android.util.Log;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.example.runnconnect.data.repositorio.EventoRepositorio;
import com.example.runnconnect.data.response.EventoResumenResponse;
import com.example.runnconnect.data.response.EventosPaginadosResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EventosPublicosViewModel extends AndroidViewModel {

  // CLASE INTERNA: SingleLiveEvent para eventos de un solo uso
  public static class SingleLiveEvent<T> extends MutableLiveData<T> {
    private static final String TAG = "SingleLiveEvent";
    private final AtomicBoolean mPending = new AtomicBoolean(false);

    @MainThread
    @Override
    public void observe(@NonNull LifecycleOwner owner, @NonNull final Observer<? super T> observer) {
      if (hasActiveObservers()) {
        Log.w(TAG, "Múltiples observadores registrados, pero solo uno será notificado.");
      }
      super.observe(owner, t -> {
        if (mPending.compareAndSet(true, false)) {
          observer.onChanged(t);
        }
      });
    }

    @MainThread
    @Override
    public void setValue(T t) {
      mPending.set(true);
      super.setValue(t);
    }
  }

  private final EventoRepositorio repositorio;

  // Estados de UI (Mantienen el estado constante)
  private final MutableLiveData<List<EventoResumenResponse>> listaEventos = new MutableLiveData<>(new ArrayList<>());
  private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
  private final MutableLiveData<Boolean> isVacio = new MutableLiveData<>(false);

  // Eventos únicos (Se consumen una vez, no requieren reset manual)
  private final SingleLiveEvent<String> mostrarMensaje = new SingleLiveEvent<>();
  private final SingleLiveEvent<Integer> navegarADetalle = new SingleLiveEvent<>();

  public EventosPublicosViewModel(@NonNull Application application) {
    super(application);
    repositorio = new EventoRepositorio(application);
  }

  // Getters
  public LiveData<List<EventoResumenResponse>> getListaEventos() { return listaEventos; }
  public LiveData<Boolean> getIsLoading() { return isLoading; }
  public LiveData<Boolean> getIsVacio() { return isVacio; }

  public LiveData<String> getMostrarMensaje() { return mostrarMensaje; }
  public LiveData<Integer> getNavegarADetalle() { return navegarADetalle; }

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
          mostrarMensaje.setValue("Error al cargar eventos: " + response.code());
          Log.d("ErrorEventoPublico", "ErrorObtener: "+ response.errorBody().toString());
        }
      }

      @Override
      public void onFailure(Call<EventosPaginadosResponse> call, Throwable t) {
        isLoading.setValue(false);
        mostrarMensaje.setValue("Error de conexión");
        isVacio.setValue(false);
        listaEventos.setValue(new ArrayList<>());
      }
    });
  }

  public void seleccionarEvento(int idEvento) {
    navegarADetalle.setValue(idEvento);
  }
}