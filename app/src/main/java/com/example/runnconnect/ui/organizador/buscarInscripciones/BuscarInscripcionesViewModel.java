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

import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

public class BuscarInscripcionesViewModel extends AndroidViewModel {
  private final ApiService apiService;
  private final SessionManager sessionManager;


  private final MutableLiveData<List<BusquedaItemUiModel>> resultadosUi = new MutableLiveData<>();
  private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
  private final MutableLiveData<String> estadoBusquedaMensaje = new MutableLiveData<>("");
  private final MutableLiveData<Integer> estadoBusquedaVisibilidad = new MutableLiveData<>(View.GONE);
  
  // Dialog LiveData
  private final MutableLiveData<DetalleUiState> detalleUiState = new MutableLiveData<>();
  //private final MutableLiveData<Boolean> cerrarDialog = new MutableLiveData<>();
  private final MutableLiveData<Boolean> ocultarDialogSignal = new MutableLiveData<>();
  private final MutableLiveData<String> feedbackDialogMensaje = new MutableLiveData<>();
  private final MutableLiveData<Integer> feedbackDialogVisibilidad = new MutableLiveData<>(View.GONE);
  private final MutableLiveData<Integer> feedbackDialogColor = new MutableLiveData<>(Color.BLACK);
  private final MutableLiveData<Boolean> btnBajaHabilitado = new MutableLiveData<>(true);

  public BuscarInscripcionesViewModel(@NonNull Application application) {
    super(application);
    this.sessionManager = new SessionManager(application);
    this.apiService = ApiClient.getApiService();
  }

  public LiveData<List<BusquedaItemUiModel>> getResultadosUi() { return resultadosUi; }
  public LiveData<Boolean> getIsLoading() { return isLoading; }
  public LiveData<String> getEstadoBusquedaMensaje() { return estadoBusquedaMensaje; }
  public LiveData<Integer> getEstadoBusquedaVisibilidad() { return estadoBusquedaVisibilidad; }
  
  public LiveData<DetalleUiState> getDetalleUiState() { return detalleUiState; }
  //public LiveData<Boolean> getCerrarDialog() { return cerrarDialog; }
  public LiveData<Boolean> getOcultarDialog() { return ocultarDialogSignal; }
  public LiveData<String> getFeedbackDialogMensaje() { return feedbackDialogMensaje; }
  public LiveData<Integer> getFeedbackDialogVisibilidad() { return feedbackDialogVisibilidad; }
  public LiveData<Integer> getFeedbackDialogColor() { return feedbackDialogColor; }
  public LiveData<Boolean> getBtnBajaHabilitado() { return btnBajaHabilitado; }

  public void onTextoBuscadorCambiado(String texto) {
    if (texto == null || texto.trim().isEmpty()) {
      limpiarBusqueda();
    }
  }

