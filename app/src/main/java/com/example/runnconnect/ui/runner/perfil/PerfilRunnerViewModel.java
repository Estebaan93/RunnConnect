package com.example.runnconnect.ui.runner.perfil;

import android.app.Application;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.runnconnect.data.repositorio.UsuarioRepositorio;
import com.example.runnconnect.data.request.ActualizarPerfilRunnerRequest;
import com.example.runnconnect.data.request.CambiarPasswordRequest;
import com.example.runnconnect.data.response.PerfilUsuarioResponse;

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

  //estado de datos
  private final MutableLiveData<PerfilUsuarioResponse> perfilData = new MutableLiveData<>();
  private final MutableLiveData<Boolean> isEditable = new MutableLiveData<>(false);
  private final MutableLiveData<String> btnText = new MutableLiveData<>("Editar");
  private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
  private final MutableLiveData<String> avatarUrl = new MutableLiveData<>();
  private final MutableLiveData<String> mensajePassword = new MutableLiveData<>();

  //mensajes globlales
  private final MutableLiveData<String> mensajeGlobal = new MutableLiveData<>();
  private final MutableLiveData<Boolean> esMensajeError = new MutableLiveData<>(false); // true=rojo, false=verde

  //errores de campo
  private final MutableLiveData<String> errorNombre = new MutableLiveData<>();
  private final MutableLiveData<String> errorApellido = new MutableLiveData<>();
  private final MutableLiveData<String> errorDni = new MutableLiveData<>();
  private final MutableLiveData<String> errorTelefono = new MutableLiveData<>();
  private final MutableLiveData<String> errorFechaNac = new MutableLiveData<>();
  private final MutableLiveData<String> errorLocalidad = new MutableLiveData<>();
  private final MutableLiveData<String> errorAgrupacion = new MutableLiveData<>();
  private final MutableLiveData<String> errorNombreContacto = new MutableLiveData<>();
  private final MutableLiveData<String> errorTelContacto = new MutableLiveData<>();
  private final MutableLiveData<String> eventShowDatePicker = new MutableLiveData<>();

  // eventoos
  private final MutableLiveData<Boolean> eventShowAvatarOptions = new MutableLiveData<>(false);
  private final MutableLiveData<Boolean> eventShowDeleteConfirmation = new MutableLiveData<>(false);
  private final MutableLiveData<Boolean> eventOpenGallery = new MutableLiveData<>(false);
  private final MutableLiveData<String> eventShowZoomImage = new MutableLiveData<>();

  public PerfilRunnerViewModel(@NonNull Application application) {
    super(application);
    repo = new UsuarioRepositorio(application);
  }

    
  //get de datos
  public LiveData<PerfilUsuarioResponse> getPerfilData() {
    return perfilData;
  }
  public LiveData<Boolean> getIsEditable() {
    return isEditable;
  }
  public LiveData<String> getBtnText() {
    return btnText;
  }
  public LiveData<Boolean> getIsLoading() {
    return isLoading;
  }
  public LiveData<String> getAvatarUrl() {
    return avatarUrl;
  }
  public LiveData<String> getEventShowDatePicker() { return eventShowDatePicker; }
  public LiveData<String> getMensajePassword() { return mensajePassword; }

  //mensjaes
  public LiveData<String> getMensajeGlobal() { return mensajeGlobal; }
  public LiveData<Boolean> getEsMensajeError() { return esMensajeError; }

  //get de errores
  public LiveData<String> getErrorNombre() { return errorNombre; }
  public LiveData<String> getErrorApellido() { return errorApellido; }
  public LiveData<String> getErrorDni() { return errorDni; }
  public LiveData<String> getErrorTelefono() { return errorTelefono; }
  public LiveData<String> getErrorFechaNac() { return errorFechaNac; }
  public LiveData<String> getErrorLocalidad() { return errorLocalidad; }
  public LiveData<String> getErrorAgrupacion() { return errorAgrupacion; }
  public LiveData<String> getErrorNombreContacto() { return errorNombreContacto; }
  public LiveData<String> getErrorTelContacto() { return errorTelContacto; }

  //get de eventos
  public LiveData<Boolean> getEventShowAvatarOptions() { return eventShowAvatarOptions; }
  public LiveData<Boolean> getEventShowDeleteConfirmation() { return eventShowDeleteConfirmation; }
  public LiveData<Boolean> getEventOpenGallery() { return eventOpenGallery; }
  public LiveData<String> getEventShowZoomImage() { return eventShowZoomImage; }



  //acciones de la vista
  // El boton principal actua distinto segun el estado
  public void onBotonPrincipalClick(RunnerInput input) {
    if (Boolean.TRUE.equals(isEditable.getValue())) {
      // Si estaba editando -> intenta Guardar
      guardarCambios(input);
    } else {
      // Si estaba viendo -> habilita edicion
      habilitarEdicion();
    }
  }
  //recibe el click
  public void onFechaNacClick(String fechaActual) {
    // El VM decide solo abrimos calendario si es editable
    if (Boolean.TRUE.equals(isEditable.getValue())) {
      eventShowDatePicker.setValue(fechaActual);
    }
  }
  public void onDatePickerShown() { eventShowDatePicker.setValue(null); }

  // Acciones de imagenes
  public void onEditAvatarClicked() { eventShowAvatarOptions.setValue(true); }
  public void onChangePhotoOptionSelected() { eventOpenGallery.setValue(true); }
  public void onDeletePhotoOptionSelected() { eventShowDeleteConfirmation.setValue(true); }
  public void onDeleteConfirmed() { borrarFoto(); }

  public void onAvatarImageClicked() {
    String currentUrl = avatarUrl.getValue();
    if (currentUrl != null && !currentUrl.isEmpty()) {
      eventShowZoomImage.setValue(currentUrl);
    }
  }

  public void onImagenSeleccionada(Uri uri) {
    if (uri == null) return;
    File archivo = convertirUriAFile(uri);
    if (archivo != null) {
      subirNuevaFoto(archivo);
    } else {
      mostrarMensajeGlobal("Error al procesar imagen", true);
    }
  }

  //CONSUMO DE EVENTOS (Reseteo para evitar rebotes)
  public void onAvatarOptionsConsumed() { eventShowAvatarOptions.setValue(false); }
  public void onDeleteConfirmationConsumed() { eventShowDeleteConfirmation.setValue(false); }
  public void onGalleryOpenConsumed() { eventOpenGallery.setValue(false); }
  public void onZoomImageConsumed() { eventShowZoomImage.setValue(null); }

  //helper privado para setear mensajes
  private void mostrarMensajeGlobal(String mensaje, boolean esError){
    mensajeGlobal.setValue(mensaje);
    esMensajeError.setValue(esError);
  }
  public void limpiarMensajePassword() {
    mensajePassword.setValue(null);
  }


  //logica de negocio
  public void cargarPerfil() {
    isLoading.setValue(true);
    // Limpiamos mensaje anterior por si acaso
    mostrarMensajeGlobal(null, false); //limpiamos mensaje previo

    repo.obtenerPerfil(new Callback<PerfilUsuarioResponse>() {
      @Override
      public void onResponse(Call<PerfilUsuarioResponse> call, Response<PerfilUsuarioResponse> response) {
        isLoading.setValue(false);
        if (response.isSuccessful() && response.body() != null) {
          PerfilUsuarioResponse p = response.body();
          //formatear fecha antes de enviarla a la vista
          p.setFechaNacimiento(formatearFechaNacimiento(p.getFechaNacimiento()));
          perfilData.setValue(p);
          procesarAvatar(p.getImgAvatar());
        } else {
          mostrarMensajeGlobal("Error al cargar perfil", true);
        }
      }
      @Override
      public void onFailure(Call<PerfilUsuarioResponse> call, Throwable t) {
        isLoading.setValue(false);
        mostrarMensajeGlobal("Error de conexion", true);
      }
    });
  }

  private void habilitarEdicion() {
    isEditable.setValue(true);
    btnText.setValue("Guardar");
    mostrarMensajeGlobal(null, false); // Limpiar mensaje
  }

  private void guardarCambios(RunnerInput input) {
    boolean esValido = true;
    // VALIDACIONES
    if (input.nombre == null || input.nombre.trim().length() < 3) { errorNombre.setValue("Mínimo 3 caracteres"); esValido = false; } else errorNombre.setValue(null);
    if (input.apellido == null || input.apellido.trim().length() < 3) { errorApellido.setValue("Mínimo 3 caracteres"); esValido = false; } else errorApellido.setValue(null);
    if (input.telefono == null || input.telefono.trim().length() < 7 || input.telefono.length() > 20) { errorTelefono.setValue("Entre 7 y 20 caracteres"); esValido = false; } else errorTelefono.setValue(null);

    int dniInt = 0;
    try {
      dniInt = Integer.parseInt(input.dni);
      if (dniInt < 1000000 || dniInt > 99999999) { errorDni.setValue("DNI inválido (7-8 dígitos)"); esValido = false; } else errorDni.setValue(null);
    } catch (NumberFormatException e) { errorDni.setValue("Solo números"); esValido = false; }

    if (input.fechaNac == null || input.fechaNac.trim().isEmpty()) { errorFechaNac.setValue("Requerido"); esValido = false; } else errorFechaNac.setValue(null);
    if (input.localidad == null || input.localidad.trim().isEmpty()) { errorLocalidad.setValue("Requerido"); esValido = false; } else errorLocalidad.setValue(null);
    if (input.agrupacion == null || input.agrupacion.trim().isEmpty()) { errorAgrupacion.setValue("Requerido"); esValido = false; } else errorAgrupacion.setValue(null);
    if (input.nombreContacto == null || input.nombreContacto.trim().length() < 3) { errorNombreContacto.setValue("Mínimo 3 caracteres"); esValido = false; } else errorNombreContacto.setValue(null);
    if (input.telContacto == null || !Pattern.matches("^\\d{6,15}$", input.telContacto)) { errorTelContacto.setValue("Solo números (6-15 dígitos)"); esValido = false; } else errorTelContacto.setValue(null);

    if (!esValido) return;

    ActualizarPerfilRunnerRequest request = new ActualizarPerfilRunnerRequest(
      input.nombre,
      input.apellido,
      input.telefono,
      input.fechaNac,
      input.genero,
      dniInt,
      input.localidad,
      input.agrupacion,
      input.nombreContacto,
      input.telContacto
    );

    isLoading.setValue(true);
    mostrarMensajeGlobal(null, false);

    repo.actualizarRunner(request, new Callback<PerfilUsuarioResponse>() {
      @Override
      public void onResponse(Call<PerfilUsuarioResponse> call, Response<PerfilUsuarioResponse> response) {
        if (response.isSuccessful()) {
          cargarPerfil();
          isEditable.setValue(false);
          btnText.setValue("Editar");
          mostrarMensajeGlobal("Perfil actualizado correctamente", false); // Éxito en verde
        } else {
          isLoading.setValue(false);
          String mensajeError = "Error al actualizar";
          try {
            if (response.errorBody() != null) {
              // El errorBody es un stream, lo convertimos a string
              String errorRaw = response.errorBody().string();

              // mostramos lo que venga o un mensaje generico
              if(errorRaw.contains("message")) {
                // Un parseo rapido
                mensajeError = new org.json.JSONObject(errorRaw).getString("message");
              }
            }
          } catch (Exception e) {
            e.printStackTrace();
          }
          mostrarMensajeGlobal(mensajeError, true);
        }
      }
      @Override
      public void onFailure(Call<PerfilUsuarioResponse> call, Throwable t) {
        isLoading.setValue(false);
        mostrarMensajeGlobal("Fallo de red", true);
      }
    });
  }

  //
  private String formatearFechaNacimiento(String fecha) {
    if (fecha == null || fecha.isEmpty()) return "";
    return fecha.contains("T") ? fecha.split("T")[0] : fecha;
  }

  private void procesarAvatar(String url) {
    if (url == null || url.isEmpty()) {
      avatarUrl.setValue(null);
      return;
    }
    if (url.contains("localhost")) {
      url = url.replace("localhost", "10.0.2.2");
    }
    avatarUrl.setValue(url);
  }

  private File convertirUriAFile(Uri uri) {
    try {
      InputStream inputStream = getApplication().getContentResolver().openInputStream(uri);
      File tempFile = File.createTempFile("avatar_upload", ".jpg", getApplication().getCacheDir());
      FileOutputStream outputStream = new FileOutputStream(tempFile);
      byte[] buffer = new byte[1024];
      int length;
      while ((length = inputStream.read(buffer)) > 0) outputStream.write(buffer, 0, length);
      outputStream.close();
      if (inputStream != null) inputStream.close();
      return tempFile;
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }

  public void subirNuevaFoto(File archivo) {
    isLoading.setValue(true);
    repo.subirAvatar(archivo, new Callback<PerfilUsuarioResponse>() {
      @Override
      public void onResponse(Call<PerfilUsuarioResponse> call, Response<PerfilUsuarioResponse> response) {
        isLoading.setValue(false);
        if (response.isSuccessful() && response.body() != null) {
          procesarAvatar(response.body().getImgAvatar());
          mostrarMensajeGlobal("Foto actualizada", false);
        } else {
          mostrarMensajeGlobal("Error al subir imagen", true);
        }
      }
      @Override
      public void onFailure(Call<PerfilUsuarioResponse> call, Throwable t) {
        isLoading.setValue(false);
        mostrarMensajeGlobal("Error de red", true);
      }
    });
  }

  public void borrarFoto() {
    isLoading.setValue(true);
    repo.eliminarAvatar(new Callback<PerfilUsuarioResponse>() {
      @Override
      public void onResponse(Call<PerfilUsuarioResponse> call, Response<PerfilUsuarioResponse> response) {
        isLoading.setValue(false);
        if (response.isSuccessful() && response.body() != null) {
          procesarAvatar(response.body().getImgAvatar());
          mostrarMensajeGlobal("Foto eliminada", false);
        } else {
          mostrarMensajeGlobal("Error al eliminar", true);
        }
      }
      @Override
      public void onFailure(Call<PerfilUsuarioResponse> call, Throwable t) {
        isLoading.setValue(false);
        mostrarMensajeGlobal("Error de red", true);
      }
    });
  }

  // DTO para pasar datos desde el Fragment
  public static class RunnerInput {
    public String nombre, apellido, telefono, dni, fechaNac, genero, localidad, agrupacion, nombreContacto, telContacto;
    public RunnerInput(String nombre, String apellido, String telefono, String dni, String fechaNac, String genero, String localidad, String agrupacion, String nombreContacto, String telContacto) {
      this.nombre = nombre; this.apellido = apellido; this.telefono = telefono; this.dni = dni; this.fechaNac = fechaNac; this.genero = genero; this.localidad = localidad; this.agrupacion = agrupacion; this.nombreContacto = nombreContacto; this.telContacto = telContacto;
    }
  }

  public Calendar obtenerFechaCalendario(String fechaActual){
    Calendar calendario= Calendar.getInstance();
    if(fechaActual != null && fechaActual.isEmpty()){
      try{
        String[] partes = fechaActual.split("-");
        int year = Integer.parseInt(partes[0]);
        int month = Integer.parseInt(partes[1]) - 1; // 0-11
        int day = Integer.parseInt(partes[2]);
        calendario.set(year, month, day);
      }catch(Exception e){
        e.printStackTrace();
      }
    }
    return calendario;
  }
  public String procesarFechaSeleccionada(int year, int month, int dayOfMonth) {
    // month + 1 porque el DatePicker devuelve 0-11
    return String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, dayOfMonth);
  }
  public int obtenerIndiceGenero(String genero, String[] opciones) {
    if (genero == null || opciones == null) return 0;
    for (int i = 0; i < opciones.length; i++) {
      if (opciones[i].equalsIgnoreCase(genero)) {
        return i;
      }
    }
    return 0; // default
  }

  //cambiar contraseña
  public void cambiarPassword(String actual, String nueva, String confirmacion) {
    // Validaciones locales rapidas
    if (actual.isEmpty() || nueva.isEmpty() || confirmacion.isEmpty()) {
      mensajePassword.setValue("Error: Todos los campos son obligatorios");
      return;
    }
    if (!nueva.equals(confirmacion)) {
      mensajePassword.setValue("Error: Las contraseñas nuevas no coinciden");
      return;
    }
    if (nueva.length() < 6) {
      mensajePassword.setValue("Error: La nueva contraseña debe tener al menos 6 caracteres");
      return;
    }

    isLoading.setValue(true);

    CambiarPasswordRequest request = new CambiarPasswordRequest(actual, nueva, confirmacion);

    repo.cambiarPassword(request, new Callback<Void>() {
      @Override
      public void onResponse(Call<Void> call, Response<Void> response) {
        isLoading.setValue(false);
        if (response.isSuccessful()) {
          mensajePassword.setValue("Éxito: Contraseña actualizada correctamente");
        } else {
          // Intentar leer el error del backend
          String errorMsg = "Error al cambiar contraseña";
          try {
            if(response.errorBody() != null) {
              String errorRaw = response.errorBody().string();
              if(errorRaw.contains("message")) {
                errorMsg = "Error: " + new org.json.JSONObject(errorRaw).getString("message");
              }
            }
          } catch (Exception e) { e.printStackTrace(); }
          mensajePassword.setValue(errorMsg);
        }
      }

      @Override
      public void onFailure(Call<Void> call, Throwable t) {
        isLoading.setValue(false);
        mensajePassword.setValue("Error: Fallo de conexión");
      }
    });
  }



}