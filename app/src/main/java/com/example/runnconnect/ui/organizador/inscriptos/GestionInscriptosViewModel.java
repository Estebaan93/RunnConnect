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
  // Necesitarás crear este repositorio o agregar los métodos al EventoRepositorio existente
  private final InscripcionRepositorio repositorio;

  // --- ESTADOS DE UI ---
  private final MutableLiveData<List<InscriptoEventoResponse>> listaInscriptos = new MutableLiveData<>();
  private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
  private final MutableLiveData<String> mensajeToast = new MutableLiveData<>();
  private final MutableLiveData<Boolean> esListaVacia = new MutableLiveData<>(false);

  // --- ESTADO INTERNO ---
  private int idEventoActual = 0;
  // Filtro por defecto: "procesando" (lo que el organizador necesita ver urgente)
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

  public void limpiarMensaje() { mensajeToast.setValue(null); }

  // --- ACCIONES (INPUTS) ---

  public void cargarInscriptos(int idEvento) {
    this.idEventoActual = idEvento;
    ejecutarConsulta();
  }

  public void cambiarFiltro(String nuevoEstado) {
    this.filtroEstado = nuevoEstado; // Puede ser "procesando", "pagado", "pendiente" o null (todos)
    ejecutarConsulta();
  }

  // Lógica para Aprobar/Rechazar pago
  public void validarPago(int idInscripcion, boolean aceptar, String motivo) {
    String nuevoEstado = aceptar ? "pagado" : "rechazado";

    isLoading.setValue(true);
    CambiarEstadoPagoRequest request = new CambiarEstadoPagoRequest(nuevoEstado, motivo);

    repositorio.cambiarEstadoPago(idInscripcion, request, new Callback<ResponseBody>() {
      @Override
      public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
        isLoading.setValue(false);
        if (response.isSuccessful()) {
          mensajeToast.setValue(aceptar ? "Pago Aprobado" : "Pago Rechazado");
          // Recargamos la lista para que el item desaparezca de la pestaña "Procesando"
          ejecutarConsulta();
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

  // --- LÓGICA PRIVADA ---

  private void ejecutarConsulta() {
    if (idEventoActual == 0) return;

    isLoading.setValue(true);

    // Llamada a la API (Asumimos página 1 y tamaño 100 para simplificar la gestión en una sola pantalla)
    // Endpoint: GET /api/Evento/{id}/Inscripciones?EstadoPago=...&Pagina=1&TamanioPagina=100
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
          // Solo mostramos error si no es un 404 esperado (lista vacía)
          if (response.code() != 404) {
            mensajeToast.setValue("Error cargando lista.");
          }
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
