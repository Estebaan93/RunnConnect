package com.example.runnconnect.data.repositorio;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import com.example.runnconnect.data.conexion.ApiClient;
import com.example.runnconnect.data.conexion.ApiService;
import com.example.runnconnect.data.preferencias.SessionManager;
import com.example.runnconnect.data.request.CrearEventoRequest;

import okhttp3.ResponseBody;
import retrofit2.Callback;

public class EventoRepositorio {
  private final ApiService apiService;
  private final SessionManager sessionManager;

  public EventoRepositorio(Application application) {
    this.sessionManager = new SessionManager(application);
    this.apiService = ApiClient.getApiService();
  }

  public void crearEvento(CrearEventoRequest request, Callback<ResponseBody> callback) {
    String token = sessionManager.leerToken();
    if (token != null && !token.isEmpty()) {
      apiService.crearEvento("Bearer " + token, request).enqueue(callback);
    } else {
      // Manejar error de no autenticado si es necesario
      callback.onFailure(null, new Throwable("No hay sesión activa. Por favor inicie sesión nuevamente."));
    }
  }


}
