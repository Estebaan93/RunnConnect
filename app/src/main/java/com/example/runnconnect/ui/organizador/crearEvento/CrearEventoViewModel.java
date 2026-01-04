package com.example.runnconnect.ui.organizador.crearEvento;

import android.app.Application;
import android.util.Log;

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

  // Estados UI
  private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
  private final MutableLiveData<String> mensajeGlobal = new MutableLiveData<>();
  private final MutableLiveData<Boolean> esError = new MutableLiveData<>(false);
  private final MutableLiveData<Boolean> navegarAtras = new MutableLiveData<>(false);

  // Datos temporales
  private String fechaIso = ""; // yyyy-MM-dd
  private String horaIso = "";  // HH:mm

  // Variables para validación de fecha/hora lógica
  private int selYear, selMonth, selDay, selHour, selMinute;

  public CrearEventoViewModel(@NonNull Application application) {
    super(application);
    repositorio = new EventoRepositorio(application);
  }

  // Getters
  public LiveData<Boolean> getIsLoading() { return isLoading; }
  public LiveData<String> getMensajeGlobal() { return mensajeGlobal; }
  public LiveData<Boolean> getEsError() { return esError; }
  public LiveData<Boolean> getNavegarAtras() { return navegarAtras; }

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

  // Validacion visual opcional inmediata
  /*public void validarFechaFutura(int year, int month, int day) {
    Calendar selected = Calendar.getInstance();
    selected.set(year, month, day);

  }*/

  // --- LÓGICA DE NEGOCIO ---

  public void procesarYPublicar(String titulo, String descripcion, String lugar, String datosPago,
                                String distanciaRaw, String modalidad, String genero,
                                String edadMinRaw, String edadMaxRaw,
                                String catPrecio, String cupoRaw) {

    // 1. Limpieza de datos (trim)
    titulo = titulo.trim();
    lugar = lugar.trim();
    distanciaRaw = distanciaRaw.trim();

    // 2. Validaciones Obligatorias
    if (titulo.isEmpty() || lugar.isEmpty() || fechaIso.isEmpty() || horaIso.isEmpty() ||
      distanciaRaw.isEmpty() || catPrecio.isEmpty()) {
      mostrarMensaje("Faltan campos obligatorios (Título, Fecha, Distancia, Precio)", true);
      return;
    }

    // 3. Validar Fecha Futura
    Calendar fechaEvento = Calendar.getInstance();
    fechaEvento.set(selYear, selMonth, selDay, selHour, selMinute, 0);
    if (fechaEvento.before(Calendar.getInstance())) {
      mostrarMensaje("La fecha del evento debe ser futura", true);
      return;
    }

    // 4. Lógica de Valores por Defecto y Formateo

    // Distancia: El usuario pone "10", el sistema agrega "K"
    String nombreDistancia = distanciaRaw + "K";

    // Edades: Si viene vacío, aplicamos defaults
    int edadMin = 18;
    int edadMax = 99;

    // Parseo Numérico Seguro
    Integer cupoInt = null;
    BigDecimal precioDecimal = BigDecimal.ZERO;

    try {
      if (!cupoRaw.isEmpty()) cupoInt = Integer.parseInt(cupoRaw);
      precioDecimal = new BigDecimal(catPrecio);

      if (!edadMinRaw.isEmpty()) edadMin = Integer.parseInt(edadMinRaw);
      if (!edadMaxRaw.isEmpty()) edadMax = Integer.parseInt(edadMaxRaw);

    } catch (NumberFormatException e) {
      mostrarMensaje("Error en formato numérico (Revisa precio, cupo o edades)", true);
      return;
    }

    // Validación Lógica Edades
    if (edadMin > edadMax) {
      mostrarMensaje("La edad mínima no puede ser mayor a la máxima", true);
      return;
    }

    // 5. Construcción del Nombre de Categoría
    // Ej: "10K" + " " + "Trail" + optional "(Damas)"
    String nombreCategoriaFinal = nombreDistancia + " " + modalidad;
    if (!genero.equals("X")) {
      // Opcional: Agregar sufijo si es específico de sexo
      nombreCategoriaFinal += " (" + (genero.equals("F") ? "Femenino" : "Masculino") + ")";
    }

    // 6. Armado de Objetos Request
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

    // 7. Llamada al Repositorio
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
          mostrarMensaje("¡Evento creado exitosamente!", false);
          new android.os.Handler().postDelayed(() -> navegarAtras.setValue(true), 1500);
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
          JSONObject errors = json.getJSONObject("errors");
          if (errors.keys().hasNext()) {
            String key = errors.keys().next();
            errorMsg = key + ": " + errors.getJSONArray(key).getString(0);
          }
        }
      }
    } catch (Exception e) { errorMsg = "Error al leer respuesta"; }
    mostrarMensaje(errorMsg, true);
  }

  private void mostrarMensaje(String msg, boolean error) {
    esError.setValue(error);
    mensajeGlobal.setValue(msg);
  }



}
