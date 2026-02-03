package com.example.runnconnect.data.repositorio;

import android.app.Application;

import com.example.runnconnect.data.conexion.ApiClient;
import com.example.runnconnect.data.conexion.ApiService;
import com.example.runnconnect.data.preferencias.SessionManager;
import com.example.runnconnect.data.response.ResultadosEventoResponse;

import java.io.File;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Callback;

public class ResultadoRepositorio {
  private final ApiService apiService;
  private final SessionManager sessionManager;

  public ResultadoRepositorio(Application application){
    this.apiService= ApiClient.getApiService();
    this.sessionManager= new SessionManager(application);
  }

  // Subir CSV
  public void subirArchivoResultados(int idEvento, File archivo, Callback<ResponseBody> callback) {
    String token = sessionManager.leerToken();
    if (token == null) return;

    // IdEvento como texto plano
    RequestBody idEventoBody = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(idEvento));

    // Archivo como multipart
    // Usamos "multipart/form-data"
    RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), archivo);

    // El nombre "archivo" debe coincidir con la propiedad IFormFile del DTO C#
    MultipartBody.Part bodyArchivo = MultipartBody.Part.createFormData("Archivo", archivo.getName(), requestFile);

    apiService.cargarArchivoResultados("Bearer " + token, idEventoBody, bodyArchivo).enqueue(callback);
  }

  // Obtener Lista
  public void obtenerResultadosEvento(int idEvento, Callback<ResultadosEventoResponse> callback) {
    // Este endpoint suele ser publico, pero si requiere token:
    /*String token = sessionManager.leerToken();
     apiService.obtenerResultadosEvento("Bearer " + token, idEvento).enqueue(callback);*/

    // Si es publico:
    apiService.obtenerResultadosEvento(idEvento).enqueue(callback);
  }


}
