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
import com.example.runnconnect.data.repositorio.UsuarioRepositorio;
import com.example.runnconnect.data.response.LoginResponse;
import com.example.runnconnect.ui.eventosPublicos.EventosPublicosActivity;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginViewModel extends AndroidViewModel {

  private final UsuarioRepositorio repositorio;

  // Estados de Datos
  private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>();
  private final MutableLiveData<Intent> navegacionEvento = new MutableLiveData<>();

  // Estados para el Error (rojo)
  private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
  private final MutableLiveData<Integer> errorVisibility = new MutableLiveData<>(View.GONE);

  // Estados para el exito (verde)
  private final MutableLiveData<String> exito = new MutableLiveData<>();
  private final MutableLiveData<Integer> exitoVisibility = new MutableLiveData<>(View.GONE);

  //estado para controlar la nav al login de vuelta
  private final MutableLiveData<Boolean> pedirConfirmacionReactivacion = new MutableLiveData<>();

  //navegar al login
  private final MutableLiveData<Boolean> navegarAlLogin = new MutableLiveData<>();

  public LoginViewModel(@NonNull Application application) {
    super(application);
    repositorio = new UsuarioRepositorio(application);
  }

  // Getters
  public LiveData<Boolean> getIsLoading() { return isLoading; }
  public LiveData<String> getErrorMessage() { return errorMessage; }
  public LiveData<Integer> getErrorVisibility() { return errorVisibility; }
  public LiveData<String> getExito() { return exito; }
  public LiveData<Integer> getExitoVisibility() { return exitoVisibility; }
  public LiveData<Intent> getNavegacionEvento() { return navegacionEvento; }
  public LiveData<Boolean> getPedirConfirmacionReactivacion() { return pedirConfirmacionReactivacion; }
  public LiveData<Boolean> getNavegarAlLogin() {return navegarAlLogin; }


  // login
  public void login(String email, String password) {
    prepararNuevaAccion();

    if (email.isEmpty() || password.isEmpty()) {
      mostrarError("Por favor complete todos los campos");
      return;
    }

    isLoading.setValue(true);
    repositorio.login(email, password, new Callback<LoginResponse>() {
      @Override
      public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
        isLoading.setValue(false);
        if (response.isSuccessful() && response.body() != null) {
          repositorio.guardarSesion(response.body());
          decidirNavegacionSegunRol();
        } else {
          // REVISAR SI EL ERROR ES POR CUENTA DESACTIVADA
          try {
            String errorBody = response.errorBody().string();
            if (errorBody.contains("desactivada") || errorBody.contains("inhabilitada")) {
              // Disparamos el evento para que la vista pregunte
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
    repositorio.recuperarPassword(email, new Callback<Void>() {
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
    repositorio.solicitarReactivacion(email, password, new Callback<Void>() {
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
      return; // Al llamar a mostrarError, la Activity escuchara y hara finish()
    }

    Log.d("DEBUG_TOKEN", "Enviando token al servidor: [" + token + "]");
    isLoading.setValue(true);

    //
    repositorio.confirmarReactivacion(token, new Callback<LoginResponse>() {
      @Override
      public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
        isLoading.setValue(false);
        if (response.isSuccessful() && response.body() != null) {
          // Guardamos la sesiin (token JWT) que nos dio el servidor
          repositorio.guardarSesion(response.body());

          // Mandamos al MainActivity (Ya esta activo y logueado)
          decidirNavegacionSegunRol();
        } else {
          mostrarError("El token es inválido o ya expiró");
        }
      }

      @Override
      public void onFailure(Call<LoginResponse> call, Throwable t) {
        isLoading.setValue(false);
        mostrarError("Error de conexión al reactivar: "+ t.getMessage());
        Log.e("API_ERROR", "Falla en reactivacion", t);
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
    repositorio.restablecerPassword(token, pass1, pass2, new Callback<Void>() {
      @Override
      public void onResponse(Call<Void> call, Response<Void> response) {
        isLoading.setValue(false);
        if (response.isSuccessful()) {
          // La palabra "actualizada" es clave porque la Activity la esta escuchando para cerrarse
          mostrarExito("Contraseña actualizada exitosamente");
          navegarAlLogin.setValue(true);
        } else {
          // Intentar capturar el mensaje real de C#
          try {
            String errorReal = response.errorBody() != null ? response.errorBody().string() : "";
            Log.d("ErrorTokenRecuperarPass", "Response: " + errorReal);
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





  //HELPER PARA LA VISTA

  // Llama a esto desde el Activity para resetear el estado
  public void confirmarReactivacionMostrada() {
    pedirConfirmacionReactivacion.setValue(false);
  }

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
  }

  private void decidirNavegacionSegunRol() {
    Intent intent = new Intent(getApplication(), MainActivity.class);
    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
    navegacionEvento.setValue(intent);
  }

  // cuando el usuario toca la flecha atras
  public void volverAtrasClick(){
    navegarAlLogin.setValue(true);
  }

  //cuando la vista ya navego resetea el estado
  public void navegacionALoginCompletada() {
    navegarAlLogin.setValue(false);
  }

  //verificar token
  public void verificarTokenRecuperacion(String token) {
    if (token == null || token.isEmpty()) {
      mostrarError("Enlace inválido o corrupto");
      navegarAlLogin.setValue(true); // Ordena a la vista que se cierre
    }
  }


}