  public void buscar(String termino) {
    if (termino == null || termino.trim().isEmpty())
      return;

    isLoading.setValue(true);
    String token = sessionManager.leerToken();
    if (token != null) {
      apiService.buscarInscriptos("Bearer " + token, termino).enqueue(new Callback<List<BusquedaInscripcionResponse>>() {
        @Override
        public void onResponse(Call<List<BusquedaInscripcionResponse>> call, Response<List<BusquedaInscripcionResponse>> response) {
          isLoading.setValue(false);
          if (response.isSuccessful() && response.body() != null) {
            List<BusquedaItemUiModel> uiModels = new ArrayList<>();
            for (BusquedaInscripcionResponse item : response.body()) {
              if ("pagado".equalsIgnoreCase(item.getEstadoPago())) {
                String nombre = item.getRunner() != null ? item.getRunner().getNombreCompleto() : "Usuario Desconocido";
                String dni = item.getRunner() != null && item.getRunner().getDni() != null ? "DNI: " + item.getRunner().getDni() : "DNI: -";
                String eventoCategoria = item.getNombreEvento() + " (" + item.getNombreCategoria() + ")";
                
                String estado = item.getEstadoPago() != null ? item.getEstadoPago().toUpperCase() : "-";
                int color = Color.parseColor("#FF9800"); // Default Naranja
                if ("PAGADO".equals(estado)) {
                  color = Color.parseColor("#2E7D32"); // Verde
                } else if ("CANCELADO".equals(estado)) {
                  color = Color.RED;
                }
                
                uiModels.add(new BusquedaItemUiModel(item.getIdInscripcion(), item, nombre, dni, eventoCategoria, estado, color));
              }
            }
            if (uiModels.isEmpty()) {
              estadoBusquedaMensaje.setValue("No se encontraron resultados");
              estadoBusquedaVisibilidad.setValue(View.VISIBLE);
            } else {
              estadoBusquedaMensaje.setValue("");
              estadoBusquedaVisibilidad.setValue(View.GONE);
            }
            resultadosUi.setValue(uiModels);
          } else {
            resultadosUi.setValue(new ArrayList<>());
            estadoBusquedaMensaje.setValue("Error al realizar la búsqueda");
            estadoBusquedaVisibilidad.setValue(View.VISIBLE);
          }
        }

        @Override
        public void onFailure(Call<List<BusquedaInscripcionResponse>> call, Throwable t) {
          isLoading.setValue(false);
          estadoBusquedaMensaje.setValue("Error de conexión");
          estadoBusquedaVisibilidad.setValue(View.VISIBLE);
        }
      });
    } else {
      isLoading.setValue(false);
      estadoBusquedaMensaje.setValue("Sin sesión");
      estadoBusquedaVisibilidad.setValue(View.VISIBLE);
    }
  }

  public void seleccionarItem(BusquedaItemUiModel itemUi) {
    BusquedaInscripcionResponse item = itemUi.original;
    BusquedaInscripcionResponse.RunnerSimpleInfo r = item.getRunner();

    String nombre = r != null ? r.getNombreCompleto() : "Usuario Desconocido";
    String dni = r != null && r.getDni() != null ? r.getDni() : "-";
    String genero = r != null && r.getGenero() != null ? r.getGenero() : "-";
    String dniSexo = String.format("DNI: %s | Sexo: %s", dni, genero);
    
    String localidad = r != null && r.getLocalidad() != null ? r.getLocalidad() : "Localidad no especificada";
    String email = r != null && r.getEmail() != null ? r.getEmail() : "";
    String telefono = r != null && r.getTelefono() != null ? r.getTelefono() : "-";
    
    String nomEmergencia = r != null && r.getNombreContactoEmergencia() != null ? r.getNombreContactoEmergencia() : "No informado";
    String emergencia = "Contacto: " + nomEmergencia;
    String tEmergencia = r != null && r.getTelefonoEmergencia() != null ? r.getTelefonoEmergencia() : "-";
    String telEmergencia = "Tel: " + tEmergencia;

    String talle = item.getTalleRemera() != null ? item.getTalleRemera() : "-";
    String categoria = item.getNombreCategoria() != null ? item.getNombreCategoria() : "Sin Cat.";
    String evento = item.getNombreEvento();
    String eventoCatTalle = "Evento: " + evento + "\nCat: " + categoria + " | Talle: " + talle;

    String estadoDelEvento = item.getEstadoEvento();
    boolean eventoCerrado = "finalizado".equalsIgnoreCase(estadoDelEvento) || "cancelado".equalsIgnoreCase(estadoDelEvento);

    // Reset feedback states for the new dialog
    feedbackDialogMensaje.setValue("");
    feedbackDialogVisibilidad.setValue(View.GONE);
    btnBajaHabilitado.setValue(true);

    detalleUiState.setValue(new DetalleUiState(
        itemUi.idInscripcion, nombre, dniSexo, localidad, email, telefono, 
        emergencia, telEmergencia, eventoCatTalle, !eventoCerrado
    ));
  }

