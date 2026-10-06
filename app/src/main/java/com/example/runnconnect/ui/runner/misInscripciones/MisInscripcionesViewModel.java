package com.example.runnconnect.ui.runner.misInscripciones;

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
import com.example.runnconnect.data.response.MisInscripcionesResponse;

import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MisInscripcionesViewModel extends AndroidViewModel {

  private final ApiService apiService;
  private final SessionManager sessionManager;

  private final List<MisInscripcionesResponse.InscripcionItem> listaCompleta = new ArrayList<>();
  private String filtroEstadoActual = "TODOS";

  private final MutableLiveData<List<String>> listaOpcionesEstado = new MutableLiveData<>(
      Arrays.asList("Todos", "Pendiente", "En revisión", "Pagado", "Rechazado", "Cancelado")
  );

  private final MutableLiveData<List<InscripcionUI>> listaInscripcionesUI = new MutableLiveData<>(new ArrayList<>());
  private final MutableLiveData<Integer> uiVisibilidadVacio = new MutableLiveData<>(View.GONE);
  private final MutableLiveData<Integer> uiVisibilidadRecycler = new MutableLiveData<>(View.GONE);
  private final MutableLiveData<String> uiTextoVacio = new MutableLiveData<>("No tienes inscripciones registradas");
  private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);

  private final MutableLiveData<String> uiMensajeTexto = new MutableLiveData<>("");
  private final MutableLiveData<Integer> uiMensajeVisibilidad = new MutableLiveData<>(View.GONE);
  private final MutableLiveData<Integer> uiMensajeColorTexto = new MutableLiveData<>(Color.BLACK);
  private final MutableLiveData<Integer> uiMensajeColorFondo = new MutableLiveData<>(Color.WHITE);

  public MisInscripcionesViewModel(@NonNull Application application) {
    super(application);
    this.apiService = ApiClient.getApiService();
    this.sessionManager = new SessionManager(application);
  }

  public LiveData<List<String>> getListaOpcionesEstado() { return listaOpcionesEstado; }
  public LiveData<List<InscripcionUI>> getListaInscripcionesUI() { return listaInscripcionesUI; }
  public LiveData<Integer> getUiVisibilidadVacio() { return uiVisibilidadVacio; }
  public LiveData<Integer> getUiVisibilidadRecycler() { return uiVisibilidadRecycler; }
  public LiveData<String> getUiTextoVacio() { return uiTextoVacio; }
  public LiveData<Boolean> getIsLoading() { return isLoading; }
  public LiveData<String> getUiMensajeTexto() { return uiMensajeTexto; }
  public LiveData<Integer> getUiMensajeVisibilidad() { return uiMensajeVisibilidad; }
  public LiveData<Integer> getUiMensajeColorTexto() { return uiMensajeColorTexto; }
  public LiveData<Integer> getUiMensajeColorFondo() { return uiMensajeColorFondo; }

  public void cargarInscripciones() {
    String token = sessionManager.leerToken();
    if (token == null || token.isEmpty()) {
      mostrarMensaje("No hay sesión activa", true);
      return;
    }

    isLoading.setValue(true);
    apiService.obtenerMisInscripciones("Bearer " + token, false).enqueue(new Callback<MisInscripcionesResponse>() {
      @Override
      public void onResponse(Call<MisInscripcionesResponse> call, Response<MisInscripcionesResponse> response) {
        isLoading.setValue(false);

        if (response.isSuccessful() && response.body() != null) {
          listaCompleta.clear();
          List<MisInscripcionesResponse.InscripcionItem> items = response.body().getInscripciones();
          if (items != null) {
            listaCompleta.addAll(items);
          }
          aplicarFiltros();
        } else {
          mostrarMensaje("Error al obtener inscripciones (" + response.code() + ")", true);
        }
      }

      @Override
      public void onFailure(Call<MisInscripcionesResponse> call, Throwable t) {
        isLoading.setValue(false);
        mostrarMensaje("Error de conexión al cargar inscripciones", true);
      }
    });
  }

  public void setFiltroEstado(String estadoSeleccionado) {
    if (estadoSeleccionado == null || estadoSeleccionado.equalsIgnoreCase("Todos")) {
      this.filtroEstadoActual = "TODOS";
    } else if (estadoSeleccionado.equalsIgnoreCase("Pendiente")) {
      this.filtroEstadoActual = "pendiente";
    } else if (estadoSeleccionado.equalsIgnoreCase("En revisión") || estadoSeleccionado.equalsIgnoreCase("Procesando")) {
      this.filtroEstadoActual = "procesando";
    } else if (estadoSeleccionado.equalsIgnoreCase("Pagado")) {
      this.filtroEstadoActual = "pagado";
    } else if (estadoSeleccionado.equalsIgnoreCase("Rechazado")) {
      this.filtroEstadoActual = "rechazado";
    } else if (estadoSeleccionado.equalsIgnoreCase("Cancelado")) {
      this.filtroEstadoActual = "cancelado";
    } else {
      this.filtroEstadoActual = "TODOS";
    }
    aplicarFiltros();
  }

  private void aplicarFiltros() {
    List<InscripcionUI> uiList = new ArrayList<>();

    for (MisInscripcionesResponse.InscripcionItem item : listaCompleta) {
      String estado = item.getEstadoPago() != null ? item.getEstadoPago().toLowerCase().trim() : "";

      if (!"TODOS".equals(filtroEstadoActual) && !filtroEstadoActual.equalsIgnoreCase(estado)) {
        continue;
      }

      // Regla 16: El backend siempre envía evento y categoría válidos
      int idEvento = item.getEvento().getIdEvento();
      String nombreEvento = item.getEvento().getNombre();
      String fecha = item.getEvento().getFechaHora().replace("T", " ");
      String lugar = item.getEvento().getLugar();
      String fechaLugar = "Fecha: " + fecha + " | Lugar: " + lugar;

      String catNombre = item.getCategoria().getNombre();
      String costo = item.getCategoria().getCostoInscripcion() != null
          ? "$" + item.getCategoria().getCostoInscripcion()
          : "Gratis";
      String categoriaCosto = "Categoría: " + catNombre + " (" + costo + ")";

      String talle = item.getTalleRemera() != null ? item.getTalleRemera() : "No especificado";
      String talleRemera = "Talle de remera: " + talle;

      String estadoTexto;
      int colorTexto;
      int colorFondo;
      int btnSubirVisibilidad = View.GONE;
      int btnVerVisibilidad = View.GONE;
      int observacionVisibilidad = View.GONE;
      String observacionTexto = "";

      switch (estado) {
        case "pendiente":
          estadoTexto = "PENDIENTE";
          colorTexto = Color.parseColor("#E65100");
          colorFondo = Color.parseColor("#FFF3E0");
          btnSubirVisibilidad = View.VISIBLE;
          break;
        case "procesando":
          estadoTexto = "EN REVISIÓN";
          colorTexto = Color.parseColor("#1565C0");
          colorFondo = Color.parseColor("#E3F2FD");
          btnVerVisibilidad = View.VISIBLE;
          break;
        case "pagado":
          estadoTexto = "CONFIRMADO";
          colorTexto = Color.parseColor("#2E7D32");
          colorFondo = Color.parseColor("#E8F5E9");
          btnVerVisibilidad = View.VISIBLE;
          break;
        case "rechazado":
          estadoTexto = "RECHAZADO";
          colorTexto = Color.parseColor("#C62828");
          colorFondo = Color.parseColor("#FFEBEE");
          observacionVisibilidad = View.VISIBLE;
          String obs = item.getObservacion();
          observacionTexto = "Motivo de rechazo: " + (obs != null && !obs.trim().isEmpty() ? obs : "Comprobante rechazado");
          break;
        case "cancelado":
          estadoTexto = "CANCELADO";
          colorTexto = Color.parseColor("#616161");
          colorFondo = Color.parseColor("#F5F5F5");
          break;
        default:
          estadoTexto = estado.toUpperCase();
          colorTexto = Color.BLACK;
          colorFondo = Color.WHITE;
          break;
      }

      String urlCompleta = normalizarUrlComprobante(item.getComprobantePagoURL());

      uiList.add(new InscripcionUI(
          item.getIdInscripcion(),
          idEvento,
          nombreEvento,
          fechaLugar,
          categoriaCosto,
          talleRemera,
          estadoTexto,
          colorTexto,
          colorFondo,
          observacionTexto,
          observacionVisibilidad,
          btnSubirVisibilidad,
          btnVerVisibilidad,
          urlCompleta
      ));
    }

    listaInscripcionesUI.setValue(uiList);

    if (uiList.isEmpty()) {
      uiVisibilidadVacio.setValue(View.VISIBLE);
      uiVisibilidadRecycler.setValue(View.GONE);
      if (listaCompleta.isEmpty()) {
        uiTextoVacio.setValue("No tienes inscripciones registradas");
      } else {
        uiTextoVacio.setValue("No se encontraron inscripciones con el estado seleccionado");
      }
    } else {
      uiVisibilidadVacio.setValue(View.GONE);
      uiVisibilidadRecycler.setValue(View.VISIBLE);
    }
  }

  public void procesarYSubirComprobante(int idInscripcion, Uri uri) {
    if (uri == null) return;

    isLoading.setValue(true);
    mostrarMensaje("Procesando comprobante...", false);

    new Thread(() -> {
      Context context = getApplication();
      String mimeType = context.getContentResolver().getType(uri);
      String extension = (mimeType != null && mimeType.contains("png")) ? ".png" : ".jpg";

      File tempFile = copiarUriAArchivo(context, uri, extension);
      if (tempFile == null) {
        new Handler(Looper.getMainLooper()).post(() -> {
          isLoading.setValue(false);
          mostrarMensaje("Error al leer la imagen seleccionada", true);
        });
        return;
      }

      if (tempFile.length() > 10 * 1024 * 1024) {
        tempFile.delete();
        new Handler(Looper.getMainLooper()).post(() -> {
          isLoading.setValue(false);
          mostrarMensaje("El comprobante no puede exceder 10MB", true);
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
          isLoading.setValue(false);
          tempFile.delete();

          if (response.isSuccessful()) {
            mostrarMensaje("¡Comprobante subido exitosamente! En revisión.", false);
            cargarInscripciones();
          } else {
            String errorMsg = "Error al subir comprobante (" + response.code() + ")";
            try {
              if (response.errorBody() != null) {
                String raw = response.errorBody().string();
                JSONObject json = new JSONObject(raw);
                if (json.has("message")) errorMsg = json.getString("message");
              }
            } catch (Exception ignored) {}
            mostrarMensaje(errorMsg, true);
          }
        }

        @Override
        public void onFailure(Call<ResponseBody> call, Throwable t) {
          isLoading.setValue(false);
          tempFile.delete();
          mostrarMensaje("Error de conexión al subir comprobante", true);
        }
      });
    }).start();
  }

  private File copiarUriAArchivo(Context context, Uri uri, String extension) {
    try {
      InputStream is = context.getContentResolver().openInputStream(uri);
      if (is == null) return null;
      File temp = File.createTempFile("comprobante_inscripcion", extension, context.getCacheDir());
      try (FileOutputStream out = new FileOutputStream(temp)) {
        byte[] buffer = new byte[16 * 1024];
        int len;
        while ((len = is.read(buffer)) != -1) {
          out.write(buffer, 0, len);
        }
      }
      is.close();
      return temp;
    } catch (Exception e) {
      return null;
    }
  }

  private String normalizarUrlComprobante(String url) {
    if (url == null || url.trim().isEmpty()) return "";
    String res = url.trim();
    if (res.startsWith("/")) {
      res = "http://10.0.2.2:5213" + res;
    } else if (res.contains("localhost")) {
      res = res.replace("localhost", "10.0.2.2");
    }
    return res;
  }

  public void mostrarMensaje(String mensaje, boolean esError) {
    if (mensaje == null || mensaje.isEmpty()) return;

    uiMensajeTexto.setValue(mensaje);
    uiMensajeVisibilidad.setValue(View.VISIBLE);

    if (esError) {
      uiMensajeColorTexto.setValue(Color.parseColor("#C62828"));
      uiMensajeColorFondo.setValue(Color.parseColor("#FFEBEE"));
    } else {
      uiMensajeColorTexto.setValue(Color.parseColor("#2E7D32"));
      uiMensajeColorFondo.setValue(Color.parseColor("#E8F5E9"));
    }

    new Handler(Looper.getMainLooper()).postDelayed(() -> {
      uiMensajeVisibilidad.setValue(View.GONE);
      uiMensajeTexto.setValue("");
    }, 4000);
  }
}
