//data/conexion/ApiService
package com.example.runnconnect.data.conexion;



import com.example.runnconnect.data.request.ActualizarPerfilOrganizadorRequest;
import com.example.runnconnect.data.request.ActualizarPerfilRunnerRequest;
import com.example.runnconnect.data.request.CambiarPasswordRequest;
import com.example.runnconnect.data.request.LoginRequest;
import com.example.runnconnect.data.response.LoginResponse;
import com.example.runnconnect.data.response.PerfilUsuarioResponse;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;

public interface ApiService {

  // -------------USUARIO--------------
  //Login (run/orga)
  @POST("Usuario/Login")
  Call<LoginResponse> login(@Body LoginRequest request); //devuelve loginRespnse

  //obtener perfil (run/orga)
  @GET("Usuario/perfil")
  Call<PerfilUsuarioResponse> obtenerPerfil(@Header("Authorization") String token);

  //actualizar perfil runner
  @PUT("Usuario/ActualizarPerfilRunner")
  Call<PerfilUsuarioResponse> actualizarPerfilRunner(@Header("Authorization") String token, @Body ActualizarPerfilRunnerRequest request);

  //actualizar perfil organizador
  @PUT("Usuario/ActualizarPerfilOrganizador")
  Call<PerfilUsuarioResponse> actualizarPerfilOrganizador(@Header("Authorization") String token, @Body ActualizarPerfilOrganizadorRequest request);

  //actualizar avatar (run/orga)
  @Multipart
  @PUT("Usuario/Avatar")
  Call<PerfilUsuarioResponse> subirAvatar(@Header("Authorization")String token, @Part MultipartBody.Part imagen);

  //eliminar avatar (run/orga)
  @DELETE("Usuario/Avatar")
  Call<PerfilUsuarioResponse> eliminarAvatar(@Header("Authorization") String token);

  //cambiar password (runner/orga)
  @PUT("Usuario/CambiarPassword")
  Call<Void> cambiarPassword(@Header("Authorization") String token, @Body CambiarPasswordRequest request);

  //registro de runner
  @Multipart
  @POST("Usuario/RegisterRunner")
  Call<LoginResponse> registrarRunner(
    @Part("Nombre") RequestBody nombre, @Part("Apellido") RequestBody apellido, @Part("Email") RequestBody email,
    @Part("Password") RequestBody password, @Part("ConfirmPassword") RequestBody confirmPassword, @Part MultipartBody.Part imgAvatar // Puede ser null
  );

  //registro del organizador
  @Multipart
  @POST("Usuario/RegisterOrganizador") // Verifica si en tu C# el controller es "Usuario" o "Usuarios"
  Call<LoginResponse> registrarOrganizador(
    @Part("RazonSocial") RequestBody razonSocial, @Part("NombreComercial") RequestBody nombreComercial,
    @Part("Email") RequestBody email, @Part("Password") RequestBody password,
    @Part("ConfirmPassword") RequestBody confirmPassword, @Part MultipartBody.Part imgAvatar
  );

  //obtener perfil public (runner)

  //obtener perfil public (organizador)

  //recuperar password (run/orga)

  //restablecerPassword (run/orga)

  //eliminar cuenta (run/orga)

  //solicitar reactivacion (run/orga)

  //reactivar cuenta (run/orga)


  //--------------EVENTOS------------------
  //crear evento (POST api/Evento)
  @POST("Evento")
  retrofit2.Call<okhttp3.ResponseBody> crearEvento(
    @retrofit2.http.Header("Authorization") String token,
    @retrofit2.http.Body com.example.runnconnect.data.request.CrearEventoRequest request
  );

  //obtener eventos del organizador
  @GET("Evento/MisEventos")
  retrofit2.Call<com.example.runnconnect.data.response.EventosPaginadosResponse> obtenerMisEventos(
    @retrofit2.http.Header("Authorization") String token,
    @retrofit2.http.Query("Pagina") int pagina,
    @retrofit2.http.Query("TamanioPagina") int tamanio
  );



}
