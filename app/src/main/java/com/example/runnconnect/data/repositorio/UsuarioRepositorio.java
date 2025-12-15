//data/repositorio/UsuarioRepositorio
package com.example.runnconnect.data.repositorio;


import android.content.Context;

import com.example.runnconnect.data.conexion.ApiClient;
import com.example.runnconnect.data.conexion.ApiService;
import com.example.runnconnect.data.preferencias.SessionManager;
import com.example.runnconnect.data.request.ActualizarPerfilOrganizadorRequest;
import com.example.runnconnect.data.request.ActualizarPerfilRunnerRequest;
import com.example.runnconnect.data.request.LoginRequest;
import com.example.runnconnect.data.response.LoginResponse;
import com.example.runnconnect.data.response.PerfilUsuarioResponse;


import retrofit2.Call;
import retrofit2.Callback;

public class UsuarioRepositorio {
  private final ApiService apiService;
  private final SessionManager sessionManager;

  public UsuarioRepositorio(Context context){
    this.apiService= ApiClient.getApiService();
    sessionManager= new SessionManager(context);
  }

  //login
  public void login(String email, String password, Callback<LoginResponse> callback){
    LoginRequest request= new LoginRequest(email,password);
    Call<LoginResponse> call= apiService.login(request);
    call.enqueue(callback); //Le pasamos al viewModel
  }

  //guardamos la sesion - guarda token y datos del usuario como tipoUsuario, email etc
  public void guardarSesion(LoginResponse response){
    sessionManager.guardarSesionUsuario(response);
  }

  //verificacmos si hay sesion activa
  public boolean haySesionActiva(){
    String token= sessionManager.leerToken();
    return token!=null&&!token.isEmpty();
  }

  //cerrar sesion - destruye token
  public void cerrarSesion(){
    sessionManager.cerrarSesion();
  }

  public String obtenerTipoUsuario(){
    return sessionManager.getTipoUsuario();
  }

  public void obtenerPerfil(Callback<PerfilUsuarioResponse> callback) {
    String token= sessionManager.leerToken();
    apiService.obtenerPerfil("Bearer "+token).enqueue(callback);
  }

  public void actualizarRunner(ActualizarPerfilRunnerRequest request, Callback<PerfilUsuarioResponse> callback) {
    String token= sessionManager.leerToken();
    apiService.actualizarPerfilRunner("Bearer "+token, request).enqueue(callback);
  }

  public void actualizarOrganizador(ActualizarPerfilOrganizadorRequest request, Callback<PerfilUsuarioResponse> callback) {
    String token= sessionManager.leerToken();
    apiService.actualizarPerfilOrganizador("Bearer "+token , request).enqueue(callback);
  }




}
