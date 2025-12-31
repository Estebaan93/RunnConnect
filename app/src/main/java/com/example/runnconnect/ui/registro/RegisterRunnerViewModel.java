package com.example.runnconnect.ui.registro;

import android.app.Application;
import android.content.Intent;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.runnconnect.MainActivity;
import com.example.runnconnect.data.repositorio.UsuarioRepositorio;
import com.example.runnconnect.data.response.LoginResponse;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterRunnerViewModel extends AndroidViewModel {
  private final UsuarioRepositorio repositorio;

  // Estados
  private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
  private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
  private final MutableLiveData<Uri> avatarUri = new MutableLiveData<>();
  private final MutableLiveData<Intent> navigateToMain = new MutableLiveData<>();

  public RegisterRunnerViewModel(@NonNull Application application) {
    super(application);
    repositorio = new UsuarioRepositorio(application);
  }

  // Getters
  public LiveData<Boolean> getIsLoading() { return isLoading; }
  public LiveData<String> getErrorMessage() { return errorMessage; }
  public LiveData<Uri> getAvatarUri() { return avatarUri; }
  public LiveData<Intent> getNavigateToMain() { return navigateToMain; }

  public void onAvatarSelected(Uri uri) {
    avatarUri.setValue(uri);
  }

  public void registrar(String nombre, String apellido, String email, String pass, String confirm) {
    errorMessage.setValue(null);

    // Validaciones Locales
    if (nombre.isEmpty() || apellido.isEmpty() || email.isEmpty() || pass.isEmpty()) {
      errorMessage.setValue("Todos los campos son obligatorios"); return;
    }
    if (!pass.equals(confirm)) {
      errorMessage.setValue("Las contraseñas no coinciden"); return;
    }
    if (pass.length() < 6) {
      errorMessage.setValue("La contraseña debe tener al menos 6 caracteres"); return;
    }

    isLoading.setValue(true);

    // Procesar imagen (si hay)
    File fileAvatar = null;
    if (avatarUri.getValue() != null) {
      fileAvatar = convertirUriAFile(avatarUri.getValue());
    }

    // Llamada al Repo (registrarRunner)
    repositorio.registrarRunner(nombre, apellido, email, pass, confirm, fileAvatar, new Callback<LoginResponse>() {
      @Override
      public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
        isLoading.setValue(false);
        if (response.isSuccessful() && response.body() != null) {
          // Guardar sesión y navegar
          repositorio.guardarSesion(response.body());

          Intent intent = new Intent(getApplication(), MainActivity.class);
          intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
          navigateToMain.setValue(intent);
        } else {
          // Manejo de errores del servidor
          String error = "Error al registrarse";
          try {
            if (response.errorBody() != null) {
              String errorRaw = response.errorBody().string();
              // Intenta leer mensaje simple
              if(errorRaw.contains("message")) {
                error = new org.json.JSONObject(errorRaw).getString("message");
              }
            }
          } catch (Exception e) { e.printStackTrace(); }
          errorMessage.setValue(error);
        }
      }

      @Override
      public void onFailure(Call<LoginResponse> call, Throwable t) {
        isLoading.setValue(false);
        errorMessage.setValue("Error de conexión");
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
