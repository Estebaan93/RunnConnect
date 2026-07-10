package com.example.runnconnect;

import android.app.Application;
import android.content.SharedPreferences;

import com.example.runnconnect.data.conexion.ApiClient;
import com.example.runnconnect.data.conexion.ApiService;
import com.example.runnconnect.data.preferencias.SessionManager;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

public class MainViewModel extends AndroidViewModel {

  private final MutableLiveData<String> nombreUsuario = new MutableLiveData<>();
  private final MutableLiveData<String> emailUsuario = new MutableLiveData<>();
  private final MutableLiveData<String> avatarUrl = new MutableLiveData<>();

  private final MutableLiveData<Integer> menuResource = new MutableLiveData<>();
  private final ApiService apiService;
  private final SessionManager sessionManager;

  public MainViewModel(@NonNull Application application) {
    super(application);
    this.apiService = ApiClient.getApiService();
    this.sessionManager = new SessionManager(application);
    cargarDatosSesion(application);
  }

  // --- GETTERS ---
  public LiveData<String> getNombreUsuario() { return nombreUsuario; }
  public LiveData<String> getEmailUsuario() { return emailUsuario; }
  public LiveData<String> getAvatarUrl() { return avatarUrl; }
  public LiveData<Integer> getMenuResource() { return menuResource; }


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



}
