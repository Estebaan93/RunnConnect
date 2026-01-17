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
import com.example.runnconnect.data.response.CategoriaResponse;
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

  // --- LIVE DATA PARA LA VISTA (OUTPUTS) ---
  private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
  private final MutableLiveData<String> mensajeGlobal = new MutableLiveData<>();

  // Campos de Texto (Para rellenar la UI)
  private final MutableLiveData<String> titulo = new MutableLiveData<>();
  private final MutableLiveData<String> descripcion = new MutableLiveData<>();
  private final MutableLiveData<String> ubicacion = new MutableLiveData<>();
  private final MutableLiveData<String> fechaDisplay = new MutableLiveData<>();
  private final MutableLiveData<String> horaDisplay = new MutableLiveData<>();
  private final MutableLiveData<String> datosPago = new MutableLiveData<>();
  private final MutableLiveData<String> cupo = new MutableLiveData<>();

  // Campos de Categoría (Para rellenar la UI)
  private final MutableLiveData<String> distancia = new MutableLiveData<>();
  private final MutableLiveData<String> precio = new MutableLiveData<>();
  private final MutableLiveData<String> edadMin = new MutableLiveData<>();
  private final MutableLiveData<String> edadMax = new MutableLiveData<>();

  // Selecciones para Spinners (El VM decide qué texto debe seleccionarse)
  private final MutableLiveData<String> seleccionModalidad = new MutableLiveData<>();
  private final MutableLiveData<String> seleccionGenero = new MutableLiveData<>();

  // Estados de Habilitación de UI
  private final MutableLiveData<Boolean> esModoEdicionUI = new MutableLiveData<>(false);
  private final MutableLiveData<Boolean> camposBloqueados = new MutableLiveData<>(false);

  // Navegación (1 = Mapa, 2 = Atrás)
  private final MutableLiveData<Integer> navegacionExito = new MutableLiveData<>(0);

  // --- VARIABLES INTERNAS (ESTADO) ---
  private boolean esEdicion = false;
  private int idEventoEdicion = 0;
  private String fechaIso = "";
  private String horaIso = "";
  private int selYear, selMonth, selDay, selHour, selMinute;

  public CrearEventoViewModel(@NonNull Application application) {
    super(application);
    repositorio = new EventoRepositorio(application);
  }

  // --- GETTERS ---
  public LiveData<Boolean> getIsLoading() { return isLoading; }
  public LiveData<String> getMensajeGlobal() { return mensajeGlobal; }
  public LiveData<String> getTitulo() { return titulo; }
  public LiveData<String> getDescripcion() { return descripcion; }
  public LiveData<String> getUbicacion() { return ubicacion; }
  public LiveData<String> getFechaDisplay() { return fechaDisplay; }
  public LiveData<String> getHoraDisplay() { return horaDisplay; }
  public LiveData<String> getDatosPago() { return datosPago; }
  public LiveData<String> getCupo() { return cupo; }
  public LiveData<String> getDistancia() { return distancia; }
  public LiveData<String> getPrecio() { return precio; }
  public LiveData<String> getEdadMin() { return edadMin; }
  public LiveData<String> getEdadMax() { return edadMax; }
  public LiveData<String> getSeleccionModalidad() { return seleccionModalidad; }
  public LiveData<String> getSeleccionGenero() { return seleccionGenero; }
  public LiveData<Boolean> getEsModoEdicionUI() { return esModoEdicionUI; }
  public LiveData<Boolean> getCamposBloqueados() { return camposBloqueados; }
  public LiveData<Integer> getNavegacionExito() { return navegacionExito; }

  public void resetearNavegacion() { navegacionExito.setValue(0); }

  // --- INPUTS DE LA VISTA (INTERACCIÓN USUARIO) ---

  public void onChipDistanciaSelected(String textoChip) {
    if (textoChip != null) {
      // Lógica de negocio: Quitar la "K" del chip para ponerlo en el EditText
      distancia.setValue(textoChip.replace("K", "").trim());
    }
  }

  public void onFechaSelected(int year, int month, int day) {
    this.selYear = year; this.selMonth = month; this.selDay = day;
    // Formato ISO para API
    fechaIso = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, day);
    // Formato legible para UI
    fechaDisplay.setValue(String.format(Locale.getDefault(), "%02d/%02d/%04d", day, month + 1, year));
  }

  public void onHoraSelected(int hour, int minute) {
    this.selHour = hour; this.selMinute = minute;
    // Formato para API y UI
    horaIso = String.format(Locale.getDefault(), "%02d:%02d", hour, minute);
    horaDisplay.setValue(horaIso);
  }

  // --- LÓGICA DE CARGA (EDICIÓN) ---

  public void verificarModoEdicion(int idEvento) {
    if (idEvento <= 0) return;

    esEdicion = true;
    idEventoEdicion = idEvento;
    esModoEdicionUI.setValue(true); // Activa flags de UI de edición
    isLoading.setValue(true);

    repositorio.obtenerDetalleEvento(idEvento, new Callback<EventoDetalleResponse>() {
      @Override
      public void onResponse(Call<EventoDetalleResponse> call, Response<EventoDetalleResponse> response) {
        isLoading.setValue(false);
        if (response.isSuccessful() && response.body() != null) {
          mapearEventoAUI(response.body());
        } else {
          mensajeGlobal.setValue("No se pudieron cargar los datos.");
        }
      }
      @Override
      public void onFailure(Call<EventoDetalleResponse> call, Throwable t) {
        isLoading.setValue(false);
        mensajeGlobal.setValue("Error de conexión al cargar evento.");
      }
    });
  }

  // Transformación: Modelo de Datos -> Estado de Vista
  private void mapearEventoAUI(EventoDetalleResponse evento) {
    // 1. Campos directos
    titulo.setValue(evento.getNombre());
    descripcion.setValue(evento.getDescripcion());
    ubicacion.setValue(evento.getLugar());
    camposBloqueados.setValue(true); // Activar bloqueo de integridad

    if (evento.getDatosPago() != null) datosPago.setValue(evento.getDatosPago());
    if (evento.getCupoTotal() != null) cupo.setValue(String.valueOf(evento.getCupoTotal()));

    // 2. Parseo Fecha (Try-Catch encapsulado aquí)
    if (evento.getFechaHora() != null && evento.getFechaHora().contains("T")) {
      try {
        String[] partes = evento.getFechaHora().split("T");
        this.fechaIso = partes[0];
        this.horaIso = partes[1].substring(0, 5);

        String[] f = fechaIso.split("-");
        fechaDisplay.setValue(f[2] + "/" + f[1] + "/" + f[0]); // dd/MM/yyyy
        horaDisplay.setValue(horaIso);
      } catch (Exception e) { e.printStackTrace(); }
    }

    // 3. Parseo Categoría (Separar nombre y mapear género)
    if (evento.getCategorias() != null && !evento.getCategorias().isEmpty()) {
      CategoriaResponse cat = evento.getCategorias().get(0);

      precio.setValue(String.valueOf(cat.getPrecio()));
      edadMin.setValue(String.valueOf(cat.getEdadMinima()));
      edadMax.setValue(String.valueOf(cat.getEdadMaxima()));

      // Mapeo de género (DB Code -> UI String)
      // Esto asegura que el Spinner seleccione el texto correcto
      String g = cat.getGenero();
      if ("F".equals(g)) seleccionGenero.setValue("Femenino");
      else if ("M".equals(g)) seleccionGenero.setValue("Masculino");
      else seleccionGenero.setValue("Mixto / General"); // Default X

      // Separar "10K Trail" -> Distancia: 10, Modalidad: Trail
      String nombreCat = cat.getNombre();
      if (nombreCat != null) {
        String[] partes = nombreCat.split(" ");
        if (partes.length > 0) distancia.setValue(partes[0].replace("K", ""));
        if (partes.length > 1) seleccionModalidad.setValue(partes[1]);
        else distancia.setValue(nombreCat);
      }
    }
  }

  // --- LÓGICA DE GUARDADO ---

  public void guardarEvento(String tituloIn, String descripcionIn, String lugarIn, String datosPagoIn,
                            String distanciaIn, String modalidadIn, String generoIn,
                            String edadMinIn, String edadMaxIn, String precioIn, String cupoIn) {

    // 1. Validaciones
    if (tituloIn == null || tituloIn.trim().isEmpty()) { mensajeGlobal.setValue("El título es obligatorio"); return; }
    if (lugarIn == null || lugarIn.trim().isEmpty()) { mensajeGlobal.setValue("La ubicación es obligatoria"); return; }
    if (fechaIso.isEmpty() || horaIso.isEmpty()) { mensajeGlobal.setValue("Selecciona fecha y hora"); return; }

    Integer cupoInt = null;
    try {
      if (cupoIn != null && !cupoIn.trim().isEmpty()) cupoInt = Integer.parseInt(cupoIn.trim());
    } catch (NumberFormatException e) { mensajeGlobal.setValue("Cupo inválido"); return; }

    String fechaHoraFinal = fechaIso + "T" + horaIso + ":00";

    // 2. RAMA EDICIÓN
    if (esEdicion) {
      ActualizarEventoRequest request = new ActualizarEventoRequest();
      request.setNombre(tituloIn);
      request.setDescripcion(descripcionIn);
      request.setFechaHora(fechaHoraFinal);
      request.setLugar(lugarIn);
      request.setCupoTotal(cupoInt);
      request.setDatosPago(datosPagoIn);

      ejecutarActualizacion(request);
      return;
    }

    // 3. RAMA CREACIÓN
    if (distanciaIn == null || distanciaIn.trim().isEmpty()) { mensajeGlobal.setValue("Falta la distancia"); return; }
    if (precioIn == null || precioIn.trim().isEmpty()) { mensajeGlobal.setValue("Falta el precio"); return; }

    BigDecimal precioDec;
    try { precioDec = new BigDecimal(precioIn.trim()); } catch (Exception e) { mensajeGlobal.setValue("Precio inválido"); return; }

    // Formateo de datos
    String nombreDistancia = distanciaIn.toUpperCase().contains("K") ? distanciaIn : distanciaIn + "K";

    // Mapeo inverso de Género (UI String -> API Code)
    String generoApi = "X";
    if (generoIn.contains("Femenino")) generoApi = "F";
    else if (generoIn.contains("Masculino")) generoApi = "M";

    CrearCategoriaRequest cat = new CrearCategoriaRequest(
      nombreDistancia + " " + modalidadIn + (generoApi.equals("X") ? "" : " (" + generoIn + ")"),
      precioDec, cupoInt
    );
    cat.setGenero(generoApi);
    try { cat.setEdadMinima(Integer.parseInt(edadMinIn)); } catch(Exception e) {}
    try { cat.setEdadMaxima(Integer.parseInt(edadMaxIn)); } catch(Exception e) {}

    List<CrearCategoriaRequest> lista = new ArrayList<>();
    lista.add(cat);

    CrearEventoRequest request = new CrearEventoRequest(
      tituloIn, descripcionIn, fechaHoraFinal, lugarIn, cupoInt, null, datosPagoIn, lista
    );

    ejecutarCreacion(request);
  }

  private void ejecutarActualizacion(ActualizarEventoRequest request) {
    isLoading.setValue(true);
    repositorio.actualizarEvento(idEventoEdicion, request, new Callback<ResponseBody>() {
      @Override
      public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
        isLoading.setValue(false);
        if (response.isSuccessful()) {
          mensajeGlobal.setValue("¡Actualizado correctamente!");
          // Código 2 = Volver Atrás
          new android.os.Handler().postDelayed(() -> navegacionExito.setValue(2), 800);
        } else {
          manejarErrorApi(response);
        }
      }
      @Override
      public void onFailure(Call<ResponseBody> call, Throwable t) {
        isLoading.setValue(false);
        mensajeGlobal.setValue("Error de conexión");
      }
    });
  }

  private void ejecutarCreacion(CrearEventoRequest request) {
    isLoading.setValue(true);
    repositorio.crearEvento(request, new Callback<ResponseBody>() {
      @Override
      public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
        isLoading.setValue(false);
        if (response.isSuccessful()) {
          try {
            String raw = response.body().string();
            JSONObject json = new JSONObject(raw);
            if (json.has("evento")) {
              int idNuevo = json.getJSONObject("evento").getInt("idEvento");
              mensajeGlobal.setValue("¡Creado! Configurando mapa...");
              // Código = ID del nuevo evento (para ir al mapa)
              navegacionExito.setValue(idNuevo);
            }
          } catch (Exception e) { mensajeGlobal.setValue("Evento creado, error al leer ID"); }
        } else {
          manejarErrorApi(response);
        }
      }
      @Override
      public void onFailure(Call<ResponseBody> call, Throwable t) {
        isLoading.setValue(false);
        mensajeGlobal.setValue("Error de conexión");
      }
    });
  }

  private void manejarErrorApi(Response<ResponseBody> response) {
    String msg = "Error del servidor: " + response.code();
    try {
      if (response.errorBody() != null) {
        JSONObject json = new JSONObject(response.errorBody().string());
        if (json.has("message")) msg = json.getString("message");
      }
    } catch (Exception e) { }
    mensajeGlobal.setValue(msg);
  }
}