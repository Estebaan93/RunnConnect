package com.example.runnconnect.ui.login;

import android.app.Application;
import android.content.Intent;
import android.util.Log;
import android.view.View; // Importante para usar View.VISIBLE

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.runnconnect.MainActivity;
import com.example.runnconnect.data.conexion.ApiClient;
import com.example.runnconnect.data.conexion.ApiService;
import com.example.runnconnect.data.preferencias.SessionManager;
import com.example.runnconnect.data.request.LoginRequest;
import com.example.runnconnect.data.request.ReactivarCuentaRequest;
import com.example.runnconnect.data.request.RecuperarPasswordRequest;
import com.example.runnconnect.data.request.RestablecerPasswordRequest;
import com.example.runnconnect.data.request.SolicitarReactivacionRequest;
import com.example.runnconnect.data.response.LoginResponse;
import com.example.runnconnect.ui.eventosPublicos.EventosPublicosActivity;
import com.example.runnconnect.ui.registro.RegisterOrganizadorActivity;
import com.example.runnconnect.ui.registro.RegisterRunnerActivity;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginViewModel extends AndroidViewModel {

  private final ApiService apiService;
  private final SessionManager sessionManager;

  // Estados de Datos
  private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>();
  private final MutableLiveData<Intent> navegacionEvento = new MutableLiveData<>();
  //nuevo 20-06
  private final MutableLiveData<Boolean> finalUser = new MutableLiveData<>();

  // Estados para el Error (rojo)
  private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
  private final MutableLiveData<Integer> errorVisibility = new MutableLiveData<>(View.GONE);

  // Estados para el exito (verde)
  private final MutableLiveData<String> exito = new MutableLiveData<>();
  private final MutableLiveData<Integer> exitoVisibility = new MutableLiveData<>(View.GONE);

  //estado para controlar la nav al login de vuelta
  private final MutableLiveData<Boolean> pedirConfirmacionReactivacion = new MutableLiveData<>();

  //navegar al login
  public LoginViewModel(@NonNull Application application) {
    super(application);
    apiService = ApiClient.getApiService();
    sessionManager = new SessionManager(application);
  }

  // Getters
  public LiveData<Boolean> getIsLoading() { return isLoading; }
  public LiveData<String> getErrorMessage() { return errorMessage; }
  public LiveData<Integer> getErrorVisibility() { return errorVisibility; }
  public LiveData<String> getExito() { return exito; }
  public LiveData<Integer> getExitoVisibility() { return exitoVisibility; }
  public LiveData<Intent> getNavegacionEvento() { return navegacionEvento; }
  //nuevo 20-06
  public LiveData<Boolean> getFinalUser() { return finalUser; }
  public LiveData<Boolean> getPedirConfirmacionReactivacion() { return pedirConfirmacionReactivacion; }
  //public LiveData<Boolean> getNavegarAlLogin() { return navegarAlLogin; }


  // login
  public void login(String email, String password) {
    prepararNuevaAccion();

    if (email.isEmpty() || password.isEmpty()) {
      mostrarError("Por favor complete todos los campos");
      return;
    }

    isLoading.setValue(true);
    LoginRequest request = new LoginRequest(email, password);
    apiService.login(request).enqueue(new Callback<LoginResponse>() {
      @Override
      public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
        isLoading.setValue(false);
        if (response.isSuccessful() && response.body() != null) {
          sessionManager.guardarSesionUsuario(response.body());
          decidirNavegacionSegunRol();
        } else {
          try {
            String errorBody = response.errorBody() != null ? response.errorBody().string() : "";
            if (errorBody.contains("desactivada") || errorBody.contains("inhabilitada")) {
              pedirConfirmacionReactivacion.setValue(true);
            } else {
              mostrarError("Usuario o contraseña incorrectos");
            }
          } catch (Exception e) {
            mostrarError("Error en las credenciales");
          }
        }
      }

      @Override
      public void onFailure(Call<LoginResponse> call, Throwable t) {
        isLoading.setValue(false);
        mostrarError("Error de conexión: " + t.getMessage());
      }
    });
  }

  public void recuperarPassword(String email) {
    prepararNuevaAccion();

    if (email.isEmpty()) {
      mostrarError("Debes ingresar tu email");
      return;
    }

    isLoading.setValue(true);
    apiService.recuperarPassword(new RecuperarPasswordRequest(email)).enqueue(new Callback<Void>() {
      @Override
      public void onResponse(Call<Void> call, Response<Void> response) {
        isLoading.setValue(false);
        if (response.isSuccessful()) {
          mostrarExito("Email enviado. Revisa tu bandeja de entrada");
        } else {
          mostrarError("No se encontró su cuenta de email");
        }
      }

      @Override
      public void onFailure(Call<Void> call, Throwable t) {
        isLoading.setValue(false);
        mostrarError("Error de conexión con el servidor");
      }
    });
  }

  public void solicitarReactivacion(String email, String password) {
    prepararNuevaAccion();

    if (email.isEmpty() || password.isEmpty()) {
      mostrarError("Complete email y contraseña para reactivar");
      return;
    }

    isLoading.setValue(true);
    apiService.solicitarReactivacion(new SolicitarReactivacionRequest(email, password)).enqueue(new Callback<Void>() {
      @Override
      public void onResponse(Call<Void> call, Response<Void> response) {
        isLoading.setValue(false);
        if (response.isSuccessful()) {
          mostrarExito("Solicitud enviada. Revisa tu email para reactivar");
        } else {
          mostrarError("Credenciales inválidas o cuenta ya activa");
        }
      }

      @Override
      public void onFailure(Call<Void> call, Throwable t) {
        isLoading.setValue(false);
        mostrarError("Error de servidor");
      }
    });
  }

  public void confirmarReactivacionFinal(String token) {
    prepararNuevaAccion();

    //valiacion seguridad
    if (token == null || token.isEmpty()) {
      mostrarError("Enlace de reactivación inválido o corrupto");
      navegarAlLoginActivity();
      return; // Al llamar a mostrarError, la Activity escuchara y hara finish()
    }

    Log.d("DEBUG_TOKEN", "Enviando token al servidor: [" + token + "]");
    isLoading.setValue(true);

    //
    apiService.confirmarReactivacion(new ReactivarCuentaRequest(token)).enqueue(new Callback<LoginResponse>() {
      @Override
      public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
        isLoading.setValue(false);
        if (response.isSuccessful() && response.body() != null) {
          sessionManager.guardarSesionUsuario(response.body());
          decidirNavegacionSegunRol();
        } else {
          mostrarError("El token es inválido o ya expiró");
          navegarAlLoginActivity();
        }
      }

      @Override
      public void onFailure(Call<LoginResponse> call, Throwable t) {
        isLoading.setValue(false);
        mostrarError("Error de conexión al reactivar: " + t.getMessage());
        Log.e("API_ERROR", "Falla en reactivacion", t);
        navegarAlLoginActivity();
      }
    });
  }

  //para la recuperacion de passowrd
  public void ejecutarRestablecerPassword(String token, String pass1, String pass2) {
    prepararNuevaAccion();

    // 1. Logica de validacion (Fuera de la Vista)
    if (token == null || token.isEmpty()) {
      mostrarError("Error de seguridad: Token no encontrado");
      return;
    }

    if (pass1.isEmpty() || pass2.isEmpty()) {
      mostrarError("Completa ambos campos");
      return;
    }

    if (pass1.length() < 6) {
      mostrarError("La contraseña debe tener al menos 6 caracteres");
      return;
    }

    if (!pass1.equals(pass2)) {
      mostrarError("Las contraseñas no coinciden");
      return;
    }

    // 2. Peticion a la API
    isLoading.setValue(true);
    apiService.restablecerPassword(new RestablecerPasswordRequest(token, pass1, pass2)).enqueue(new Callback<Void>() {
      @Override
      public void onResponse(Call<Void> call, Response<Void> response) {
        isLoading.setValue(false);
        if (response.isSuccessful()) {
          mostrarExito("Contraseña actualizada exitosamente");
          navegarAlLoginActivity();
        } else {
          try {
            String errorReal = response.errorBody() != null ? response.errorBody().string() : "";
            if (errorReal.contains("expirado")) {
              mostrarError("El enlace ha expirado. Solicita uno nuevo.");
            } else {
              mostrarError("El enlace es inválido o ya fue utilizado.");
            }
          } catch (Exception e) {
            mostrarError("El enlace es inválido o ya fue utilizado.");
          }
        }
      }

      @Override
      public void onFailure(Call<Void> call, Throwable t) {
        isLoading.setValue(false);
        mostrarError("Error de conexión: " + t.getMessage());
      }
    });
  }

  //nuevo 20-06
  public void onRolSeleccionadoParaRegistro(int indice) {
    // 0 = runner, 1 = organizador
    Class<?> destino = (indice == 0) ? RegisterRunnerActivity.class : RegisterOrganizadorActivity.class;
    Intent intent = new Intent(getApplication(), destino);
    navegacionEvento.setValue(intent);
    finalUser.setValue(false);
  }

  //nuevo 21-06
  private void navegarAlLoginActivity() {
    Intent intent = new Intent(getApplication(), LoginActivity.class);
    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
    navegacionEvento.setValue(intent);
    finalUser.setValue(true);
  }



  //HELPER PARA LA VISTA
  private void prepararNuevaAccion() {
    errorVisibility.setValue(View.GONE);
    exitoVisibility.setValue(View.GONE);
  }

  private void mostrarError(String mensaje) {
    errorMessage.setValue(mensaje);
    errorVisibility.setValue(View.VISIBLE);
    exitoVisibility.setValue(View.GONE);
  }

  private void mostrarExito(String mensaje) {
    exito.setValue(mensaje);
    exitoVisibility.setValue(View.VISIBLE);
    errorVisibility.setValue(View.GONE);
  }

  public void esVisitanteClicked() {
    Intent intent = new Intent(getApplication(), EventosPublicosActivity.class);
    navegacionEvento.setValue(intent);
    finalUser.setValue(false); //nuevo 20-06 - no cerramos el login
  }

  private void decidirNavegacionSegunRol() {
    Intent intent = new Intent(getApplication(), MainActivity.class);
    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
    navegacionEvento.setValue(intent);
    finalUser.setValue(true); //nuevo 20-06 - cerrar login
  }

  // cuando el usuario toca la flecha atras
  public void volverAtrasClick(){
    navegarAlLoginActivity();
  }

  //verificar token
  public void verificarTokenRecuperacion(String token) {
    if (token == null || token.isEmpty()) {
      mostrarError("Enlace inválido o corrupto");
      //navegarAlLogin.setValue(true); // Ordena a la vista que se cierre
      navegarAlLoginActivity();
    }
  }


}