package com.example.runnconnect.ui.organizador.crearEvento;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.runnconnect.data.repositorio.EventoRepositorio;
import com.example.runnconnect.data.request.CrearCategoriaRequest;
import com.example.runnconnect.data.request.CrearEventoRequest;

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

  //resetear mapa
  public void resetearNav(){
    irAlMapa.setValue(null);
  }

  // Helpers UI
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

  // --- LÓGICA DE NEGOCIO ---
  public void procesarYContinuar(String titulo, String descripcion, String lugar, String datosPago,
                                 String distanciaRaw, String modalidad, String genero,
                                 String edadMinRaw, String edadMaxRaw,
                                 String catPrecio, String cupoRaw) {

    // 1. Limpieza (Trim)
    titulo = (titulo != null) ? titulo.trim() : "";
    lugar = (lugar != null) ? lugar.trim() : "";
    distanciaRaw = (distanciaRaw != null) ? distanciaRaw.trim() : "";

    // 2. Validaciones de Campos Obligatorios
    if (titulo.isEmpty()) { mostrarMensaje("El título es obligatorio", true); return; }
    if (lugar.isEmpty()) { mostrarMensaje("La ubicación es obligatoria", true); return; }
    if (fechaIso.isEmpty() || horaIso.isEmpty()) { mostrarMensaje("Debes seleccionar fecha y hora", true); return; }
    if (distanciaRaw.isEmpty()) { mostrarMensaje("Debes indicar la distancia (ej: 10)", true); return; }
    if (catPrecio.isEmpty()) { mostrarMensaje("Debes indicar el precio", true); return; }

    // 3. Validar Fecha Futura
    Calendar fechaEvento = Calendar.getInstance();
    fechaEvento.set(selYear, selMonth, selDay, selHour, selMinute, 0);
    if (fechaEvento.before(Calendar.getInstance())) {
      mostrarMensaje("La fecha debe ser futura", true);
      return;
    }

    // 4. Lógica de Negocio y Valores por Defecto

    // Distancia: Si el usuario puso "10", le agregamos "K". Si puso "10k", lo dejamos igual.
    String nombreDistancia = distanciaRaw.toUpperCase().contains("K") ? distanciaRaw : distanciaRaw + "K";

    // Edades: Si vienen vacías, asignamos defaults del negocio
    int edadMin = 18;
    int edadMax = 99;
    if (!edadMinRaw.trim().isEmpty()) {
      try { edadMin = Integer.parseInt(edadMinRaw.trim()); } catch (NumberFormatException e) {}
    }
    if (!edadMaxRaw.trim().isEmpty()) {
      try { edadMax = Integer.parseInt(edadMaxRaw.trim()); } catch (NumberFormatException e) {}
    }

    // Parseo seguro de números
    Integer cupoInt = null;
    BigDecimal precioDecimal = BigDecimal.ZERO;
    try {
      if (!cupoRaw.trim().isEmpty()) cupoInt = Integer.parseInt(cupoRaw.trim());
      precioDecimal = new BigDecimal(catPrecio.trim());
    } catch (NumberFormatException e) {
      mostrarMensaje("Formato de precio o cupo inválido", true);
      return;
    }

    if (edadMin > edadMax) {
      mostrarMensaje("La edad mínima no puede ser mayor a la máxima", true);
      return;
    }

    // 5. Construcción del Nombre de Categoría (Ej: "10K Trail")
    String nombreCategoriaFinal = nombreDistancia + " " + modalidad;
    if (!genero.equals("X")) {
      nombreCategoriaFinal += " (" + (genero.equals("F") ? "Damas" : "Caballeros") + ")";
    }

    // 6. Armado del Request
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

    String fechaHoraFinal = fechaIso + "T" + horaIso + ":00";
    CrearEventoRequest request = new CrearEventoRequest(
      titulo, descripcion, fechaHoraFinal, lugar,
      cupoInt, null, datosPago, listaCategorias
    );

    // 7. Llamada a API
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
}