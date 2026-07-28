package com.example.runnconnect.ui.organizador.inscriptos;

import android.app.Application;

import android.util.Log;
import android.graphics.Color;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.runnconnect.data.conexion.ApiClient;
import com.example.runnconnect.data.conexion.ApiService;
import com.example.runnconnect.data.preferencias.SessionManager;
import com.example.runnconnect.data.request.MotivoBajaRequest;
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
  private final ApiService apiService;
  private final SessionManager sessionManager;

  // --- ESTADOS DE DATOS ---
  public static class InscriptoItemUIState {
      public final InscriptoEventoResponse data;
      public final String estadoTexto;
      public final int estadoColor;
      public final int actionVisibility;
      
      public InscriptoItemUIState(InscriptoEventoResponse item) {
          this.data = item;
          String estado = item.getEstadoPago() != null ? item.getEstadoPago().toLowerCase() : "pendiente";
          this.estadoTexto = estado.toUpperCase();
          switch (estado) {
              case "pagado":
                  this.estadoColor = Color.parseColor("#2E7D32");
                  this.actionVisibility = View.GONE;
                  break;
              case "procesando":
                  this.estadoColor = Color.parseColor("#EF6C00");
                  this.actionVisibility = View.VISIBLE;
                  break;
              case "rechazado":
                  this.estadoColor = Color.parseColor("#C62828");
                  this.actionVisibility = View.GONE;
                  break;
              default:
                  this.estadoColor = Color.GRAY;
                  this.actionVisibility = View.GONE;
                  break;
          }
      }
  }

  private final MutableLiveData<List<InscriptoItemUIState>> listaInscriptos = new MutableLiveData<>();
  private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
  private final MutableLiveData<String> uiMensajeGlobal = new MutableLiveData<>();
  private final MutableLiveData<Integer> uiMensajeGlobalColor = new MutableLiveData<>();
  private final MutableLiveData<Integer> uiMensajeGlobalVisibilidad = new MutableLiveData<>(View.GONE);

  private final MutableLiveData<Boolean> esListaVacia = new MutableLiveData<>(false);

  private final MutableLiveData<Boolean> permitirBajas = new MutableLiveData<>(true);

  // --- ORDENES DE UI (Navegacion / Dialogos) ---
  private final MutableLiveData<InscriptoEventoResponse> datosValidacion = new MutableLiveData<>();
  private final MutableLiveData<InscriptoEventoResponse> datosDetalle = new MutableLiveData<>();
  private final MutableLiveData<Boolean> mostrarValidacionSignal = new MutableLiveData<>();
  private final MutableLiveData<Boolean> mostrarDetalleSignal = new MutableLiveData<>();

  // --- ORDENES DE CONFIRMACION ---
  private final MutableLiveData<InscriptoEventoResponse> datosConfirmacionBaja = new MutableLiveData<>();
  private final MutableLiveData<Boolean> mostrarConfirmacionBajaSignal = new MutableLiveData<>();

  // Estado interno
  private int idEventoActual = 0;
  private String filtroEstado = "procesando";

  public GestionInscriptosViewModel(@NonNull Application application) {
    super(application);
    apiService = ApiClient.getApiService();
    sessionManager = new SessionManager(application);
  }

  // --- GETTERS ---
  public LiveData<List<InscriptoItemUIState>> getListaInscriptos() { return listaInscriptos; }
  public LiveData<Boolean> getIsLoading() { return isLoading; }
  public LiveData<String> getUiMensajeGlobal() { return uiMensajeGlobal; }
  public LiveData<Integer> getUiMensajeGlobalColor() { return uiMensajeGlobalColor; }
  public LiveData<Integer> getUiMensajeGlobalVisibilidad() { return uiMensajeGlobalVisibilidad; }
  public LiveData<Boolean> getEsListaVacia() { return esListaVacia; }

  public LiveData<Boolean> getPermitirBajas() { return permitirBajas; }

  // Getters de ordenes
  public LiveData<InscriptoEventoResponse> getDatosValidacion() { return datosValidacion; }
  public LiveData<InscriptoEventoResponse> getDatosDetalle() { return datosDetalle; }
  public LiveData<Boolean> getMostrarValidacionSignal() { return mostrarValidacionSignal; }
  public LiveData<Boolean> getMostrarDetalleSignal() { return mostrarDetalleSignal; }

  public LiveData<InscriptoEventoResponse> getDatosConfirmacionBaja() { return datosConfirmacionBaja; }
  public LiveData<Boolean> getMostrarConfirmacionBajaSignal() { return mostrarConfirmacionBajaSignal; }

  // --- CONSUMO DE ORDENES ---
  public void limpiarMensajes() {
    uiMensajeGlobalVisibilidad.setValue(View.GONE);
  }

  // --- ENTRADAS (Acciones del Usuario) ---
  public void inicializar(int idEvento, String estadoEvento) {
    this.idEventoActual = idEvento;
    boolean cerrado = "finalizado".equalsIgnoreCase(estadoEvento) || "cancelado".equalsIgnoreCase(estadoEvento);
    permitirBajas.setValue(!cerrado);
    ejecutarConsulta();
  }

  public void cambiarFiltro(String nuevoEstado) {
    this.filtroEstado = nuevoEstado;
    ejecutarConsulta();
  }

  private void mostrarErrorGlobal(String mensaje) {
    uiMensajeGlobal.setValue(mensaje);
    uiMensajeGlobalColor.setValue(Color.RED);
    uiMensajeGlobalVisibilidad.setValue(View.VISIBLE);
  }

  private void mostrarExitoGlobal(String mensaje) {
    uiMensajeGlobal.setValue(mensaje);
    uiMensajeGlobalColor.setValue(Color.parseColor("#388E3C")); // Verde
    uiMensajeGlobalVisibilidad.setValue(View.VISIBLE);
  }

  // LOGICA CLAVE: El VM decide que dialogo mostrar segun el estado
  public void onInscriptoSeleccionado(InscriptoItemUIState uiState) {
    if (uiState == null || uiState.data == null) return;
    InscriptoEventoResponse item = uiState.data;

    if ("procesando".equalsIgnoreCase(item.getEstadoPago())) {
      datosValidacion.setValue(item);
      mostrarValidacionSignal.setValue(true);
    } else {
      datosDetalle.setValue(item);
      mostrarDetalleSignal.setValue(true);
    }
  }

  public void intentarDarDeBajaRunner(InscriptoEventoResponse item) {
    if (item == null) return;
    datosConfirmacionBaja.setValue(item);
    mostrarConfirmacionBajaSignal.setValue(true);
  }

  // Logica encapsulada: Aprobar
  public void aprobarPago(int idInscripcion) {
    ejecutarCambioEstado(idInscripcion, "pagado", "Pago confirmado por organizador");
  }

  // Logica encapsulada: Rechazar
  public void rechazarPago(int idInscripcion) {
    ejecutarCambioEstado(idInscripcion, "rechazado", "Comprobante inválido o ilegible");
  }

  // --- PRIVADO: API ---
  private void ejecutarCambioEstado(int idInscripcion, String nuevoEstado, String motivo) {
    isLoading.setValue(true);
    CambiarEstadoPagoRequest request = new CambiarEstadoPagoRequest(nuevoEstado, motivo);
    
    String token = sessionManager.leerToken();
    if (token != null) {
      apiService.cambiarEstadoPago("Bearer " + token, idInscripcion, request).enqueue(new Callback<ResponseBody>() {
        @Override
        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
          isLoading.setValue(false);
          if (response.isSuccessful()) {
            mostrarExitoGlobal("pagado".equals(nuevoEstado) ? "Pago Aprobado" : "Pago Rechazado");
            ejecutarConsulta(); // Recargar lista
          } else {
            mostrarErrorGlobal("Error al procesar: " + response.code());
          }
        }
        @Override
        public void onFailure(Call<ResponseBody> call, Throwable t) {
          isLoading.setValue(false);
          mostrarErrorGlobal("Error de conexión");
        }
      });
    } else {
      isLoading.setValue(false);
      mostrarErrorGlobal("No hay sesión activa.");
    }
  }

  private void ejecutarConsulta() {
    if (idEventoActual == 0) return;
    isLoading.setValue(true);

    String token = sessionManager.leerToken();
    if (token != null) {
      apiService.obtenerInscriptos("Bearer " + token, idEventoActual, filtroEstado, 1, 100).enqueue(new Callback<ListaInscriptosResponse>() {
        @Override
        public void onResponse(Call<ListaInscriptosResponse> call, Response<ListaInscriptosResponse> response) {
          isLoading.setValue(false);
          if (response.isSuccessful() && response.body() != null) {
            List<InscriptoEventoResponse> listaResp = response.body().getInscripciones();
            List<InscriptoItemUIState> listaUi = new ArrayList<>();
            for (InscriptoEventoResponse resp : listaResp) {
              listaUi.add(new InscriptoItemUIState(resp));
            }
            listaInscriptos.setValue(listaUi);
            esListaVacia.setValue(listaUi.isEmpty());
          } else {
            listaInscriptos.setValue(new ArrayList<>());
            esListaVacia.setValue(true);
            if (response.code() != 404) mostrarErrorGlobal("Error cargando lista.");
          }
        }
        @Override
        public void onFailure(Call<ListaInscriptosResponse> call, Throwable t) {
          isLoading.setValue(false);
          mostrarErrorGlobal("Error de conexión");
          listaInscriptos.setValue(new ArrayList<>());
          esListaVacia.setValue(true);
        }
      });
    } else {
      isLoading.setValue(false);
      mostrarErrorGlobal("No hay sesión activa.");
      listaInscriptos.setValue(new ArrayList<>());
      esListaVacia.setValue(true);
    }
  }

  //Dar de baja
  public void darDeBajaRunner(int idInscripcion) {
    isLoading.setValue(true);

    //motivo generico
    String motivo= "Baja solicitada por el organizador en gestion de inscripciones";

    //lamamos al repo
    String token = sessionManager.leerToken();
    if (token != null) {
      MotivoBajaRequest request = new MotivoBajaRequest(motivo);
      apiService.darDeBajaRunner("Bearer " + token, idInscripcion, request).enqueue(new Callback<ResponseBody>() {
        @Override
        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
          isLoading.setValue(false);
          
          if(response.isSuccessful()){
            mostrarExitoGlobal("Runner dado de baja exitosamente");
            ejecutarConsulta(); //Recarga la lista para ver cambios

          }else{
            mostrarErrorGlobal("Error al dar de baja:");
            Log.d("GestionInscriptosVM", "Error al dar de baja: " + response.code());
          }
        
        }
        @Override
        public void onFailure(Call<ResponseBody> call, Throwable t) {
          isLoading.setValue(false);
          mostrarErrorGlobal("Error de conexión");
        }
      });
    } else {
      isLoading.setValue(false);
      mostrarErrorGlobal("No hay sesión activa.");
    }
    


  }


}