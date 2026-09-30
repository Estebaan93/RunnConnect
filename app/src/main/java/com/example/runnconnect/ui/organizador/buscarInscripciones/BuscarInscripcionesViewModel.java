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
  
  private List<BusquedaItemUiModel> listaMaestra = new ArrayList<>();
  private String filtroEstado = "Todos";

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

  public void setFiltroEstado(String estado) {
    this.filtroEstado = estado;
    aplicarFiltro();
  }

  private void aplicarFiltro() {
    if (listaMaestra.isEmpty()) {
      return;
    }
    List<BusquedaItemUiModel> filtrada = new ArrayList<>();
    for (BusquedaItemUiModel item : listaMaestra) {
      if ("Todos".equals(filtroEstado)) {
        filtrada.add(item);
      } else {
        String estadoBuscado = filtroEstado;
        if (filtroEstado.equals("Pagados")) estadoBuscado = "pagado";
        if (filtroEstado.equals("Procesando")) estadoBuscado = "procesando";
        if (filtroEstado.equals("Pendientes")) estadoBuscado = "pendiente";
        if (filtroEstado.equals("Cancelados")) estadoBuscado = "cancelado";
        if (filtroEstado.equals("Rechazados")) estadoBuscado = "rechazado";
        if (filtroEstado.equals("Reembolsados")) estadoBuscado = "reembolsado";
        
        if (item.original.getEstadoPago().equalsIgnoreCase(estadoBuscado)) {
          filtrada.add(item);
        }
      }
    }
    
    resultadosUi.setValue(filtrada);
    if (filtrada.isEmpty()) {
      estadoBusquedaMensaje.setValue("No hay resultados para '" + filtroEstado + "'");
      estadoBusquedaVisibilidad.setValue(View.VISIBLE);
    } else {
      estadoBusquedaVisibilidad.setValue(View.GONE);
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
            if (response.body().isEmpty()) {
              listaMaestra.clear();
              resultadosUi.setValue(new ArrayList<>());
              estadoBusquedaMensaje.setValue("No se encontraron resultados");
              estadoBusquedaVisibilidad.setValue(View.VISIBLE);
            } else {
              List<BusquedaItemUiModel> tempMaestra = new ArrayList<>();
              for (BusquedaInscripcionResponse item : response.body()) {
                String nombre = item.getRunner().getNombreCompleto();
                String dni = "DNI: " + item.getRunner().getDni();
                String eventoCategoria = item.getNombreEvento() + " (" + item.getNombreCategoria() + ")";
                
                String estado = item.getEstadoPago() != null ? item.getEstadoPago().toUpperCase() : "-";
                int color = Color.parseColor("#FF9800"); // Naranja
                if ("PAGADO".equals(estado)) {
                  color = Color.parseColor("#2E7D32"); // Verde
                } else if ("CANCELADO".equals(estado) || "RECHAZADO".equals(estado) || "REEMBOLSADO".equals(estado)) {
                  color = Color.RED;
                } else if ("PENDIENTE".equals(estado) || "PROCESANDO".equals(estado)) {
                  color = Color.parseColor("#FF9800"); // Naranja
                }
                
                String fechaInscripcion = item.getFechaInscripcion();
                String fechaFormateada = "";
                if (fechaInscripcion != null && fechaInscripcion.length() >= 10) {
                    String[] parts = fechaInscripcion.substring(0, 10).split("-");
                    if (parts.length == 3) {
                        fechaFormateada = "Inscripción: " + parts[2] + "/" + parts[1] + "/" + parts[0];
                    }
                }
                
                tempMaestra.add(new BusquedaItemUiModel(
                    item.getIdInscripcion(),
                    item,
                    nombre,
                    dni,
                    eventoCategoria,
                    estado,
                    color,
                    fechaFormateada
                ));
              }
              listaMaestra = tempMaestra;
              aplicarFiltro();
            }
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

    String nombre = r.getNombreCompleto();
    String dniSexo = String.format("DNI: %s | Sexo: %s", r.getDni(), r.getGenero());
    
    String localidad = r.getLocalidad();
    String email = r.getEmail();
    String telefono = r.getTelefono();
    
    String emergencia = "Contacto: " + r.getNombreContactoEmergencia();
    String telEmergencia = "Tel: " + r.getTelefonoEmergencia();

    String talle = item.getTalleRemera() != null ? item.getTalleRemera() : "-";
    String categoria = item.getNombreCategoria();
    String evento = item.getNombreEvento();
    String eventoCatTalle = "Evento: " + evento + "\nCat: " + categoria + " | Talle: " + talle;

    String estadoDelEvento = item.getEstadoEvento();
    boolean eventoCerrado = "finalizado".equalsIgnoreCase(estadoDelEvento) || "cancelado".equalsIgnoreCase(estadoDelEvento);

    String estadoPago = item.getEstadoPago();
    boolean yaEstaDeBaja = estadoPago != null && (
        "cancelado".equalsIgnoreCase(estadoPago) ||
        "cancelada".equalsIgnoreCase(estadoPago) ||
        "baja".equalsIgnoreCase(estadoPago) ||
        "reembolsado".equalsIgnoreCase(estadoPago) ||
        "rechazado".equalsIgnoreCase(estadoPago)
    );

    boolean puedeDarDeBaja = !eventoCerrado && !yaEstaDeBaja;

    // Reset feedback states for the new dialog
    feedbackDialogMensaje.setValue("");
    feedbackDialogVisibilidad.setValue(View.GONE);
    btnBajaHabilitado.setValue(true);

    detalleUiState.setValue(new DetalleUiState(
        itemUi.idInscripcion, nombre, dniSexo, localidad, email, telefono, 
        emergencia, telEmergencia, eventoCatTalle, puedeDarDeBaja
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
    listaMaestra.clear();
    resultadosUi.setValue(new ArrayList<>());
    estadoBusquedaVisibilidad.setValue(View.GONE);
    limpiarDetalle();
  }

  public void limpiarDetalle() {
    detalleUiState.setValue(null);
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
    public final String fechaInscripcionTexto;

    public BusquedaItemUiModel(int idInscripcion, BusquedaInscripcionResponse original, String nombreCompleto, String dni, String eventoCategoria, String estadoPagoTexto, int estadoPagoColor, String fechaInscripcionTexto) {
      this.idInscripcion = idInscripcion;
      this.original = original;
      this.nombreCompleto = nombreCompleto;
      this.dni = dni;
      this.eventoCategoria = eventoCategoria;
      this.estadoPagoTexto = estadoPagoTexto;
      this.estadoPagoColor = estadoPagoColor;
      this.fechaInscripcionTexto = fechaInscripcionTexto;
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