package com.example.runnconnect.data.repositorio;

import android.app.Application;

import com.example.runnconnect.data.conexion.ApiClient;
import com.example.runnconnect.data.conexion.ApiService;
import com.example.runnconnect.data.preferencias.SessionManager;
import com.example.runnconnect.data.response.BusquedaInscripcionResponse;
import com.example.runnconnect.data.request.CambiarEstadoPagoRequest;
import com.example.runnconnect.data.request.MotivoBajaRequest;
import com.example.runnconnect.data.response.ListaInscriptosResponse;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Callback;
import retrofit2.Call;

public class InscripcionRepositorio {
  private final ApiService apiService;
  private final SessionManager sessionManager;

  public InscripcionRepositorio(Application application) {
    this.sessionManager = new SessionManager(application);
    this.apiService = ApiClient.getApiService();
  }

  // Obtener lista de inscriptos con filtros y paginacion
  // GET api/Evento/{idEvento}/Inscripciones
  public void obtenerInscriptos(int idEvento, String estadoPago, int pagina, int tamanioPagina, Callback<ListaInscriptosResponse> callback) {
    String token = sessionManager.leerToken();
    if (token != null) {
      // El apiService espera @Path("idEvento") y @Query params
      apiService.obtenerInscriptos("Bearer " + token, idEvento, estadoPago, pagina, tamanioPagina).enqueue(callback);
    } else {
      // Manejo básico de error de sesiin
      callback.onFailure(null, new Throwable("No hay sesión activa."));
    }
  }

  // Cambiar estado de pago (Aceptar/Rechazar comprobante)
  // PUT api/Inscripcion/{id}/EstadoPago
  public void cambiarEstadoPago(int idInscripcion, CambiarEstadoPagoRequest request, Callback<ResponseBody> callback) {
    String token = sessionManager.leerToken();
    if (token != null) {
      apiService.cambiarEstadoPago("Bearer " + token, idInscripcion, request).enqueue(callback);
    } else {
      callback.onFailure(null, new Throwable("No hay sesión activa."));
    }
  }

  //dar de baja un runner
  public void darDeBajaRunner(int idInscripcion, String motivo, Callback<ResponseBody> callback){
    String token= sessionManager.leerToken();
    if(token != null){
      MotivoBajaRequest request= new MotivoBajaRequest(motivo);
      apiService.darDeBajaRunner("Bearer "+token, idInscripcion, request).enqueue(callback);
    }else{
      callback.onFailure(null, new Throwable("No hay sesion activa"));
    }
  }

  //buscar inscripcion
  public void buscarIncripcion(String termino, Callback<List<BusquedaInscripcionResponse>> callback) {
    String token = sessionManager.leerToken();
    if (token != null) {
      apiService.buscarInscriptos("Bearer " + token, termino).enqueue(callback);
    } else {
      callback.onFailure(null, new Throwable("Sin sesión"));
    }
  }


}
