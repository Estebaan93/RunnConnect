package com.example.runnconnect.ui.organizador.misEventos;

import android.app.Application;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.runnconnect.R;
import com.example.runnconnect.data.repositorio.EventoRepositorio;
import com.example.runnconnect.data.repositorio.InscripcionRepositorio;
import com.example.runnconnect.data.repositorio.ResultadoRepositorio;
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
  private final EventoRepositorio repositorio;
  private final InscripcionRepositorio inscripcionRepositorio;
  private final ResultadoRepositorio resultadoRepositorio;

  // --- VARIABLES INTERNAS ---
  private boolean existenResultados = false;
  private File archivoListoParaSubir = null;

  // --- LIVE DATA ---
  private final MutableLiveData<String> nombreArchivoSeleccionado = new MutableLiveData<>();
  private final MutableLiveData<Boolean> archivoEsValido = new MutableLiveData<>(false);
  private final MutableLiveData<String> mensajeCargaArchivo = new MutableLiveData<>();

  // Menú
  private final MutableLiveData<String[]> opcionesMenuResultados = new MutableLiveData<>();
  private final MutableLiveData<String> accionNavegacionResultados = new MutableLiveData<>();

  // UI Generales
  private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
  private final MutableLiveData<String> mensajeGlobal = new MutableLiveData<>();

  // Binding
  private final MutableLiveData<EventoDetalleResponse> eventoRaw = new MutableLiveData<>();
  private final MutableLiveData<Integer> visibilityBtnResultados = new MutableLiveData<>(View.GONE);
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
  private final MutableLiveData<Integer> uiVisibilidadDatosCategoria = new MutableLiveData<>(View.GONE);

  // Dialogs
  private final MutableLiveData<String> dialogError = new MutableLiveData<>();
  private final MutableLiveData<Boolean> dialogDismiss = new MutableLiveData<>();
  private final MutableLiveData<List<CategoriaResponse>> listaCategorias = new MutableLiveData<>();
  private final MutableLiveData<List<InscriptoEventoResponse>> listaRunnerDialog = new MutableLiveData<>();

  public DetalleEventoViewModel(@NonNull Application application) {
    super(application);
    repositorio = new EventoRepositorio(application);
    inscripcionRepositorio = new InscripcionRepositorio(application);
    resultadoRepositorio = new ResultadoRepositorio(application);
  }

  // --- GETTERS ---
  public LiveData<Boolean> getIsLoading() { return isLoading; }
  public LiveData<String> getMensajeGlobal() { return mensajeGlobal; }
  public LiveData<String> getNombreArchivoSeleccionado() { return nombreArchivoSeleccionado; }
  public LiveData<Boolean> getArchivoEsValido() { return archivoEsValido; }
  public LiveData<String> getMensajeCargaArchivo() { return mensajeCargaArchivo; }
  public LiveData<String[]> getOpcionesMenuResultados() { return opcionesMenuResultados; }
  public LiveData<String> getAccionNavegacionResultados() { return accionNavegacionResultados; }

  public LiveData<EventoDetalleResponse> getEventoRaw() { return eventoRaw; }
  public LiveData<Integer> getVisibilityBtnResultados() { return visibilityBtnResultados; }

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
  public LiveData<Integer> getUiVisibilidadDatosCategoria() { return uiVisibilidadDatosCategoria; }
  public LiveData<String> getDialogError() { return dialogError; }
  public LiveData<Boolean> getDialogDismiss() { return dialogDismiss; }
  public LiveData<List<CategoriaResponse>> getListaCategorias() { return listaCategorias; }
  public LiveData<List<InscriptoEventoResponse>> getListaRunnersDialog() { return listaRunnerDialog; }


  // 1. PROCESAMIENTO DE ARCHIVO (SIN LOADER VISUAL - ANTI ANR)
  public void procesarArchivoSeleccionado(Uri uri) {
    // NO activamos isLoading. Solo mensaje.
    mensajeGlobal.setValue("Analizando archivo...");

    new Thread(() -> {
      Context context = getApplication();

      // 1. Validar tamaño
      if (esArchivoMuyGrande(context, uri)) {
        archivoListoParaSubir = null;
        archivoEsValido.postValue(false);
        mensajeGlobal.postValue("El archivo es demasiado pesado. Máx 5MB.");
        return;
      }

      // 2. Copiar
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
        if (!cursor.isNull(sizeIndex)) {
          // Limite de 5MB
          return cursor.getLong(sizeIndex) > (5 * 1024 * 1024);
        }
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
        // REDUCIMOS EL BUFFER: 16KB
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

  public void subirArchivoGuardado(int idEvento) {
    if (archivoListoParaSubir == null) return;

    // Solo un mensaje al inicio para no saturar el bus de datos de la UI
    mensajeGlobal.postValue("Subiendo...");

    resultadoRepositorio.subirArchivoResultados(idEvento, archivoListoParaSubir, new Callback<ResponseBody>() {
      @Override
      public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
        // Usamos postValue para asegurar que la actualización ocurra en el siguiente ciclo del main loop
        if (response.isSuccessful()) {
          existenResultados = true;
          archivoListoParaSubir = null;
          mensajeCargaArchivo.postValue("EXITO");
        } else {
          mensajeCargaArchivo.postValue("Error en servidor");
        }
      }

      @Override
      public void onFailure(Call<ResponseBody> call, Throwable t) {
        mensajeCargaArchivo.postValue("Fallo de conexión");
      }
    });
  }

  public void resetMensajeCarga() { mensajeCargaArchivo.setValue(null); }


  // 2. GESTION DEL MENU (LOGICA DE BLOQUEO)

  public void solicitarMenuResultados() {
    if (existenResultados) {
      // SI YA HAY RESULTADOS, SOLO MOSTRAMOS VER
      opcionesMenuResultados.setValue(new String[]{"Ver Resultados Oficiales"});
    } else {
      // SI NO HAY, MOSTRAMOS AMBAS
      opcionesMenuResultados.setValue(new String[]{"Cargar Resultados (CSV)", "Ver Resultados"});
    }
  }

  public void onOpcionMenuSeleccionada(int indice) {
    if (existenResultados) {
      // Única opción es VER
      accionNavegacionResultados.setValue("VER");
    } else {
      // Opciones: 0=Cargar, 1=Ver
      if (indice == 0) accionNavegacionResultados.setValue("CARGAR");
      else accionNavegacionResultados.setValue("VER");
    }
  }

  public void resetOpcionesMenu() { opcionesMenuResultados.setValue(null); }
  public void resetAccionNavegacion() { accionNavegacionResultados.setValue(null); }



  // 3. CARGA DE DATOS
  public void cargarDetalle(int idEvento) {
    isLoading.setValue(true);
    repositorio.obtenerDetalleEvento(idEvento, new Callback<EventoDetalleResponse>() {
      @Override
      public void onResponse(Call<EventoDetalleResponse> call, Response<EventoDetalleResponse> response) {
        isLoading.setValue(false);
        if (response.isSuccessful() && response.body() != null) {
          eventoRaw.setValue(response.body());
          mapearDatosAUI(response.body());
          verificarSiExistenResultados(idEvento);
        } else mensajeGlobal.setValue("Error al cargar evento");
      }
      @Override public void onFailure(Call<EventoDetalleResponse> call, Throwable t) {
        isLoading.setValue(false);
        mensajeGlobal.setValue("Error de conexión");
      }
    });
  }

  private void verificarSiExistenResultados(int idEvento) {
    resultadoRepositorio.obtenerResultadosEvento(idEvento, new Callback<ResultadosEventoResponse>() {
      @Override
      public void onResponse(Call<ResultadosEventoResponse> call, Response<ResultadosEventoResponse> response) {
        if (response.isSuccessful() && response.body() != null) {
          List<ResultadosEventoResponse.ResultadoEventoItem> lista = response.body().getResultados();
          existenResultados = (lista != null && !lista.isEmpty());
        } else {
          existenResultados = false;
        }
      }
      @Override
      public void onFailure(Call<ResultadosEventoResponse> call, Throwable t) {
        existenResultados = false;
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
    switch (estado) {
      case "PUBLICADO": uiEstadoColor.setValue(Color.parseColor("#2E7D32")); break;
      case "SUSPENDIDO": uiEstadoColor.setValue(Color.parseColor("#FF9800")); break;
      case "FINALIZADO": uiEstadoColor.setValue(Color.GRAY); break;
      case "CANCELADO": uiEstadoColor.setValue(Color.RED); break;
      default: uiEstadoColor.setValue(Color.BLACK);
    }

    if (evento.getCategorias() != null && !evento.getCategorias().isEmpty()) {
      CategoriaResponse cat = evento.getCategorias().get(0);
      uiDistanciaTipo.setValue(cat.getNombre() != null ? cat.getNombre() : "General");
      uiGeneroPrecio.setValue("$" + cat.getPrecio());
      uiVisibilidadDatosCategoria.setValue(View.VISIBLE);
    } else uiVisibilidadDatosCategoria.setValue(View.GONE);

    if (evento.getCategorias() != null) listaCategorias.setValue(evento.getCategorias());
    visibilityBtnResultados.setValue("FINALIZADO".equals(estado) ? View.VISIBLE : View.GONE);
  }

  // OTROS METODOS DE APOYO
  public void cargarRunnersDeCategoria(int idEvento, int idCategoria) {
    inscripcionRepositorio.obtenerInscriptos(idEvento, null, 1, 100, new Callback<ListaInscriptosResponse>() {
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
  }

  public void darDeBajaRunner(int idInsc, String motivo, int idEvento, int idCat) {
    inscripcionRepositorio.darDeBajaRunner(idInsc, motivo, new Callback<ResponseBody>() {
      @Override
      public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
        if (response.isSuccessful()) {
          mensajeGlobal.setValue("Baja exitosa");
          cargarRunnersDeCategoria(idEvento, idCat);
          cargarDetalle(idEvento);
        } else mensajeGlobal.setValue("Error baja");
      }
      @Override public void onFailure(Call<ResponseBody> call, Throwable t) { mensajeGlobal.setValue("Error conexión"); }
    });
  }
  // MeTODO HELPER PARA MENSAJES TEMPORALES
  private void mostrarMensajeExito(String msg) {
    mensajeGlobal.setValue(msg);
    // Borrar automáticamente a los 4 segundos
    new Handler(Looper.getMainLooper()).postDelayed(() -> {
      mensajeGlobal.setValue(null);
    }, 4000);
  }

  public int calcularPreseleccionRadio(String estado) {
    if(estado==null) return -1;
    switch (estado.toLowerCase()) {
      case "publicado": return R.id.rbPublicado;
      case "suspendido": return R.id.rbSuspendido;
      case "finalizado": return R.id.rbFinalizado;
      case "cancelado": return R.id.rbCancelado;
      default: return -1;
    }
  }

  public void procesarCambioEstado(int idEvento, int radioId, String motivo) {
    String estadoNuevo = "";
    if (radioId == R.id.rbPublicado) estadoNuevo = "publicado";
    else if (radioId == R.id.rbSuspendido) estadoNuevo = "suspendido";
    else if (radioId == R.id.rbFinalizado) estadoNuevo = "finalizado";
    else if (radioId == R.id.rbCancelado) estadoNuevo = "cancelado";
    else { dialogError.setValue("Seleccione una opción"); return; }

    dialogDismiss.setValue(true);
    CambiarEstadoRequest req = new CambiarEstadoRequest(estadoNuevo, motivo);
    isLoading.setValue(true);
    final String estaParaGuardar= estadoNuevo;
    repositorio.cambiarEstado(idEvento, req, new Callback<ResponseBody>() {
      @Override
      public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
        isLoading.setValue(false);

        if (response.isSuccessful()) {
          // 1. Mensaje de ÉXITO
          mostrarMensajeExito("EXITO: Estado actualizado correctamente");

          // Actualizar UI localmente
          EventoDetalleResponse actual = eventoRaw.getValue();
          if (actual != null) {
            actual.setEstado(estaParaGuardar);
            mapearDatosAUI(actual);
          } else {
            cargarDetalle(idEvento);
          }
        } else {
          // 2. Mensaje de ERROR (Capturando JSON)
          String msjError = "No se puede actualizar: " + response.code();
          try {
            if (response.errorBody() != null) {
              String errorJson = response.errorBody().string();
              JSONObject jsonObject = new JSONObject(errorJson);
              if (jsonObject.has("message")) {
                msjError = jsonObject.getString("message");
              }
            }
          } catch (Exception e) {
            e.printStackTrace();
          }
          // IMPORTANTE: Prefijo ERROR
          mostrarMensajeExito("ERROR: " + msjError);
        }
      }
      @Override public void onFailure(Call<ResponseBody> call, Throwable t) {
        isLoading.setValue(false);
        mensajeGlobal.setValue("Error conexión");
      }
    });
  }

  public void limpiarMensajeGlobal() {
    mensajeGlobal.setValue(null);
  }


}