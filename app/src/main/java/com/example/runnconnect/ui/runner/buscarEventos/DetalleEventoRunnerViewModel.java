package com.example.runnconnect.ui.runner.buscarEventos;

import android.app.Application;
import android.graphics.Color;
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
import com.example.runnconnect.data.response.PerfilUsuarioResponse;

import com.example.runnconnect.ui.runner.buscarEventos.CategoriasRunnerAdapter.CategoriaCompatibilidadUI;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

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
        // 2. Obtener detalle del evento
        cargarEvento(idEvento, token);
      }

      @Override
      public void onFailure(Call<PerfilUsuarioResponse> call, Throwable t) {
        perfilRunner = null;
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
    String costo = cat.getPrecio() != null ? String.format("$%.2f", cat.getPrecio()) : "$0.00";
    int disponibles = cat.getCupoCategoria() - cat.getInscriptosActuales();
    String cupos = "Cupos: " + Math.max(0, disponibles) + " disponibles (de " + cat.getCupoCategoria() + ")";

    String genDesc = "Mixto";
    if ("M".equalsIgnoreCase(cat.getGenero())) genDesc = "Masculino";
    else if ("F".equalsIgnoreCase(cat.getGenero())) genDesc = "Femenino";
    String requisitos = "Edad: " + cat.getEdadMinima() + " - " + cat.getEdadMaxima() + " años | Género: " + genDesc;

    // Si el perfil está incompleto -> Modo lectura
    if (!perfilCompleto) {
      return new CategoriaCompatibilidadUI(
          idCat, nombre, costo, cupos, requisitos,
          "⚠️ Completa tu perfil para poder inscribirte",
          false,
          Color.parseColor("#757575")
      );
    }

    // Si la categoría no está programada
    if (cat.getEstado() != null && !cat.getEstado().equalsIgnoreCase("programada")) {
      return new CategoriaCompatibilidadUI(
          idCat, nombre, costo, cupos, requisitos,
          "Estado de categoría: " + cat.getEstado().toUpperCase(),
          false,
          Color.parseColor("#C62828")
      );
    }

    // Si no hay cupos
    if (disponibles <= 0) {
      return new CategoriaCompatibilidadUI(
          idCat, nombre, costo, cupos, requisitos,
          "Sin cupos disponibles",
          false,
          Color.parseColor("#C62828")
      );
    }

    // Validar edad
    if (runnerEdad < cat.getEdadMinima() || runnerEdad > cat.getEdadMaxima()) {
      return new CategoriaCompatibilidadUI(
          idCat, nombre, costo, cupos, requisitos,
          "Rango de edad " + cat.getEdadMinima() + "-" + cat.getEdadMaxima(),
          false,
          Color.parseColor("#E65100")
      );
    }

    // Validar género
    String catGen = cat.getGenero() != null ? cat.getGenero().trim() : "X";
    if (!catGen.equalsIgnoreCase("X") && !catGen.equalsIgnoreCase(runnerGenero)) {
      return new CategoriaCompatibilidadUI(
          idCat, nombre, costo, cupos, requisitos,
          "Exclusivo para género " + genDesc,
          false,
          Color.parseColor("#E65100")
      );
    }

    // Cumple todos los requisitos (habilitado para inscripción, sin mensaje extra)
    return new CategoriaCompatibilidadUI(
        idCat, nombre, costo, cupos, requisitos,
        "",
        true,
        Color.TRANSPARENT
    );
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
          mostrarMensaje("¡Inscripción realizada con éxito! Realiza el pago indicado abajo para completar tu registro.", true);
          // Recargar el evento para actualizar cupos
          cargarEvento(currentIdEvento, token);
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

  private void mostrarMensaje(String texto, boolean exito) {
    mensajeTexto.setValue(texto);
    mensajeColorFondo.setValue(exito ? Color.parseColor("#E8F5E9") : Color.parseColor("#FFEBEE"));
    mensajeColorTexto.setValue(exito ? Color.parseColor("#2E7D32") : Color.parseColor("#C62828"));
    mensajeVisibility.setValue(View.VISIBLE);
  }
}
