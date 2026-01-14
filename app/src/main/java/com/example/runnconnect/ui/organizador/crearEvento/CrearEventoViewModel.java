package com.example.runnconnect.ui.organizador.crearEvento;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.runnconnect.data.repositorio.EventoRepositorio;
import com.example.runnconnect.data.request.ActualizarEventoRequest;
import com.example.runnconnect.data.request.CrearCategoriaRequest;
import com.example.runnconnect.data.request.CrearEventoRequest;
import com.example.runnconnect.data.response.EventoDetalleResponse;

import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CrearEventoViewModel extends AndroidViewModel {
  private final EventoRepositorio repositorio;

  // Estados
  private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
  private final MutableLiveData<String> mensajeGlobal = new MutableLiveData<>();
  private final MutableLiveData<Boolean> esError = new MutableLiveData<>(false);
  private final MutableLiveData<Integer> irAlMapa = new MutableLiveData<>(null);

  //habilita edicion
  private final MutableLiveData<EventoDetalleResponse> eventoCargado= new MutableLiveData<>();
  private boolean esModoEdicion= false;
  private int idEventoEdicion=0;

  // Datos temporales
  private String fechaIso = "";
  private String horaIso = "";
  private int selYear, selMonth, selDay, selHour, selMinute;

  public CrearEventoViewModel(@NonNull Application application) {
    super(application);
    repositorio = new EventoRepositorio(application);
  }

  // Getters
  public LiveData<Boolean> getIsLoading() { return isLoading; }
  public LiveData<String> getMensajeGlobal() { return mensajeGlobal; }
  public LiveData<Boolean> getEsError() { return esError; }
  public LiveData<Integer> getIrAlMapa() { return irAlMapa; }

  public LiveData<EventoDetalleResponse> getEventoCargado() { return eventoCargado; }

  //resetear mapa
  public void resetearNav(){
    irAlMapa.setValue(null);
  }

  // helpers UI
  public String procesarFecha(int year, int month, int day) {
    this.selYear = year; this.selMonth = month; this.selDay = day;
    fechaIso = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, day);
    return String.format(Locale.getDefault(), "%02d/%02d/%04d", day, month + 1, year);
  }

  public String procesarHora(int hour, int minute) {
    this.selHour = hour; this.selMinute = minute;
    horaIso = String.format(Locale.getDefault(), "%02d:%02d", hour, minute);
    return horaIso;
  }

  // Mtodo auxiliar para setear fecha interna cuando cargamos datos existentes
  public void setFechaHoraInterna(String fechaIso, String horaIso) {
    this.fechaIso = fechaIso;
    this.horaIso = horaIso;
    // Nota: No seteamos selYear/selMonth aquí por simplicidad,
    // pero validamos que fechaIso no esté vacía al guardar.
  }

  // --- 1. DETECTAR SI ES EDICIÓN Y CARGAR DATOS ---
  public void verificarModoEdicion(int idEvento) {
    if (idEvento > 0) {
      esModoEdicion = true;
      idEventoEdicion = idEvento;
      isLoading.setValue(true);

      repositorio.obtenerDetalleEvento(idEvento, new Callback<EventoDetalleResponse>() {
        @Override
        public void onResponse(Call<EventoDetalleResponse> call, Response<EventoDetalleResponse> response) {
          isLoading.setValue(false);
          if (response.isSuccessful() && response.body() != null) {
            eventoCargado.setValue(response.body());
          } else {
            mostrarMensaje("Error al cargar datos del evento", true);
          }
        }
        @Override
        public void onFailure(Call<EventoDetalleResponse> call, Throwable t) {
          isLoading.setValue(false);
          mostrarMensaje("Error de conexión", true);
        }
      });
    }
  }



  // --- LÓGICA DE NEGOCIO FUSIONADA (CREAR + EDITAR) ---
  public void procesarYContinuar(String titulo, String descripcion, String lugar, String datosPago,
                                 String distanciaRaw, String modalidad, String genero,
                                 String edadMinRaw, String edadMaxRaw,
                                 String catPrecio, String cupoRaw) {

    // 1. Limpieza Común (Trim)
    titulo = (titulo != null) ? titulo.trim() : "";
    lugar = (lugar != null) ? lugar.trim() : "";

    // 2. Validaciones Comunes (Obligatorias para ambos casos)
    if (titulo.isEmpty()) { mostrarMensaje("El título es obligatorio", true); return; }
    if (lugar.isEmpty()) { mostrarMensaje("La ubicación es obligatoria", true); return; }

    // Validar que se haya seleccionado fecha y hora (ya sea por picker o cargada del backend)
    if (fechaIso.isEmpty() || horaIso.isEmpty()) {
      mostrarMensaje("Debes seleccionar fecha y hora", true);
      return;
    }

    // 3. Validar Fecha Futura (Común)
    // Solo validamos si el usuario usó los pickers (selYear != 0).
    // Si viene de edición y no tocó la fecha, asumimos que es válida o la está cambiando.
    if (selYear != 0) {
      Calendar fechaEvento = Calendar.getInstance();
      fechaEvento.set(selYear, selMonth, selDay, selHour, selMinute, 0);
      if (fechaEvento.before(Calendar.getInstance())) {
        mostrarMensaje("La fecha debe ser futura", true);
        return;
      }
    }

    // 4. Parseo seguro de Cupo (Común)
    Integer cupoInt = null;
    try {
      if (cupoRaw != null && !cupoRaw.trim().isEmpty()) cupoInt = Integer.parseInt(cupoRaw.trim());
    } catch (NumberFormatException e) {
      mostrarMensaje("Formato de cupo inválido", true);
      return;
    }

    // Construcción de la fecha final ISO
    String fechaHoraFinal = fechaIso + "T" + horaIso + ":00";

    // =================================================================
    // RAMA 1: MODO EDICIÓN (PUT)
    // =================================================================
    if (esModoEdicion) {
      // En edición IGNORAMOS campos de categoría (precio, distancia, etc.)
      ActualizarEventoRequest request = new ActualizarEventoRequest();
      request.setNombre(titulo);
      request.setDescripcion(descripcion);
      request.setFechaHora(fechaHoraFinal);
      request.setLugar(lugar);
      request.setCupoTotal(cupoInt);
      request.setDatosPago(datosPago);
      // request.setUrlPronosticoClima(...) // Si lo agregas a futuro

      ejecutarActualizacionAPI(request);
      return; // ¡IMPORTANTE! Salimos aquí para no ejecutar lógica de creación
    }

    // =================================================================
    // RAMA 2: MODO CREACIÓN (POST) - (TU LÓGICA ORIGINAL)
    // =================================================================

    // Validaciones exclusivas de creación (Categorías y Precios)
    distanciaRaw = (distanciaRaw != null) ? distanciaRaw.trim() : "";
    if (distanciaRaw.isEmpty()) { mostrarMensaje("Debes indicar la distancia (ej: 10)", true); return; }
    if (catPrecio == null || catPrecio.trim().isEmpty()) { mostrarMensaje("Debes indicar el precio", true); return; }

    // Parseo de precio
    BigDecimal precioDecimal = BigDecimal.ZERO;
    try {
      precioDecimal = new BigDecimal(catPrecio.trim());
    } catch (Exception e) {
      mostrarMensaje("Formato de precio inválido", true);
      return;
    }

    // Distancia: Lógica de la "K"
    String nombreDistancia = distanciaRaw.toUpperCase().contains("K") ? distanciaRaw : distanciaRaw + "K";

    // Edades: Defaults del negocio
    int edadMin = 18;
    int edadMax = 99;
    if (edadMinRaw != null && !edadMinRaw.trim().isEmpty()) {
      try { edadMin = Integer.parseInt(edadMinRaw.trim()); } catch (NumberFormatException e) {}
    }
    if (edadMaxRaw != null && !edadMaxRaw.trim().isEmpty()) {
      try { edadMax = Integer.parseInt(edadMaxRaw.trim()); } catch (NumberFormatException e) {}
    }

    if (edadMin > edadMax) {
      mostrarMensaje("La edad mínima no puede ser mayor a la máxima", true);
      return;
    }

    // Construccion del Nombre de Categoría
    String nombreCategoriaFinal = nombreDistancia + " " + modalidad;
    if (!genero.equals("X")) {
      nombreCategoriaFinal += " (" + (genero.equals("F") ? "Damas" : "Caballeros") + ")";
    }

    // Armado del Request Completo
    List<CrearCategoriaRequest> listaCategorias = new ArrayList<>();
    CrearCategoriaRequest cat = new CrearCategoriaRequest(
      nombreCategoriaFinal,
      precioDecimal,
      cupoInt
    );
    cat.setEdadMinima(edadMin);
    cat.setEdadMaxima(edadMax);
    cat.setGenero(genero);

    listaCategorias.add(cat);

    CrearEventoRequest request = new CrearEventoRequest(
      titulo, descripcion, fechaHoraFinal, lugar,
      cupoInt, null, datosPago, listaCategorias
    );

    // Llamada a API Creacion
    ejecutarLlamadaAPI(request);
  }

  private void ejecutarLlamadaAPI(CrearEventoRequest request) {
    isLoading.setValue(true);
    mostrarMensaje(null, false);

    repositorio.crearEvento(request, new Callback<ResponseBody>() {
      @Override
      public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
        isLoading.setValue(false);
        if (response.isSuccessful()) {
          try {
            String raw = response.body().string();
            JSONObject json = new JSONObject(raw);

            // Extraer el ID para ir al mapa
            if (json.has("evento")) {
              int idNuevo = json.getJSONObject("evento").getInt("idEvento");
              mostrarMensaje("¡Guardado! Cargando mapa...", false);
              // Retardo visual breve antes de cambiar de pantalla
              new android.os.Handler().postDelayed(() -> irAlMapa.setValue(idNuevo), 800);
            } else {
              mostrarMensaje("Evento creado, pero sin ID retornado.", true);
            }
          } catch (Exception e) {
            mostrarMensaje("Error al procesar respuesta: " + e.getMessage(), true);
          }
        } else {
          manejarErrorApi(response);
        }
      }

      @Override
      public void onFailure(Call<ResponseBody> call, Throwable t) {
        isLoading.setValue(false);
        mostrarMensaje("Fallo de conexión: " + t.getMessage(), true);
      }
    });
  }

  private void manejarErrorApi(Response<ResponseBody> response) {
    String errorMsg = "Error desconocido";
    try {
      if (response.errorBody() != null) {
        String raw = response.errorBody().string();
        JSONObject json = new JSONObject(raw);
        if (json.has("message")) errorMsg = json.getString("message");
        else if (json.has("errors")) {
          // Manejo de errores de validación .NET
          JSONObject errors = json.getJSONObject("errors");
          if (errors.keys().hasNext()) {
            String key = errors.keys().next();
            errorMsg = key + ": " + errors.getJSONArray(key).getString(0);
          }
        }
      }
    } catch (Exception e) { errorMsg = "Error de lectura"; }
    mostrarMensaje(errorMsg, true);
  }

  private void mostrarMensaje(String msg, boolean error) {
    esError.setValue(error);
    mensajeGlobal.setValue(msg);
  }

  // Mtodo para EDITAR (PUT)
  private void ejecutarActualizacionAPI(ActualizarEventoRequest request) {
    isLoading.setValue(true);
    repositorio.actualizarEvento(idEventoEdicion, request, new Callback<ResponseBody>() {
      @Override
      public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
        isLoading.setValue(false);
        if (response.isSuccessful()) {
          mostrarMensaje("¡Evento reprogramado/actualizado!", false);
          // Volver al mapa/detalle tras breve pausa
          new android.os.Handler().postDelayed(() -> irAlMapa.setValue(idEventoEdicion), 800);
        } else {
          manejarErrorApi(response);
        }
      }
      @Override
      public void onFailure(Call<ResponseBody> call, Throwable t) {
        isLoading.setValue(false);
        mostrarMensaje("Fallo de conexión", true);
      }
    });
  }


}