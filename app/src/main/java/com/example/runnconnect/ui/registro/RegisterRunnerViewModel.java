package com.example.runnconnect.ui.registro;

import android.app.Application;
import android.net.Uri;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.runnconnect.data.conexion.ApiClient;
import com.example.runnconnect.data.conexion.ApiService;
import com.example.runnconnect.data.preferencias.SessionManager;
import com.example.runnconnect.data.response.LoginResponse;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterRunnerViewModel extends AndroidViewModel {
  private final ApiService apiService;
  private final SessionManager sessionManager;

  // Estados
  private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
  private final MutableLiveData<String> errorMessage = new MutableLiveData<>("");
  private final MutableLiveData<Integer> errorVisibility = new MutableLiveData<>(View.GONE);
  private final MutableLiveData<Uri> avatarUri = new MutableLiveData<>();
  private final MutableLiveData<Boolean> registroExitoso = new MutableLiveData<>();

  public RegisterRunnerViewModel(@NonNull Application application) {
    super(application);
    apiService = ApiClient.getApiService();
    sessionManager = new SessionManager(application);
  }

  // Getters
  public LiveData<Boolean> getIsLoading() { return isLoading; }
  public LiveData<String> getErrorMessage() { return errorMessage; }
  public LiveData<Integer> getErrorVisibility() { return errorVisibility; }
  public LiveData<Uri> getAvatarUri() { return avatarUri; }
  public LiveData<Boolean> getRegistroExitoso() { return registroExitoso; }

  public void onAvatarSelected(Uri uri) {
    avatarUri.setValue(uri);
  }

  private void mostrarError(String mensaje) {
    errorMessage.setValue(mensaje);
    errorVisibility.setValue(View.VISIBLE);
  }

  private void ocultarError() {
    errorVisibility.setValue(View.GONE);
  }

  public void registrar(String nombre, String apellido, String email, String pass, String confirm) {
    ocultarError();

    // Validaciones Locales
    if (nombre.isEmpty() || apellido.isEmpty() || email.isEmpty() || pass.isEmpty()) {
      mostrarError("Todos los campos son obligatorios");
      return;
    }
    if (!pass.equals(confirm)) {
      mostrarError("Las contraseñas no coinciden");
      return;
    }
    if (pass.length() < 6) {
      mostrarError("La contraseña debe tener al menos 6 caracteres");
      return;
    }

    isLoading.setValue(true);

    // Procesar imagen (si hay)
    File fileAvatar = null;
    if (avatarUri.getValue() != null) {
      fileAvatar = convertirUriAFile(avatarUri.getValue());
    }

    RequestBody rbNombre = RequestBody.create(MediaType.parse("text/plain"), nombre);
    RequestBody rbApellido = RequestBody.create(MediaType.parse("text/plain"), apellido);
    RequestBody rbEmail = RequestBody.create(MediaType.parse("text/plain"), email);
    RequestBody rbPass = RequestBody.create(MediaType.parse("text/plain"), pass);
    RequestBody rbConfirm = RequestBody.create(MediaType.parse("text/plain"), confirm);

    MultipartBody.Part bodyAvatar = null;
    if (fileAvatar != null) {
      RequestBody reqFile = RequestBody.create(MediaType.parse("image/*"), fileAvatar);
      bodyAvatar = MultipartBody.Part.createFormData("ImgAvatar", fileAvatar.getName(), reqFile);
    }

    // Llamada al Repo (registrarRunner)
    apiService.registrarRunner(rbNombre, rbApellido, rbEmail, rbPass, rbConfirm, bodyAvatar).enqueue(new Callback<LoginResponse>() {
      @Override
      public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
        isLoading.setValue(false);
        if (response.isSuccessful() && response.body() != null) {
          // Guardar sesión y emitir éxito
          sessionManager.guardarSesionUsuario(response.body());
          registroExitoso.setValue(true);
        } else {
          // Manejo de errores del servidor
          String error = "Error al registrarse";
          try {
            if (response.errorBody() != null) {
              String errorRaw = response.errorBody().string();
              // Intenta leer mensaje simple
              if (errorRaw.contains("message")) {
                error = new org.json.JSONObject(errorRaw).getString("message");
              }
            }
          } catch (Exception e) { e.printStackTrace(); }
          mostrarError(error);
        }
      }

      @Override
      public void onFailure(Call<LoginResponse> call, Throwable t) {
        isLoading.setValue(false);
        mostrarError("Error de conexión");
      }
    });
  }

  // Helper para imagen
  private File convertirUriAFile(Uri uri) {
    try {
      InputStream inputStream = getApplication().getContentResolver().openInputStream(uri);
      File tempFile = File.createTempFile("avatar_reg_runner", ".jpg", getApplication().getCacheDir());
      FileOutputStream outputStream = new FileOutputStream(tempFile);
      byte[] buffer = new byte[1024];
      int length;
      while ((length = inputStream.read(buffer)) > 0) outputStream.write(buffer, 0, length);
      outputStream.close();
      if(inputStream!=null) inputStream.close();
      return tempFile;
    } catch (Exception e) { return null; }
  }

}