  public void confirmarBaja(int idInscripcion, String nombreRunner, String terminoActual) {
    btnBajaHabilitado.setValue(false);
    String token = sessionManager.leerToken();
    if (token != null) {
      MotivoBajaRequest request = new MotivoBajaRequest("Cancelado desde Búsqueda");
      apiService.darDeBajaRunner("Bearer " + token, idInscripcion, request).enqueue(new Callback<okhttp3.ResponseBody>() {
        @Override
        public void onResponse(Call<okhttp3.ResponseBody> call, Response<okhttp3.ResponseBody> response) {
          if (response.isSuccessful()) {
            feedbackDialogColor.setValue(Color.parseColor("#2E7D32")); // Verde
            feedbackDialogMensaje.setValue("Dado de baja correctamente");
            feedbackDialogVisibilidad.setValue(View.VISIBLE);
            
            // Cerrar automaticamente luego de 1.5s
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
              //cerrarDialog.setValue(true);
              ocultarDialogSignal.setValue(true);
              ocultarDialogSignal.postValue(false);  // reseteo automatico
              buscar(terminoActual); // Refrescar lista
            }, 1500);
            
          } else {
            feedbackDialogColor.setValue(Color.RED);
            feedbackDialogMensaje.setValue("Error al dar de baja");
            feedbackDialogVisibilidad.setValue(View.VISIBLE);
            btnBajaHabilitado.setValue(true);
          }
        }

        @Override
        public void onFailure(Call<okhttp3.ResponseBody> call, Throwable t) {
          feedbackDialogColor.setValue(Color.RED);
          feedbackDialogMensaje.setValue("Error de conexión");
          feedbackDialogVisibilidad.setValue(View.VISIBLE);
          btnBajaHabilitado.setValue(true);
        }
      });
    } else {
      feedbackDialogColor.setValue(Color.RED);
      feedbackDialogMensaje.setValue("No hay sesión activa");
      feedbackDialogVisibilidad.setValue(View.VISIBLE);
      btnBajaHabilitado.setValue(true);
    }
  }

  public void limpiarBusqueda() {
    resultadosUi.setValue(new ArrayList<>());
    estadoBusquedaMensaje.setValue("");
    estadoBusquedaVisibilidad.setValue(View.GONE);
  }

  // UI States
  public static class BusquedaItemUiModel {
    public final int idInscripcion;
    public final BusquedaInscripcionResponse original;
    public final String nombreCompleto;
    public final String dni;
    public final String eventoCategoria;
    public final String estadoPagoTexto;
    public final int estadoPagoColor;

    public BusquedaItemUiModel(int idInscripcion, BusquedaInscripcionResponse original, String nombreCompleto, String dni, String eventoCategoria, String estadoPagoTexto, int estadoPagoColor) {
      this.idInscripcion = idInscripcion;
      this.original = original;
      this.nombreCompleto = nombreCompleto;
      this.dni = dni;
      this.eventoCategoria = eventoCategoria;
      this.estadoPagoTexto = estadoPagoTexto;
      this.estadoPagoColor = estadoPagoColor;
    }
  }

  public static class DetalleUiState {
    public final int idInscripcion;
    public final String nombre;
    public final String dniSexo;
    public final String localidad;
    public final String email;
    public final String telefono;
    public final String emergencia;
    public final String telEmergencia;
    public final String eventoCatTalle;
    public final boolean btnBajaVisible;

    public DetalleUiState(int idInscripcion, String nombre, String dniSexo, String localidad, String email, String telefono, String emergencia, String telEmergencia, String eventoCatTalle, boolean btnBajaVisible) {
      this.idInscripcion = idInscripcion;
      this.nombre = nombre;
      this.dniSexo = dniSexo;
      this.localidad = localidad;
      this.email = email;
      this.telefono = telefono;
      this.emergencia = emergencia;
      this.telEmergencia = telEmergencia;
      this.eventoCatTalle = eventoCatTalle;
      this.btnBajaVisible = btnBajaVisible;
    }
  }



}