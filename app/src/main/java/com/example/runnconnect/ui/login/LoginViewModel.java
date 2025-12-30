//ui/login/LoginViewModel
package com.example.runnconnect.ui.login;

import android.app.Application;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.runnconnect.MainActivity;
import com.example.runnconnect.data.repositorio.UsuarioRepositorio;
import com.example.runnconnect.data.response.LoginResponse;
import com.example.runnconnect.ui.eventosPublicos.EventosPublicosActivity;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginViewModel extends AndroidViewModel {

  private final UsuarioRepositorio repositorio;

  // Estados para la Vista
  private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>();
  private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
  private final MutableLiveData<Intent> navegacionEvento = new MutableLiveData<>();

  public LoginViewModel(@NonNull Application application) {
    super(application);
    // Inicializamos el repositorio pasándole el contexto de la aplicación
    repositorio = new UsuarioRepositorio(application);
  }

  // Getters para observar
  public LiveData<Boolean> getIsLoading() { return isLoading; }
  public LiveData<String> getErrorMessage() { return errorMessage; }
  public LiveData<Intent> getNavegacionEvento() { return navegacionEvento; }

  public void login(String email, String password) {
    //limpiamos errores previos
    errorMessage.setValue(null);

    // Validaciones basicas antes de llamar a la red
    if (email.isEmpty() || password.isEmpty()) {
      errorMessage.setValue("Por favor complete todos los campos");
      return;
    }

    isLoading.setValue(true);

    // Llamamos al repositorio y le pasamos el Callback
    repositorio.login(email, password, new Callback<LoginResponse>() {
      @Override
      public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
        isLoading.setValue(false);

        if (response.isSuccessful() && response.body() != null) {
          //Guardar la sesion en SharedPreferences (via Repo)
          repositorio.guardarSesion(response.body());
          decidirNavegacionSegunRol();
        } else {
          // Error de credenciales (401)
          errorMessage.setValue("Usuario o contraseña incorrectos");
        }
      }

      @Override
      public void onFailure(Call<LoginResponse> call, Throwable t) {
        isLoading.setValue(false);
        errorMessage.setValue("Error de conexión: " + t.getMessage());
        Log.d("ERROR","Falla con el servidor"+t.getMessage());
      }
    });
  }

  public void esVisitanteClicked(){
    Intent intent= new Intent(getApplication(), EventosPublicosActivity.class);
    navegacionEvento.setValue(intent);
  }

  private void decidirNavegacionSegunRol(){
    //El mainActivity decide si el rol es orga o runner
    Intent intent= new Intent(getApplication(), MainActivity.class);
    //limpiamos el historial de login exitos
    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
    navegacionEvento.setValue(intent);

  }

}