package com.example.runnconnect.ui.organizador.misEventos;

import android.app.Application;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.runnconnect.data.conexion.ApiClient;
import com.example.runnconnect.data.conexion.ApiService;
import com.example.runnconnect.data.preferencias.SessionManager;
import com.example.runnconnect.data.response.EventoResumenResponse;
import com.example.runnconnect.data.response.EventosPaginadosResponse;

import java.util.ArrayList;
import java.util.List;
import android.os.Handler;
import android.view.View;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MisEventosViewModel extends AndroidViewModel {
  public static class EventoUI {
    public int idEvento;
    public String nombre;
    public String fecha;
    public String lugar;
    public String inscriptos;
    public String cupo;
    public String estadoTexto;
    public int estadoColorTexto;
    public int estadoColorFondo;
  }

  private final MutableLiveData<List<EventoUI>> listaEventos = new MutableLiveData<>();
  private final List<EventoResumenResponse> listaAcumulada = new ArrayList<>();
  private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
  private final ApiService apiService;
  private final SessionManager sessionManager;

  //control de paginancion de los items card mis eventos
  private int paginaActual = 1;
  private boolean esUltimaPagina = false;
  private boolean isLoadingMore = false; // Evita multiples llamadas al scrollear rapido

  public MisEventosViewModel(@NonNull Application application) {
    super(application);
    this.apiService = ApiClient.getApiService();
    this.sessionManager = new SessionManager(application);
  }

  public LiveData<List<EventoUI>> getListaEventos() { return listaEventos; }
  public LiveData<Boolean> getIsLoading() { return isLoading; }

  private final MutableLiveData<Integer> uiVisibilidadVacio = new MutableLiveData<>(View.GONE);
  private final MutableLiveData<Integer> uiVisibilidadRecycler = new MutableLiveData<>(View.GONE);
  
  public LiveData<Integer> getUiVisibilidadVacio() { return uiVisibilidadVacio; }
  public LiveData<Integer> getUiVisibilidadRecycler() { return uiVisibilidadRecycler; }

  private final MutableLiveData<String> uiMensajeTexto = new MutableLiveData<>("");
  private final MutableLiveData<Integer> uiMensajeVisibilidad = new MutableLiveData<>(View.GONE);
  private final MutableLiveData<Integer> uiMensajeColorTexto = new MutableLiveData<>(android.graphics.Color.BLACK);
  private final MutableLiveData<Integer> uiMensajeColorFondo = new MutableLiveData<>(android.graphics.Color.WHITE);

  public LiveData<String> getUiMensajeTexto() { return uiMensajeTexto; }
  public LiveData<Integer> getUiMensajeVisibilidad() { return uiMensajeVisibilidad; }
  public LiveData<Integer> getUiMensajeColorTexto() { return uiMensajeColorTexto; }
  public LiveData<Integer> getUiMensajeColorFondo() { return uiMensajeColorFondo; }

  // NUEVO: Metodo unificado para mostrar mensajes (exito o error)
  public void mostrarMensaje(String mensaje, boolean esError) {
    if (mensaje == null || mensaje.isEmpty()) return;

    uiMensajeTexto.setValue(mensaje);
    uiMensajeVisibilidad.setValue(View.VISIBLE);

    if (esError) {
      uiMensajeColorTexto.setValue(android.graphics.Color.parseColor("#C62828")); // Rojo
      uiMensajeColorFondo.setValue(android.graphics.Color.parseColor("#FFEBEE"));
    } else {
      uiMensajeColorTexto.setValue(android.graphics.Color.parseColor("#2E7D32")); // Verde
      uiMensajeColorFondo.setValue(android.graphics.Color.parseColor("#E8F5E9"));
    }

    // Timer para ocultar el mensaje automaticamente
    new Handler(Looper.getMainLooper()).postDelayed(() -> {
      uiMensajeVisibilidad.setValue(View.GONE);
      uiMensajeTexto.setValue("");
    }, 4000);

    if (!esError) {
      cargarEventos(true);
    }
  }

  //recibe los metodos y decide si cargar
  public void verificarScroll(int itemsVisibles, int totalItems, int primerItemVisible) {
    // 1. Validaciones de estado (Proteccion)
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
    if (isLoadingMore) return; // Si ya esta cargando, no hacemos nada

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
    String token = sessionManager.leerToken();
    if (token != null) {
      apiService.obtenerMisEventos("Bearer " + token, paginaActual, 10).enqueue(new Callback<EventosPaginadosResponse>() {
        @Override
        public void onResponse(Call<EventosPaginadosResponse> call, Response<EventosPaginadosResponse> response) {
          isLoading.setValue(false);
          isLoadingMore = false;

          if (response.isSuccessful() && response.body() != null) {
            EventosPaginadosResponse data= response.body();

            //verifica si llega la final para pedir datos
            if(paginaActual>= data.getTotalPaginas()){
              esUltimaPagina= true;
            }

            if (paginaActual == 1) {
              listaAcumulada.clear();
            }
            if (data.getEventos() != null) {
              listaAcumulada.addAll(data.getEventos());
            }

            //enviamos la lista al fragment mapeada
            List<EventoUI> uiList = mapearAEventoUI(listaAcumulada);
            
            if (uiList.isEmpty()) {
                uiVisibilidadVacio.setValue(View.VISIBLE);
                uiVisibilidadRecycler.setValue(View.GONE);
            } else {
                uiVisibilidadVacio.setValue(View.GONE);
                uiVisibilidadRecycler.setValue(View.VISIBLE);
            }
            
            listaEventos.setValue(uiList);
          } else {
            // Si el servidor devuelve error (ej: 401, 500)
            mostrarMensaje("Error del servidor: " + response.code(), true);
            Log.d("ErrorServidor", "onResponse: " + response.code());
          }
        }

        @Override
        public void onFailure(Call<EventosPaginadosResponse> call, Throwable t) {
          isLoading.setValue(false);
          isLoadingMore= false;
          if(paginaActual>1) paginaActual--; //si falla retrocedemos

          mostrarMensaje("Error de conexion: " + t.getMessage(), true);
          Log.e("MisEventosVM", "Error API: " + t.getMessage());
        }
      });
    } else {
      isLoading.setValue(false);
      isLoadingMore= false;
      mostrarMensaje("No hay sesion activa", true);
    }
  }

  private List<EventoUI> mapearAEventoUI(List<EventoResumenResponse> fuente) {
    List<EventoUI> resultado = new ArrayList<>();
    for (EventoResumenResponse evento : fuente) {
      EventoUI ui = new EventoUI();
      ui.idEvento = evento.getIdEvento();
      ui.nombre = (evento.getNombre() != null) ? evento.getNombre() : "Sin nombre";
      ui.fecha = (evento.getFechaHora() != null) ? "Fecha: " + evento.getFechaHora().replace("T", " ") : "Fecha: --/--/----";
      ui.lugar = (evento.getLugar() != null) ? "Lugar: " + evento.getLugar() : "Lugar: Sin ubicación";
      ui.inscriptos = "Inscriptos: " + evento.getInscriptosActuales();
      ui.cupo = "Cupo Total: " + (evento.getCupoTotal() != null ? String.valueOf(evento.getCupoTotal()) : "Ilimitado");
      
      String estado = (evento.getEstado() != null) ? evento.getEstado().toUpperCase() : "DESCONOCIDO";
      ui.estadoTexto = estado;
      
      switch (estado) {
        case "PUBLICADO":
          ui.estadoColorTexto = android.graphics.Color.parseColor("#2E7D32");
          ui.estadoColorFondo = android.graphics.Color.parseColor("#E8F5E9");
          break;
        case "SUSPENDIDO":
          ui.estadoColorTexto = android.graphics.Color.parseColor("#EF6C00");
          ui.estadoColorFondo = android.graphics.Color.parseColor("#FFF3E0");
          break;
        case "FINALIZADO":
          ui.estadoColorTexto = android.graphics.Color.parseColor("#616161");
          ui.estadoColorFondo = android.graphics.Color.parseColor("#F5F5F5");
          break;
        case "CANCELADO":
          ui.estadoColorTexto = android.graphics.Color.parseColor("#C62828");
          ui.estadoColorFondo = android.graphics.Color.parseColor("#FFEBEE");
          break;
        default:
          ui.estadoColorTexto = android.graphics.Color.BLACK;
          ui.estadoColorFondo = android.graphics.Color.WHITE;
      }
      resultado.add(ui);
    }
    return resultado;
  }
}