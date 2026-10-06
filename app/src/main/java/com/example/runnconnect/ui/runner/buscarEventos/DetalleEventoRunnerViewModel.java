package com.example.runnconnect.ui.runner.buscarEventos;

import android.app.Application;
import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.runnconnect.data.conexion.ApiClient;
import com.example.runnconnect.data.conexion.ApiService;
import com.example.runnconnect.data.preferencias.SessionManager;
import com.example.runnconnect.data.request.CrearInscripcionRequest;
import com.example.runnconnect.data.response.CategoriaResponse;
import com.example.runnconnect.data.response.EventoDetalleResponse;
import com.example.runnconnect.data.response.MisInscripcionesResponse;
import com.example.runnconnect.data.response.PerfilUsuarioResponse;
import com.example.runnconnect.ui.runner.buscarEventos.CategoriasRunnerAdapter.CategoriaCompatibilidadUI;

import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DetalleEventoRunnerViewModel extends AndroidViewModel {

  private final ApiService apiService;
  private final SessionManager sessionManager;

  private int currentIdEvento = 0;
  private PerfilUsuarioResponse perfilRunner = null;
  private EventoDetalleResponse eventoDetalle = null;
  private MisInscripcionesResponse.InscripcionItem inscripcionActual = null;

  // Estados UI para datos del evento
  private final MutableLiveData<String> uiTitulo = new MutableLiveData<>("");
  private final MutableLiveData<String> uiEstado = new MutableLiveData<>("");
  private final MutableLiveData<String> uiFecha = new MutableLiveData<>("");
  private final MutableLiveData<String> uiLugar = new MutableLiveData<>("");
  private final MutableLiveData<String> uiCupos = new MutableLiveData<>("");
  private final MutableLiveData<String> uiDescripcion = new MutableLiveData<>("");
  private final MutableLiveData<String> uiOrganizador = new MutableLiveData<>("");
  private final MutableLiveData<String> uiDatosPago = new MutableLiveData<>("");

  // Estado del perfil del runner
  private final MutableLiveData<Integer> avisoPerfilVisibility = new MutableLiveData<>(View.GONE);

  // Lista de categorías pre-procesadas
  private final MutableLiveData<List<CategoriaCompatibilidadUI>> listaCategoriasUI = new MutableLiveData<>(new ArrayList<>());

  // Loading y Mensajes
  private final MutableLiveData<Integer> progressVisibility = new MutableLiveData<>(View.GONE);
  private final MutableLiveData<String> mensajeTexto = new MutableLiveData<>("");
  private final MutableLiveData<Integer> mensajeVisibility = new MutableLiveData<>(View.GONE);
  private final MutableLiveData<Integer> mensajeColorFondo = new MutableLiveData<>(Color.TRANSPARENT);
  private final MutableLiveData<Integer> mensajeColorTexto = new MutableLiveData<>(Color.BLACK);

  // Errores del diálogo
  private final MutableLiveData<String> dialogErrorTexto = new MutableLiveData<>("");
  private final MutableLiveData<Integer> dialogErrorVisibility = new MutableLiveData<>(View.GONE);
  private final MutableLiveData<Boolean> dialogCerrarEvento = new MutableLiveData<>();

  // Selector de talles (Spinner)
  private final MutableLiveData<List<String>> listaTallesRemera = new MutableLiveData<>(
      Arrays.asList("XS", "S", "M", "L", "XL", "XXL")
  );

  public DetalleEventoRunnerViewModel(@NonNull Application application) {
    super(application);
    this.apiService = ApiClient.getApiService();
    this.sessionManager = new SessionManager(application);
  }

  // Getters
  public LiveData<String> getUiTitulo() { return uiTitulo; }
  public LiveData<String> getUiEstado() { return uiEstado; }
  public LiveData<String> getUiFecha() { return uiFecha; }
  public LiveData<String> getUiLugar() { return uiLugar; }
  public LiveData<String> getUiCupos() { return uiCupos; }
  public LiveData<String> getUiDescripcion() { return uiDescripcion; }
  public LiveData<String> getUiOrganizador() { return uiOrganizador; }
  public LiveData<String> getUiDatosPago() { return uiDatosPago; }
  public LiveData<Integer> getAvisoPerfilVisibility() { return avisoPerfilVisibility; }
  public LiveData<List<CategoriaCompatibilidadUI>> getListaCategoriasUI() { return listaCategoriasUI; }
  public LiveData<Integer> getProgressVisibility() { return progressVisibility; }
  public LiveData<String> getMensajeTexto() { return mensajeTexto; }
  public LiveData<Integer> getMensajeVisibility() { return mensajeVisibility; }
  public LiveData<Integer> getMensajeColorFondo() { return mensajeColorFondo; }
  public LiveData<Integer> getMensajeColorTexto() { return mensajeColorTexto; }
  public LiveData<String> getDialogErrorTexto() { return dialogErrorTexto; }
  public LiveData<Integer> getDialogErrorVisibility() { return dialogErrorVisibility; }
  public LiveData<Boolean> getDialogCerrarEvento() { return dialogCerrarEvento; }
  public LiveData<List<String>> getListaTallesRemera() { return listaTallesRemera; }

  public void resetDialogCerrarEvento() {
    dialogCerrarEvento.setValue(null);
  }

  public void cargarDetalle(int idEvento) {
    this.currentIdEvento = idEvento;
    progressVisibility.setValue(View.VISIBLE);
    mensajeVisibility.setValue(View.GONE);

    String token = sessionManager.leerToken();

    // 1. Obtener perfil del runner
    apiService.obtenerPerfil("Bearer " + token).enqueue(new Callback<PerfilUsuarioResponse>() {
      @Override
      public void onResponse(Call<PerfilUsuarioResponse> call, Response<PerfilUsuarioResponse> response) {
        if (response.isSuccessful() && response.body() != null) {
          perfilRunner = response.body();
        } else {
          perfilRunner = null;
        }
        // 2. Obtener inscripciones del runner para verificar estado en este evento
        cargarInscripcionesYEvento(idEvento, token);
      }

      @Override
      public void onFailure(Call<PerfilUsuarioResponse> call, Throwable t) {
        perfilRunner = null;
        cargarInscripcionesYEvento(idEvento, token);
      }
    });
  }

  private void cargarInscripcionesYEvento(int idEvento, String token) {
    apiService.obtenerMisInscripciones("Bearer " + token, false).enqueue(new Callback<MisInscripcionesResponse>() {
      @Override
      public void onResponse(Call<MisInscripcionesResponse> call, Response<MisInscripcionesResponse> response) {
        inscripcionActual = null;
        if (response.isSuccessful() && response.body() != null && response.body().getInscripciones() != null) {
          MisInscripcionesResponse.InscripcionItem activa = null;
          MisInscripcionesResponse.InscripcionItem rechazada = null;

          for (MisInscripcionesResponse.InscripcionItem item : response.body().getInscripciones()) {
            if (item.getEvento() != null && item.getEvento().getIdEvento() == idEvento) {
              String st = item.getEstadoPago() != null ? item.getEstadoPago().toLowerCase().trim() : "";
              if ("pendiente".equals(st) || "procesando".equals(st) || "pagado".equals(st)) {
                activa = item;
                break; // Máxima prioridad: inscripción activa
              } else if ("rechazado".equals(st) && rechazada == null) {
                rechazada = item; // Guardamos la rechazada más reciente por si no hay activa
              }
            }
          }
          inscripcionActual = (activa != null) ? activa : rechazada;
        }
        // 3. Obtener detalle del evento
        cargarEvento(idEvento, token);
      }

      @Override
      public void onFailure(Call<MisInscripcionesResponse> call, Throwable t) {
        inscripcionActual = null;
        cargarEvento(idEvento, token);
      }
    });
  }

  private void cargarEvento(int idEvento, String token) {
    apiService.obtenerEventoPorId("Bearer " + token, idEvento).enqueue(new Callback<EventoDetalleResponse>() {
      @Override
      public void onResponse(Call<EventoDetalleResponse> call, Response<EventoDetalleResponse> response) {
        progressVisibility.setValue(View.GONE);

        if (response.isSuccessful() && response.body() != null) {
          eventoDetalle = response.body();
          procesarDatosEvento();
        } else {
          mostrarMensaje("No se pudo cargar el detalle del evento (" + response.code() + ")", false);
        }
      }

      @Override
      public void onFailure(Call<EventoDetalleResponse> call, Throwable t) {
        progressVisibility.setValue(View.GONE);
        mostrarMensaje("Error de conexión al cargar el evento.", false);
      }
    });
  }

  private void procesarDatosEvento() {
    if (eventoDetalle == null) return;

    uiTitulo.setValue(eventoDetalle.getNombre() != null ? eventoDetalle.getNombre() : "Sin nombre");
    uiEstado.setValue(eventoDetalle.getEstado() != null ? eventoDetalle.getEstado().toUpperCase() : "DESCONOCIDO");

    String fecha = eventoDetalle.getFechaHora() != null ? eventoDetalle.getFechaHora().replace("T", " ") : "-";
    uiFecha.setValue("Fecha: " + fecha);
    uiLugar.setValue("Lugar: " + (eventoDetalle.getLugar() != null ? eventoDetalle.getLugar() : "-"));

    int inscriptos = eventoDetalle.getInscriptosActuales();
    String cupoTot = eventoDetalle.getCupoTotal() != null ? String.valueOf(eventoDetalle.getCupoTotal()) : "Ilimitado";
    uiCupos.setValue("Cupos: " + inscriptos + " inscriptos / " + cupoTot + " totales");

    uiDescripcion.setValue(eventoDetalle.getDescripcion() != null && !eventoDetalle.getDescripcion().trim().isEmpty()
        ? eventoDetalle.getDescripcion() : "Sin descripción disponible.");

    if (eventoDetalle.getOrganizador() != null) {
      String nomOrg = eventoDetalle.getOrganizador().getNombre();
      uiOrganizador.setValue("Organizado por: " + (nomOrg != null ? nomOrg : "-"));
    } else {
      uiOrganizador.setValue("Organizado por: -");
    }

    uiDatosPago.setValue("Datos de pago: " + (eventoDetalle.getDatosPago() != null && !eventoDetalle.getDatosPago().isEmpty()
        ? eventoDetalle.getDatosPago() : "A coordinar con el organizador"));

    // Procesar compatibilidad de categorías
    boolean perfilCompleto = verificarPerfilCompleto(perfilRunner);
    avisoPerfilVisibility.setValue(perfilCompleto ? View.GONE : View.VISIBLE);

    int runnerEdad = perfilCompleto ? calcularEdad(perfilRunner.getFechaNacimiento()) : 0;
    String runnerGenero = (perfilCompleto && perfilRunner.getGenero() != null) ? perfilRunner.getGenero().trim() : "";

    List<CategoriaCompatibilidadUI> items = new ArrayList<>();
    if (eventoDetalle.getCategorias() != null) {
      for (CategoriaResponse cat : eventoDetalle.getCategorias()) {
        items.add(evaluarCategoria(cat, perfilCompleto, runnerEdad, runnerGenero));
      }
    }
    listaCategoriasUI.setValue(items);
  }

  private CategoriaCompatibilidadUI evaluarCategoria(CategoriaResponse cat,
                                                     boolean perfilCompleto,
                                                     int runnerEdad,
                                                     String runnerGenero) {
    int idCat = cat.getIdCategoria();
    String nombre = cat.getNombre() != null ? cat.getNombre() : "Categoría";
    String costo = cat.getPrecio() != null ? String.format(Locale.getDefault(), "$%.2f", cat.getPrecio()) : "$0.00";

    String cupos;
    boolean hayCupo = true;

    if (cat.getCupoCategoria() != null) {
      int disponibles = cat.getCupoCategoria() - cat.getInscriptosActuales();
      cupos = "Cupos: " + Math.max(0, disponibles) + " disponibles (de " + cat.getCupoCategoria() + ")";
      if (disponibles <= 0) {
        hayCupo = false;
      }
    } else if (eventoDetalle != null && eventoDetalle.getCupoTotal() != null) {
      int disponibles = eventoDetalle.getCupoTotal() - eventoDetalle.getInscriptosActuales();
      cupos = "Cupos: " + Math.max(0, disponibles) + " disponibles (cupo del evento)";
      if (disponibles <= 0) {
        hayCupo = false;
      }
    } else {
      cupos = "Cupos: Ilimitados (" + cat.getInscriptosActuales() + " inscriptos)";
    }

    // Si el evento en general ya alcanzó su cupo total (si tiene límite)
    if (eventoDetalle != null && eventoDetalle.getCupoTotal() != null) {
      if (eventoDetalle.getCupoTotal() - eventoDetalle.getInscriptosActuales() <= 0) {
        hayCupo = false;
      }
    }

    String genDesc = "Mixto";
    if ("M".equalsIgnoreCase(cat.getGenero())) genDesc = "Masculino";
    else if ("F".equalsIgnoreCase(cat.getGenero())) genDesc = "Femenino";
    String requisitos = "Edad: " + cat.getEdadMinima() + " - " + cat.getEdadMaxima() + " años | Género: " + genDesc;

    // === CASO 1: EL RUNNER TIENE UNA INSCRIPCIÓN ACTIVA EN ESTE EVENTO ("pendiente", "procesando", "pagado") ===
    if (inscripcionActual != null) {
      String stPago = inscripcionActual.getEstadoPago() != null ? inscripcionActual.getEstadoPago().toLowerCase().trim() : "";
      boolean esActiva = "pendiente".equals(stPago) || "procesando".equals(stPago) || "pagado".equals(stPago);

      if (esActiva) {
        boolean esEstaCategoria = inscripcionActual.getCategoria() != null && inscripcionActual.getCategoria().getIdCategoria() == idCat;
        int idInscripcion = inscripcionActual.getIdInscripcion();

        if (esEstaCategoria) {
          switch (stPago) {
            case "pendiente":
              return new CategoriaCompatibilidadUI(
                  idCat, nombre, costo, cupos, requisitos,
                  "Inscripción registrada. Sube tu comprobante de pago.",
                  Color.parseColor("#E65100"),
                  "SUBIR COMPROBANTE",
                  true,
                  Color.parseColor("#FB8C00"),
                  CategoriaCompatibilidadUI.ACCION_SUBIR_COMPROBANTE,
                  idInscripcion,
                  ""
              );

            case "procesando":
              return new CategoriaCompatibilidadUI(
                  idCat, nombre, costo, cupos, requisitos,
                  "Comprobante en revisión por el organizador.",
                  Color.parseColor("#0288D1"),
                  "EN REVISIÓN",
                  false,
                  Color.parseColor("#78909C"),
                  CategoriaCompatibilidadUI.ACCION_NINGUNA,
                  idInscripcion,
                  ""
              );

            case "pagado":
              String urlComprobante = obtenerUrlCompletaComprobante(inscripcionActual.getComprobantePagoURL());
              return new CategoriaCompatibilidadUI(
                  idCat, nombre, costo, cupos, requisitos,
                  "¡Pago confirmado! Ya estás inscripto en esta carrera.",
                  Color.parseColor("#2E7D32"),
                  "INSCRIPTO",
                  true,
                  Color.parseColor("#2E7D32"),
                  CategoriaCompatibilidadUI.ACCION_VER_COMPROBANTE,
                  idInscripcion,
                  urlComprobante
              );
          }
        } else {
          // Está activamente inscripto en otra categoría de este mismo evento
          return new CategoriaCompatibilidadUI(
              idCat, nombre, costo, cupos, requisitos,
              "Ya estás inscripto en otra categoría de este evento",
              Color.parseColor("#757575"),
              "INSCRIBIRSE",
              false,
              Color.parseColor("#BDBDBD"),
              CategoriaCompatibilidadUI.ACCION_NINGUNA,
              0,
              ""
          );
        }
      }
    }

    // === CASO 2: NO TIENE INSCRIPCIÓN ACTIVA (No inscripto o inscripción previa rechazada) ===
    boolean esCategoriaRechazada = (inscripcionActual != null
        && "rechazado".equalsIgnoreCase(inscripcionActual.getEstadoPago())
        && inscripcionActual.getCategoria() != null
        && inscripcionActual.getCategoria().getIdCategoria() == idCat);

    String textoBtnBase = esCategoriaRechazada ? "VOLVER A INSCRIBIRSE" : "INSCRIBIRSE";

    // 1. Perfil incompleto
    if (!perfilCompleto) {
      return new CategoriaCompatibilidadUI(
          idCat, nombre, costo, cupos, requisitos,
          "Completa tu perfil para poder inscribirte",
          Color.parseColor("#757575"),
          textoBtnBase,
          false,
          Color.parseColor("#BDBDBD"),
          CategoriaCompatibilidadUI.ACCION_NINGUNA,
          0,
          ""
      );
    }

    // 2. Si la categoría no está programada
    if (cat.getEstado() != null && !cat.getEstado().equalsIgnoreCase("programada")) {
      return new CategoriaCompatibilidadUI(
          idCat, nombre, costo, cupos, requisitos,
          "Estado de categoría: " + cat.getEstado().toUpperCase(),
          Color.parseColor("#C62828"),
          textoBtnBase,
          false,
          Color.parseColor("#BDBDBD"),
          CategoriaCompatibilidadUI.ACCION_NINGUNA,
          0,
          ""
      );
    }

    // 3. Si no hay cupos
    if (!hayCupo) {
      return new CategoriaCompatibilidadUI(
          idCat, nombre, costo, cupos, requisitos,
          "Sin cupos disponibles",
          Color.parseColor("#C62828"),
          textoBtnBase,
          false,
          Color.parseColor("#BDBDBD"),
          CategoriaCompatibilidadUI.ACCION_NINGUNA,
          0,
          ""
      );
    }

    // 4. Validar edad
    if (runnerEdad < cat.getEdadMinima() || runnerEdad > cat.getEdadMaxima()) {
      return new CategoriaCompatibilidadUI(
          idCat, nombre, costo, cupos, requisitos,
          "Rango de edad " + cat.getEdadMinima() + "-" + cat.getEdadMaxima(),
          Color.parseColor("#E65100"),
          textoBtnBase,
          false,
          Color.parseColor("#BDBDBD"),
          CategoriaCompatibilidadUI.ACCION_NINGUNA,
          0,
          ""
      );
    }

    // 5. Validar género
    String catGen = cat.getGenero() != null ? cat.getGenero().trim() : "X";
    if (!catGen.equalsIgnoreCase("X") && !catGen.equalsIgnoreCase(runnerGenero)) {
      return new CategoriaCompatibilidadUI(
          idCat, nombre, costo, cupos, requisitos,
          "Exclusivo para género " + genDesc,
          Color.parseColor("#E65100"),
          textoBtnBase,
          false,
          Color.parseColor("#BDBDBD"),
          CategoriaCompatibilidadUI.ACCION_NINGUNA,
          0,
          ""
      );
    }

    // 6. Cumple todos los requisitos:
    if (esCategoriaRechazada) {
      String obs = inscripcionActual.getObservacion();
      String motivo = (obs != null && !obs.trim().isEmpty()) ? ": " + obs : "";
      return new CategoriaCompatibilidadUI(
          idCat, nombre, costo, cupos, requisitos,
          "Comprobante rechazado" + motivo + ". Vuelve a inscribirte para generar un nuevo pago.",
          Color.parseColor("#C62828"),
          "VOLVER A INSCRIBIRSE",
          true,
          Color.parseColor("#D32F2F"),
          CategoriaCompatibilidadUI.ACCION_INSCRIBIR,
          0,
          ""
      );
    }

    // Inscripcion normal disponible
    return new CategoriaCompatibilidadUI(
        idCat, nombre, costo, cupos, requisitos,
        "",
        Color.TRANSPARENT,
        "INSCRIBIRSE",
        true,
        Color.parseColor("#6200EE"),
        CategoriaCompatibilidadUI.ACCION_INSCRIBIR,
        0,
        ""
    );
  }

  private String obtenerUrlCompletaComprobante(String url) {
    if (url == null || url.trim().isEmpty()) return "";
    String res = url.trim();
    if (res.startsWith("/")) {
      res = "http://10.0.2.2:5213" + res;
    } else if (res.contains("localhost")) {
      res = res.replace("localhost", "10.0.2.2");
    }
    return res;
  }

  private boolean verificarPerfilCompleto(PerfilUsuarioResponse p) {
    if (p == null) return false;
    return p.getNombre() != null && !p.getNombre().trim().isEmpty()
        && p.getApellido() != null && !p.getApellido().trim().isEmpty()
        && p.getDni() != null && p.getDni() > 0
        && p.getFechaNacimiento() != null && !p.getFechaNacimiento().trim().isEmpty()
        && p.getGenero() != null && !p.getGenero().trim().isEmpty()
        && p.getTelefono() != null && !p.getTelefono().trim().isEmpty()
        && p.getLocalidad() != null && !p.getLocalidad().trim().isEmpty()
        && p.getNombreContactoEmergencia() != null && !p.getNombreContactoEmergencia().trim().isEmpty()
        && p.getTelefonoEmergencia() != null && !p.getTelefonoEmergencia().trim().isEmpty();
  }

  private int calcularEdad(String fechaNacStr) {
    if (fechaNacStr == null || fechaNacStr.trim().isEmpty()) return 0;
    try {
      String fecha = fechaNacStr.contains("T") ? fechaNacStr.split("T")[0] : fechaNacStr;
      String[] partes = fecha.split("-");
      if (partes.length == 3) {
        int anio = Integer.parseInt(partes[0].trim());
        int mes = Integer.parseInt(partes[1].trim());
        int dia = Integer.parseInt(partes[2].trim());

        Calendar dob = Calendar.getInstance();
        dob.set(anio, mes - 1, dia);

        Calendar hoy = Calendar.getInstance();
        int edad = hoy.get(Calendar.YEAR) - dob.get(Calendar.YEAR);
        if (hoy.get(Calendar.DAY_OF_YEAR) < dob.get(Calendar.DAY_OF_YEAR)) {
          edad--;
        }
        return Math.max(0, edad);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return 0;
  }

  public void ejecutarInscripcion(int idCategoria, String talleRemera, boolean aceptoDeslinde) {
    if (!aceptoDeslinde) {
      dialogErrorTexto.setValue("Debe aceptar el deslinde de responsabilidad");
      dialogErrorVisibility.setValue(View.VISIBLE);
      return;
    }

    if (talleRemera == null || talleRemera.trim().isEmpty()) {
      dialogErrorTexto.setValue("Seleccione un talle de remera");
      dialogErrorVisibility.setValue(View.VISIBLE);
      return;
    }

    dialogErrorVisibility.setValue(View.GONE);
    progressVisibility.setValue(View.VISIBLE);

    String token = sessionManager.leerToken();
    CrearInscripcionRequest request = new CrearInscripcionRequest(idCategoria, talleRemera, true);

    apiService.inscribirse("Bearer " + token, request).enqueue(new Callback<ResponseBody>() {
      @Override
      public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
        progressVisibility.setValue(View.GONE);

        if (response.isSuccessful()) {
          dialogCerrarEvento.setValue(true);
          mostrarMensaje("¡Inscripción realizada con éxito! Ahora puedes subir tu comprobante de pago.", true);
          // Recargar tod el detalle del evento para actualizar inscripciones y botones
          cargarDetalle(currentIdEvento);
        } else {
          String errorMsg = "Error al inscribirse (" + response.code() + ")";
          try {
            if (response.errorBody() != null) {
              String errorJson = response.errorBody().string();
              JSONObject json = new JSONObject(errorJson);
              if (json.has("message")) {
                errorMsg = json.getString("message");
              }
            }
          } catch (Exception ignored) {}

          dialogErrorTexto.setValue(errorMsg);
          dialogErrorVisibility.setValue(View.VISIBLE);
          mostrarMensaje(errorMsg, false);
        }
      }

      @Override
      public void onFailure(Call<ResponseBody> call, Throwable t) {
        progressVisibility.setValue(View.GONE);
        String msg = "Error de conexión. Intente nuevamente.";
        dialogErrorTexto.setValue(msg);
        dialogErrorVisibility.setValue(View.VISIBLE);
        mostrarMensaje(msg, false);
      }
    });
  }

  public void procesarYSubirComprobante(int idInscripcion, Uri uri) {
    if (uri == null) return;

    progressVisibility.setValue(View.VISIBLE);
    mostrarMensaje("Procesando comprobante...", true);

    new Thread(() -> {
      Context context = getApplication();
      String mimeType = context.getContentResolver().getType(uri);
      String extension = (mimeType != null && mimeType.contains("png")) ? ".png" : ".jpg";

      File tempFile = copiarUriAArchivo(context, uri, extension);
      if (tempFile == null) {
        new Handler(Looper.getMainLooper()).post(() -> {
          progressVisibility.setValue(View.GONE);
          mostrarMensaje("Error al leer la imagen seleccionada.", false);
        });
        return;
      }

      if (tempFile.length() > 10 * 1024 * 1024) {
        tempFile.delete();
        new Handler(Looper.getMainLooper()).post(() -> {
          progressVisibility.setValue(View.GONE);
          mostrarMensaje("El comprobante no puede exceder 10MB.", false);
        });
        return;
      }

      String reqMime = (mimeType != null) ? mimeType : "image/jpeg";
      RequestBody requestFile = RequestBody.create(MediaType.parse(reqMime), tempFile);
      MultipartBody.Part body = MultipartBody.Part.createFormData("comprobante", tempFile.getName(), requestFile);

      String token = sessionManager.leerToken();
      apiService.subirComprobante("Bearer " + token, idInscripcion, body).enqueue(new Callback<ResponseBody>() {
        @Override
        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
          progressVisibility.setValue(View.GONE);
          tempFile.delete();

          if (response.isSuccessful()) {
            mostrarMensaje("¡Comprobante subido exitosamente! El pago se encuentra en revisión.", true);
            cargarDetalle(currentIdEvento);
          } else {
            String errorMsg = "Error al subir comprobante (" + response.code() + ")";
            try {
              if (response.errorBody() != null) {
                String raw = response.errorBody().string();
                JSONObject json = new JSONObject(raw);
                if (json.has("message")) errorMsg = json.getString("message");
              }
            } catch (Exception ignored) {}
            mostrarMensaje(errorMsg, false);
          }
        }

        @Override
        public void onFailure(Call<ResponseBody> call, Throwable t) {
          progressVisibility.setValue(View.GONE);
          tempFile.delete();
          mostrarMensaje("Error de conexión al subir comprobante.", false);
        }
      });
    }).start();
  }

  private File copiarUriAArchivo(Context context, Uri uri, String extension) {
    try {
      InputStream is = context.getContentResolver().openInputStream(uri);
      if (is == null) return null;
      File temp = File.createTempFile("comprobante_upload", extension, context.getCacheDir());
      try (FileOutputStream out = new FileOutputStream(temp)) {
        byte[] buffer = new byte[16 * 1024];
        int len;
        while ((len = is.read(buffer)) != -1) {
          out.write(buffer, 0, len);
        }
        out.flush();
      }
      is.close();
      return temp;
    } catch (Exception e) {
      return null;
    }
  }

  private void mostrarMensaje(String texto, boolean exito) {
    mensajeTexto.setValue(texto);
    mensajeColorFondo.setValue(exito ? Color.parseColor("#E8F5E9") : Color.parseColor("#FFEBEE"));
    mensajeColorTexto.setValue(exito ? Color.parseColor("#2E7D32") : Color.parseColor("#C62828"));
    mensajeVisibility.setValue(View.VISIBLE);
  }
}
