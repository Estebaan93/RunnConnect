package com.example.runnconnect.data.repositorio;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import com.example.runnconnect.data.conexion.ApiClient;
import com.example.runnconnect.data.conexion.ApiService;
import com.example.runnconnect.data.preferencias.SessionManager;
import com.example.runnconnect.data.request.CambiarEstadoRequest;
import com.example.runnconnect.data.request.CrearEventoRequest;
import com.example.runnconnect.data.response.EventoDetalleResponse;
import com.example.runnconnect.data.response.EventoResumenResponse;
import com.example.runnconnect.data.response.EventosPaginadosResponse;

import java.util.List;

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

  //obtener listado de mis eventos
  //GET api/Evento/MisEventos
  public void obtenerMisEventos(int pagina, Callback<EventosPaginadosResponse>callback) {
    String token = sessionManager.leerToken(); //
    if (token != null) {
      // Pedimos página X, 5 items por página
      apiService.obtenerMisEventos("Bearer " + token, pagina, 10).enqueue(callback);
    }
  }

  //detalle de evento(orga) - nota: si es orga puede modificar datos del evento, si es runner no tiene
  //acceso a modificar - el orga tiene que ser mismo dueño del organizador
  public void obtenerDetalleEvento(int idEvento, Callback<EventoDetalleResponse> callback){
    String token= sessionManager.leerToken();
    if(token!= null){
      apiService.obtenerEventoPorId("Bearer " + token, idEvento).enqueue(callback);
    }
  }

  //cambiar estado (publicado, cancelado, finalizado)
  public void cambiarEstado(int idEvento, CambiarEstadoRequest request, Callback<ResponseBody> callback) {
    String token = sessionManager.leerToken();
    if (token != null) {
      apiService.cambiarEstado("Bearer " + token, idEvento, request).enqueue(callback);
    }
  }

}
