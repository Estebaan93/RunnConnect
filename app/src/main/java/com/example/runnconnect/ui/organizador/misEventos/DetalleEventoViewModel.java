package com.example.runnconnect.ui.organizador.misEventos;

import android.app.Application;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.provider.OpenableColumns;

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

  // Enum nativo de Java para evitar Magic Strings en la UI
  public enum AccionResultados { CARGAR, VER }

  // --- LIVE DATA ---
  private final MutableLiveData<String> nombreArchivoSeleccionado = new MutableLiveData<>();
  private final MutableLiveData<Boolean> archivoEsValido = new MutableLiveData<>(false);

  // MVVM Puro: Evento binario de éxito vs Mensajes de error en UI
  private final MutableLiveData<Boolean> exitoCargaArchivo = new MutableLiveData<>();


  // NUEVO: LiveData para el resumen detallado de la carga (errores de DNI)
  private final MutableLiveData<String> resumenCargaArchivo = new MutableLiveData<>();

  private final MutableLiveData<Boolean> eventShowRunnersDialog = new MutableLiveData<>(false);
  private final MutableLiveData<Boolean> eventShowCambiarEstadoCategoria = new MutableLiveData<>(false);

  private final MutableLiveData<String[]> estadosCategoriasValidos = new MutableLiveData<>(
    new String[]{"programada", "retrasada", "cancelada", "finalizada", "suspendido"}
  );
  private final MutableLiveData<Integer> posicionPreseleccionadaCategoria = new MutableLiveData<>(0);
  private final MutableLiveData<String> errorMotivoCategoria = new MutableLiveData<>();

  private final MutableLiveData<String[]> opcionesMenuResultados = new MutableLiveData<>();
  private final MutableLiveData<AccionResultados> accionNavegacionResultados = new MutableLiveData<>();


  // NUEVO: Lista de categorías que aún NO tienen CSV cargado
  private final MutableLiveData<List<CategoriaResponse>> categoriasPendientesCarga = new MutableLiveData<>();
  private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
  private final MutableLiveData<String> mensajeGlobal = new MutableLiveData<>();
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

  private final MutableLiveData<String> dialogError = new MutableLiveData<>();
  private final MutableLiveData<Boolean> dialogDismiss = new MutableLiveData<>();
  private final MutableLiveData<List<CategoriaResponse>> listaCategorias = new MutableLiveData<>();
  private final MutableLiveData<List<InscriptoEventoResponse>> listaRunnerDialog = new MutableLiveData<>();

  public DetalleEventoViewModel(@NonNull Application application) {
    super(application);
    apiService = ApiClient.getApiService();
    sessionManager = new SessionManager(application);
  }

  // --- GETTERS ---
  public LiveData<Boolean> getIsLoading() { return isLoading; }
  public LiveData<String> getMensajeGlobal() { return mensajeGlobal; }
  public LiveData<Integer> getMensajeColorTexto() { return mensajeColorTexto; }
  public LiveData<Integer> getMensajeColorFondo() { return mensajeColorFondo; }
  public LiveData<Boolean> getHabilitarEliminacionRunners() { return habilitarEliminacionRunners; }
  public LiveData<String> getEstadoActualEvento() { return estadoActualEvento; }

  public LiveData<String> getNombreArchivoSeleccionado() { return nombreArchivoSeleccionado; }
  public LiveData<Boolean> getArchivoEsValido() { return archivoEsValido; }
  public LiveData<Boolean> getExitoCargaArchivo() { return exitoCargaArchivo; }
  public LiveData<String> getResumenCargaArchivo() { return resumenCargaArchivo; }

  public LiveData<String[]> getOpcionesMenuResultados() { return opcionesMenuResultados; }
  public LiveData<AccionResultados> getAccionNavegacionResultados() { return accionNavegacionResultados; }
  public LiveData<List<CategoriaResponse>> getCategoriasPendientesCarga() { return categoriasPendientesCarga; }

  public LiveData<EventoDetalleResponse> getEventoRaw() { return eventoRaw; }
  public LiveData<Boolean> getVisibilityBtnResultados() { return visibilityBtnResultados; }

  public LiveData<String> getUiTitulo() { return uiTitulo; }
  public LiveData<String> getUiFecha() { return uiFecha; }
  public LiveData<String> getUiLugar() { return uiLugar; }
  public LiveData<String> getUiDescripcion() { return uiDescripcion; }
  public LiveData<String> getUiInscriptos() { return uiInscriptos; }
  public LiveData<String> getUiCupo() { return uiCupo; }
  public LiveData<String> getUiEstadoTexto() { return uiEstadoTexto; }
  public LiveData<Integer> getUiEstadoColor() { return uiEstadoColor; }
  public LiveData<String> getUiDistanciaTipo() { return uiDistanciaTipo; }
  public LiveData<String> getUiGeneroPrecio() { return uiGeneroPrecio; }
  public LiveData<Boolean> getUiVisibilidadDatosCategoria() { return uiVisibilidadDatosCategoria; }
  public LiveData<String> getDialogError() { return dialogError; }
  public LiveData<Boolean> getDialogDismiss() { return dialogDismiss; }
  public LiveData<List<CategoriaResponse>> getListaCategorias() { return listaCategorias; }
  public LiveData<List<InscriptoEventoResponse>> getListaRunnersDialog() { return listaRunnerDialog; }

  public CategoriaResponse getCategoriaSeleccionada() { return categoriaSeleccionada; }
  public LiveData<Integer> getPosicionPreseleccionadaCategoria() { return posicionPreseleccionadaCategoria; }
  public LiveData<String> getErrorMotivoCategoria() { return errorMotivoCategoria; }
  public LiveData<String[]> getEstadosCategoriasValidos() { return estadosCategoriasValidos; }

  public void observarEventoRunners(androidx.lifecycle.LifecycleOwner owner, Runnable accion) {
    eventShowRunnersDialog.observe(owner, debeMostrar -> {
      if (Boolean.TRUE.equals(debeMostrar)) {
        eventShowRunnersDialog.setValue(false);
        accion.run();
      }
    });
  }

  public void onCategoriaClickNormal(CategoriaResponse categoria) {
    if (categoria != null) {
      this.categoriaSeleccionada = categoria;
      cargarRunnersDeCategoria(categoria.getIdEvento(), categoria.getIdCategoria());
      eventShowRunnersDialog.setValue(true);
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
      errorMotivoCategoria.setValue(null);
      eventShowCambiarEstadoCategoria.setValue(true);
    }
  }

  public void observarEventoEstadoCategoria(androidx.lifecycle.LifecycleOwner owner, Runnable accion) {
    eventShowCambiarEstadoCategoria.observe(owner, debeMostrar -> {
      if (Boolean.TRUE.equals(debeMostrar)) {
        eventShowCambiarEstadoCategoria.setValue(false);
        accion.run();
      }
    });
  }

  public void guardarNuevoEstadoCategoria(int posicionSeleccionada, String motivoInput) {
    if (motivoInput == null || motivoInput.trim().isEmpty()) {
      errorMotivoCategoria.setValue("El motivo es obligatorio para notificar a los runners");
      return;
    }

    if (categoriaSeleccionada == null) return;
    String[] estados = estadosCategoriasValidos.getValue();
    if (estados == null || posicionSeleccionada < 0 || posicionSeleccionada >= estados.length) return;

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
            errorMotivoCategoria.setValue("DISMISS");
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
            } catch (Exception e) { e.printStackTrace(); }
            lanzarMensaje(msjError, 2);
          }
        }
        @Override
        public void onFailure(Call<ResponseBody> call, Throwable t) {
          isLoading.setValue(false);
          lanzarMensaje("Error de conexión", 2);
        }
      });
    } else {
      isLoading.setValue(false);
      lanzarMensaje("No hay sesión activa. Por favor inicie sesión nuevamente.", 2);
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
  }

  public void procesarArchivoSeleccionado(Uri uri) {
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
    } catch (Exception e) { e.printStackTrace(); }
    finally { if (cursor != null) cursor.close(); }
    return false;
  }

  private File copiarUriAArchivo(Context context, Uri uri) {
    try {
      InputStream is = context.getContentResolver().openInputStream(uri);
      if (is == null) return null;
      File temp = new File(context.getCacheDir(), "upload_temp.csv");
      if(temp.exists()) temp.delete();
      try (FileOutputStream out = new FileOutputStream(temp)) {
        byte[] buffer = new byte[16 * 1024];
        int len;
        while ((len = is.read(buffer)) != -1) { out.write(buffer, 0, len); }
        out.flush();
      }
      is.close();
      return temp;
    } catch (Exception e) { return null; }
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
          exitoCargaArchivo.postValue(true);

          lanzarMensaje("Resultados cargados!",1);

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
              // Enviamos el resumen a la Vista
              resumenCargaArchivo.postValue(resumen.toString());
            }
          } catch (Exception e) {
            e.printStackTrace();
          }

        } else {
          lanzarMensaje("Error en servidor al procesar archivo", 2);
        }
      }
      @Override public void onFailure(Call<ResponseBody> call, Throwable t) {
        lanzarMensaje("Fallo de conexión", 2);
      }
    });
  }

  public void resetResumenCargaArchivo() { resumenCargaArchivo.setValue(null); }
  public void resetExitoCarga() { exitoCargaArchivo.setValue(null); }

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

  public void onOpcionMenuSeleccionada(String opcionSeleccionada) {
    if (opcionSeleccionada.contains("Cargar")) {
      accionNavegacionResultados.setValue(AccionResultados.CARGAR);
    } else {
      accionNavegacionResultados.setValue(AccionResultados.VER);
    }
  }

  public void resetOpcionesMenu() {
    opcionesMenuResultados.setValue(null);
  }
  public void resetAccionNavegacion() {
    accionNavegacionResultados.setValue(null);
  }

  public void cargarDetalle(int idEvento) {
    isLoading.setValue(true);
    String token = sessionManager.leerToken();
    if(token != null) {
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
        @Override public void onFailure(Call<EventoDetalleResponse> call, Throwable t) {
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
            todosLosResultadosCargados = pendientes.isEmpty(); // Si no hay pendientes, ya subió todo
          }
        } else {
          existenResultados = false;
          todosLosResultadosCargados = false;
          categoriasPendientesCarga.setValue(listaCategorias.getValue());
        }
      }
      @Override public void onFailure(Call<ResultadosEventoResponse> call, Throwable t) {
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
    uiCupo.setValue(String.valueOf(evento.getCupoTotal()));
    if (evento.getFechaHora() != null) uiFecha.setValue(evento.getFechaHora().replace("T", " "));

    String estado = (evento.getEstado() != null) ? evento.getEstado().toUpperCase() : "";
    uiEstadoTexto.setValue(estado);
    estadoActualEvento.setValue(estado.toLowerCase()); // Expone el string limpio sin que la vista pregunte

    habilitarEliminacionRunners.setValue(!("FINALIZADO".equals(estado) || "CANCELADO".equals(estado)));

    switch (estado) {
      case "PUBLICADO": uiEstadoColor.setValue(Color.parseColor("#2E7D32")); break;
      case "SUSPENDIDO": uiEstadoColor.setValue(Color.parseColor("#FF9800")); break;
      case "FINALIZADO": uiEstadoColor.setValue(Color.GRAY); break;
      case "CANCELADO": uiEstadoColor.setValue(Color.RED); break;
      default: uiEstadoColor.setValue(Color.BLACK);
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
      if (evento.getCategorias().size() == 1) uiGeneroPrecio.setValue("$" + evento.getCategorias().get(0).getPrecio());
      else uiGeneroPrecio.setValue("Múltiples categorías y precios");

      uiVisibilidadDatosCategoria.setValue(true);
    } else {
      if (evento.getTipoEvento() != null) {
        uiDistanciaTipo.setValue(evento.getTipoEvento().toUpperCase());
        uiVisibilidadDatosCategoria.setValue(true);
      } else uiVisibilidadDatosCategoria.setValue(false);
    }

    if (evento.getCategorias() != null) listaCategorias.setValue(evento.getCategorias());
    visibilityBtnResultados.setValue("FINALIZADO".equals(estado));
  }

  public void cargarRunnersDeCategoria(int idEvento, int idCategoria) {
    String token = sessionManager.leerToken();
    if (token != null) {
      apiService.obtenerInscriptos("Bearer " + token, idEvento, null, 1, 100).enqueue(new Callback<ListaInscriptosResponse>() {
        @Override
        public void onResponse(Call<ListaInscriptosResponse> call, Response<ListaInscriptosResponse> response) {
          if (response.isSuccessful() && response.body() != null && response.body().getInscripciones() != null) {
            List<InscriptoEventoResponse> f = new ArrayList<>();
            for(InscriptoEventoResponse i : response.body().getInscripciones())
              if(i.getIdCategoria() == idCategoria && "pagado".equalsIgnoreCase(i.getEstadoPago())) f.add(i);
            listaRunnerDialog.setValue(f);
          } else listaRunnerDialog.setValue(new ArrayList<>());
        }
        @Override public void onFailure(Call<ListaInscriptosResponse> call, Throwable t) { listaRunnerDialog.setValue(new ArrayList<>()); }
      });
    } else {
      listaRunnerDialog.setValue(new ArrayList<>());
    }
  }

  public void darDeBajaRunner(int idInsc, String motivo, int idEvento, int idCat) {
    String token = sessionManager.leerToken();
    if(token != null) {
      MotivoBajaRequest request = new MotivoBajaRequest(motivo);
      apiService.darDeBajaRunner("Bearer "+token, idInsc, request).enqueue(new Callback<ResponseBody>() {
        @Override
        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
          if (response.isSuccessful()) {
            lanzarMensaje("Baja exitosa", 1);
            cargarRunnersDeCategoria(idEvento, idCat);
            cargarDetalle(idEvento);
          } else lanzarMensaje("Error en la baja", 2);
        }
        @Override public void onFailure(Call<ResponseBody> call, Throwable t) { lanzarMensaje("Error conexión", 2); }
      });
    } else {
      lanzarMensaje("No hay sesión activa", 2);
    }
  }

  public void procesarCambioEstadoEvento(int idEvento, String estadoNuevo, String motivo) {
    if (estadoNuevo.isEmpty()) {
      dialogError.setValue("Seleccione un estado válido");
      return;
    }
    dialogDismiss.setValue(true);
    CambiarEstadoRequest req = new CambiarEstadoRequest(estadoNuevo, motivo);
    isLoading.setValue(true);

    String token = sessionManager.leerToken();
    if (token != null) {
      apiService.cambiarEstado("Bearer " + token, idEvento, req).enqueue(new Callback<ResponseBody>() {
        @Override
        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
          isLoading.setValue(false);
          if (response.isSuccessful()) {
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
              }
            } catch (Exception e) { e.printStackTrace(); }
            lanzarMensaje(msjError, 2);
          }
        }
        @Override public void onFailure(Call<ResponseBody> call, Throwable t) {
          isLoading.setValue(false);
          lanzarMensaje("Error conexión", 2);
        }
      });
    } else {
      isLoading.setValue(false);
      lanzarMensaje("No hay sesión activa", 2);
    }
  }

  public void limpiarMensajeGlobal() {
    mensajeGlobal.setValue(null);
  }
}