package com.example.runnconnect.data.repositorio;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.runnconnect.data.conexion.ApiClient;
import com.example.runnconnect.data.conexion.ApiService;
import com.example.runnconnect.data.preferencias.SessionManager;
import com.example.runnconnect.data.request.ActualizarEventoRequest;
import com.example.runnconnect.data.request.CambiarEstadoRequest;
import com.example.runnconnect.data.request.CrearEventoRequest;
import com.example.runnconnect.data.request.CrearPuntoInteresRequest;
import com.example.runnconnect.data.response.EventoDetalleResponse;
import com.example.runnconnect.data.response.EventoResumenResponse;
import com.example.runnconnect.data.response.EventosPaginadosResponse;
import com.example.runnconnect.data.response.MapaEventoResponse;
import com.example.runnconnect.data.response.PuntoInteresResponse;
import com.example.runnconnect.data.response.PuntosInteresEventoResponse;

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

  // Metodo para editar evento
  public void actualizarEvento(int idEvento, ActualizarEventoRequest request, Callback<ResponseBody> callback) {
    String token = sessionManager.leerToken();
    if (token != null) {
      apiService.actualizarEvento("Bearer " + token, idEvento, request).enqueue(callback);
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
  //----EVENTOS PUBLICOS---------
  //obt eventos publicos
  public void obtenerEventosPublicados(int pagina, int tamanio, Callback<EventosPaginadosResponse> callback){
    //sin token, eventos publicos
    apiService.obtenerEventosPublicados(pagina, tamanio).enqueue(callback);

  }

  // Obtener detalle para invitados (Sin Token)
  public void obtenerDetalleEventoPublico(int idEvento, Callback<EventoDetalleResponse> callback) {
    // Llamamos a la variante token
    apiService.obtenerEventoPorIdPublico(idEvento).enqueue(callback);
  }
  //obt circuito de un evento (publico)
  public void obtenerMapaPublico(int idEvento, Callback<MapaEventoResponse> callback) {
    apiService.obtenerMapaPublico(idEvento).enqueue(callback);
  }

  //puntos interes
  public void crearPuntoInteres(int idEvento, CrearPuntoInteresRequest request, Callback<ResponseBody> callback) {
    String token = sessionManager.leerToken();
    if (token != null && !token.isEmpty()) {
      // Llamamos al endpoint definido en ApiService
      apiService.crearPuntoInteres("Bearer " + token, idEvento, request).enqueue(callback);
    } else {
      callback.onFailure(null, new Throwable("Sesion expirada."));
    }

  }

  //obtener punto interes
  public void obtenerPuntosInteres(int idEvento, Callback<PuntosInteresEventoResponse> callback) {
    // Nota: Aunque el GET suele ser público, a veces Retrofit requiere url completa base
    // Si tu endpoint es público no hace falta Header, pero no daña enviarlo si el usuario es Orga.

    // Opción A: Si el endpoint valida token (aunque tu C# dice que es público)
    /*
    String token = sessionManager.leerToken();
    if (token != null) {
         apiService.obtenerPuntosInteres("Bearer " + token, idEvento).enqueue(callback);
    }
    */

    // Opción B: Llamada directa (según tu MapaController.cs es público)
    apiService.obtenerPuntosInteres(idEvento).enqueue(callback);
  }


}
