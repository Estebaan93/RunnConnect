package com.example.runnconnect.ui.organizador.perfil;

import android.app.Application;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.runnconnect.data.repositorio.UsuarioRepositorio;
import com.example.runnconnect.data.request.ActualizarPerfilOrganizadorRequest;
import com.example.runnconnect.data.response.PerfilUsuarioResponse;

import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.regex.Pattern;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class PerfilOrganizadorViewModel extends AndroidViewModel {
  private final UsuarioRepositorio repo;
  // TODO: Implement the ViewModel

  // estados de Datos
  private final MutableLiveData<PerfilUsuarioResponse> perfilData = new MutableLiveData<>();
  private final MutableLiveData<Boolean> isEditable = new MutableLiveData<>(false);
  private final MutableLiveData<String> btnText = new MutableLiveData<>("Editar");
  private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
  private final MutableLiveData<String> avatarUrl = new MutableLiveData<>();

  // mensajes Globales
  private final MutableLiveData<String> mensajeGlobal = new MutableLiveData<>();
  private final MutableLiveData<Boolean> esMensajeError = new MutableLiveData<>(false);

  // errores de Campos Específicos Organizador
  private final MutableLiveData<String> errorNombreComercial = new MutableLiveData<>();
  private final MutableLiveData<String> errorRazonSocial = new MutableLiveData<>();
  private final MutableLiveData<String> errorCuit = new MutableLiveData<>();
  private final MutableLiveData<String> errorNombreContacto = new MutableLiveData<>(); // Campo 'Nombre' en Usuario
  private final MutableLiveData<String> errorTelefono = new MutableLiveData<>();
  private final MutableLiveData<String> errorDireccion = new MutableLiveData<>();

  // eventos (Imagenes)
  private final MutableLiveData<Boolean> eventShowAvatarOptions = new MutableLiveData<>(false);
  private final MutableLiveData<Boolean> eventShowDeleteConfirmation = new MutableLiveData<>(false);
  private final MutableLiveData<Boolean> eventOpenGallery = new MutableLiveData<>(false);
  private final MutableLiveData<String> eventShowZoomImage = new MutableLiveData<>();

  public PerfilOrganizadorViewModel(@NonNull Application application) {
    super(application);
    repo = new UsuarioRepositorio(application);
  }

  //get
  public LiveData<PerfilUsuarioResponse> getPerfilData() { return perfilData; }
  public LiveData<Boolean> getIsEditable() { return isEditable; }
  public LiveData<String> getBtnText() { return btnText; }
  public LiveData<Boolean> getIsLoading() { return isLoading; }
  public LiveData<String> getAvatarUrl() { return avatarUrl; }
  public LiveData<String> getMensajeGlobal() { return mensajeGlobal; }
  public LiveData<Boolean> getEsMensajeError() { return esMensajeError; }

  // get Errores
  public LiveData<String> getErrorNombreComercial() { return errorNombreComercial; }
  public LiveData<String> getErrorRazonSocial() { return errorRazonSocial; }
  public LiveData<String> getErrorCuit() { return errorCuit; }
  public LiveData<String> getErrorNombreContacto() { return errorNombreContacto; }
  public LiveData<String> getErrorTelefono() { return errorTelefono; }
  public LiveData<String> getErrorDireccion() { return errorDireccion; }

  // get Eventos
  public LiveData<Boolean> getEventShowAvatarOptions() { return eventShowAvatarOptions; }
  public LiveData<Boolean> getEventShowDeleteConfirmation() { return eventShowDeleteConfirmation; }
  public LiveData<Boolean> getEventOpenGallery() { return eventOpenGallery; }
  public LiveData<String> getEventShowZoomImage() { return eventShowZoomImage; }


  //acciones
  public void onBotonPrincipalClick(OrganizadorInput input) {
    if (Boolean.TRUE.equals(isEditable.getValue())) {
      guardarCambios(input);
    } else {
      habilitarEdicion();
    }
  }

  //acciones de Imagen (Igual que Runner)
  public void onEditAvatarClicked() { eventShowAvatarOptions.setValue(true); }
  public void onChangePhotoOptionSelected() { eventOpenGallery.setValue(true); }
  public void onDeletePhotoOptionSelected() { eventShowDeleteConfirmation.setValue(true); }
  public void onDeleteConfirmed() { borrarFoto(); }
  public void onAvatarImageClicked() {
    String currentUrl = avatarUrl.getValue();
    if (currentUrl != null && !currentUrl.isEmpty()) eventShowZoomImage.setValue(currentUrl);
  }

  public void onImagenSeleccionada(Uri uri) {
    if (uri == null) return;
    File archivo = convertirUriAFile(uri);
    if (archivo != null) subirNuevaFoto(archivo);
    else mostrarMensajeGlobal("Error al procesar imagen", true);
  }

  // Reset eventos
  public void onAvatarOptionsConsumed() { eventShowAvatarOptions.setValue(false); }
  public void onDeleteConfirmationConsumed() { eventShowDeleteConfirmation.setValue(false); }
  public void onGalleryOpenConsumed() { eventOpenGallery.setValue(false); }
  public void onZoomImageConsumed() { eventShowZoomImage.setValue(null); }


  //logicade negocio

  public void cargarPerfil() {
    isLoading.setValue(true);
    mostrarMensajeGlobal(null, false);

    repo.obtenerPerfil(new Callback<PerfilUsuarioResponse>() {
      @Override
      public void onResponse(Call<PerfilUsuarioResponse> call, Response<PerfilUsuarioResponse> response) {
        isLoading.setValue(false);
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
        isLoading.setValue(false);
        mostrarMensajeGlobal("Error de conexión", true);
      }
    });
  }

  private void habilitarEdicion() {
    isEditable.setValue(true);
    btnText.setValue("Guardar");
    mostrarMensajeGlobal(null, false);
  }

  private void guardarCambios(OrganizadorInput input) {
    boolean esValido = true;

    //validaciones Especificas Organizador
    if (input.nombreComercial == null || input.nombreComercial.trim().length() < 3) {
      errorNombreComercial.setValue("Mínimo 3 caracteres"); esValido = false;
    } else errorNombreComercial.setValue(null);

    if (input.razonSocial == null || input.razonSocial.trim().length() < 2) {
      errorRazonSocial.setValue("Requerido"); esValido = false;
    } else errorRazonSocial.setValue(null);

    // Validacion CUIT (Regex simple para XX-XXXXXXXX-X o 11 dígitos seguidos)
    if (input.cuit == null || !Pattern.matches("^\\d{2}-\\d{8}-\\d{1}$|^\\d{11}$", input.cuit)) {
      errorCuit.setValue("Formato inválido (XX-XXXXXXXX-X o 11 números)"); esValido = false;
    } else errorCuit.setValue(null);

    if (input.direccion == null || input.direccion.trim().isEmpty()) {
      errorDireccion.setValue("Requerido"); esValido = false;
    } else errorDireccion.setValue(null);

    if (input.nombreContacto == null || input.nombreContacto.trim().length() < 3) {
      errorNombreContacto.setValue("Mínimo 3 caracteres"); esValido = false;
    } else errorNombreContacto.setValue(null);

    if (input.telefono == null || input.telefono.trim().length() < 7) {
      errorTelefono.setValue("Mínimo 7 dígitos"); esValido = false;
    } else errorTelefono.setValue(null);

    if (!esValido) return;

    //crear Request DTO
    ActualizarPerfilOrganizadorRequest request = new ActualizarPerfilOrganizadorRequest(
      input.nombreContacto,
      input.telefono,
      input.razonSocial,
      input.nombreComercial,
      input.cuit,
      input.direccion
    );

    isLoading.setValue(true);
    mostrarMensajeGlobal(null, false);

    //llamada al repositorio (ENDPOINT DE ORGANIZADOR)
    repo.actualizarOrganizador(request, new Callback<PerfilUsuarioResponse>() {
      @Override
      public void onResponse(Call<PerfilUsuarioResponse> call, Response<PerfilUsuarioResponse> response) {
        if (response.isSuccessful()) {
          cargarPerfil();
          isEditable.setValue(false);
          btnText.setValue("Editar");
          mostrarMensajeGlobal("Perfil de Organizador actualizado", false);
        } else {
          isLoading.setValue(false);
          String msg = "Error al actualizar";
          try {
            if (response.errorBody() != null) {
              String errorRaw = response.errorBody().string();
              if (errorRaw.contains("message")) {
                msg = new JSONObject(errorRaw).getString("message");
              }
            }
          } catch (Exception e) { e.printStackTrace(); }
          mostrarMensajeGlobal(msg, true);
        }
      }

      @Override
      public void onFailure(Call<PerfilUsuarioResponse> call, Throwable t) {
        isLoading.setValue(false);
        mostrarMensajeGlobal("Fallo de red", true);
      }
    });
  }

  //metodos Helper (Privados)
  private void mostrarMensajeGlobal(String mensaje, boolean esError){
    mensajeGlobal.setValue(mensaje);
    esMensajeError.setValue(esError);
  }

  private void procesarAvatar(String url) {
    if (url == null || url.isEmpty()) {
      avatarUrl.setValue(null);
      return;
    }
    if (url.contains("localhost")) url = url.replace("localhost", "10.0.2.2");
    avatarUrl.setValue(url);
  }

  //funciones de Archivo
  private File convertirUriAFile(Uri uri) {
    try {
      InputStream inputStream = getApplication().getContentResolver().openInputStream(uri);
      File tempFile = File.createTempFile("avatar_upload", ".jpg", getApplication().getCacheDir());
      FileOutputStream outputStream = new FileOutputStream(tempFile);
      byte[] buffer = new byte[1024];
      int length;
      while ((length = inputStream.read(buffer)) > 0) outputStream.write(buffer, 0, length);
      outputStream.close();
      if(inputStream!=null) inputStream.close();
      return tempFile;
    } catch (Exception e) { return null; }
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
        } else mostrarMensajeGlobal("Error al subir imagen", true);
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
        } else mostrarMensajeGlobal("Error al eliminar", true);
      }
      @Override
      public void onFailure(Call<PerfilUsuarioResponse> call, Throwable t) {
        isLoading.setValue(false);
        mostrarMensajeGlobal("Error de red", true);
      }
    });
  }

  // DTO Input para la vista
  public static class OrganizadorInput {
    public String nombreComercial, razonSocial, cuit, nombreContacto, telefono, direccion;
    public OrganizadorInput(String nombreComercial, String razonSocial, String cuit, String nombreContacto, String telefono, String direccion) {
      this.nombreComercial = nombreComercial;
      this.razonSocial = razonSocial;
      this.cuit = cuit;
      this.nombreContacto = nombreContacto;
      this.telefono = telefono;
      this.direccion = direccion;
    }
  }


}