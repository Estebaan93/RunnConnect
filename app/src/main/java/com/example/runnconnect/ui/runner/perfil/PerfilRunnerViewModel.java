package com.example.runnconnect.ui.runner.perfil;

import android.app.Application;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.runnconnect.data.repositorio.UsuarioRepositorio;
import com.example.runnconnect.data.request.ActualizarPerfilRunnerRequest;
import com.example.runnconnect.data.response.PerfilUsuarioResponse;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import java.util.Calendar;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PerfilRunnerViewModel extends AndroidViewModel {
  private final UsuarioRepositorio repo;

  // --- ESTADOS DE DATOS (UI State) ---
  private final MutableLiveData<PerfilUsuarioResponse> perfilData = new MutableLiveData<>();
  private final MutableLiveData<Boolean> isEditable = new MutableLiveData<>(false);
  private final MutableLiveData<String> btnText = new MutableLiveData<>("Editar");
  private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
  private final MutableLiveData<String> mensajeToast = new MutableLiveData<>();
  private final MutableLiveData<String> avatarUrl = new MutableLiveData<>();

  // --- EVENTOS (Single Shot) ---
  private final MutableLiveData<Boolean> eventShowAvatarOptions = new MutableLiveData<>(false);
  private final MutableLiveData<Boolean> eventShowDeleteConfirmation = new MutableLiveData<>(false);
  private final MutableLiveData<Boolean> eventOpenGallery = new MutableLiveData<>(false);
  private final MutableLiveData<String> eventShowZoomImage = new MutableLiveData<>();

  public PerfilRunnerViewModel(@NonNull Application application) {
    super(application);
    repo = new UsuarioRepositorio(application);
  }

  // --- GETTERS ---
  public LiveData<PerfilUsuarioResponse> getPerfilData() { return perfilData; }
  public LiveData<Boolean> getIsEditable() { return isEditable; }
  public LiveData<String> getBtnText() { return btnText; }
  public LiveData<Boolean> getIsLoading() { return isLoading; }
  public LiveData<String> getMensajeToast() { return mensajeToast; }
  public LiveData<String> getAvatarUrl() { return avatarUrl; }

  public LiveData<Boolean> getEventShowAvatarOptions() { return eventShowAvatarOptions; }
  public LiveData<Boolean> getEventShowDeleteConfirmation() { return eventShowDeleteConfirmation; }
  public LiveData<Boolean> getEventOpenGallery() { return eventOpenGallery; }
  public LiveData<String> getEventShowZoomImage() { return eventShowZoomImage; }


  // --- ACCIONES DE LA VISTA (User Actions) ---

  // El botón principal actúa distinto según el estado (Igual que en tu ejemplo de Inmobiliaria)
  public void onBotonPrincipalClick(RunnerInput input) {
    if (Boolean.TRUE.equals(isEditable.getValue())) {
      // Si estaba editando -> Intenta Guardar
      guardarCambios(input);
    } else {
      // Si estaba viendo -> Habilita Edición
      habilitarEdicion();
    }
  }

  // Acciones de imágenes
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
      mensajeToast.setValue("Error al procesar la imagen seleccionada");
    }
  }

  // --- CONSUMO DE EVENTOS (Reseteo para evitar rebotes) ---
  public void onAvatarOptionsConsumed() { eventShowAvatarOptions.setValue(false); }
  public void onDeleteConfirmationConsumed() { eventShowDeleteConfirmation.setValue(false); }
  public void onGalleryOpenConsumed() { eventOpenGallery.setValue(false); }
  public void onZoomImageConsumed() { eventShowZoomImage.setValue(null); }

  // IMPORTANTE: Esto soluciona que el Toast salga dos veces al volver a la pantalla
  public void onToastConsumed() { mensajeToast.setValue(null); }


  // --- LÓGICA DE NEGOCIO ---

  public void cargarPerfil() {
    isLoading.setValue(true);
    // Limpiamos mensaje anterior por si acaso
    mensajeToast.setValue(null);

    repo.obtenerPerfil(new Callback<PerfilUsuarioResponse>() {
      @Override
      public void onResponse(Call<PerfilUsuarioResponse> call, Response<PerfilUsuarioResponse> response) {
        isLoading.setValue(false);
        if (response.isSuccessful() && response.body() != null) {
          PerfilUsuarioResponse p = response.body();
          // Lógica: Formatear fecha antes de enviarla a la vista
          p.setFechaNacimiento(formatearFechaNacimiento(p.getFechaNacimiento()));
          perfilData.setValue(p);
          procesarAvatar(p.getImgAvatar());
        } else {
          mensajeToast.setValue("Error al cargar perfil");
        }
      }
      @Override
      public void onFailure(Call<PerfilUsuarioResponse> call, Throwable t) {
        isLoading.setValue(false);
        mensajeToast.setValue("Error de conexion");
      }
    });
  }

  private void habilitarEdicion() {
    isEditable.setValue(true);
    btnText.setValue("Guardar");
  }

  private void guardarCambios(RunnerInput input) {
    // Validaciones (Lógica de negocio)
    if (input.nombre.isEmpty() || input.apellido.isEmpty()) {
      mensajeToast.setValue("Nombre y Apellido son obligatorios");
      return;
    }

    int dniInt = 0;
    try {
      if (input.dni != null && !input.dni.isEmpty()) {
        dniInt = Integer.parseInt(input.dni);
      }
    } catch (NumberFormatException e) {
      mensajeToast.setValue("El DNI debe ser numérico");
      return;
    }

    ActualizarPerfilRunnerRequest request = new ActualizarPerfilRunnerRequest(
            input.nombre, input.apellido, input.telefono, input.fechaNac,
            input.genero, dniInt, input.localidad, input.agrupacion,
            input.nombreContacto, input.telContacto
    );

    isLoading.setValue(true);
    repo.actualizarRunner(request, new Callback<PerfilUsuarioResponse>() {
      @Override
      public void onResponse(Call<PerfilUsuarioResponse> call, Response<PerfilUsuarioResponse> response) {

        if (response.isSuccessful()) {
          cargarPerfil(); // recargamos los datos frescos
          // Logica de estado post-guardado
          isEditable.setValue(false);
          btnText.setValue("Editar");
          mensajeToast.setValue("Perfil actualizado correctamente");
        } else {
          isLoading.setValue(false);
          mensajeToast.setValue("Error al actualizar");
        }
      }
      @Override
      public void onFailure(Call<PerfilUsuarioResponse> call, Throwable t) {
        isLoading.setValue(false);
        mensajeToast.setValue("Fallo de red");
      }
    });
  }

  // --- UTILS ---
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
          mensajeToast.setValue("Foto actualizada con éxito");
        } else {
          mensajeToast.setValue("Error al subir imagen");
        }
      }
      @Override
      public void onFailure(Call<PerfilUsuarioResponse> call, Throwable t) {
        isLoading.setValue(false);
        mensajeToast.setValue("Error de red");
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
          mensajeToast.setValue("Foto eliminada");
        } else {
          mensajeToast.setValue("Error al eliminar");
        }
      }
      @Override
      public void onFailure(Call<PerfilUsuarioResponse> call, Throwable t) {
        isLoading.setValue(false);
        mensajeToast.setValue("Error de red");
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
    return 0; // Default
  }



}