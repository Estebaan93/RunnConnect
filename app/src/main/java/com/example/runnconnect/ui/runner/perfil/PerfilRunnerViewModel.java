package com.example.runnconnect.ui.runner.perfil;

import android.app.Application;
import android.net.Uri;
import android.graphics.Color;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.runnconnect.data.repositorio.UsuarioRepositorio;
import com.example.runnconnect.data.request.ActualizarPerfilRunnerRequest;
import com.example.runnconnect.data.request.CambiarPasswordRequest;
import com.example.runnconnect.data.response.PerfilUsuarioResponse;

import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Calendar;
import java.util.Locale;
import java.util.regex.Pattern;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PerfilRunnerViewModel extends AndroidViewModel {

  private final UsuarioRepositorio repo;

  // Estado interno
  private boolean modoEdicion = false;

  // Datos
  private final MutableLiveData<PerfilUsuarioResponse> perfilData = new MutableLiveData<>();
  private final MutableLiveData<String> avatarUrl = new MutableLiveData<>();
  private final MutableLiveData<Boolean> isEditable = new MutableLiveData<>(false);
  private final MutableLiveData<String> btnText = new MutableLiveData<>("Editar");

  //campos de una sola carga
  private final MutableLiveData<Boolean> isDniEditable = new MutableLiveData<>(false);
  private final MutableLiveData<Boolean> isUbicacionEditable = new MutableLiveData<>(false);
  private final MutableLiveData<Boolean> isFechaNacEditable = new MutableLiveData<>(false);
  private final MutableLiveData<Boolean> isGeneroEditable = new MutableLiveData<>(false);

  // Loading
  private final MutableLiveData<Integer> progressVisibility = new MutableLiveData<>(View.GONE);

  // Mensaje global
  private final MutableLiveData<String> mensajeGlobal = new MutableLiveData<>("");
  private final MutableLiveData<Integer> mensajeVisibility = new MutableLiveData<>(View.GONE);
  private final MutableLiveData<Integer> mensajeColor = new MutableLiveData<>(Color.RED);

  // Errores de campos del perfil
  private final MutableLiveData<String> errorNombre = new MutableLiveData<>();
  private final MutableLiveData<String> errorApellido = new MutableLiveData<>();
  private final MutableLiveData<String> errorDni = new MutableLiveData<>();
  private final MutableLiveData<String> errorTelefono = new MutableLiveData<>();
  private final MutableLiveData<String> errorFechaNac = new MutableLiveData<>();
  private final MutableLiveData<String> errorLocalidad = new MutableLiveData<>();
  private final MutableLiveData<String> errorAgrupacion = new MutableLiveData<>();
  private final MutableLiveData<String> errorNombreContacto = new MutableLiveData<>();
  private final MutableLiveData<String> errorTelContacto = new MutableLiveData<>();

  // Errores de password
  private final MutableLiveData<String> errorPassActual = new MutableLiveData<>();
  private final MutableLiveData<String> errorPassNuevo = new MutableLiveData<>();
  private final MutableLiveData<String> errorPassConfirm = new MutableLiveData<>();

  // Eventos — SingleLiveEvent para evitar re-entrega al volver al Fragment
  private final SingleLiveEvent<String> eventShowDatePicker = new SingleLiveEvent<>();
  private final SingleLiveEvent<Boolean> eventShowAvatarOptions = new SingleLiveEvent<>();
  private final SingleLiveEvent<Boolean> eventShowDeleteConfirmation = new SingleLiveEvent<>();
  private final SingleLiveEvent<Boolean> eventOpenGallery = new SingleLiveEvent<>();
  private final SingleLiveEvent<String> eventShowZoomImage = new SingleLiveEvent<>();
  private final SingleLiveEvent<Boolean> eventConfirmarBaja = new SingleLiveEvent<>();
  private final SingleLiveEvent<Boolean> eventNavegarAlLogin = new SingleLiveEvent<>();
  private final SingleLiveEvent<Boolean> eventCerrarDialogPassword = new SingleLiveEvent<>();

  public PerfilRunnerViewModel(@NonNull Application application) {
    super(application);
    repo = new UsuarioRepositorio(application);
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

  public LiveData<String> getErrorNombre() {
    return errorNombre;
  }

  public LiveData<String> getErrorApellido() {
    return errorApellido;
  }

  public LiveData<String> getErrorDni() {
    return errorDni;
  }

  public LiveData<String> getErrorTelefono() {
    return errorTelefono;
  }

  public LiveData<String> getErrorFechaNac() {
    return errorFechaNac;
  }

  public LiveData<String> getErrorLocalidad() {
    return errorLocalidad;
  }

  public LiveData<String> getErrorAgrupacion() {
    return errorAgrupacion;
  }

  public LiveData<String> getErrorNombreContacto() {
    return errorNombreContacto;
  }

  public LiveData<String> getErrorTelContacto() {
    return errorTelContacto;
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

  public LiveData<String> getEventShowDatePicker() {
    return eventShowDatePicker;
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

  public LiveData<Boolean> getIsDniEditable() {
    return isDniEditable;
  }

  public LiveData<Boolean> getIsUbicacionEditable() {
    return isUbicacionEditable;
  }

  public LiveData<Boolean> getIsFechaNacEditable() {
    return isFechaNacEditable;
  }

  public LiveData<Boolean> getIsGeneroEditable() {
    return isGeneroEditable;
  }

  //Acciones publicas

  public void onBotonPrincipalClick(RunnerInput input) {
    if (modoEdicion) guardarCambios(input);
    else habilitarEdicion();
  }

  public void onFechaNacClick(String fechaActual) {
    if (modoEdicion) eventShowDatePicker.setValue(fechaActual);
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
    repo.cambiarPassword(new CambiarPasswordRequest(actual, nueva, confirmacion), new Callback<Void>() {
      @Override
      public void onResponse(Call<Void> call, Response<Void> response) {
        setLoading(false);
        if (response.isSuccessful()) {
          eventCerrarDialogPassword.setValue(true);
          mostrarMensajeGlobal("Contraseña actualizada correctamente, cerrando sesión", false);
          new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
            repo.cerrarSesion();
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
    repo.eliminarCuenta(new Callback<Void>() {
      @Override
      public void onResponse(Call<Void> call, Response<Void> response) {
        setLoading(false);
        if (response.isSuccessful()) {
          repo.cerrarSesion();
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
    repo.obtenerPerfil(new Callback<PerfilUsuarioResponse>() {
      @Override
      public void onResponse(Call<PerfilUsuarioResponse> call, Response<PerfilUsuarioResponse> response) {
        setLoading(false);
        if (response.isSuccessful() && response.body() != null) {
          PerfilUsuarioResponse p = response.body();
          p.setFechaNacimiento(formatearFechaNacimiento(p.getFechaNacimiento()));
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
    repo.subirAvatar(archivo, new Callback<PerfilUsuarioResponse>() {
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
    repo.eliminarAvatar(new Callback<PerfilUsuarioResponse>() {
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

    //revisamos si los campos dni, localidad, fecha nac y genero tienen datos
    PerfilUsuarioResponse p = perfilData.getValue();
    boolean tieneDni= p!=null && p.getDni() != null && p.getDni() > 0;
    boolean tieneUbicacion = p != null && p.getLocalidad() != null && !p.getLocalidad().trim().isEmpty() && !p.getLocalidad().equals(", ");
    boolean tieneFechaNac = p != null && p.getFechaNacimiento() != null && !p.getFechaNacimiento().trim().isEmpty();
    boolean tieneGenero = p != null && p.getGenero() != null && !p.getGenero().trim().isEmpty();

    isDniEditable.setValue(!tieneDni);
    isUbicacionEditable.setValue(!tieneUbicacion);
    isFechaNacEditable.setValue(!tieneFechaNac);
    isGeneroEditable.setValue(!tieneGenero);
  }

  private void deshabilitarEdicion() {
    modoEdicion = false;
    isEditable.setValue(false);


    btnText.setValue("Editar");
    errorNombre.setValue(null);
    errorApellido.setValue(null);
    errorDni.setValue(null);
    errorTelefono.setValue(null);
    errorFechaNac.setValue(null);
    errorLocalidad.setValue(null);
    errorAgrupacion.setValue(null);
    errorNombreContacto.setValue(null);
    errorTelContacto.setValue(null);
  }

  private void guardarCambios(RunnerInput input) {
    boolean esValido = true;

    if (input.nombre == null || input.nombre.trim().length() < 3) {
      errorNombre.setValue("Mínimo 3 caracteres");
      esValido = false;
    } else errorNombre.setValue(null);

    if (input.apellido == null || input.apellido.trim().length() < 3) {
      errorApellido.setValue("Mínimo 3 caracteres");
      esValido = false;
    } else errorApellido.setValue(null);

    if (input.telefono == null || input.telefono.trim().length() < 7 || input.telefono.length() > 20) {
      errorTelefono.setValue("Entre 7 y 20 caracteres");
      esValido = false;
    } else errorTelefono.setValue(null);

    int dniInt = 0;
    try {
      dniInt = Integer.parseInt(input.dni);
      if (dniInt < 1000000 || dniInt > 99999999) {
        errorDni.setValue("DNI inválido (7-8 dígitos)");
        esValido = false;
      } else errorDni.setValue(null);
    } catch (NumberFormatException e) {
      errorDni.setValue("Solo números");
      esValido = false;
    }

    if (input.fechaNac == null || input.fechaNac.trim().isEmpty()) {
      errorFechaNac.setValue("Requerido");
      esValido = false;
    } else errorFechaNac.setValue(null);

    if (input.localidad == null || input.localidad.trim().isEmpty() || input.localidad.equals(", ")) {
      errorLocalidad.setValue("Requerido");
      esValido = false;
    } else errorLocalidad.setValue(null);

    if (input.agrupacion == null || input.agrupacion.trim().isEmpty()) {
      errorAgrupacion.setValue("Requerido");
      esValido = false;
    } else errorAgrupacion.setValue(null);

    if (input.nombreContacto == null || input.nombreContacto.trim().length() < 3) {
      errorNombreContacto.setValue("Mínimo 3 caracteres");
      esValido = false;
    } else errorNombreContacto.setValue(null);

    if (input.telContacto == null || !Pattern.matches("^\\d{6,15}$", input.telContacto)) {
      errorTelContacto.setValue("Solo números (6-15 dígitos)");
      esValido = false;
    } else errorTelContacto.setValue(null);

    if (!esValido) return;

    setLoading(true);
    mostrarMensajeGlobal(null, false);

    final int dniFinal = dniInt;
    repo.actualizarRunner(
      new ActualizarPerfilRunnerRequest(
        input.nombre, input.apellido, input.telefono, input.fechaNac,
        input.genero, dniFinal, input.localidad, input.agrupacion,
        input.nombreContacto, input.telContacto),
      new Callback<PerfilUsuarioResponse>() {
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

  private String formatearFechaNacimiento(String fecha) {
    if (fecha == null || fecha.isEmpty()) return "";
    return fecha.contains("T") ? fecha.split("T")[0] : fecha;
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

  //Helpers para el Fragment
  public Calendar obtenerFechaCalendario(String fechaActual) {
    Calendar calendario = Calendar.getInstance();
    // BUG CORREGIDO: era isEmpty(), debe ser !isEmpty()
    if (fechaActual != null && !fechaActual.isEmpty()) {
      try {
        String[] partes = fechaActual.split("-");
        int year = Integer.parseInt(partes[0]);
        int month = Integer.parseInt(partes[1]) - 1;
        int day = Integer.parseInt(partes[2]);
        calendario.set(year, month, day);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    return calendario;
  }

  public String procesarFechaSeleccionada(int year, int month, int dayOfMonth) {
    return String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, dayOfMonth);
  }

  public int obtenerIndiceGenero(String genero, String[] opciones) {
    if (genero == null || opciones == null) return 0;
    for (int i = 0; i < opciones.length; i++) {
      if (opciones[i].equalsIgnoreCase(genero)) return i;
    }
    return 0;
  }

  //Input DTO
  public static class RunnerInput {
    public final String nombre, apellido, telefono, dni, fechaNac,
      genero, localidad, agrupacion, nombreContacto, telContacto;

    public RunnerInput(String nombre, String apellido, String telefono, String dni,
                       String fechaNac, String genero, String localidad, String agrupacion,
                       String nombreContacto, String telContacto) {
      this.nombre = nombre;
      this.apellido = apellido;
      this.telefono = telefono;
      this.dni = dni;
      this.fechaNac = fechaNac;
      this.genero = genero;
      this.localidad = localidad;
      this.agrupacion = agrupacion;
      this.nombreContacto = nombreContacto;
      this.telContacto = telContacto;
    }
  }

  //SingleLiveEvent
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