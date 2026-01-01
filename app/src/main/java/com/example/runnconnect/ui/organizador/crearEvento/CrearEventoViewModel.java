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
  public void validarFechaFutura(int year, int month, int day) {
    Calendar selected = Calendar.getInstance();
    selected.set(year, month, day);

  }

  public void publicarEvento(String titulo, String descripcion, String lugar,
                             String datosPago, String catNombre, String catPrecio, String cupo) {
    // 1. Validaciones Básicas
    if (titulo.isEmpty() || lugar.isEmpty() || fechaIso.isEmpty() || horaIso.isEmpty() ||
      catNombre.isEmpty() || catPrecio.isEmpty()) {
      mostrarMensaje("Complete todos los campos obligatorios", true);
      return;
    }

    // 2. Validación Fecha Futura
    Calendar fechaEvento = Calendar.getInstance();
    fechaEvento.set(selYear, selMonth, selDay, selHour, selMinute, 0);
    Calendar ahora = Calendar.getInstance();
    if (fechaEvento.before(ahora)) {
      mostrarMensaje("La fecha y hora deben ser futuras.", true);
      return;
    }

    // 3. Parsear Números
    Integer cupoInt = null;
    BigDecimal precioDecimal = BigDecimal.ZERO;
    try {
      if (!cupo.isEmpty()) cupoInt = Integer.parseInt(cupo);
      precioDecimal = new BigDecimal(catPrecio);
    } catch (NumberFormatException e) {
      mostrarMensaje("El cupo o precio no son válidos", true);
      return;
    }

    // 4. Crear Lista de Categorías
    List<CrearCategoriaRequest> listaCategorias = new ArrayList<>();
    listaCategorias.add(new CrearCategoriaRequest(
      catNombre,      // Ej: "10K General"
      precioDecimal,  // Ej: 5000.00
      cupoInt         // Usamos el cupo total para la categoría por defecto
    ));

    // 5. Preparar Request
    String fechaHoraFinal = fechaIso + "T" + horaIso + ":00";

    CrearEventoRequest request = new CrearEventoRequest(
      titulo,
      descripcion,
      fechaHoraFinal,
      lugar,
      cupoInt,
      null,
      datosPago,      // CBU / Alias
      listaCategorias // Lista de categorías
    );

    // 6. Llamada API
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
          String errorMsg = "Error desconocido";
          try {
            if (response.errorBody() != null) {
              String raw = response.errorBody().string();
              Log.e("API_ERROR", raw); // Log para debug

              JSONObject json = new JSONObject(raw);

              if (json.has("message")) {
                errorMsg = json.getString("message");
              } else if (json.has("errors")) {
                JSONObject errors = json.getJSONObject("errors");
                if (errors.keys().hasNext()) {
                  String key = errors.keys().next();
                  errorMsg = key + ": " + errors.getJSONArray(key).getString(0);
                }
              }
            }
          } catch (Exception e) {
            errorMsg = "Error: " + e.getMessage();
          }
          mostrarMensaje(errorMsg, true);
        }
      }

      @Override
      public void onFailure(Call<ResponseBody> call, Throwable t) {
        isLoading.setValue(false);
        mostrarMensaje("Fallo de conexión: " + t.getMessage(), true);
      }
    });
  }

  private void mostrarMensaje(String msg, boolean error) {
    esError.setValue(error);
    mensajeGlobal.setValue(msg);
  }



}
