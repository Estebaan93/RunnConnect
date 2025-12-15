//data/conexion/ApiService
package com.example.runnconnect.data.conexion;



import com.example.runnconnect.data.request.ActualizarPerfilOrganizadorRequest;
import com.example.runnconnect.data.request.ActualizarPerfilRunnerRequest;
import com.example.runnconnect.data.request.LoginRequest;
import com.example.runnconnect.data.response.LoginResponse;
import com.example.runnconnect.data.response.PerfilUsuarioResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;

public interface ApiService {
  //Login orga/runner
  @POST("Usuario/Login")
  Call<LoginResponse> login(@Body LoginRequest request); //devuelve loginRespnse


  //obtener perfil
  @GET("Usuario/perfil")
  Call<PerfilUsuarioResponse> obtenerPerfil(@Header("Authorization") String token);

  //actualizar perfil runner
  @PUT("Usuario/ActualizarPerfilRunner")
  Call<PerfilUsuarioResponse> actualizarPerfilRunner(@Header("Authorization") String token, @Body ActualizarPerfilRunnerRequest request);

  //actualizar perfil organizador
  @PUT("Usuario/ActualizarPerfilOrganizador")
  Call<PerfilUsuarioResponse> actualizarPerfilOrganizador(@Header("Authorization") String token, @Body ActualizarPerfilOrganizadorRequest request);







}
