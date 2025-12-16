package com.example.runnconnect.ui.runner.perfil;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.runnconnect.data.repositorio.UsuarioRepositorio;
import com.example.runnconnect.data.request.ActualizarPerfilRunnerRequest;
import com.example.runnconnect.data.response.PerfilUsuarioResponse;

import java.io.File;

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

  // --- EVENTOS DE NAVEGACIÓN/ACCIÓN (Single Shot Events) ---
  // Usamos estos para decirle al Fragment que muestre dialogos
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

  // Getters de Eventos
  public LiveData<Boolean> getEventShowAvatarOptions() { return eventShowAvatarOptions; }
  public LiveData<Boolean> getEventShowDeleteConfirmation() { return eventShowDeleteConfirmation; }
  public LiveData<Boolean> getEventOpenGallery() { return eventOpenGallery; }
  public LiveData<String> getEventShowZoomImage() { return eventShowZoomImage; }


  // --- MÉTODOS DE ENTRADA (User Actions) ---

  // 1. Usuario hace clic en el icono de cámara (lápiz)
  public void onEditAvatarClicked() {
    // El VM decide mostrar las opciones
    eventShowAvatarOptions.setValue(true);
  }

  // 2. Usuario selecciona "Cambiar Foto" en el dialogo
  public void onChangePhotoOptionSelected() {
    eventOpenGallery.setValue(true);
  }

  // 3. Usuario selecciona "Eliminar Foto" en el dialogo
  public void onDeletePhotoOptionSelected() {
    eventShowDeleteConfirmation.setValue(true);
  }

  // 4. Usuario confirma "SÍ" en el dialogo de eliminar
  public void onDeleteConfirmed() {
    borrarFoto();
  }

  // 5. Usuario hace clic en la cara del runner
  public void onAvatarImageClicked() {
    String currentUrl = avatarUrl.getValue();
    if (currentUrl != null && !currentUrl.isEmpty()) {
      eventShowZoomImage.setValue(currentUrl);
    }
  }

  // Métodos para "consumir" los eventos y que no se repitan al rotar pantalla
  public void onAvatarOptionsShown() { eventShowAvatarOptions.setValue(false); }
  public void onDeleteConfirmationShown() { eventShowDeleteConfirmation.setValue(false); }
  public void onGalleryOpened() { eventOpenGallery.setValue(false); }
  public void onZoomImageShown() { eventShowZoomImage.setValue(null); }


  // --- LÓGICA DE NEGOCIO ---

  public void cargarPerfil() {
    isLoading.setValue(true);
    repo.obtenerPerfil(new Callback<PerfilUsuarioResponse>() {
      @Override
      public void onResponse(Call<PerfilUsuarioResponse> call, Response<PerfilUsuarioResponse> response) {
        isLoading.setValue(false);
        if (response.isSuccessful() && response.body() != null) {
          PerfilUsuarioResponse p = response.body();
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

  public void onBotonClick(RunnerInput input) {
    if (Boolean.TRUE.equals(isEditable.getValue())) {
      intentarGuardarCambios(input);
    } else {
      habilitarEdicion();
    }
  }

  private void habilitarEdicion() {
    isEditable.setValue(true);
    btnText.setValue("Guardar");
  }

  private void intentarGuardarCambios(RunnerInput input) {
    int dniInt = 0;
    try {
      if (input.dni != null && !input.dni.isEmpty()) {
        dniInt = Integer.parseInt(input.dni);
      }
    } catch (NumberFormatException e) {
      mensajeToast.setValue("El DNI debe ser un numero valido");
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
        isLoading.setValue(false);
        if (response.isSuccessful() && response.body() != null) {
          PerfilUsuarioResponse p = response.body();
          perfilData.setValue(p);
          procesarAvatar(p.getImgAvatar());
          isEditable.setValue(false);
          btnText.setValue("Editar");
          mensajeToast.setValue("Perfil actualizado correctamente");
        } else {
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

  public static class RunnerInput {
    public String nombre, apellido, telefono, dni, fechaNac, genero, localidad, agrupacion, nombreContacto, telContacto;
    public RunnerInput(String nombre, String apellido, String telefono, String dni, String fechaNac, String genero, String localidad, String agrupacion, String nombreContacto, String telContacto) {
      this.nombre = nombre; this.apellido = apellido; this.telefono = telefono; this.dni = dni; this.fechaNac = fechaNac; this.genero = genero; this.localidad = localidad; this.agrupacion = agrupacion; this.nombreContacto = nombreContacto; this.telContacto = telContacto;
    }
  }
}