package com.example.runnconnect.ui.eventosPublicos.detalle;

import android.app.Application;
import android.content.Intent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.runnconnect.data.conexion.ApiClient;
import com.example.runnconnect.data.conexion.ApiService;
import com.example.runnconnect.data.preferencias.SessionManager;
import com.example.runnconnect.data.response.CategoriaResponse;
import com.example.runnconnect.data.response.EventoDetalleResponse;
import com.example.runnconnect.ui.eventosPublicos.mapa.MapaPublicoActivity;
import com.example.runnconnect.ui.login.LoginActivity;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DetalleEventoPublicoViewModel extends AndroidViewModel {

  private final ApiService apiService;

  // Estados de UI
  private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
  //nuevo 23-03
  private final MutableLiveData<String> errorText = new MutableLiveData<>();
  private final MutableLiveData<Integer> errorVisibility = new MutableLiveData<>(View.GONE);
  private final MutableLiveData<String> nombre = new MutableLiveData<>();
  private final MutableLiveData<String> fechaHora = new MutableLiveData<>();
  private final MutableLiveData<String> lugar = new MutableLiveData<>();
  private final MutableLiveData<String> descripcion = new MutableLiveData<>();
  private final MutableLiveData<String> estado = new MutableLiveData<>();
  private final MutableLiveData<String> cupos = new MutableLiveData<>();
  private final MutableLiveData<String> nombreOrganizador = new MutableLiveData<>();
  private final MutableLiveData<List<CategoriaResponse>> categorias = new MutableLiveData<>();
  private final MutableLiveData<Intent> navegacionEvento = new MutableLiveData<>();
  private final MutableLiveData<Boolean> finalUser = new MutableLiveData<>();


  public DetalleEventoPublicoViewModel(@NonNull Application application) {
    super(application);
    this.apiService = ApiClient.getApiService();
  }

  // Getters
  public LiveData<Boolean> getIsLoading() { return isLoading; }
  public LiveData<String> getErrorText() { return errorText; }
  public LiveData<Integer> getErrorVisibility() { return errorVisibility; }
  public LiveData<String> getNombre() { return nombre; }
  public LiveData<String> getFechaHora() { return fechaHora; }
  public LiveData<String> getLugar() { return lugar; }
  public LiveData<String> getDescripcion() { return descripcion; }
  public LiveData<String> getEstado() { return estado; }
  public LiveData<String> getCupos() { return cupos; }
  public LiveData<String> getNombreOrganizador() { return nombreOrganizador; }
  public LiveData<List<CategoriaResponse>> getCategorias() { return categorias; }
  public LiveData<Intent> getNavegacionEvento() { return navegacionEvento; }
  public LiveData<Boolean> getFinalUser() { return finalUser; }

  public void inicializar(int idEvento) {
    if (idEvento == 0) {
      finalUser.setValue(true);
      return;
    }
    cargarDetalle(idEvento);
  }

  public void cargarDetalle(int idEvento) {
    isLoading.setValue(true);
    ocultarError();

    apiService.obtenerEventoPorIdPublico(idEvento).enqueue(new Callback<EventoDetalleResponse>() {
      @Override
      public void onResponse(Call<EventoDetalleResponse> call, Response<EventoDetalleResponse> response) {
        isLoading.setValue(false);
        if (response.isSuccessful() && response.body() != null) {
          //evento.setValue(response.body());
          EventoDetalleResponse e = response.body();
          nombre.setValue(e.getNombre());
          fechaHora.setValue("Fecha: " + (e.getFechaHora() != null ? e.getFechaHora().replace("T", " ") : "-"));
          lugar.setValue("Lugar: " + e.getLugar());
          descripcion.setValue(e.getDescripcion());
          estado.setValue("Estado: " + (e.getEstado() != null ? e.getEstado().toUpperCase() : ""));
          int disponibles = e.getCuposDisponibles();
          int total = (e.getCupoTotal() != null) ? e.getCupoTotal() : 0;
          cupos.setValue("Cupos: " + disponibles + " / " + total);
          nombreOrganizador.setValue(e.getOrganizador() != null ? e.getOrganizador().getNombre() : "");
          categorias.setValue(e.getCategorias());
        } else {
          try {
            response.errorBody().string();
          } catch(Exception e){}
          mostrarError("No se pudo cargar la información del evento.");
        }
      }

      @Override
      public void onFailure(Call<EventoDetalleResponse> call, Throwable t) {
        isLoading.setValue(false);
        mostrarError("Error de conexión con el servidor.");
      }
    });
  }


  public void onLoginClicked() {
    Intent intent = new Intent(getApplication(), LoginActivity.class);
    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
    navegacionEvento.setValue(intent);
    finalUser.setValue(true);
  }

  public void onVerMapaClicked(int idEvento) {
    Intent intent = new Intent(getApplication(), MapaPublicoActivity.class);
    intent.putExtra("idEvento", idEvento);
    navegacionEvento.setValue(intent);
    finalUser.setValue(false);
  }
  private void mostrarError(String mensaje) {
    errorText.setValue(mensaje);
    errorVisibility.setValue(View.VISIBLE);
  }

  private void ocultarError() {
    errorVisibility.setValue(View.GONE);
  }


}