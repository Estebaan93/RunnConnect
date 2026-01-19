package com.example.runnconnect.ui.organizador.inscriptos;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.runnconnect.data.repositorio.InscripcionRepositorio;
import com.example.runnconnect.data.request.CambiarEstadoPagoRequest;
import com.example.runnconnect.data.response.InscriptoEventoResponse;
import com.example.runnconnect.data.response.ListaInscriptosResponse;

import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GestionInscriptosViewModel extends AndroidViewModel {
  private final InscripcionRepositorio repositorio;

  // --- ESTADOS DE DATOS ---
  private final MutableLiveData<List<InscriptoEventoResponse>> listaInscriptos = new MutableLiveData<>();
  private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
  private final MutableLiveData<String> mensajeToast = new MutableLiveData<>();
  private final MutableLiveData<Boolean> esListaVacia = new MutableLiveData<>(false);

  // --- ORDENES DE UI (Navegación / Diálogos) ---
  private final MutableLiveData<InscriptoEventoResponse> ordenMostrarValidacion = new MutableLiveData<>();
  private final MutableLiveData<InscriptoEventoResponse> ordenMostrarDetalle = new MutableLiveData<>();

  // Estado interno
  private int idEventoActual = 0;
  private String filtroEstado = "procesando";

  public GestionInscriptosViewModel(@NonNull Application application) {
    super(application);
    repositorio = new InscripcionRepositorio(application);
  }

  // --- GETTERS ---
  public LiveData<List<InscriptoEventoResponse>> getListaInscriptos() { return listaInscriptos; }
  public LiveData<Boolean> getIsLoading() { return isLoading; }
  public LiveData<String> getMensajeToast() { return mensajeToast; }
  public LiveData<Boolean> getEsListaVacia() { return esListaVacia; }

  // Getters de Órdenes
  public LiveData<InscriptoEventoResponse> getOrdenMostrarValidacion() { return ordenMostrarValidacion; }
  public LiveData<InscriptoEventoResponse> getOrdenMostrarDetalle() { return ordenMostrarDetalle; }

  // --- CONSUMO DE ÓRDENES ---
  public void limpiarMensaje() { mensajeToast.setValue(null); }
  public void limpiarOrdenesDialogo() {
    ordenMostrarValidacion.setValue(null);
    ordenMostrarDetalle.setValue(null);
  }

  // --- ENTRADAS (Acciones del Usuario) ---

  public void cargarInscriptos(int idEvento) {
    this.idEventoActual = idEvento;
    ejecutarConsulta();
  }

  public void cambiarFiltro(String nuevoEstado) {
    this.filtroEstado = nuevoEstado;
    ejecutarConsulta();
  }

  // LÓGICA CLAVE: El VM decide qué diálogo mostrar según el estado
  public void onInscriptoSeleccionado(InscriptoEventoResponse item) {
    if (item == null) return;

    if ("procesando".equalsIgnoreCase(item.getEstadoPago())) {
      ordenMostrarValidacion.setValue(item);
    } else {
      ordenMostrarDetalle.setValue(item);
    }
  }

  // Lógica encapsulada: Aprobar
  public void aprobarPago(int idInscripcion) {
    ejecutarCambioEstado(idInscripcion, "pagado", "Pago confirmado por organizador");
  }

  // Lógica encapsulada: Rechazar
  public void rechazarPago(int idInscripcion) {
    ejecutarCambioEstado(idInscripcion, "rechazado", "Comprobante inválido o ilegible");
  }

  // --- PRIVADO: API ---

  private void ejecutarCambioEstado(int idInscripcion, String nuevoEstado, String motivo) {
    isLoading.setValue(true);
    CambiarEstadoPagoRequest request = new CambiarEstadoPagoRequest(nuevoEstado, motivo);

    repositorio.cambiarEstadoPago(idInscripcion, request, new Callback<ResponseBody>() {
      @Override
      public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
        isLoading.setValue(false);
        if (response.isSuccessful()) {
          mensajeToast.setValue("pagado".equals(nuevoEstado) ? "Pago Aprobado" : "Pago Rechazado");
          ejecutarConsulta(); // Recargar lista
        } else {
          mensajeToast.setValue("Error al procesar: " + response.code());
        }
      }
      @Override
      public void onFailure(Call<ResponseBody> call, Throwable t) {
        isLoading.setValue(false);
        mensajeToast.setValue("Error de conexión");
      }
    });
  }

  private void ejecutarConsulta() {
    if (idEventoActual == 0) return;
    isLoading.setValue(true);

    repositorio.obtenerInscriptos(idEventoActual, filtroEstado, 1, 100, new Callback<ListaInscriptosResponse>() {
      @Override
      public void onResponse(Call<ListaInscriptosResponse> call, Response<ListaInscriptosResponse> response) {
        isLoading.setValue(false);
        if (response.isSuccessful() && response.body() != null) {
          List<InscriptoEventoResponse> lista = response.body().getInscripciones();
          listaInscriptos.setValue(lista);
          esListaVacia.setValue(lista.isEmpty());
        } else {
          listaInscriptos.setValue(new ArrayList<>());
          esListaVacia.setValue(true);
          if (response.code() != 404) mensajeToast.setValue("Error cargando lista.");
        }
      }
      @Override
      public void onFailure(Call<ListaInscriptosResponse> call, Throwable t) {
        isLoading.setValue(false);
        mensajeToast.setValue("Error de conexión");
        listaInscriptos.setValue(new ArrayList<>());
        esListaVacia.setValue(true);
      }
    });
  }
}