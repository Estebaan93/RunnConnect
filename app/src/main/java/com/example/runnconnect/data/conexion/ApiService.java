//data/conexion/ApiService
package com.example.runnconnect.data.conexion;



import com.example.runnconnect.data.request.ActualizarEventoRequest;
import com.example.runnconnect.data.request.ActualizarPerfilOrganizadorRequest;
import com.example.runnconnect.data.request.ActualizarPerfilRunnerRequest;
import com.example.runnconnect.data.request.CrearPuntoInteresRequest;
import com.example.runnconnect.data.response.BusquedaInscripcionResponse;
import com.example.runnconnect.data.request.CambiarEstadoPagoRequest;
import com.example.runnconnect.data.request.CambiarEstadoRequest;
import com.example.runnconnect.data.request.CambiarPasswordRequest;
import com.example.runnconnect.data.request.CrearEventoRequest;
import com.example.runnconnect.data.request.GuardarRutaRequest;
import com.example.runnconnect.data.request.LoginRequest;
import com.example.runnconnect.data.request.MotivoBajaRequest;
import com.example.runnconnect.data.response.EventoDetalleResponse;
import com.example.runnconnect.data.response.EventoResumenResponse;
import com.example.runnconnect.data.response.EventosPaginadosResponse;
import com.example.runnconnect.data.response.ListaInscriptosResponse;
import com.example.runnconnect.data.response.LoginResponse;
import com.example.runnconnect.data.response.MapaEventoResponse;
import com.example.runnconnect.data.response.PerfilUsuarioResponse;
import com.example.runnconnect.data.response.PuntoInteresResponse;
import com.example.runnconnect.data.response.PuntosInteresEventoResponse;

import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

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
  //crear evento (POST api/Evento)- orga
  @POST("Evento")
  Call<ResponseBody> crearEvento(
    @Header("Authorization") String token,
    @Body CrearEventoRequest request
  );

  //guardar ruta (trazado de mapa) - orga
  @PUT("Evento/{idEvento}/Ruta")
  Call<ResponseBody> guardarRuta(
    @Header("Authorization") String token,
    @Path("idEvento") int idEvento,
    @Body GuardarRutaRequest request
  );

  //recuperar ruta y puntos (orga)
  @GET("Evento/{idEvento}/Mapa")
  Call<MapaEventoResponse> obtenerMapaCompleto(
    @Header("Authorization") String token,
    @Path("idEvento") int idEvento);

  //mapa publico
  @GET("Evento/{idEvento}/Mapa")
  Call<MapaEventoResponse> obtenerMapaPublico(
    @Path("idEvento") int idEvento
  );

  // Actualizar evento (PUT)
  @PUT("Evento/{id}")
  Call<ResponseBody> actualizarEvento(
    @Header("Authorization") String token,
    @Path("id") int idEvento,
    @Body ActualizarEventoRequest request
  );

  //obtener eventos del organizador (orga)
  @GET("Evento/MisEventos")
  Call<EventosPaginadosResponse> obtenerMisEventos(
    @Header("Authorization") String token,
    @Query("pagina") int pagina,
    @Query("tamanioPagina") int tamanioPagina
  );

  //obtener detalles de evento (token para el organizador)
  @GET("Evento/{id}")
  Call<EventoDetalleResponse> obtenerEventoPorId(
    @Header("Authorization") String token,
    @Path("id") int idEvento
  );
  // Obtener detalle PUBLICO (Sin Token)
  @GET("Evento/{id}")
  Call<EventoDetalleResponse> obtenerEventoPorIdPublico(
    @Path("id") int idEvento
  );

  //cambiar estado de un evento
  @PUT("Evento/{id}/CambiarEstado")
  Call<ResponseBody> cambiarEstado(
    @Header("Authorization") String token,
    @Path("id") int id,
    @Body CambiarEstadoRequest request);

  // GET para obtener lista filtrada
  @GET("Evento/{idEvento}/Inscripciones")
  Call<ListaInscriptosResponse> obtenerInscriptos(
    @Header("Authorization") String token,
    @Path("idEvento") int idEvento,
    @Query("EstadoPago") String estadoPago, // "procesando", "pagado", etc.
    @Query("Pagina") int pagina,
    @Query("TamanioPagina") int tamanio
  );

  //obt eventos publicos
  @GET("Evento/Publicados")
  Call<EventosPaginadosResponse> obtenerEventosPublicados(
    @Query("pagina") int pagina,
    @Query("tamanioPagina") int tamanioPagina
  );

  // PUT para cambiar estado (Aprobar/Rechazar pago)
  @PUT("Inscripcion/{id}/EstadoPago")
  Call<ResponseBody> cambiarEstadoPago(
    @Header("Authorization") String token,
    @Path("id") int idInscripcion,
    @Body CambiarEstadoPagoRequest request
  );

  //dar de baja runner
  @PUT("Inscripcion/{id}/bajaRunner")
  Call<ResponseBody> darDeBajaRunner(
    @Header("Authorization") String token,
    @Path("id") int idInscripcion,
    @Body MotivoBajaRequest request // clase simple en Java
  );

  //buscar inscripcion
  @GET("Inscripcion/BuscarInscriptos")
  Call<List<BusquedaInscripcionResponse>> buscarInscriptos(
    @Header("Authorization") String token,
    @Query("busqueda") String termino
  );

  //agregar puntos de interes
  @POST("Evento/{idEvento}/PuntosInteres")
  Call<ResponseBody> crearPuntoInteres(
    @Header("Authorization") String token,
    @Path("idEvento") int idEvento,
    @Body CrearPuntoInteresRequest request
    );

  //obtener puntos de interes
  @GET("Evento/{idEvento}/PuntosInteres")
  Call<PuntosInteresEventoResponse> obtenerPuntosInteres(
    @Path("idEvento") int idEvento
  );



}
