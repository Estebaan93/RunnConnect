package com.example.runnconnect.ui.organizador.perfil;

import android.app.Application;
import android.graphics.Color;
import android.net.Uri;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.runnconnect.data.conexion.ApiClient;
import com.example.runnconnect.data.conexion.ApiService;
import com.example.runnconnect.data.preferencias.SessionManager;
import com.example.runnconnect.data.request.ActualizarPerfilOrganizadorRequest;
import com.example.runnconnect.data.request.CambiarPasswordRequest;
import com.example.runnconnect.data.response.PerfilUsuarioResponse;

import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.regex.Pattern;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PerfilOrganizadorViewModel extends AndroidViewModel {

  private final ApiService apiService;
  private final SessionManager sessionManager;

  // Estado interno
  private boolean modoEdicion = false;

  // Datos
  private final MutableLiveData<PerfilUsuarioResponse> perfilData = new MutableLiveData<>();
  private final MutableLiveData<String> avatarUrl = new MutableLiveData<>();
  private final MutableLiveData<Boolean> isEditable = new MutableLiveData<>(false);
  private final MutableLiveData<String> btnText = new MutableLiveData<>("Editar");

  // Loading — expone visibilidad directamente
  private final MutableLiveData<Integer> progressVisibility = new MutableLiveData<>(View.GONE);

  // Mensaje global — el VM decide texto, visibilidad y color
  private final MutableLiveData<String> mensajeGlobal = new MutableLiveData<>("");
  private final MutableLiveData<Integer> mensajeVisibility = new MutableLiveData<>(View.GONE);
  private final MutableLiveData<Integer> mensajeColor = new MutableLiveData<>(Color.RED);

  // Errores de campos del perfil
  private final MutableLiveData<String> errorNombreComercial = new MutableLiveData<>();
  private final MutableLiveData<String> errorRazonSocial = new MutableLiveData<>();
  private final MutableLiveData<String> errorCuit = new MutableLiveData<>();
  private final MutableLiveData<String> errorNombreContacto = new MutableLiveData<>();
  private final MutableLiveData<String> errorTelefono = new MutableLiveData<>();
  private final MutableLiveData<String> errorDireccion = new MutableLiveData<>();

  // Errores de password
  private final MutableLiveData<String> errorPassActual = new MutableLiveData<>();
  private final MutableLiveData<String> errorPassNuevo = new MutableLiveData<>();
  private final MutableLiveData<String> errorPassConfirm = new MutableLiveData<>();

  // Eventos — sin valor inicial, solo emiten cuando el evento ocurre
  private final SingleLiveEvent<Boolean> eventShowAvatarOptions = new SingleLiveEvent<>();
  private final SingleLiveEvent<Boolean> eventShowDeleteConfirmation = new SingleLiveEvent<>();
  private final SingleLiveEvent<Boolean> eventOpenGallery = new SingleLiveEvent<>();
  private final SingleLiveEvent<String> eventShowZoomImage = new SingleLiveEvent<>();
  private final SingleLiveEvent<Boolean> eventConfirmarBaja = new SingleLiveEvent<>();
  private final SingleLiveEvent<Boolean> eventNavegarAlLogin = new SingleLiveEvent<>();
  private final SingleLiveEvent<Boolean> eventCerrarDialogPassword = new SingleLiveEvent<>();

  public PerfilOrganizadorViewModel(@NonNull Application application) {
    super(application);
    apiService = ApiClient.getApiService();
    sessionManager = new SessionManager(application);
  }

  //Getters
  public LiveData<PerfilUsuarioResponse> getPerfilData() {
    return perfilData;
  }

  public LiveData<String> getAvatarUrl() {
    return avatarUrl;
  }

  public LiveData<Boolean> getIsEditable() {
    return isEditable;
  }

  public LiveData<String> getBtnText() {
    return btnText;
  }

  public LiveData<Integer> getProgressVisibility() {
    return progressVisibility;
  }

  public LiveData<String> getMensajeGlobal() {
    return mensajeGlobal;
  }

  public LiveData<Integer> getMensajeVisibility() {
    return mensajeVisibility;
  }

  public LiveData<Integer> getMensajeColor() {
    return mensajeColor;
  }

  public LiveData<String> getErrorNombreComercial() {
    return errorNombreComercial;
  }

  public LiveData<String> getErrorRazonSocial() {
    return errorRazonSocial;
  }

  public LiveData<String> getErrorCuit() {
    return errorCuit;
  }

  public LiveData<String> getErrorNombreContacto() {
    return errorNombreContacto;
  }

  public LiveData<String> getErrorTelefono() {
    return errorTelefono;
  }

  public LiveData<String> getErrorDireccion() {
    return errorDireccion;
  }

  public LiveData<String> getErrorPassActual() {
    return errorPassActual;
  }

  public LiveData<String> getErrorPassNuevo() {
    return errorPassNuevo;
  }

  public LiveData<String> getErrorPassConfirm() {
    return errorPassConfirm;
  }

  public LiveData<Boolean> getEventShowAvatarOptions() {
    return eventShowAvatarOptions;
  }

  public LiveData<Boolean> getEventShowDeleteConfirmation() {
    return eventShowDeleteConfirmation;
  }

  public LiveData<Boolean> getEventOpenGallery() {
    return eventOpenGallery;
  }

  public LiveData<String> getEventShowZoomImage() {
    return eventShowZoomImage;
  }

  public LiveData<Boolean> getEventConfirmarBaja() {
    return eventConfirmarBaja;
  }

  public LiveData<Boolean> getEventNavegarAlLogin() {
    return eventNavegarAlLogin;
  }

  public LiveData<Boolean> getEventCerrarDialogPassword() {
    return eventCerrarDialogPassword;
  }

  // Acciones publicas
  public void onBotonPrincipalClick(OrganizadorInput input) {
    if (modoEdicion) guardarCambios(input);
    else habilitarEdicion();
  }

  public void btnDarBaja() {
    eventConfirmarBaja.setValue(true);
  }

  public void onEditAvatarClicked() {
    eventShowAvatarOptions.setValue(true);
  }

  public void onChangePhotoOptionSelected() {
    eventOpenGallery.setValue(true);
  }

  public void onDeletePhotoOptionSelected() {
    eventShowDeleteConfirmation.setValue(true);
  }

  public void onDeleteConfirmed() {
    borrarFoto();
  }

  public void onAvatarImageClicked() {
    String url = avatarUrl.getValue();
    if (url != null && !url.isEmpty()) eventShowZoomImage.setValue(url);
  }

  public void onImagenSeleccionada(Uri uri) {
    if (uri == null) return;
    File archivo = convertirUriAFile(uri);
    if (archivo != null) subirNuevaFoto(archivo);
    else mostrarMensajeGlobal("Error al procesar imagen", true);
  }

  public void cambiarPassword(String actual, String nueva, String confirmacion) {
    errorPassActual.setValue(null);
    errorPassNuevo.setValue(null);
    errorPassConfirm.setValue(null);

    boolean esValido = true;
    if (actual.isEmpty()) {
      errorPassActual.setValue("Requerido");
      esValido = false;
    }
    if (nueva.isEmpty()) {
      errorPassNuevo.setValue("Requerido");
      esValido = false;
    }
    if (confirmacion.isEmpty()) {
      errorPassConfirm.setValue("Requerido");
      esValido = false;
    }
    if (!esValido) return;

    if (nueva.length() < 6) {
      errorPassNuevo.setValue("Mínimo 6 caracteres");
      return;
    }
    if (!nueva.equals(confirmacion)) {
      errorPassConfirm.setValue("Las contraseñas no coinciden");
      return;
    }

    setLoading(true);
    String token = sessionManager.leerToken();
    apiService.cambiarPassword("Bearer " + token, new CambiarPasswordRequest(actual, nueva, confirmacion)).enqueue(new Callback<Void>() {
      @Override
      public void onResponse(Call<Void> call, Response<Void> response) {
        setLoading(false);
        if (response.isSuccessful()) {
          eventCerrarDialogPassword.setValue(true);
          mostrarMensajeGlobal("Contraseña actualizada correctamente, cerrando sesión..", false);
          new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
            sessionManager.cerrarSesion();
            eventNavegarAlLogin.setValue(true);
          }, 2000);
        } else {
          String errorMsg = "Credenciales incorrectas";
          try {
            if (response.errorBody() != null) {
              String raw = response.errorBody().string();
              JSONObject json = new JSONObject(raw);
              if (json.has("message")) errorMsg = json.getString("message");
              else if (json.has("title")) errorMsg = json.getString("title");
              else if (json.has("errors")) {
                JSONObject errors = json.getJSONObject("errors");
                java.util.Iterator<String> keys = errors.keys();
                if (keys.hasNext()) errorMsg = errors.getJSONArray(keys.next()).getString(0);
              }
            }
          } catch (Exception e) {
            e.printStackTrace();
          }
          errorPassActual.setValue(errorMsg);
        }
      }

      @Override
      public void onFailure(Call<Void> call, Throwable t) {
        setLoading(false);
        errorPassActual.setValue("Fallo de conexión. Reintente.");
      }
    });
  }

  public void confirmarDarDeBaja() {
    setLoading(true);
    String token = sessionManager.leerToken();
    apiService.eliminarCuenta("Bearer " + token).enqueue(new Callback<Void>() {
      @Override
      public void onResponse(Call<Void> call, Response<Void> response) {
        setLoading(false);
        if (response.isSuccessful()) {
          sessionManager.cerrarSesion();
          eventNavegarAlLogin.setValue(true);
        } else {
          mostrarMensajeGlobal("No se pudo dar de baja la cuenta.", true);
        }
      }

      @Override
      public void onFailure(Call<Void> call, Throwable t) {
        setLoading(false);
        mostrarMensajeGlobal("Error de conexión al dar de baja.", true);
      }
    });
  }

  public void cargarPerfil() {
    setLoading(true);
    mostrarMensajeGlobal(null, false);
    String token = sessionManager.leerToken();
    apiService.obtenerPerfil("Bearer " + token).enqueue(new Callback<PerfilUsuarioResponse>() {
      @Override
      public void onResponse(Call<PerfilUsuarioResponse> call, Response<PerfilUsuarioResponse> response) {
        setLoading(false);
        if (response.isSuccessful() && response.body() != null) {
          PerfilUsuarioResponse p = response.body();
          perfilData.setValue(p);
          procesarAvatar(p.getImgAvatar());
        } else {
          mostrarMensajeGlobal("Error al cargar perfil", true);
        }
      }

      @Override
      public void onFailure(Call<PerfilUsuarioResponse> call, Throwable t) {
        setLoading(false);
        mostrarMensajeGlobal("Error de conexión", true);
      }
    });
  }

  public void subirNuevaFoto(File archivo) {
    setLoading(true);
    String token = sessionManager.leerToken();
    RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), archivo);
    MultipartBody.Part body = MultipartBody.Part.createFormData("imagen", archivo.getName(), requestFile);
    apiService.subirAvatar("Bearer " + token, body).enqueue(new Callback<PerfilUsuarioResponse>() {
      @Override
      public void onResponse(Call<PerfilUsuarioResponse> call, Response<PerfilUsuarioResponse> response) {
        setLoading(false);
        if (response.isSuccessful() && response.body() != null) {
          procesarAvatar(response.body().getImgAvatar());
          mostrarMensajeGlobal("Foto actualizada", false);
        } else {
          mostrarMensajeGlobal("Error al subir imagen", true);
        }
      }

      @Override
      public void onFailure(Call<PerfilUsuarioResponse> call, Throwable t) {
        setLoading(false);
        mostrarMensajeGlobal("Error de red", true);
      }
    });
  }

  public void borrarFoto() {
    setLoading(true);
    String token = sessionManager.leerToken();
    apiService.eliminarAvatar("Bearer " + token).enqueue(new Callback<PerfilUsuarioResponse>() {
      @Override
      public void onResponse(Call<PerfilUsuarioResponse> call, Response<PerfilUsuarioResponse> response) {
        setLoading(false);
        if (response.isSuccessful() && response.body() != null) {
          procesarAvatar(response.body().getImgAvatar());
          mostrarMensajeGlobal("Foto eliminada", false);
        } else {
          mostrarMensajeGlobal("Error al eliminar", true);
        }
      }

      @Override
      public void onFailure(Call<PerfilUsuarioResponse> call, Throwable t) {
        setLoading(false);
        mostrarMensajeGlobal("Error de red", true);
      }
    });
  }

  //Privados
  private void habilitarEdicion() {
    modoEdicion = true;
    isEditable.setValue(true);
    btnText.setValue("Guardar");
    mostrarMensajeGlobal(null, false);
  }

  private void deshabilitarEdicion() {
    modoEdicion = false;
    isEditable.setValue(false);
    btnText.setValue("Editar");
    errorNombreComercial.setValue(null);
    errorRazonSocial.setValue(null);
    errorCuit.setValue(null);
    errorNombreContacto.setValue(null);
    errorTelefono.setValue(null);
    errorDireccion.setValue(null);
  }

  private void guardarCambios(OrganizadorInput input) {
    boolean esValido = true;

    if (input.nombreComercial == null || input.nombreComercial.trim().length() < 3) {
      errorNombreComercial.setValue("Mínimo 3 caracteres");
      esValido = false;
    } else errorNombreComercial.setValue(null);

    if (input.razonSocial == null || input.razonSocial.trim().length() < 2) {
      errorRazonSocial.setValue("Requerido");
      esValido = false;
    } else errorRazonSocial.setValue(null);

    if (input.cuit == null || !Pattern.matches("^\\d{11}$", input.cuit)) {
      errorCuit.setValue("Debe tener 11 números sin guiones");
      esValido = false;
    } else errorCuit.setValue(null);

    if (input.direccion == null || input.direccion.trim().isEmpty()) {
      errorDireccion.setValue("Requerido");
      esValido = false;
    } else errorDireccion.setValue(null);

    if (input.nombreContacto == null || input.nombreContacto.trim().length() < 3) {
      errorNombreContacto.setValue("Mínimo 3 caracteres");
      esValido = false;
    } else errorNombreContacto.setValue(null);

    if (input.telefono == null || input.telefono.trim().length() < 7) {
      errorTelefono.setValue("Mínimo 7 dígitos");
      esValido = false;
    } else errorTelefono.setValue(null);

    if (!esValido) return;

    setLoading(true);
    mostrarMensajeGlobal(null, false);

    String token = sessionManager.leerToken();
    apiService.actualizarPerfilOrganizador("Bearer " + token,
      new ActualizarPerfilOrganizadorRequest(
        input.nombreContacto, input.telefono, input.razonSocial,
        input.nombreComercial, input.cuit, input.direccion)).enqueue(new Callback<PerfilUsuarioResponse>() {
        @Override
        public void onResponse(Call<PerfilUsuarioResponse> call, Response<PerfilUsuarioResponse> response) {
          if (response.isSuccessful()) {
            deshabilitarEdicion();
            cargarPerfil();
            mostrarMensajeGlobal("Perfil actualizado correctamente", false);
          } else {
            setLoading(false);
            String msg = "Error al actualizar";
            try {
              if (response.errorBody() != null) {
                String raw = response.errorBody().string();
                if (raw.contains("message"))
                  msg = new JSONObject(raw).getString("message");
              }
            } catch (Exception e) {
              e.printStackTrace();
            }
            mostrarMensajeGlobal(msg, true);
          }
        }

        @Override
        public void onFailure(Call<PerfilUsuarioResponse> call, Throwable t) {
          setLoading(false);
          mostrarMensajeGlobal("Fallo de red", true);
        }
      });
  }

  private void setLoading(boolean loading) {
    progressVisibility.setValue(loading ? View.VISIBLE : View.GONE);
  }

  private void mostrarMensajeGlobal(String mensaje, boolean esError) {
    boolean tieneContenido = mensaje != null && !mensaje.isEmpty();
    mensajeGlobal.setValue(tieneContenido ? mensaje : "");
    mensajeVisibility.setValue(tieneContenido ? View.VISIBLE : View.GONE);
    mensajeColor.setValue(esError ? Color.RED : Color.parseColor("#008000"));
  }

  private void procesarAvatar(String url) {
    if (url == null || url.isEmpty()) {
      avatarUrl.setValue(null);
      return;
    }
    if (url.contains("localhost")) url = url.replace("localhost", "10.0.2.2");
    avatarUrl.setValue(url);
  }

  private File convertirUriAFile(Uri uri) {
    try (InputStream in = getApplication().getContentResolver().openInputStream(uri)) {
      File tempFile = File.createTempFile("avatar_upload", ".jpg", getApplication().getCacheDir());
      try (FileOutputStream out = new FileOutputStream(tempFile)) {
        byte[] buffer = new byte[4096];
        int length;
        while ((length = in.read(buffer)) > 0) out.write(buffer, 0, length);
      }
      return tempFile;
    } catch (Exception e) {
      return null;
    }
  }

  //Input DTO
  public static class OrganizadorInput {
    public final String nombreComercial, razonSocial, cuit, nombreContacto, telefono, direccion;

    public OrganizadorInput(String nombreComercial, String razonSocial, String cuit,
                            String nombreContacto, String telefono, String direccion) {
      this.nombreComercial = nombreComercial;
      this.razonSocial = razonSocial;
      this.cuit = cuit;
      this.nombreContacto = nombreContacto;
      this.telefono = telefono;
      this.direccion = direccion;
    }
  }

  private static class SingleLiveEvent<T> extends MutableLiveData<T> {
    private final java.util.concurrent.atomic.AtomicBoolean pending =
      new java.util.concurrent.atomic.AtomicBoolean(false);

    @Override
    public void observe(androidx.lifecycle.LifecycleOwner owner,
                        androidx.lifecycle.Observer<? super T> observer) {
      super.observe(owner, value -> {
        if (pending.compareAndSet(true, false)) {
          observer.onChanged(value);
        }
      });
    }

    @Override
    public void setValue(T value) {
      pending.set(true);
      super.setValue(value);
    }
  }


}