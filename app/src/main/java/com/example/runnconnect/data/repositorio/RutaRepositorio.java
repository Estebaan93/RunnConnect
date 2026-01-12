package com.example.runnconnect.data.repositorio;

import android.app.Application;

import com.example.runnconnect.data.conexion.ApiClient;
import com.example.runnconnect.data.conexion.ApiService;
import com.example.runnconnect.data.preferencias.SessionManager;
import com.example.runnconnect.data.request.GuardarRutaRequest;
import com.example.runnconnect.data.response.MapaEventoResponse;

import okhttp3.ResponseBody;
import retrofit2.Callback;

public class RutaRepositorio {
  private final ApiService apiService;
  private final SessionManager sessionManager;

  public RutaRepositorio(Application application) {
    this.apiService = ApiClient.getApiService();
    this.sessionManager = new SessionManager(application);
  }

  public void guardarRuta(int idEvento, GuardarRutaRequest request, Callback<ResponseBody> callback) {
    String token = sessionManager.leerToken();
    // Llamada al endpoint PUT api/Evento/{id}/Ruta
    apiService.guardarRuta("Bearer " + token, idEvento, request).enqueue(callback);
  }

  public void obtenerRuta (int idEvento, Callback<MapaEventoResponse> callback){
    String token= sessionManager.leerToken();
    if(token !=null){
      apiService.obtenerMapaCompleto("Bearer "+token, idEvento).enqueue(callback);
    }

  }

}
