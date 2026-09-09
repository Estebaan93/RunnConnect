package com.example.runnconnect.ui.organizador.misEventos;

import android.app.Application;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.provider.OpenableColumns;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.runnconnect.data.conexion.ApiClient;
import com.example.runnconnect.data.conexion.ApiService;
import com.example.runnconnect.data.preferencias.SessionManager;
import com.example.runnconnect.data.request.MotivoBajaRequest;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import com.example.runnconnect.data.request.CambiarEstadoCategoriaRequest;
import com.example.runnconnect.data.request.CambiarEstadoRequest;
import com.example.runnconnect.data.response.CategoriaResponse;
import com.example.runnconnect.data.response.EventoDetalleResponse;
import com.example.runnconnect.data.response.InscriptoEventoResponse;
import com.example.runnconnect.data.response.ListaInscriptosResponse;
import com.example.runnconnect.data.response.ResultadosEventoResponse;

import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DetalleEventoViewModel extends AndroidViewModel {
  private final ApiService apiService;
  private final SessionManager sessionManager;
  private CategoriaResponse categoriaSeleccionada;

  // --- VARIABLES INTERNAS ---
  private boolean existenResultados = false;
  private boolean todosLosResultadosCargados = false;
  private File archivoListoParaSubir = null;

  // --- LIVE DATA ---
  private final MutableLiveData<String> nombreArchivoSeleccionado = new MutableLiveData<>();
  private final MutableLiveData<Boolean> archivoEsValido = new MutableLiveData<>(false);

  // MVVM Puro: Evento binario de éxito vs Mensajes de error en UI
  private final MutableLiveData<Boolean> exitoCargaArchivo = new MutableLiveData<>();


  // NUEVO: LiveData para el resumen detallado de la carga (errores de DNI)
  private final MutableLiveData<String> resumenCargaArchivo = new MutableLiveData<>();

  private final MutableLiveData<Integer> uiVisibilidadRunners = new MutableLiveData<>(View.GONE);
  private final MutableLiveData<Integer> uiVisibilidadEstadoCategoria = new MutableLiveData<>(View.GONE);

  private final MutableLiveData<String[]> estadosCategoriasValidos = new MutableLiveData<>(
    new String[]{"programada", "retrasada", "cancelada", "finalizada", "suspendido"}
  );
  
  private final String[] estadosEventoValidos = {"publicado", "suspendido", "finalizado", "cancelado"};
  private final MutableLiveData<Integer> posicionPreseleccionadaCategoria = new MutableLiveData<>(0);
  private final MutableLiveData<String> errorMotivoCategoriaTexto = new MutableLiveData<>("");
  private final MutableLiveData<Integer> errorMotivoCategoriaVisibilidad = new MutableLiveData<>(View.GONE);

  private final MutableLiveData<String[]> opcionesMenuResultados = new MutableLiveData<>();
  private final MutableLiveData<Boolean> eventVerResultados = new MutableLiveData<>();

  // Estado para la confirmación de baja
  private final MutableLiveData<String> textoConfirmacionBaja = new MutableLiveData<>();
  private InscriptoEventoResponse runnerBajaPendiente = null;
  private int idCatBajaPendiente = 0;


  // NUEVO: Lista de categorías que aún NO tienen CSV cargado
  private final MutableLiveData<List<CategoriaResponse>> categoriasPendientesCarga = new MutableLiveData<>();
  private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
  private final MutableLiveData<String> mensajeGlobal = new MutableLiveData<>();
  private final MutableLiveData<Integer> mensajeGlobalVisibilidad = new MutableLiveData<>(View.GONE);
  private final MutableLiveData<Integer> mensajeColorTexto = new MutableLiveData<>(Color.BLACK);
  private final MutableLiveData<Integer> mensajeColorFondo = new MutableLiveData<>(Color.parseColor("#F5F5F5"));

  private final MutableLiveData<Boolean> habilitarEliminacionRunners = new MutableLiveData<>(true);

  // MVVM Puro: Propiedades expuestas listas para la vista
  private final MutableLiveData<String> estadoActualEvento = new MutableLiveData<>("");

  private final MutableLiveData<EventoDetalleResponse> eventoRaw = new MutableLiveData<>();

  // MVVM Puro: Se usan Booleans en lugar de View.VISIBLE / View.GONE
  private final MutableLiveData<Boolean> visibilityBtnResultados = new MutableLiveData<>(false);
  private final MutableLiveData<Boolean> uiVisibilidadDatosCategoria = new MutableLiveData<>(false);

  private final MutableLiveData<String> uiTitulo = new MutableLiveData<>();
  private final MutableLiveData<String> uiFecha = new MutableLiveData<>();
  private final MutableLiveData<String> uiLugar = new MutableLiveData<>();
  private final MutableLiveData<String> uiDescripcion = new MutableLiveData<>();
  private final MutableLiveData<String> uiInscriptos = new MutableLiveData<>();
  private final MutableLiveData<String> uiCupo = new MutableLiveData<>();
  private final MutableLiveData<String> uiEstadoTexto = new MutableLiveData<>();
  private final MutableLiveData<Integer> uiEstadoColor = new MutableLiveData<>();
  private final MutableLiveData<String> uiDistanciaTipo = new MutableLiveData<>();
  private final MutableLiveData<String> uiGeneroPrecio = new MutableLiveData<>();
  private final MutableLiveData<Integer> uiVisibilidadCarga = new MutableLiveData<>(View.GONE);
  private final MutableLiveData<String[]> opcionesSpinnerCategoria = new MutableLiveData<>();

  private final MutableLiveData<String> dialogErrorTexto = new MutableLiveData<>("");
  private final MutableLiveData<Integer> dialogErrorVisibilidad = new MutableLiveData<>(View.GONE);
  private final MutableLiveData<List<CategoriaResponse>> listaCategorias = new MutableLiveData<>();
  private final MutableLiveData<List<CategoriasInfoAdapter.CategoriaUI>> listaCategoriasUI = new MutableLiveData<>();
  private final MutableLiveData<List<RunnerSimpleAdapter.RunnerUI>> listaRunnerDialog = new MutableLiveData<>();

  public DetalleEventoViewModel(@NonNull Application application) {
    super(application);
    apiService = ApiClient.getApiService();
    sessionManager = new SessionManager(application);
  }

  // --- GETTERS ---
  public LiveData<Boolean> getIsLoading() {
    return isLoading;
  }

  public LiveData<String> getMensajeGlobal() {
    return mensajeGlobal;
  }

  public LiveData<Integer> getMensajeGlobalVisibilidad() {
    return mensajeGlobalVisibilidad;
  }

  public LiveData<Integer> getMensajeColorTexto() {
    return mensajeColorTexto;
  }

  public LiveData<Integer> getMensajeColorFondo() {
    return mensajeColorFondo;
  }

  public LiveData<Boolean> getHabilitarEliminacionRunners() {
    return habilitarEliminacionRunners;
  }

  public LiveData<String> getEstadoActualEvento() {
    return estadoActualEvento;
  }

  public LiveData<String> getNombreArchivoSeleccionado() {
    return nombreArchivoSeleccionado;
  }

  public LiveData<Boolean> getArchivoEsValido() {
    return archivoEsValido;
  }

  public LiveData<Boolean> getExitoCargaArchivo() {
    return exitoCargaArchivo;
  }

  public LiveData<String> getResumenCargaArchivo() {
    return resumenCargaArchivo;
  }

  public LiveData<String[]> getOpcionesMenuResultados() {
    return opcionesMenuResultados;
  }

  public LiveData<Boolean> getEventVerResultados() {
    return eventVerResultados;
  }

  public LiveData<String> getTextoConfirmacionBaja() {
    return textoConfirmacionBaja;
  }

  public LiveData<List<CategoriaResponse>> getCategoriasPendientesCarga() {
    return categoriasPendientesCarga;
  }

  public LiveData<EventoDetalleResponse> getEventoRaw() {
    return eventoRaw;
  }

  public LiveData<Boolean> getVisibilityBtnResultados() {
    return visibilityBtnResultados;
  }

  public LiveData<String> getUiTitulo() {
    return uiTitulo;
  }

  public LiveData<String> getUiFecha() {
    return uiFecha;
  }

  public LiveData<String> getUiLugar() {
    return uiLugar;
  }

  public LiveData<String> getUiDescripcion() {
    return uiDescripcion;
  }

  public LiveData<String> getUiInscriptos() {
    return uiInscriptos;
  }

  public LiveData<String> getUiCupo() {
    return uiCupo;
  }

  public LiveData<String> getUiEstadoTexto() {
    return uiEstadoTexto;
  }

  public LiveData<Integer> getUiEstadoColor() {
    return uiEstadoColor;
  }

  public LiveData<String> getUiDistanciaTipo() {
    return uiDistanciaTipo;
  }

  public LiveData<Integer> getUiVisibilidadRunners() {
    return uiVisibilidadRunners;
  }

  public LiveData<Integer> getUiVisibilidadEstadoCategoria() {
    return uiVisibilidadEstadoCategoria;
  }

  public LiveData<String> getDialogErrorTexto() { return dialogErrorTexto; }
  public LiveData<Integer> getDialogErrorVisibilidad() { return dialogErrorVisibilidad; }

  public LiveData<String> getUiGeneroPrecio() {
    return uiGeneroPrecio;
  }

  public LiveData<Boolean> getUiVisibilidadDatosCategoria() {
    return uiVisibilidadDatosCategoria;
  }


  /*public LiveData<Boolean> getDialogDismiss() {
    return dialogDismiss;
  } 27-08*/

  public LiveData<List<CategoriaResponse>> getListaCategorias() {
    return listaCategorias;
  }

  public LiveData<List<CategoriasInfoAdapter.CategoriaUI>> getListaCategoriasUI() { return listaCategoriasUI; }
  public LiveData<List<RunnerSimpleAdapter.RunnerUI>> getListaRunnersDialog() { return listaRunnerDialog; }
  public LiveData<Integer> getUiVisibilidadCarga() { return uiVisibilidadCarga; }
  public LiveData<String[]> getOpcionesSpinnerCategoria() { return opcionesSpinnerCategoria; }

  public CategoriaResponse getCategoriaSeleccionada() {
    return categoriaSeleccionada;
  }

  public LiveData<Integer> getPosicionPreseleccionadaCategoria() {
    return posicionPreseleccionadaCategoria;
  }

  public LiveData<String> getErrorMotivoCategoriaTexto() {
    return errorMotivoCategoriaTexto;
  }

  public LiveData<Integer> getErrorMotivoCategoriaVisibilidad() {
    return errorMotivoCategoriaVisibilidad;
  }

  public LiveData<String[]> getEstadosCategoriasValidos() {
    return estadosCategoriasValidos;
  }

  public void cerrarRunnersOverlay() {
    uiVisibilidadRunners.setValue(View.GONE);
  }

  private final MutableLiveData<Integer> posicionEstadoEvento = new MutableLiveData<>(0);
  private final MutableLiveData<Integer> uiVisibilidadEstado = new MutableLiveData<>(View.GONE);
  private final MutableLiveData<String> motivoEventoTexto = new MutableLiveData<>("");

  public LiveData<Integer> getPosicionEstadoEvento() { return posicionEstadoEvento; }
  public LiveData<Integer> getUiVisibilidadEstado() { return uiVisibilidadEstado; }
  public LiveData<String> getMotivoEventoTexto() { return motivoEventoTexto; }

  public void prepararDialogoEstado() {
    String actual = estadoActualEvento.getValue();
    int index = 0;
    if (actual != null) {
      for (int i = 0; i < estadosEventoValidos.length; i++) {
        if (estadosEventoValidos[i].equalsIgnoreCase(actual)) {
          index = i;
          break;
        }
      }
    }
    posicionEstadoEvento.setValue(index);
    dialogErrorTexto.setValue("");
    dialogErrorVisibilidad.setValue(View.GONE);
    uiVisibilidadEstado.setValue(View.VISIBLE);
  }

  public void solicitarConfirmacionBaja(InscriptoEventoResponse runner, int idCat) {
    if (runner != null && runner.getRunner() != null) {
      this.runnerBajaPendiente = runner;
      this.idCatBajaPendiente = idCat;
      textoConfirmacionBaja.setValue("¿Confirmar baja de " + runner.getRunner().getNombre() + "?");
    }
  }

  public void confirmarBajaRunnerPendiente() {
    if (runnerBajaPendiente != null && categoriaSeleccionada != null) {
      darDeBajaRunner(runnerBajaPendiente.getIdInscripcion(), "Baja organizador", categoriaSeleccionada.getIdEvento(), idCatBajaPendiente);
    }
    limpiarConfirmacionBaja();
  }

  public void limpiarConfirmacionBaja() {
    runnerBajaPendiente = null;
    idCatBajaPendiente = 0;
    textoConfirmacionBaja.setValue(null);
  }

  public void onCategoriaClickNormal(CategoriaResponse categoria) {
    if (categoria != null) {
      this.categoriaSeleccionada = categoria;
      cargarRunnersDeCategoria(categoria.getIdEvento(), categoria.getIdCategoria());
      uiVisibilidadRunners.setValue(View.VISIBLE);
    }
  }

  public void onCategoriaClickLargo(CategoriaResponse categoria) {
    if (categoria != null) {
      this.categoriaSeleccionada = categoria;
      String[] estados = estadosCategoriasValidos.getValue();
      String estadoActual = categoria.getEstado();
      int indiceEncontrado = 0;

      if (estados != null && estadoActual != null) {
        for (int i = 0; i < estados.length; i++) {
          if (estados[i].equalsIgnoreCase(estadoActual)) {
            indiceEncontrado = i;
            break;
          }
        }
      }
      posicionPreseleccionadaCategoria.setValue(indiceEncontrado);
      errorMotivoCategoriaTexto.setValue(null);
      uiVisibilidadEstadoCategoria.setValue(View.VISIBLE);
    }
  }

  public void cerrarEstadoCategoria() {
    uiVisibilidadEstadoCategoria.setValue(View.GONE);
    errorMotivoCategoriaVisibilidad.setValue(View.GONE);
  }

  public void guardarNuevoEstadoCategoria(int posicionSeleccionada, String motivoInput) {
    if (motivoInput == null || motivoInput.trim().isEmpty()) {
      errorMotivoCategoriaTexto.setValue("El motivo es obligatorio para notificar a los runners");
      errorMotivoCategoriaVisibilidad.setValue(View.VISIBLE);
      return;
    }

    errorMotivoCategoriaVisibilidad.setValue(View.GONE);

    if (categoriaSeleccionada == null) return;
    String[] estados = estadosCategoriasValidos.getValue();
    if (estados == null || posicionSeleccionada < 0 || posicionSeleccionada >= estados.length)
      return;

    String nuevoEstado = estados[posicionSeleccionada];
    isLoading.setValue(true);
    int idEv = categoriaSeleccionada.getIdEvento();
    int idCat = categoriaSeleccionada.getIdCategoria();

    CambiarEstadoCategoriaRequest req = new CambiarEstadoCategoriaRequest(nuevoEstado, motivoInput.trim());

    String token = sessionManager.leerToken();
    if (token != null && !token.isEmpty()) {
      apiService.cambiarEstadoCategoria("Bearer " + token, idEv, idCat, req).enqueue(new Callback<ResponseBody>() {
        @Override
        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
          isLoading.setValue(false);
          if (response.isSuccessful()) {
            errorMotivoCategoriaTexto.setValue(null);
            uiVisibilidadEstadoCategoria.setValue(View.GONE);
            lanzarMensaje("Estado de categoría actualizado correctamente", 1);
            cargarDetalle(idEv);
          } else {
            String msjError = "No se puede actualizar";
            try {
              if (response.errorBody() != null) {
                String errorJson = response.errorBody().string();
                JSONObject jsonObject = new JSONObject(errorJson);
                if (jsonObject.has("message")) msjError = jsonObject.getString("message");
              }
            } catch (Exception e) {
              e.printStackTrace();
            }
            errorMotivoCategoriaTexto.setValue(msjError);
            errorMotivoCategoriaVisibilidad.setValue(View.VISIBLE);
          }
        }

        @Override
        public void onFailure(Call<ResponseBody> call, Throwable t) {
          isLoading.setValue(false);
          errorMotivoCategoriaTexto.setValue("Error de conexión");
          errorMotivoCategoriaVisibilidad.setValue(View.VISIBLE);
        }
      });
    } else {
      isLoading.setValue(false);
      errorMotivoCategoriaTexto.setValue("No hay sesión activa. Por favor inicie sesión nuevamente.");
      errorMotivoCategoriaVisibilidad.setValue(View.VISIBLE);
    }
  }

  public void lanzarMensaje(String msg, int tipo) {
    if (tipo == 1) {
      mensajeColorTexto.setValue(Color.parseColor("#1B5E20"));
      mensajeColorFondo.setValue(Color.parseColor("#C8E6C9"));
    } else if (tipo == 2) {
      mensajeColorTexto.setValue(Color.parseColor("#B71C1C"));
      mensajeColorFondo.setValue(Color.parseColor("#FFCDD2"));
    } else {
      mensajeColorTexto.setValue(Color.BLACK);
      mensajeColorFondo.setValue(Color.parseColor("#F5F5F5"));
    }
    mensajeGlobal.setValue(msg);
    mensajeGlobalVisibilidad.setValue(View.VISIBLE);

    new Handler(Looper.getMainLooper()).postDelayed(() -> {
      mensajeGlobalVisibilidad.setValue(View.GONE);
    }, 5000);
  }

  public void procesarArchivoSeleccionado(Uri uri) {
    if (uri == null) return;
    lanzarMensaje("Analizando archivo...", 0);
    new Thread(() -> {
      Context context = getApplication();
      if (esArchivoMuyGrande(context, uri)) {
        archivoListoParaSubir = null;
        archivoEsValido.postValue(false);
        mensajeGlobal.postValue("El archivo es demasiado pesado. Máx 5MB.");
        return;
      }
      File tempFile = copiarUriAArchivo(context, uri);
      if (tempFile != null) {
        String nombre = tempFile.getName();
        nombreArchivoSeleccionado.postValue(nombre);
        if (nombre.toLowerCase().endsWith(".csv") || nombre.toLowerCase().endsWith(".txt")) {
          archivoListoParaSubir = tempFile;
          archivoEsValido.postValue(true);
          mensajeGlobal.postValue("Archivo listo.");
        } else {
          archivoListoParaSubir = null;
          archivoEsValido.postValue(false);
          mensajeGlobal.postValue("Formato incorrecto. Solo .csv");
        }
      } else {
        archivoListoParaSubir = null;
        archivoEsValido.postValue(false);
        mensajeGlobal.postValue("Error al leer archivo");
      }
    }).start();
  }

  private boolean esArchivoMuyGrande(Context context, Uri uri) {
    Cursor cursor = null;
    try {
      cursor = context.getContentResolver().query(uri, null, null, null, null);
      if (cursor != null && cursor.moveToFirst()) {
        int sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE);
        if (!cursor.isNull(sizeIndex)) return cursor.getLong(sizeIndex) > (5 * 1024 * 1024);
      }
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      if (cursor != null) cursor.close();
    }
    return false;
  }

  private File copiarUriAArchivo(Context context, Uri uri) {
    try {
      InputStream is = context.getContentResolver().openInputStream(uri);
      if (is == null) return null;
      File temp = new File(context.getCacheDir(), "upload_temp.csv");
      if (temp.exists()) temp.delete();
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

  public void procesarSubidaArchivo(int idEvento, int posicionSeleccionada) {
    List<CategoriaResponse> pendientes = categoriasPendientesCarga.getValue();
    if (pendientes != null && posicionSeleccionada >= 0 && posicionSeleccionada < pendientes.size()) {
      int idCategoria = pendientes.get(posicionSeleccionada).getIdCategoria();
      archivoEsValido.setValue(false); // Deshabilita el botón
      subirArchivoGuardado(idEvento, idCategoria);
    }
  }

  public void subirArchivoGuardado(int idEvento, int idCategoria) {
    if (archivoListoParaSubir == null) return;
    lanzarMensaje("Subiendo...", 0);

    String token = sessionManager.leerToken();
    if (token == null) {
      lanzarMensaje("No hay sesión activa", 2);
      return;
    }

    RequestBody idEventoBody = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(idEvento));
    RequestBody idCategoriaBody = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(idCategoria));
    RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), archivoListoParaSubir);
    MultipartBody.Part bodyArchivo = MultipartBody.Part.createFormData("Archivo", archivoListoParaSubir.getName(), requestFile);

    apiService.cargarArchivoResultados("Bearer " + token, idEventoBody, idCategoriaBody, bodyArchivo).enqueue(new Callback<ResponseBody>() {
      @Override
      public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
        if (response.isSuccessful()) {
          existenResultados = true;
          archivoListoParaSubir = null;
          
          // Refrescamos las listas para recalcular qué categorías faltan
          verificarSiExistenResultados(idEvento);

          // NUEVO: Capturar y parsear el detalle de fallidos del JSON
          try {
            if (response.body() != null) {
              String jsonStr = response.body().string();
              JSONObject jsonObj = new JSONObject(jsonStr);

              String mensajeGral = jsonObj.optString("message", "Carga completada");
              StringBuilder resumen = new StringBuilder(mensajeGral);

              if (jsonObj.has("detalles")) {
                JSONObject detalles = jsonObj.getJSONObject("detalles");
                int fallidos = detalles.optInt("fallidos", 0);

                if (fallidos > 0 && detalles.has("errores")) {
                  resumen.append("\n\nDetalle de errores:\n");
                  org.json.JSONArray erroresArr = detalles.getJSONArray("errores");

                  for (int i = 0; i < erroresArr.length(); i++) {
                    JSONObject err = erroresArr.getJSONObject(i);
                    resumen.append("• DNI ").append(err.getInt("dni"))
                      .append(": ").append(err.getString("motivo")).append("\n");
                  }
                }
              }
              uiVisibilidadCarga.postValue(View.GONE);
              exitoCargaArchivo.postValue(true);
              resumenCargaArchivo.postValue(resumen.toString());
              cargarDetalle(idEvento);
            }
          } catch (Exception e) {
            e.printStackTrace();
          }

        } else {
          lanzarMensaje("Error en servidor al procesar archivo", 2);
        }
      }

      @Override
      public void onFailure(Call<ResponseBody> call, Throwable t) {
        lanzarMensaje("Fallo de conexión", 2);
      }
    });
  }

  public void resetResumenCargaArchivo() {
    resumenCargaArchivo.setValue(null);
  }

  public void resetExitoCarga() {
    exitoCargaArchivo.setValue(null);
  }

  public void solicitarMenuResultados() {
    if (todosLosResultadosCargados) {
      // Si tod se cargo, ocultamos el boton de carga del menu
      opcionesMenuResultados.setValue(new String[]{"Ver Resultados Oficiales"});
    } else {
      // Aún hay categorías sin CSV cargados
      if (existenResultados) {
        opcionesMenuResultados.setValue(new String[]{"Cargar Resultados (CSV)", "Ver Resultados Oficiales"});
      } else {
        opcionesMenuResultados.setValue(new String[]{"Cargar Resultados (CSV)"});
      }
    }
  }

  public void procesarSeleccionMenuResultados(String opcion) {
    if (opcion == null) return;
    if (opcion.contains("Cargar")) {
      List<CategoriaResponse> pendientes = categoriasPendientesCarga.getValue();
      if (pendientes == null || pendientes.isEmpty()) {
        lanzarMensaje("Todas las categorias tienen resultados", 0);
      } else {
        String[] nombres = new String[pendientes.size()];
        for(int i = 0; i < pendientes.size(); i++) nombres[i] = pendientes.get(i).getNombre();
        opcionesSpinnerCategoria.setValue(nombres);
        uiVisibilidadCarga.setValue(View.VISIBLE);
      }
    } else if (opcion.contains("Ver")) {
      eventVerResultados.setValue(true);
    }
  }

  public void cerrarCarga() {
    uiVisibilidadCarga.setValue(View.GONE);
    archivoListoParaSubir = null;
    archivoEsValido.setValue(false);
    nombreArchivoSeleccionado.setValue("Ningún archivo seleccionado");
  }

  public void resetEventVerResultados() {
    eventVerResultados.setValue(null);
  }

  public void resetOpcionesMenu() {
    opcionesMenuResultados.setValue(null);
  }

  public void cargarDetalle(int idEvento) {
    isLoading.setValue(true);
    String token = sessionManager.leerToken();
    if (token != null) {
      apiService.obtenerEventoPorId("Bearer " + token, idEvento).enqueue(new Callback<EventoDetalleResponse>() {
        @Override
        public void onResponse(Call<EventoDetalleResponse> call, Response<EventoDetalleResponse> response) {
          isLoading.setValue(false);
          if (response.isSuccessful() && response.body() != null) {
            eventoRaw.setValue(response.body());
            mapearDatosAUI(response.body());
            verificarSiExistenResultados(idEvento);
          } else lanzarMensaje("Error al cargar evento", 2);
        }

        @Override
        public void onFailure(Call<EventoDetalleResponse> call, Throwable t) {
          isLoading.setValue(false);
          lanzarMensaje("Error de conexión", 2);
        }
      });
    } else {
      isLoading.setValue(false);
      lanzarMensaje("No hay sesión activa", 2);
    }
  }

  private void verificarSiExistenResultados(int idEvento) {
    apiService.obtenerResultadosEvento(idEvento).enqueue(new Callback<ResultadosEventoResponse>() {
      @Override
      public void onResponse(Call<ResultadosEventoResponse> call, Response<ResultadosEventoResponse> response) {
        if (response.isSuccessful() && response.body() != null) {
          List<ResultadosEventoResponse.ResultadoEventoItem> listaRes = response.body().getResultados();
          existenResultados = (listaRes != null && !listaRes.isEmpty());

          // Comparamos los resultados existentes contra todas las categorías
          List<CategoriaResponse> todas = listaCategorias.getValue();
          if (todas != null && !todas.isEmpty()) {
            List<CategoriaResponse> pendientes = new ArrayList<>();
            for (CategoriaResponse cat : todas) {
              boolean cargada = false;
              if (listaRes != null) {
                for (ResultadosEventoResponse.ResultadoEventoItem r : listaRes) {
                  if (cat.getNombre().equalsIgnoreCase(r.getNombreCategoria())) {
                    cargada = true;
                    break;
                  }
                }
              }
              if (!cargada) pendientes.add(cat);
            }
            categoriasPendientesCarga.setValue(pendientes);
            todosLosResultadosCargados = pendientes.isEmpty(); // Si no hay pendientes, ya subio tod
          }
        } else {
          existenResultados = false;
          todosLosResultadosCargados = false;
          categoriasPendientesCarga.setValue(listaCategorias.getValue());
        }
      }

      @Override
      public void onFailure(Call<ResultadosEventoResponse> call, Throwable t) {
        existenResultados = false;
        todosLosResultadosCargados = false;
        categoriasPendientesCarga.setValue(listaCategorias.getValue());
      }
    });
  }

  private void mapearDatosAUI(EventoDetalleResponse evento) {
    uiTitulo.setValue(evento.getNombre());
    uiLugar.setValue(evento.getLugar());
    uiDescripcion.setValue(evento.getDescripcion());
    uiInscriptos.setValue(String.valueOf(evento.getInscriptosActuales()));
    uiCupo.setValue(evento.getCupoTotal() != null ? String.valueOf(evento.getCupoTotal()) : "Ilimitado");
    if (evento.getFechaHora() != null) uiFecha.setValue(evento.getFechaHora().replace("T", " "));

    String estado = (evento.getEstado() != null) ? evento.getEstado().toUpperCase() : "";
    uiEstadoTexto.setValue(estado);
    estadoActualEvento.setValue(estado.toLowerCase()); // Expone el string limpio sin que la vista pregunte

    habilitarEliminacionRunners.setValue(!("FINALIZADO".equals(estado) || "CANCELADO".equals(estado)));

    switch (estado) {
      case "PUBLICADO":
        uiEstadoColor.setValue(Color.parseColor("#2E7D32"));
        break;
      case "SUSPENDIDO":
        uiEstadoColor.setValue(Color.parseColor("#FF9800"));
        break;
      case "FINALIZADO":
        uiEstadoColor.setValue(Color.GRAY);
        break;
      case "CANCELADO":
        uiEstadoColor.setValue(Color.RED);
        break;
      default:
        uiEstadoColor.setValue(Color.BLACK);
    }

    if (evento.getCategorias() != null && !evento.getCategorias().isEmpty()) {
      StringBuilder nombresCategorias = new StringBuilder();
      for (int i = 0; i < evento.getCategorias().size(); i++) {
        String nom = evento.getCategorias().get(i).getNombre();
        nombresCategorias.append(nom != null ? nom : "General");
        if (i < evento.getCategorias().size() - 1) nombresCategorias.append(", ");
      }
      String tipoTexto = "";
      if (evento.getTipoEvento() != null && !evento.getTipoEvento().isEmpty()) {
        String raw = evento.getTipoEvento();
        tipoTexto = raw.substring(0, 1).toUpperCase() + raw.substring(1);
      }
      String textoFinal = nombresCategorias.toString();
      if (!tipoTexto.isEmpty()) textoFinal += "  |  " + tipoTexto;
      uiDistanciaTipo.setValue(textoFinal);
      if (evento.getCategorias().size() == 1)
        uiGeneroPrecio.setValue("$" + evento.getCategorias().get(0).getPrecio());
      else uiGeneroPrecio.setValue("Múltiples categorías y precios");

      uiVisibilidadDatosCategoria.setValue(true);
    } else {
      if (evento.getTipoEvento() != null) {
        uiDistanciaTipo.setValue(evento.getTipoEvento().toUpperCase());
        uiVisibilidadDatosCategoria.setValue(true);
      } else uiVisibilidadDatosCategoria.setValue(false);
    }

    if (evento.getCategorias() != null) {
      listaCategorias.setValue(evento.getCategorias());
      List<CategoriasInfoAdapter.CategoriaUI> uiList = new ArrayList<>();
      evento.getCategorias().forEach(item -> {
        String precio = "$ " + item.getPrecio();
        String genero = "Mixto";
        if ("F".equalsIgnoreCase(item.getGenero())) genero = "Fem";
        if ("M".equalsIgnoreCase(item.getGenero())) genero = "Masc";
        String info = item.getEdadMinima() + "-" + item.getEdadMaxima() + " años | " + genero;
        String inscriptos = "Inscriptos: " + item.getInscriptosActuales();
        String est = item.getEstado() != null ? item.getEstado().toLowerCase() : "";
        int colorFondo = android.graphics.Color.WHITE;
        switch (est) {
            case "programada": colorFondo = android.graphics.Color.parseColor("#4CAF50"); break;
            case "retrasada": colorFondo = android.graphics.Color.parseColor("#FFC107"); break;
            case "cancelada": colorFondo = android.graphics.Color.parseColor("#F44336"); break;
            case "finalizada": colorFondo = android.graphics.Color.parseColor("#9E9E9E"); break;
            case "suspendida": 
            case "suspendido": colorFondo = android.graphics.Color.parseColor("#FF9800"); break;
        }

        uiList.add(new CategoriasInfoAdapter.CategoriaUI(
          item.getNombre() != null ? item.getNombre() : "General",
          precio, info, inscriptos, colorFondo, item));
      });
      listaCategoriasUI.setValue(uiList);
    }
    visibilityBtnResultados.setValue("FINALIZADO".equals(estado));
  }

  public void cargarRunnersDeCategoria(int idEvento, int idCategoria) {
    String token = sessionManager.leerToken();
    if (token != null) {
      apiService.obtenerInscriptos("Bearer " + token, idEvento, null, 1, 100).enqueue(new Callback<ListaInscriptosResponse>() {
        @Override
        public void onResponse(Call<ListaInscriptosResponse> call, Response<ListaInscriptosResponse> response) {
          if (response.isSuccessful() && response.body() != null && response.body().getInscripciones() != null) {
            List<RunnerSimpleAdapter.RunnerUI> uiList = new ArrayList<>();
            boolean puedeEliminar = Boolean.TRUE.equals(habilitarEliminacionRunners.getValue());
            for (InscriptoEventoResponse i : response.body().getInscripciones()) {
              if (i.getIdCategoria() == idCategoria && "pagado".equalsIgnoreCase(i.getEstadoPago())) {
                String nombreCompleto = "";
                String dniTexto = "DNI: -";
                if (i.getRunner() != null) {
                  String nom = i.getRunner().getNombre() != null ? i.getRunner().getNombre() : "";
                  String ape = i.getRunner().getApellido() != null ? i.getRunner().getApellido() : "";
                  nombreCompleto = (nom + " " + ape).trim();
                  if (i.getRunner().getDni() != null) {
                    dniTexto = "DNI: " + i.getRunner().getDni();
                  }
                }
                String estadoPago = i.getEstadoPago() != null ? i.getEstadoPago().toUpperCase() : "";
                boolean runnerCancelado = "cancelado".equalsIgnoreCase(i.getEstadoPago());
                int colorEstado = runnerCancelado ? Color.RED : Color.BLACK;
                int visibilidadBaja = (puedeEliminar && !runnerCancelado) ? View.VISIBLE : View.GONE;

                uiList.add(new RunnerSimpleAdapter.RunnerUI(
                    nombreCompleto,
                    dniTexto,
                    estadoPago,
                    colorEstado,
                    visibilidadBaja,
                    i
                ));
              }
            }
            listaRunnerDialog.setValue(uiList);
          } else listaRunnerDialog.setValue(new ArrayList<>());
        }

        @Override
        public void onFailure(Call<ListaInscriptosResponse> call, Throwable t) {
          listaRunnerDialog.setValue(new ArrayList<>());
        }
      });
    } else {
      listaRunnerDialog.setValue(new ArrayList<>());
    }
  }

  public void darDeBajaRunner(int idInsc, String motivo, int idEvento, int idCat) {
    String token = sessionManager.leerToken();
    if (token != null) {
      MotivoBajaRequest request = new MotivoBajaRequest(motivo);
      apiService.darDeBajaRunner("Bearer " + token, idInsc, request).enqueue(new Callback<ResponseBody>() {
        @Override
        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
          if (response.isSuccessful()) {
            lanzarMensaje("Baja exitosa", 1);
            cargarRunnersDeCategoria(idEvento, idCat);
            cargarDetalle(idEvento);
          } else lanzarMensaje("Error en la baja", 2);
        }

        @Override
        public void onFailure(Call<ResponseBody> call, Throwable t) {
          lanzarMensaje("Error conexión", 2);
        }
      });
    } else {
      lanzarMensaje("No hay sesión activa", 2);
    }
  }

  public void cerrarDialogoEstado() {
    motivoEventoTexto.setValue("");
    uiVisibilidadEstado.setValue(View.GONE);
  }

  public void procesarCambioEstadoEvento(int idEvento, int estadoIndex, String motivo) {
    if (estadoIndex < 0 || estadoIndex >= estadosEventoValidos.length) {
      dialogErrorTexto.setValue("Seleccione un estado válido");
      dialogErrorVisibilidad.setValue(View.VISIBLE);
      return;
    }
    String estadoNuevo = estadosEventoValidos[estadoIndex];

    dialogErrorVisibilidad.setValue(View.GONE);
    CambiarEstadoRequest req = new CambiarEstadoRequest(estadoNuevo, motivo);
    isLoading.setValue(true);

    String token = sessionManager.leerToken();
    if (token != null) {
      apiService.cambiarEstado("Bearer " + token, idEvento, req).enqueue(new Callback<ResponseBody>() {
        @Override
        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
          isLoading.setValue(false);
          if (response.isSuccessful()) {
            cerrarDialogoEstado();
            lanzarMensaje("Estado actualizado correctamente", 1);
            EventoDetalleResponse actual = eventoRaw.getValue();
            if (actual != null) {
              actual.setEstado(estadoNuevo);
              mapearDatosAUI(actual);
            } else cargarDetalle(idEvento);
          } else {
            String msjError = "No se puede actualizar";
            try {
              if (response.errorBody() != null) {
                String errorJson = response.errorBody().string();
                JSONObject jsonObject = new JSONObject(errorJson);
                if (jsonObject.has("error")) msjError = jsonObject.getString("error");
                else if (jsonObject.has("message")) msjError = jsonObject.getString("message");
              }
            } catch (Exception e) {
              e.printStackTrace();
            }
            dialogErrorTexto.setValue(msjError);
            dialogErrorVisibilidad.setValue(View.VISIBLE);
          }
        }

        @Override
        public void onFailure(Call<ResponseBody> call, Throwable t) {
          isLoading.setValue(false);
          dialogErrorTexto.setValue("Error de conexión");
          dialogErrorVisibilidad.setValue(View.VISIBLE);
        }
      });
    } else {
      isLoading.setValue(false);
      dialogErrorTexto.setValue("No hay sesión activa");
      dialogErrorVisibilidad.setValue(View.VISIBLE);
    }
  }

  public boolean habilitarEliminacionRunner(){
    Boolean valor= habilitarEliminacionRunners.getValue();
    return valor != null && valor; //por si por algun motivo es null, devuelve false
  }

}

