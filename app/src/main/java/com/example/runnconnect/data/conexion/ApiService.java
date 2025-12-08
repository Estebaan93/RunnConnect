//data/conexion/ApiService
package com.example.runnconnect.data.conexion;

import com.example.runnconnect.data.request.LoginRequest;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ApiService {
  //Login orga/runner
  @POST("api/Usuario/Login")
  Call<LoginRequest> login(@Body LoginRequest request);


}
