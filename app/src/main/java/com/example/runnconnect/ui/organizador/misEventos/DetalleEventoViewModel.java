package com.example.runnconnect.ui.organizador.misEventos;

import android.app.Application;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
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

import org.json.JSONObject;

import java.io.File;
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


  private final MutableLiveData<Integer> visibilityBtnResultados = new MutableLiveData<>(View.GONE);
  private final MutableLiveData<String> mensajeCargaArchivo = new MutableLiveData<>();
  private final MutableLiveData<EventoDetalleResponse> eventoRaw = new MutableLiveData<>();

  // --- ESTADOS DE UI ---
  private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
  private final MutableLiveData<String> mensajeGlobal = new MutableLiveData<>();

  // Campos de Texto
  private final MutableLiveData<String> uiTitulo = new MutableLiveData<>();
  private final MutableLiveData<String> uiFecha = new MutableLiveData<>();
  private final MutableLiveData<String> uiLugar = new MutableLiveData<>();
  private final MutableLiveData<String> uiDescripcion = new MutableLiveData<>();
  private final MutableLiveData<String> uiInscriptos = new MutableLiveData<>();
  private final MutableLiveData<String> uiCupo = new MutableLiveData<>();

  // Estado (Texto y Color)
  private final MutableLiveData<String> uiEstadoTexto = new MutableLiveData<>();
  private final MutableLiveData<Integer> uiEstadoColor = new MutableLiveData<>();

  // Datos Técnicos (Categoría)
  private final MutableLiveData<String> uiDistanciaTipo = new MutableLiveData<>();
  private final MutableLiveData<String> uiGeneroPrecio = new MutableLiveData<>();
  private final MutableLiveData<Integer> uiVisibilidadDatosCategoria = new MutableLiveData<>(View.GONE);

  // Control del Dialogo
  private final MutableLiveData<String> dialogError = new MutableLiveData<>();
  private final MutableLiveData<Boolean> dialogDismiss = new MutableLiveData<>();
  private final MutableLiveData<List<CategoriaResponse>> listaCategorias = new MutableLiveData<>();

  //dialog
  private final MutableLiveData<List<InscriptoEventoResponse>> listaRunnerDialog = new MutableLiveData<>();

  public DetalleEventoViewModel(@NonNull Application application) {
    super(application);
    repositorio = new EventoRepositorio(application);
    inscripcionRepositorio= new InscripcionRepositorio(application);
    resultadoRepositorio= new ResultadoRepositorio(application);
  }

  // --- GETTERS ---
  public LiveData<Integer> getVisibilityBtnResultados() { return visibilityBtnResultados; }
  public LiveData<String> getMensajeCargaArchivo() { return mensajeCargaArchivo; }
  public LiveData<Boolean> getIsLoading() { return isLoading; }
  public LiveData<String> getMensajeGlobal() { return mensajeGlobal; } // Getter actualizado
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
  public LiveData<EventoDetalleResponse> getEventoRaw() { return eventoRaw; }
  public LiveData<List<CategoriaResponse>> getListaCategorias() { return listaCategorias; }
  public LiveData<List<InscriptoEventoResponse>> getListaRunnersDialog() { return listaRunnerDialog; }

  /*public void limpiarMensajes() {
    mensajeGlobal.setValue(null);
    dialogError.setValue(null);
    dialogDismiss.setValue(false);
  }*/

  // Llamar esto al cargar el evento
  /*public void actualizarVisibilidadBotones(String estadoEvento) {
    if (estadoEvento != null && estadoEvento.equalsIgnoreCase("finalizado")) {
      visibilityBtnResultados.setValue(View.VISIBLE);
    } else {
      visibilityBtnResultados.setValue(View.GONE);
    }
  }*/


  //cargar runner de una categoria especifica
  public void cargarRunnersDeCategoria(int idEvento, int idCategoria) {
    // isLoading.setValue(true);

    // CORRECCION: Usar Callback<ListaInscriptosResponse> en lugar de InscriptoEventoResponse
    inscripcionRepositorio.obtenerInscriptos(idEvento, null, 1, 100 , new Callback<ListaInscriptosResponse>() {
      @Override
      public void onResponse(Call<ListaInscriptosResponse> call, Response<ListaInscriptosResponse> response) {
        // isLoading.setValue(false);
        if (response.isSuccessful() && response.body() != null) {

          List<InscriptoEventoResponse> filtrados = new ArrayList<>();

          // Ahora si existe el metodo getInscripciones() porque response.body() es ListaInscriptosResponse
          if(response.body().getInscripciones() != null){
            for(InscriptoEventoResponse ins : response.body().getInscripciones()){
              //filtrar estado pagado
              boolean esMismaCateg= ins.getIdCategoria() == idCategoria;
              boolean estadoPago= "pagado".equalsIgnoreCase(ins.getEstadoPago());

              //
              if(esMismaCateg && estadoPago){
                filtrados.add(ins);
              }
            }
          }
          listaRunnerDialog.setValue(filtrados);
        } else {
          listaRunnerDialog.setValue(new ArrayList<>());
        }
      }

      @Override
      public void onFailure(Call<ListaInscriptosResponse> call, Throwable t) {
        // isLoading.setValue(false);
        mensajeGlobal.setValue("Error de conexión");
        listaRunnerDialog.setValue(new ArrayList<>());
      }
    });
  }
  public void darDeBajaRunner(int idInscripcion, String motivo, int idEvento, int idCatActual) {
    // isLoading.setValue(true);
    inscripcionRepositorio.darDeBajaRunner(idInscripcion, motivo, new Callback<ResponseBody>() {
      @Override
      public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
        // isLoading.setValue(false);
        if (response.isSuccessful()) {
          mostrarMensajeExito("Runner dado de baja.");
          // 1. Recargar la lista del dialogo para que desaparezca (o cambie estado)
          cargarRunnersDeCategoria(idEvento, idCatActual);
          // 2. Recargar el detalle general para actualizar contadores
          cargarDetalle(idEvento);
        } else {
          mostrarMensajeExito("Error al dar de baja: " + response.code());
        }
      }
      @Override public void onFailure(Call<ResponseBody> call, Throwable t) {
        // isLoading.setValue(false);
        mostrarMensajeExito("Error de conexión.");
      }
    });
  }

  // para mostrar mensaje temporalmente
  private void mostrarMensajeExito(String msg) {
    mensajeGlobal.setValue(msg);
    // Borrar automáticamente a los 4 segundos
    new Handler(Looper.getMainLooper()).postDelayed(() -> {
      mensajeGlobal.setValue(null);
    }, 4000);
  }

  // --- CARGA DE DATOS ---
  public void cargarDetalle(int idEvento) {
    isLoading.setValue(true);
    repositorio.obtenerDetalleEvento(idEvento, new Callback<EventoDetalleResponse>() {
      @Override
      public void onResponse(Call<EventoDetalleResponse> call, Response<EventoDetalleResponse> response) {
        isLoading.setValue(false);
        if (response.isSuccessful() && response.body() != null) {
          eventoRaw.setValue(response.body());
          mapearDatosAUI(response.body());
        } else {
          mensajeGlobal.setValue("No se pudo cargar la información del evento.");
        }
      }
      @Override
      public void onFailure(Call<EventoDetalleResponse> call, Throwable t) {
        isLoading.setValue(false);
        mensajeGlobal.setValue("Error de conexión al cargar detalle.");
      }
    });
  }

  // Transformacion: Modelo -> Vista
  private void mapearDatosAUI(EventoDetalleResponse evento) {
    uiTitulo.setValue(evento.getNombre());
    uiLugar.setValue(evento.getLugar());
    uiDescripcion.setValue(evento.getDescripcion());
    uiInscriptos.setValue(String.valueOf(evento.getInscriptosActuales()));
    uiCupo.setValue(String.valueOf(evento.getCupoTotal()));

    if (evento.getFechaHora() != null) {
      uiFecha.setValue(evento.getFechaHora().replace("T", " "));
    }

    String estadoUpper = (evento.getEstado() != null) ? evento.getEstado().toUpperCase() : "DESCONOCIDO";
    uiEstadoTexto.setValue(estadoUpper);

    switch (estadoUpper) {
      case "PUBLICADO": uiEstadoColor.setValue(Color.parseColor("#2E7D32")); break;
      case "SUSPENDIDO": uiEstadoColor.setValue(Color.parseColor("#FF9800")); break;
      case "FINALIZADO": uiEstadoColor.setValue(Color.GRAY); break;
      case "CANCELADO": uiEstadoColor.setValue(Color.RED); break;
      default: uiEstadoColor.setValue(Color.BLACK);
    }

    if (evento.getCategorias() != null && !evento.getCategorias().isEmpty()) {
      CategoriaResponse cat = evento.getCategorias().get(0);

      String distMod = cat.getNombre() != null ? cat.getNombre() : "Sin categoría";
      uiDistanciaTipo.setValue(distMod);

      String generoTexto = "General";
      if ("F".equalsIgnoreCase(cat.getGenero())) generoTexto = "Femenino";
      else if ("M".equalsIgnoreCase(cat.getGenero())) generoTexto = "Masculino";
      else if ("X".equalsIgnoreCase(cat.getGenero())) generoTexto = "Mixto";

      String precio = "$" + cat.getPrecio();
      uiGeneroPrecio.setValue(String.format("%s  |  %s", generoTexto, precio));
      uiVisibilidadDatosCategoria.setValue(View.VISIBLE);
    } else {
      uiVisibilidadDatosCategoria.setValue(View.GONE);
    }
    if (evento.getCategorias() != null) {
      listaCategorias.setValue(evento.getCategorias());
    }

    // LÓGICA RESULTADOS: Solo mostrar si FINALIZADO
    //String estadoUpper = (evento.getEstado() != null) ? evento.getEstado().toUpperCase() : "";
    if ("FINALIZADO".equals(estadoUpper)) {
      visibilityBtnResultados.setValue(View.VISIBLE);
    } else {
      visibilityBtnResultados.setValue(View.GONE);
    }


  }

  // Subir CSV
  public void subirArchivoCsv(int idEvento, File archivo) {
    isLoading.setValue(true);
    resultadoRepositorio.subirArchivoResultados(idEvento, archivo, new Callback<ResponseBody>() {
      @Override
      public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
        isLoading.setValue(false);
        if (response.isSuccessful()) {
          mensajeCargaArchivo.setValue("EXITO");
        } else {
          try {
            String error = response.errorBody() != null ? response.errorBody().string() : "Error desconocido";
            mensajeCargaArchivo.setValue("Error: " + error);
          } catch (Exception e) {
            mensajeCargaArchivo.setValue("Error de respuesta");
          }
        }
      }
      @Override
      public void onFailure(Call<ResponseBody> call, Throwable t) {
        isLoading.setValue(false);
        mensajeCargaArchivo.setValue("Fallo de conexión");
      }
    });
  }
  public void resetMensajeCarga() {
    mensajeCargaArchivo.setValue(null);
  }

  // --- LOGICA DEL DIALOGO ---
  public int calcularPreseleccionRadio(String estadoActual) {
    if (estadoActual == null) return -1;
    switch (estadoActual.toLowerCase()) {
      case "publicado": return R.id.rbPublicado;
      case "suspendido": return R.id.rbSuspendido;
      case "finalizado": return R.id.rbFinalizado;
      case "cancelado": return R.id.rbCancelado;
      default: return -1;
    }
  }

  public void procesarCambioEstado(int idEvento, int radioIdSeleccionado, String motivo) {
    String nuevoEstado = "";

    if (radioIdSeleccionado == R.id.rbPublicado) nuevoEstado = "publicado";
    else if (radioIdSeleccionado == R.id.rbSuspendido) nuevoEstado = "suspendido";
    else if (radioIdSeleccionado == R.id.rbFinalizado) nuevoEstado = "finalizado";
    else if (radioIdSeleccionado == R.id.rbCancelado) nuevoEstado = "cancelado";
    else {
      dialogError.setValue("Debes seleccionar una opción.");
      return;
    }

    if ((nuevoEstado.equals("cancelado") || nuevoEstado.equals("suspendido")) && motivo.trim().isEmpty()) {
      dialogError.setValue("Escribe el motivo (requerido).");
      return;
    }

    dialogDismiss.setValue(true);
    ejecutarCambioEstado(idEvento, nuevoEstado, motivo);
  }

  private void ejecutarCambioEstado(int idEvento, String nuevoEstado, String motivo) {
    isLoading.setValue(true);
    CambiarEstadoRequest request = new CambiarEstadoRequest(nuevoEstado, motivo);

    repositorio.cambiarEstado(idEvento, request, new Callback<ResponseBody>() {
      @Override
      public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
        isLoading.setValue(false);
        if (response.isSuccessful()) {
            mostrarMensajeExito("Estado actualizado correctamente");

            EventoDetalleResponse actual = eventoRaw.getValue();
          if (actual != null) {
            actual.setEstado(nuevoEstado);
            mapearDatosAUI(actual);
          } else {
            cargarDetalle(idEvento);
          }
        } else {
          String msjError="No se puede actualizar: "+ response.code();
          //mensajeGlobal.setValue("No se pudo actualizar: " + response.code());
          Log.d("ErrorEstado", "Error al actualizar el estado: " + response.body());
          try{
            if(response.errorBody() !=null){
              String errorJson= response.errorBody().string();
              JSONObject jsonObject= new JSONObject(errorJson);

              if(jsonObject.has("message")){
                msjError= jsonObject.getString("message");
              }
            }
          } catch (Exception e) {
            e.printStackTrace();
          }
          mostrarMensajeExito(msjError);
        }
      }
      @Override
      public void onFailure(Call<ResponseBody> call, Throwable t) {
        isLoading.setValue(false);
        mensajeGlobal.setValue("Error de conexión");
      }
    });
  }
}