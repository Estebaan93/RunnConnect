package com.example.runnconnect;

import android.app.Application;
import android.content.SharedPreferences;

import android.view.View;

import com.example.runnconnect.data.conexion.ApiClient;
import com.example.runnconnect.data.conexion.ApiService;
import com.example.runnconnect.data.fcm.FcmTopicManager;
import com.example.runnconnect.data.preferencias.SessionManager;
import com.example.runnconnect.data.response.ContadorNotificacionesResponse;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainViewModel extends AndroidViewModel {

  private final MutableLiveData<String> nombreUsuario = new MutableLiveData<>();
  private final MutableLiveData<String> emailUsuario = new MutableLiveData<>();
  private final MutableLiveData<String> avatarUrl = new MutableLiveData<>();

  private final MutableLiveData<Integer> menuResource = new MutableLiveData<>();
  private final MutableLiveData<String> contadorTexto = new MutableLiveData<>("");
  private final MutableLiveData<Integer> contadorVisibility = new MutableLiveData<>(View.GONE);

  private final ApiService apiService;
  private final SessionManager sessionManager;

  public MainViewModel(@NonNull Application application) {
    super(application);
    this.apiService = ApiClient.getApiService();
    this.sessionManager = new SessionManager(application);
    cargarDatosSesion(application);
    cargarContadorNotificaciones();
    FcmTopicManager.suscribirTopicosUsuario(sessionManager.getIdUsuario());
  }

  // --- GETTERS ---
  public LiveData<String> getNombreUsuario() { return nombreUsuario; }
  public LiveData<String> getEmailUsuario() { return emailUsuario; }
  public LiveData<String> getAvatarUrl() { return avatarUrl; }
  public LiveData<Integer> getMenuResource() { return menuResource; }
  public LiveData<String> getContadorTexto() { return contadorTexto; }
  public LiveData<Integer> getContadorVisibility() { return contadorVisibility; }


  private void cargarDatosSesion(Application application) {
    // El ViewModel se encarga de buscar los datos en SharedPreferences o el Repositorio
    SharedPreferences sp = application.getSharedPreferences("session_sp", 0);
    String tipo = sessionManager.getTipoUsuario();
    String nombre = sp.getString("nombre", "Usuario");
    String email = sp.getString("email", "correo@ejemplo.com");
    String avatar = sp.getString("imgAvatar", "");

    // Emitimos los textos puros
    nombreUsuario.setValue(nombre);
    emailUsuario.setValue(email);


    if (avatar == null) {
      avatar = "";
    } else if (avatar.contains("localhost")) {
      avatar = avatar.replace("localhost", "10.0.2.2");
    }
    avatarUrl.setValue(avatar);

    // El ViewModel toma la decision de negocio y expone el recurso visual
    if ("organizador".equalsIgnoreCase(tipo)) {
      menuResource.setValue(R.menu.menu_organizador);
    } else {
      menuResource.setValue(R.menu.activity_main_drawer);
    }
  }

  public void cargarContadorNotificaciones() {
    String token = sessionManager.leerToken();
    if (token == null || token.trim().isEmpty()) {
      contadorTexto.setValue("");
      contadorVisibility.setValue(View.GONE);
      return;
    }

    apiService.obtenerContadorNotificaciones("Bearer " + token).enqueue(new Callback<ContadorNotificacionesResponse>() {
      @Override
      public void onResponse(Call<ContadorNotificacionesResponse> call, Response<ContadorNotificacionesResponse> response) {
        if (response.isSuccessful() && response.body() != null) {
          int cantidad = response.body().getCantidadRecientes();
          if (cantidad > 0) {
            contadorTexto.setValue(cantidad > 99 ? "99+" : String.valueOf(cantidad));
            contadorVisibility.setValue(View.VISIBLE);
          } else {
            contadorTexto.setValue("");
            contadorVisibility.setValue(View.GONE);
          }
        }
      }

      @Override
      public void onFailure(Call<ContadorNotificacionesResponse> call, Throwable t) {
        // En caso de fallo de conexión se mantiene el estado previo o invisible
      }
    });
  }

  public void limpiarContadorNotificaciones() {
    contadorTexto.setValue("");
    contadorVisibility.setValue(View.GONE);
  }

  public void onDestinoNavegado(int destinationId) {
    if (destinationId == R.id.nav_notificaciones) {
      limpiarContadorNotificaciones();
    } else {
      cargarContadorNotificaciones();
    }
  }

}
