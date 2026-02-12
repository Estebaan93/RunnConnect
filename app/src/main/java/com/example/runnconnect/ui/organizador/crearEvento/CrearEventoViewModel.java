package com.example.runnconnect.ui.organizador.crearEvento;

import android.app.Application;
import android.view.View;

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
import java.util.List;
import java.util.Locale;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CrearEventoViewModel extends AndroidViewModel {
  private final EventoRepositorio repositorio;

  // --- ESTADOS GENERALES ---
  private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
  private final MutableLiveData<String> mensajeGlobal = new MutableLiveData<>(); // Para errores generales (API, Conexión)

  // --- ERRORES DE CAMPOS ESPECIFICOS (Para setError y focus) ---
  private final MutableLiveData<String> errorTitulo = new MutableLiveData<>();
  private final MutableLiveData<String> errorUbicacion = new MutableLiveData<>();
  private final MutableLiveData<String> errorDistancia = new MutableLiveData<>();
  private final MutableLiveData<String> errorPrecio = new MutableLiveData<>();
  private final MutableLiveData<String> errorCupo = new MutableLiveData<>();
  // Fecha y Hora no tienen setError nativo usaremos mensajeGlobal

  // --- DATOS DE LA UI (Binding) ---
  private final MutableLiveData<String> titulo = new MutableLiveData<>();
  private final MutableLiveData<String> descripcion = new MutableLiveData<>();
  private final MutableLiveData<String> ubicacion = new MutableLiveData<>();
  private final MutableLiveData<String> fechaDisplay = new MutableLiveData<>();
  private final MutableLiveData<String> horaDisplay = new MutableLiveData<>();
  private final MutableLiveData<String> datosPago = new MutableLiveData<>();
  private final MutableLiveData<String> cupo = new MutableLiveData<>();

  //datos de categorias
  private final MutableLiveData<String> distancia = new MutableLiveData<>();
  private final MutableLiveData<String> precio = new MutableLiveData<>();
  private final MutableLiveData<String> edadMin = new MutableLiveData<>();
  private final MutableLiveData<String> edadMax = new MutableLiveData<>();

  //selectores de modalidad
  private final MutableLiveData<String> seleccionModalidad = new MutableLiveData<>();
  private final MutableLiveData<String> seleccionGenero = new MutableLiveData<>();

  private final MutableLiveData<String> tipoEventoGlobal = new MutableLiveData<>();

  // --- CONTROL UI ---
  private final MutableLiveData<String> uiTituloPagina = new MutableLiveData<>();
  private final MutableLiveData<String> uiTextoBoton = new MutableLiveData<>();
  private final MutableLiveData<String> uiTextoAviso = new MutableLiveData<>();
  private final MutableLiveData<Integer> uiVisibilidadCamposExtra = new MutableLiveData<>();
  private final MutableLiveData<Integer> uiVisibilidadChips = new MutableLiveData<>();
  private final MutableLiveData<Boolean> uiCamposHabilitados = new MutableLiveData<>();
  private final MutableLiveData<Integer> navegacionExito = new MutableLiveData<>(0);
  private final List<CrearCategoriaRequest> categoriasTemporales= new ArrayList<>();
  private final MutableLiveData<List<CrearCategoriaRequest>> categoriasLive = new MutableLiveData<>(new ArrayList<>());


  // Estado Interno
  private boolean esEdicion = false;
  private int idEventoEdicion = 0;
  private String fechaIso = "";
  private String horaIso = "";
  private int selYear, selMonth, selDay, selHour, selMinute;

  public CrearEventoViewModel(@NonNull Application application) {
    super(application);
    repositorio = new EventoRepositorio(application);
    configurarModoCrear();
  }

  // --- GETTERS ---
  public LiveData<Boolean> getIsLoading() { return isLoading; }
  public LiveData<String> getMensajeGlobal() { return mensajeGlobal; }

  // Getters de Errores
  public LiveData<String> getErrorTitulo() { return errorTitulo; }
  public LiveData<String> getErrorUbicacion() { return errorUbicacion; }
  public LiveData<String> getErrorDistancia() { return errorDistancia; }
  public LiveData<String> getErrorPrecio() { return errorPrecio; }
  public LiveData<String> getErrorCupo() { return errorCupo; }

  // Getters de Datos
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

  public LiveData<String> getUiTituloPagina() { return uiTituloPagina; }
  public LiveData<String> getUiTextoBoton() { return uiTextoBoton; }
  public LiveData<String> getUiTextoAviso() { return uiTextoAviso; }
  public LiveData<Integer> getUiVisibilidadCamposExtra() { return uiVisibilidadCamposExtra; }
  public LiveData<Integer> getUiVisibilidadChips() { return uiVisibilidadChips; }
  public LiveData<Boolean> getUiCamposHabilitados() { return uiCamposHabilitados; }
  public LiveData<Integer> getNavegacionExito() { return navegacionExito; }

  //Getter de categoria
  public LiveData<List<CrearCategoriaRequest>> getCategoriasLive() { return categoriasLive; }

  public LiveData<String> getTipoEventoGlobal() { return tipoEventoGlobal; }

  //agregar categoria
  public boolean agregarCategoriaLocal(String distanciaIn, String generoIn,
                                    String edadMinIn, String edadMaxIn, String precioIn, String cupoEvento) {
    // 1. Validaciones basicas de la categoría
    if (distanciaIn.trim().isEmpty()) {
      errorDistancia.setValue("Indica la distancia");
      return false;
    }
    if (precioIn.trim().isEmpty()) {
      errorPrecio.setValue("Indica el precio");
      return false;
    }

    BigDecimal precioDec;
    try {
      precioDec = new BigDecimal(precioIn.trim());
    } catch (Exception e) {
      errorPrecio.setValue("Precio inválido");
      return false;
    }

    // 2. Construir Nombre y Datos
    String nombreDistancia = distanciaIn.toUpperCase().contains("K") ? distanciaIn : distanciaIn + "K";

    // Mapeo Genero
    String generoApi = "X";
    String generoNombre = "";
    if (generoIn.contains("Femenino")) { generoApi = "F"; generoNombre = "(Fem)"; }
    else if (generoIn.contains("Masculino")) { generoApi = "M"; generoNombre = "(Masc)"; }

    // Construir Nombre: "10K Calle (Fem)", el tipo (calle/trail) viene del padre evento
    String nombreFinal = nombreDistancia +  generoNombre;

    // Cupo: Usamos el cupo total del evento como limite individual
    Integer cupoInt = null;
    try { cupoInt = Integer.parseInt(cupoEvento); } catch(Exception e){}

    // 3. Crear Objeto Request
    CrearCategoriaRequest nuevaCat = new CrearCategoriaRequest(
      nombreFinal.trim(),
      precioDec,
      cupoInt // el mismo cupo del evento
    );
    nuevaCat.setGenero(generoApi);
    try { nuevaCat.setEdadMinima(Integer.parseInt(edadMinIn)); } catch(Exception e) {}
    try { nuevaCat.setEdadMaxima(Integer.parseInt(edadMaxIn)); } catch(Exception e) {}

    // 4. Agregar a la lista y notificar
    categoriasTemporales.add(nuevaCat);
    categoriasLive.setValue(new ArrayList<>(categoriasTemporales)); // Copia nueva para activar observer

    // 5. Limpiar errores (exito)
    errorDistancia.setValue(null);
    errorPrecio.setValue(null);
    return true;
  }

  public void eliminarCategoriaLocal(int posicion) {
    if (posicion >= 0 && posicion < categoriasTemporales.size()) {
      categoriasTemporales.remove(posicion);
      categoriasLive.setValue(new ArrayList<>(categoriasTemporales));
    }
  }

  public void resetearNavegacion() { navegacionExito.setValue(0); }

  // LOGICA DE VALIDACION Y GUARDADO
  public void guardarEvento(String tituloIn, String descripcionIn, String lugarIn, String datosPagoIn, String cupoIn,String tipoEventoIn) {

    // 1. Limpiar errores previos (Solo los del evento)
    mensajeGlobal.setValue(null);
    errorTitulo.setValue(null);
    errorUbicacion.setValue(null);
    errorCupo.setValue(null);

    boolean hayError = false;

    // 2. Validaciones del Evento
    if (tituloIn.trim().isEmpty()) {
      errorTitulo.setValue("El título es obligatorio");
      hayError = true;
    }

    if (lugarIn.trim().isEmpty()) {
      errorUbicacion.setValue("La ubicación es obligatoria");
      hayError = true;
    }

    if (hayError) return;

    // Validaciones de fecha
    if (fechaIso.isEmpty() || horaIso.isEmpty()) {
      mensajeGlobal.setValue("Debes seleccionar fecha y hora");
      return;
    }

    // Validacion Cupo Total del Evento
    Integer cupoInt = null;
    try {
      if (cupoIn != null && !cupoIn.trim().isEmpty()) {
        cupoInt = Integer.parseInt(cupoIn.trim());
      }
    } catch (NumberFormatException e) {
      errorCupo.setValue("Formato de número inválido");
      return;
    }

    String fechaHoraFinal = fechaIso + "T" + horaIso + ":00";

    //  RAMA EDICION
    // (Nota: En edicion no tocamos categorias)
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

    //  RAMA CREACION (Aqui usamos el modelo CrearCategoriaRequest)
    // VALIDACION CLAVE: la lista tiene datos?
    if (categoriasTemporales.isEmpty()) {
      mensajeGlobal.setValue("Debes agregar al menos una categoría con el botón '+'.");
      return;
    }

    // creamos el request usando la lista de objetos CrearCategoriaRequest que fuimos llenando
    //constructor
    CrearEventoRequest request = new CrearEventoRequest(
      tituloIn,
      descripcionIn,
      fechaHoraFinal,
      lugarIn,
      cupoInt,
      null,
      datosPagoIn,
      tipoEventoIn.toLowerCase().trim(),
      new ArrayList<>(categoriasTemporales) // Pasamos la lista acumulada
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
          mensajeGlobal.setValue("¡Cambios guardados con éxito!");
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
              mensajeGlobal.setValue("¡Evento creado! Redirigiendo al mapa...");
              navegacionExito.setValue(idNuevo);
            }
          } catch (Exception e) { mensajeGlobal.setValue("Evento creado, pero hubo error leyendo la respuesta."); }
        } else {
          manejarErrorApi(response);
        }
      }
      @Override
      public void onFailure(Call<ResponseBody> call, Throwable t) {
        isLoading.setValue(false);
        mensajeGlobal.setValue("No se pudo conectar con el servidor.");
      }
    });
  }

  private void manejarErrorApi(Response<ResponseBody> response) {
    String msg = "Error del servidor: " + response.code();
    try {
      if (response.errorBody() != null) {
        JSONObject json = new JSONObject(response.errorBody().string());
        if (json.has("message")) msg = json.getString("message");
        else if (json.has("errors")) { // Captura de errores de validacion del backend (.NET)
          JSONObject errors = json.getJSONObject("errors");
          if(errors.keys().hasNext()) {
            String key = errors.keys().next();
            msg = errors.getJSONArray(key).getString(0);
          }
        }
      }
    } catch (Exception e) { }
    mensajeGlobal.setValue(msg);
  }

  //  CONFIGURACION UI
  private void configurarModoCrear() {
    uiTituloPagina.setValue("Nuevo Evento");
    uiTextoBoton.setValue("Guardar y Definir Ruta");
    uiTextoAviso.setValue("Siguiente paso: Dibujar el circuito en el mapa!");
    uiVisibilidadCamposExtra.setValue(View.VISIBLE);
    uiVisibilidadChips.setValue(View.VISIBLE);
    uiCamposHabilitados.setValue(true);
  }

  private void configurarModoEditar() {
    uiTituloPagina.setValue("Editar Evento");
    uiTextoBoton.setValue("Guardar Cambios");
    uiTextoAviso.setValue("Nota: Precio, Distancia y Lugar no se editan.");
    uiVisibilidadCamposExtra.setValue(View.GONE);
    uiVisibilidadChips.setValue(View.GONE);
    uiCamposHabilitados.setValue(false);
  }

  // Inputs UI
  public void onChipDistanciaSelected(String textoChip) {
    if (textoChip != null) distancia.setValue(textoChip.replace("K", "").trim());
  }
  public void onFechaSelected(int year, int month, int day) {
    this.selYear = year; this.selMonth = month; this.selDay = day;
    fechaIso = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, day);
    fechaDisplay.setValue(String.format(Locale.getDefault(), "%02d/%02d/%04d", day, month + 1, year));
  }
  public void onHoraSelected(int hour, int minute) {
    this.selHour = hour; this.selMinute = minute;
    horaIso = String.format(Locale.getDefault(), "%02d:%02d", hour, minute);
    horaDisplay.setValue(horaIso);
  }

  public void verificarModoEdicion(int idEvento) {
    if (idEvento <= 0) return;
    esEdicion = true;
    idEventoEdicion = idEvento;
    configurarModoEditar();
    isLoading.setValue(true);
    repositorio.obtenerDetalleEvento(idEvento, new Callback<EventoDetalleResponse>() {
      @Override
      public void onResponse(Call<EventoDetalleResponse> call, Response<EventoDetalleResponse> response) {
        isLoading.setValue(false);
        if (response.isSuccessful() && response.body() != null) mapearEventoAUI(response.body());
        else mensajeGlobal.setValue("Error cargando datos.");
      }
      @Override
      public void onFailure(Call<EventoDetalleResponse> call, Throwable t) {
        isLoading.setValue(false);
        mensajeGlobal.setValue("Error de conexión.");
      }
    });
  }

  private void mapearEventoAUI(EventoDetalleResponse evento) {
    titulo.setValue(evento.getNombre());
    descripcion.setValue(evento.getDescripcion());
    ubicacion.setValue(evento.getLugar());
    if (evento.getDatosPago() != null) datosPago.setValue(evento.getDatosPago());
    if (evento.getCupoTotal() != null) cupo.setValue(String.valueOf(evento.getCupoTotal()));
    if (evento.getFechaHora() != null && evento.getFechaHora().contains("T")) {
      try {
        String[] partes = evento.getFechaHora().split("T");
        this.fechaIso = partes[0];
        this.horaIso = partes[1].substring(0, 5);
        String[] f = fechaIso.split("-");
        fechaDisplay.setValue(f[2] + "/" + f[1] + "/" + f[0]);
        horaDisplay.setValue(horaIso);
      } catch (Exception e) { e.printStackTrace(); }
    }
    if (evento.getCategorias() != null && !evento.getCategorias().isEmpty()) {
      CategoriaResponse cat = evento.getCategorias().get(0);
      precio.setValue(String.valueOf(cat.getPrecio()));
      edadMin.setValue(String.valueOf(cat.getEdadMinima()));
      edadMax.setValue(String.valueOf(cat.getEdadMaxima()));
      String g = cat.getGenero();
      if ("F".equals(g)) seleccionGenero.setValue("Femenino");
      else if ("M".equals(g)) seleccionGenero.setValue("Masculino");
      else seleccionGenero.setValue("Mixto / General");
      String nombreCat = cat.getNombre();
      if (nombreCat != null) {
        String[] partes = nombreCat.split(" ");
        if (partes.length > 0) distancia.setValue(partes[0].replace("K", ""));
        if (partes.length > 1) seleccionModalidad.setValue(partes[1]);
        else distancia.setValue(nombreCat);
      }
    }
  }
}