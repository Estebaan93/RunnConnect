//data/conexion/ApiClient
package com.example.runnconnect.data.conexion;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {
  private static final String BASE_URL="http://10.0.2.2:5213/api/"; //para el emulador y no localhost genera conficto con el mismo emulador
  private static final String BASE_URLlocal="http://192.168.1.176:5213/api/";
  private static final String BASE_URLTrabajoD="http://192.168.5.54:5213/api/";
  private static final String BASE_URLTrabajoI="http://192.168.4.103:5213/api/";
  private static ApiService service;

  public static ApiService getApiService() {
    if (service == null) {
      // 2. Configurar Gson para manejar las fechas de C# correctamente
      Gson gson = new GsonBuilder()
              .setDateFormat("yyyy-MM-dd'T'HH:mm:ss") // Formato estandar de .NET
              .create();

      // 3. Construir Retrofit
      Retrofit retrofit = new Retrofit.Builder()
              .baseUrl(BASE_URLTrabajoI)
              .addConverterFactory(GsonConverterFactory.create(gson)) // Para convertir JSON a Objetos
              // .addConverterFactory(ScalarsConverterFactory.create()) // Descomentar si alguna vez necesitas recibir Strings puros
              .build();

      service = retrofit.create(ApiService.class);
    }
    return service;
  }


}
